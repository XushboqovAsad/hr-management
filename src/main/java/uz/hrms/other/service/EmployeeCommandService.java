package uz.hrms.other.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.OrganizationService;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.repository.AuditLogRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@Transactional
public class EmployeeCommandService {

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
