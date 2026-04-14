package uz.hrms.other;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.audit.SecurityAuditService;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.repository.AuditLogRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

record EmployeeProfileResponse(
        UUID employeeId,
        UUID userId,
        String personnelNumber,
        String status,
        String username,
        String fullName,
        UUID departmentId,
        String departmentName,
        UUID managerEmployeeId,
        boolean sensitiveFieldsMasked
) {
}

record EmployeeListItemResponse(
        UUID employeeId,
        UUID userId,
        String personnelNumber,
        String status,
        String username,
        String fullName,
        @Email String email,
        UUID departmentId,
        String departmentName,
        UUID positionId,
        String positionTitle,
        UUID managerEmployeeId,
        String managerFullName,
        LocalDate hireDate,
        LocalDate dismissalDate,
        boolean sensitiveFieldsMasked
) {
}

record EmployeeCardResponse(
        UUID employeeId,
        UUID userId,
        String personnelNumber,
        String status,
        String username,
        String firstName,
        String lastName,
        String middleName,
        @Email String email,
        String fullName,
        UUID departmentId,
        String departmentName,
        UUID positionId,
        String positionTitle,
        UUID staffingUnitId,
        String staffingUnitCode,
        UUID managerEmployeeId,
        String managerFullName,
        LocalDate hireDate,
        LocalDate dismissalDate,
        boolean sensitiveFieldsMasked
) {
}

record EmployeeHistoryItemResponse(
        String entryType,
        String status,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        UUID departmentId,
        String departmentName,
        UUID positionId,
        String positionTitle,
        UUID staffingUnitId,
        String staffingUnitCode,
        UUID managerEmployeeId,
        String managerFullName
) {
}

record EmployeeUpsertRequest(
        @NotBlank String personnelNumber,
        @NotBlank String username,
        String password,
        @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String middleName,
        @NotBlank String status,
        @NotNull LocalDate hireDate,
        LocalDate dismissalDate,
        UUID departmentId,
        UUID positionId,
        UUID staffingUnitId,
        UUID managerEmployeeId,
        LocalDate assignmentStartedAt,
        Boolean assignEmployeeRole
) {
}

record EmployeeStatusChangeRequest(
        @NotBlank String status,
        LocalDate dismissalDate
) {
}

@Service
@Transactional(readOnly = true)
class EmployeeQueryService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeAssignmentRepository employeeAssignmentRepository;
    private final AccessPolicy accessPolicy;
    private final SecurityAuditService securityAuditService;

    EmployeeQueryService(EmployeeRepository employeeRepository,
                         EmployeeAssignmentRepository employeeAssignmentRepository,
                         AccessPolicy accessPolicy,
                         SecurityAuditService securityAuditService) {
        this.employeeRepository = employeeRepository;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.accessPolicy = accessPolicy;
        this.securityAuditService = securityAuditService;
    }

    EmployeeProfileResponse getProfile(Authentication authentication, String requestUri, UUID employeeId) {
        Employee employee = getEmployeeEntity(employeeId);
        EmployeeAssignment assignment = employeeAssignmentRepository.findCurrentPrimaryAssignment(employeeId, LocalDate.now()).orElse(null);
        boolean canReadSensitive = canReadSensitive(authentication);
        securityAuditService.recordPersonalDataAccess(
                authentication,
                employeeId,
                requestUri,
                "READ",
                List.of("personnelNumber", "status", "username", "fullName", "department", "manager"),
                canReadSensitive ? List.of() : List.of("personnelNumber", "username"),
                true
        );
        return new EmployeeProfileResponse(
                employee.getId(),
                employee.getUser() == null ? null : employee.getUser().getId(),
                canReadSensitive ? employee.getPersonnelNumber() : maskPersonnelNumber(employee.getPersonnelNumber()),
                employee.getStatus(),
                canReadSensitive ? username(employee) : maskUsername(username(employee)),
                fullName(employee.getUser()),
                assignment == null ? null : assignment.getDepartment().getId(),
                assignment == null ? null : assignment.getDepartment().getName(),
                assignment == null || assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId(),
                canReadSensitive == false
        );
    }

    List<EmployeeListItemResponse> list(Authentication authentication, UUID departmentId, String status, String query) {
        boolean canReadSensitive = canReadSensitive(authentication);
        String normalizedQuery = normalize(query);
        String normalizedStatus = normalize(status);
        return employeeRepository.findAll().stream()
                .filter(Employee::isDeletedFalse)
                .filter(employee -> accessPolicy.canReadEmployee(authentication, employee.getId()))
                .map(employee -> toListItem(employee, employeeAssignmentRepository.findCurrentPrimaryAssignment(employee.getId(), LocalDate.now()).orElse(null), canReadSensitive))
                .filter(item -> departmentId == null || Objects.equals(item.departmentId(), departmentId))
                .filter(item -> normalizedStatus == null || normalize(item.status()).equals(normalizedStatus))
                .filter(item -> matchesQuery(item, normalizedQuery))
                .sorted(Comparator.comparing(EmployeeListItemResponse::fullName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    EmployeeCardResponse getEmployee(Authentication authentication, String requestUri, UUID employeeId) {
        Employee employee = getEmployeeEntity(employeeId);
        EmployeeAssignment assignment = employeeAssignmentRepository.findCurrentPrimaryAssignment(employeeId, LocalDate.now()).orElse(null);
        boolean canReadSensitive = canReadSensitive(authentication);
        securityAuditService.recordPersonalDataAccess(
                authentication,
                employeeId,
                requestUri,
                "READ",
                List.of("personnelNumber", "status", "username", "email", "department", "position", "manager"),
                canReadSensitive ? List.of() : List.of("personnelNumber", "username", "email"),
                true
        );
        return toCardResponse(employee, assignment, canReadSensitive);
    }

    List<EmployeeHistoryItemResponse> getHistory(Authentication authentication, String requestUri, UUID employeeId) {
        Employee employee = getEmployeeEntity(employeeId);
        securityAuditService.recordPersonalDataAccess(
                authentication,
                employeeId,
                requestUri,
                "READ",
                List.of("statusHistory", "assignmentHistory"),
                List.of(),
                true
        );
        List<EmployeeHistoryItemResponse> items = new ArrayList<>();
        items.add(new EmployeeHistoryItemResponse(
                "EMPLOYMENT",
                employee.getStatus(),
                employee.getHireDate(),
                employee.getDismissalDate(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));
        employeeAssignmentRepository.findAll().stream()
                .filter(EmployeeAssignment::isDeletedFalse)
                .filter(assignment -> assignment.getEmployee() != null && employeeId.equals(assignment.getEmployee().getId()))
                .sorted(Comparator.comparing(EmployeeAssignment::getStartedAt, Comparator.nullsLast(LocalDate::compareTo)).reversed())
                .forEach(assignment -> items.add(new EmployeeHistoryItemResponse(
                        "ASSIGNMENT",
                        employee.getStatus(),
                        assignment.getStartedAt(),
                        assignment.getEndedAt(),
                        assignment.getDepartment() == null ? null : assignment.getDepartment().getId(),
                        assignment.getDepartment() == null ? null : assignment.getDepartment().getName(),
                        assignment.getPosition() == null ? null : assignment.getPosition().getId(),
                        assignment.getPosition() == null ? null : assignment.getPosition().getTitle(),
                        assignment.getStaffingUnit() == null ? null : assignment.getStaffingUnit().getId(),
                        assignment.getStaffingUnit() == null ? null : assignment.getStaffingUnit().getCode(),
                        assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId(),
                        fullName(assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getUser())
                )));
        items.sort(Comparator.comparing(EmployeeHistoryItemResponse::effectiveFrom, Comparator.nullsLast(LocalDate::compareTo)).reversed());
        return items;
    }

    EmployeeCardResponse toCardResponse(Employee employee, EmployeeAssignment assignment, boolean canReadSensitive) {
        return new EmployeeCardResponse(
                employee.getId(),
                employee.getUser() == null ? null : employee.getUser().getId(),
                canReadSensitive ? employee.getPersonnelNumber() : maskPersonnelNumber(employee.getPersonnelNumber()),
                employee.getStatus(),
                canReadSensitive ? username(employee) : maskUsername(username(employee)),
                employee.getUser() == null ? null : employee.getUser().getFirstName(),
                employee.getUser() == null ? null : employee.getUser().getLastName(),
                employee.getUser() == null ? null : employee.getUser().getMiddleName(),
                canReadSensitive ? email(employee) : maskEmail(email(employee)),
                fullName(employee.getUser()),
                assignment == null || assignment.getDepartment() == null ? null : assignment.getDepartment().getId(),
                assignment == null || assignment.getDepartment() == null ? null : assignment.getDepartment().getName(),
                assignment == null || assignment.getPosition() == null ? null : assignment.getPosition().getId(),
                assignment == null || assignment.getPosition() == null ? null : assignment.getPosition().getTitle(),
                assignment == null || assignment.getStaffingUnit() == null ? null : assignment.getStaffingUnit().getId(),
                assignment == null || assignment.getStaffingUnit() == null ? null : assignment.getStaffingUnit().getCode(),
                assignment == null || assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId(),
                fullName(assignment == null || assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getUser()),
                employee.getHireDate(),
                employee.getDismissalDate(),
                canReadSensitive == false
        );
    }

    private EmployeeListItemResponse toListItem(Employee employee, EmployeeAssignment assignment, boolean canReadSensitive) {
        return new EmployeeListItemResponse(
                employee.getId(),
                employee.getUser() == null ? null : employee.getUser().getId(),
                canReadSensitive ? employee.getPersonnelNumber() : maskPersonnelNumber(employee.getPersonnelNumber()),
                employee.getStatus(),
                canReadSensitive ? username(employee) : maskUsername(username(employee)),
                fullName(employee.getUser()),
                canReadSensitive ? email(employee) : maskEmail(email(employee)),
                assignment == null || assignment.getDepartment() == null ? null : assignment.getDepartment().getId(),
                assignment == null || assignment.getDepartment() == null ? null : assignment.getDepartment().getName(),
                assignment == null || assignment.getPosition() == null ? null : assignment.getPosition().getId(),
                assignment == null || assignment.getPosition() == null ? null : assignment.getPosition().getTitle(),
                assignment == null || assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId(),
                fullName(assignment == null || assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getUser()),
                employee.getHireDate(),
                employee.getDismissalDate(),
                canReadSensitive == false
        );
    }

    private Employee getEmployeeEntity(UUID employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private boolean matchesQuery(EmployeeListItemResponse item, String normalizedQuery) {
        if (normalizedQuery == null) {
            return true;
        }
        String haystack = String.join(" ", List.of(
                Objects.toString(item.fullName(), ""),
                Objects.toString(item.personnelNumber(), ""),
                Objects.toString(item.username(), ""),
                Objects.toString(item.email(), ""),
                Objects.toString(item.departmentName(), ""),
                Objects.toString(item.positionTitle(), "")
        )).toLowerCase(Locale.ROOT);
        return haystack.contains(normalizedQuery);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean canReadSensitive(Authentication authentication) {
        if (authentication == null || (authentication.getPrincipal() instanceof CurrentUser) == false) {
            return false;
        }
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return currentUser.roles().contains(RoleCode.SUPER_ADMIN.name())
                || currentUser.permissions().contains("EMPLOYEE_SENSITIVE:READ")
                || currentUser.permissions().contains("EMPLOYEE:WRITE");
    }

    private String fullName(UserAccount user) {
        if (user == null) {
            return null;
        }
        return String.join(" ",
                Objects.toString(user.getLastName(), ""),
                Objects.toString(user.getFirstName(), ""),
                Objects.toString(user.getMiddleName(), "")
        ).trim();
    }

    private String username(Employee employee) {
        return employee.getUser() == null ? null : employee.getUser().getUsername();
    }

    private String email(Employee employee) {
        return employee.getUser() == null ? null : employee.getUser().getEmail();
    }

    private String maskPersonnelNumber(String value) {
        if (value == null || value.length() <= 2) {
            return value;
        }
        return "*".repeat(Math.max(0, value.length() - 2)) + value.substring(value.length() - 2);
    }

    private String maskUsername(String value) {
        if (value == null || value.length() <= 2) {
            return value;
        }
        return value.substring(0, 1) + "***" + value.substring(value.length() - 1);
    }

    private String maskEmail(String value) {
        if (value == null || value.isBlank() || value.contains("@") == false) {
            return value;
        }
        String[] parts = value.split("@", 2);
        String local = parts[0];
        if (local.length() <= 2) {
            return "**@" + parts[1];
        }
        return local.substring(0, 1) + "***" + local.substring(local.length() - 1) + "@" + parts[1];
    }
}

@Service
@Transactional
class EmployeeCommandService {

    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final EmployeeAssignmentRepository employeeAssignmentRepository;
    private final OrganizationService organizationService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;
    private final EmployeeQueryService employeeQueryService;

    EmployeeCommandService(EmployeeRepository employeeRepository,
                           UserAccountRepository userAccountRepository,
                           RoleRepository roleRepository,
                           UserRoleAssignmentRepository userRoleAssignmentRepository,
                           EmployeeAssignmentRepository employeeAssignmentRepository,
                           OrganizationService organizationService,
                           PasswordEncoder passwordEncoder,
                           AuditLogRepository auditLogRepository,
                           EmployeeQueryService employeeQueryService) {
        this.employeeRepository = employeeRepository;
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.organizationService = organizationService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogRepository = auditLogRepository;
        this.employeeQueryService = employeeQueryService;
    }

    EmployeeCardResponse create(Authentication authentication, EmployeeUpsertRequest request) {
        validateRequest(request, null, true);
        UserAccount user = new UserAccount();
        applyUser(user, request, true);
        userAccountRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(user);
        applyEmployee(employee, request);
        employeeRepository.save(employee);

        if (Boolean.FALSE.equals(request.assignEmployeeRole()) == false) {
            ensureEmployeeRole(user, request.hireDate());
        }
        upsertAssignment(employee, request);

        EmployeeAssignment assignment = employeeAssignmentRepository.findCurrentPrimaryAssignment(employee.getId(), LocalDate.now()).orElse(null);
        writeAudit(authentication, "EMPLOYEE_CREATED", employee.getId(), null, employeeSnapshot(employee, assignment));
        return employeeQueryService.toCardResponse(employee, assignment, canReadSensitive(authentication));
    }

    EmployeeCardResponse update(Authentication authentication, UUID employeeId, EmployeeUpsertRequest request) {
        validateRequest(request, employeeId, false);
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        EmployeeAssignment beforeAssignment = employeeAssignmentRepository.findCurrentPrimaryAssignment(employeeId, LocalDate.now()).orElse(null);
        String before = employeeSnapshot(employee, beforeAssignment);

        applyUser(employee.getUser(), request, false);
        if (employee.getUser() != null) {
            userAccountRepository.save(employee.getUser());
        }
        applyEmployee(employee, request);
        employeeRepository.save(employee);
        upsertAssignment(employee, request);

        EmployeeAssignment afterAssignment = employeeAssignmentRepository.findCurrentPrimaryAssignment(employee.getId(), LocalDate.now()).orElse(null);
        writeAudit(authentication, "EMPLOYEE_UPDATED", employee.getId(), before, employeeSnapshot(employee, afterAssignment));
        return employeeQueryService.toCardResponse(employee, afterAssignment, canReadSensitive(authentication));
    }

    EmployeeCardResponse changeStatus(Authentication authentication, UUID employeeId, EmployeeStatusChangeRequest request) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        EmployeeAssignment assignment = employeeAssignmentRepository.findCurrentPrimaryAssignment(employeeId, LocalDate.now()).orElse(null);
        String before = employeeSnapshot(employee, assignment);
        employee.setStatus(request.status().trim().toUpperCase(Locale.ROOT));
        employee.setDismissalDate("DISMISSED".equalsIgnoreCase(request.status()) ? (request.dismissalDate() == null ? LocalDate.now() : request.dismissalDate()) : request.dismissalDate());
        employeeRepository.save(employee);
        writeAudit(authentication, "EMPLOYEE_STATUS_CHANGED", employee.getId(), before, employeeSnapshot(employee, assignment));
        return employeeQueryService.toCardResponse(employee, assignment, canReadSensitive(authentication));
    }

    private void validateRequest(EmployeeUpsertRequest request, UUID employeeId, boolean creating) {
        if (creating && (request.password() == null || request.password().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required for new employee user");
        }
        boolean duplicatePersonnelNumber = employeeRepository.findAll().stream()
                .filter(Employee::isDeletedFalse)
                .anyMatch(item -> item.getPersonnelNumber() != null
                        && item.getPersonnelNumber().equalsIgnoreCase(request.personnelNumber())
                        && (employeeId == null || Objects.equals(item.getId(), employeeId) == false));
        if (duplicatePersonnelNumber) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Personnel number already exists");
        }
        UserAccount existingUser = userAccountRepository.findByUsernameIgnoreCaseAndDeletedFalse(request.username()).orElse(null);
        UUID currentUserId = employeeId == null
                ? null
                : employeeRepository.findByIdAndDeletedFalse(employeeId).map(Employee::getUser).map(UserAccount::getId).orElse(null);
        if (existingUser != null && Objects.equals(existingUser.getId(), currentUserId) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }
        validateAssignmentFields(request);
    }

    private void validateAssignmentFields(EmployeeUpsertRequest request) {
        Set<UUID> values = new LinkedHashSet<>();
        if (request.departmentId() != null) {
            values.add(request.departmentId());
        }
        if (request.positionId() != null) {
            values.add(request.positionId());
        }
        if (request.staffingUnitId() != null) {
            values.add(request.staffingUnitId());
        }
        boolean hasAnyAssignmentField = values.isEmpty() == false || request.managerEmployeeId() != null || request.assignmentStartedAt() != null;
        boolean hasCoreAssignment = request.departmentId() != null && request.positionId() != null && request.staffingUnitId() != null;
        if (hasAnyAssignmentField && hasCoreAssignment == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "departmentId, positionId and staffingUnitId must be provided together");
        }
    }

    private void applyUser(UserAccount user, EmployeeUpsertRequest request, boolean creating) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee user account is missing");
        }
        user.setUsername(request.username().trim());
        user.setEmail(request.email());
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setMiddleName(blankToNull(request.middleName()));
        user.setActive("DISMISSED".equalsIgnoreCase(request.status()) == false);
        if (creating || (request.password() != null && request.password().isBlank() == false)) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
    }

    private void applyEmployee(Employee employee, EmployeeUpsertRequest request) {
        employee.setPersonnelNumber(request.personnelNumber().trim());
        employee.setHireDate(request.hireDate());
        employee.setDismissalDate(request.dismissalDate());
        employee.setStatus(request.status().trim().toUpperCase(Locale.ROOT));
        if (employee.getUser() != null) {
            employee.getUser().setActive("DISMISSED".equalsIgnoreCase(request.status()) == false);
        }
    }

    private void upsertAssignment(Employee employee, EmployeeUpsertRequest request) {
        if (request.departmentId() == null || request.positionId() == null || request.staffingUnitId() == null) {
            return;
        }
        EmployeeAssignment current = employeeAssignmentRepository.findCurrentPrimaryAssignment(employee.getId(), LocalDate.now()).orElse(null);
        LocalDate startedAt = request.assignmentStartedAt() != null
                ? request.assignmentStartedAt()
                : current == null ? employee.getHireDate() : current.getStartedAt();
        AssignmentRequest assignmentRequest = new AssignmentRequest(
                employee.getId(),
                request.departmentId(),
                request.positionId(),
                request.staffingUnitId(),
                request.managerEmployeeId(),
                startedAt,
                current == null ? null : current.getEndedAt()
        );
        if (current == null) {
            organizationService.createAssignment(assignmentRequest);
        } else {
            organizationService.updateAssignment(current.getId(), assignmentRequest);
        }
    }

    private void ensureEmployeeRole(UserAccount user, LocalDate validFrom) {
        boolean alreadyAssigned = userRoleAssignmentRepository.findActiveAssignments(user.getId()).stream()
                .anyMatch(item -> item.getRole() != null && item.getRole().getCode() == RoleCode.EMPLOYEE);
        if (alreadyAssigned) {
            return;
        }
        Role employeeRole = roleRepository.findByCodeAndDeletedFalse(RoleCode.EMPLOYEE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "EMPLOYEE role is not configured"));
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(employeeRole);
        assignment.setScopeType(AccessScopeType.SELF);
        assignment.setActive(true);
        assignment.setValidFrom(validFrom == null ? LocalDate.now() : validFrom);
        userRoleAssignmentRepository.save(assignment);
    }

    private void writeAudit(Authentication authentication, String action, UUID entityId, String beforeData, String afterData) {
        CurrentUser actor = currentUser(authentication);
        AuditLog log = new AuditLog();
        log.setActorUserId(actor == null ? null : actor.userId());
        log.setActorEmployeeId(actor == null ? null : actor.employeeId());
        log.setAction(action);
        log.setEntitySchema("hr");
        log.setEntityTable("employees");
        log.setEntityId(entityId);
        log.setBeforeData(beforeData);
        log.setAfterData(afterData);
        log.setOccurredAt(OffsetDateTime.now());
        auditLogRepository.save(log);
    }

    private String employeeSnapshot(Employee employee, EmployeeAssignment assignment) {
        StringBuilder value = new StringBuilder("{");
        value.append("\"personnelNumber\":\"").append(escape(employee.getPersonnelNumber())).append("\"");
        value.append(",\"status\":\"").append(escape(employee.getStatus())).append("\"");
        value.append(",\"hireDate\":\"").append(employee.getHireDate()).append("\"");
        if (employee.getDismissalDate() != null) {
            value.append(",\"dismissalDate\":\"").append(employee.getDismissalDate()).append("\"");
        }
        if (employee.getUser() != null) {
            value.append(",\"username\":\"").append(escape(employee.getUser().getUsername())).append("\"");
            if (employee.getUser().getEmail() != null) {
                value.append(",\"email\":\"").append(escape(employee.getUser().getEmail())).append("\"");
            }
        }
        if (assignment != null) {
            value.append(",\"departmentId\":\"").append(assignment.getDepartment().getId()).append("\"");
            if (assignment.getPosition() != null) {
                value.append(",\"positionId\":\"").append(assignment.getPosition().getId()).append("\"");
            }
            if (assignment.getStaffingUnit() != null) {
                value.append(",\"staffingUnitId\":\"").append(assignment.getStaffingUnit().getId()).append("\"");
            }
            if (assignment.getManagerEmployee() != null) {
                value.append(",\"managerEmployeeId\":\"").append(assignment.getManagerEmployee().getId()).append("\"");
            }
        }
        value.append("}");
        return value.toString();
    }

    private CurrentUser currentUser(Authentication authentication) {
        if (authentication == null || (authentication.getPrincipal() instanceof CurrentUser) == false) {
            return null;
        }
        return (CurrentUser) authentication.getPrincipal();
    }

    private boolean canReadSensitive(Authentication authentication) {
        CurrentUser currentUser = currentUser(authentication);
        if (currentUser == null) {
            return false;
        }
        return currentUser.roles().contains(RoleCode.SUPER_ADMIN.name())
                || currentUser.permissions().contains("EMPLOYEE_SENSITIVE:READ")
                || currentUser.permissions().contains("EMPLOYEE:WRITE");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employees")
@SecurityRequirement(name = "bearerAuth")
class EmployeeController {

    private final EmployeeQueryService employeeQueryService;
    private final EmployeeCommandService employeeCommandService;

    EmployeeController(EmployeeQueryService employeeQueryService,
                       EmployeeCommandService employeeCommandService) {
        this.employeeQueryService = employeeQueryService;
        this.employeeCommandService = employeeCommandService;
    }

    @GetMapping
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'EMPLOYEE', 'READ')")
    @Operation(summary = "List employees with scope-aware filtering")
    ResponseEntity<List<EmployeeListItemResponse>> list(Authentication authentication,
                                                        @RequestParam(name = "departmentId", required = false) UUID departmentId,
                                                        @RequestParam(name = "status", required = false) String status,
                                                        @RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok(employeeQueryService.list(authentication, departmentId, status, query));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("@accessPolicy.canReadEmployee(authentication, #p2)")
    @Operation(summary = "Get employee card")
    ResponseEntity<EmployeeCardResponse> get(Authentication authentication,
                                             HttpServletRequest request,
                                             @PathVariable("employeeId") UUID employeeId) {
        return ResponseEntity.ok(employeeQueryService.getEmployee(authentication, request.getRequestURI(), employeeId));
    }

    @GetMapping("/{employeeId}/history")
    @PreAuthorize("@accessPolicy.canReadEmployee(authentication, #p2)")
    @Operation(summary = "Get employee history")
    ResponseEntity<List<EmployeeHistoryItemResponse>> history(Authentication authentication,
                                                              HttpServletRequest request,
                                                              @PathVariable("employeeId") UUID employeeId) {
        return ResponseEntity.ok(employeeQueryService.getHistory(authentication, request.getRequestURI(), employeeId));
    }

    @GetMapping("/{employeeId}/profile")
    @PreAuthorize("@accessPolicy.canReadEmployee(authentication, #p2)")
    @Operation(summary = "Get employee profile with scope-aware authorization")
    ResponseEntity<EmployeeProfileResponse> getProfile(Authentication authentication,
                                                       HttpServletRequest request,
                                                       @PathVariable("employeeId") UUID employeeId) {
        return ResponseEntity.ok(employeeQueryService.getProfile(authentication, request.getRequestURI(), employeeId));
    }

    @PostMapping
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'EMPLOYEE', 'WRITE')")
    @Operation(summary = "Create employee")
    ResponseEntity<EmployeeCardResponse> create(Authentication authentication,
                                                @Valid @RequestBody EmployeeUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeCommandService.create(authentication, request));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'EMPLOYEE', 'WRITE')")
    @Operation(summary = "Update employee")
    ResponseEntity<EmployeeCardResponse> update(Authentication authentication,
                                                @PathVariable("employeeId") UUID employeeId,
                                                @Valid @RequestBody EmployeeUpsertRequest request) {
        return ResponseEntity.ok(employeeCommandService.update(authentication, employeeId, request));
    }

    @PatchMapping("/{employeeId}/status")
    @PreAuthorize("@accessPolicy.hasPermission(authentication, 'EMPLOYEE', 'WRITE')")
    @Operation(summary = "Change employee status")
    ResponseEntity<EmployeeCardResponse> changeStatus(Authentication authentication,
                                                      @PathVariable("employeeId") UUID employeeId,
                                                      @Valid @RequestBody EmployeeStatusChangeRequest request) {
        return ResponseEntity.ok(employeeCommandService.changeStatus(authentication, employeeId, request));
    }
}
