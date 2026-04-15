package uz.hrms.other.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.audit.SecurityAuditService;
import uz.hrms.other.AccessPolicy;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class EmployeeQueryService {

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
