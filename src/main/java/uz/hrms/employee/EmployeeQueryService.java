package uz.hrms.employee;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.audit.SecurityAuditService;
import uz.hrms.security.AccessPolicy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EmployeeQueryService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeAssignmentRepository employeeAssignmentRepository;
    private final AccessPolicy accessPolicy;
    private final SecurityAuditService securityAuditService;

    public EmployeeQueryService(EmployeeRepository employeeRepository,
                                EmployeeAssignmentRepository employeeAssignmentRepository,
                                AccessPolicy accessPolicy,
                                SecurityAuditService securityAuditService) {
        this.employeeRepository = employeeRepository;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.accessPolicy = accessPolicy;
        this.securityAuditService = securityAuditService;
    }

    public EmployeeDtos.EmployeeProfileResponse getProfile(Authentication authentication, String requestUri, UUID employeeId) {
        Employee employee = employeeRepository.findByIdAndIsDeletedFalse(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        EmployeeAssignment assignment = employeeAssignmentRepository.findCurrentPrimaryAssignment(employeeId, LocalDate.now()).orElse(null);
        String username = employee.getUser() == null ? null : employee.getUser().getUsername();
        String fullName = employee.getUser() == null
                ? null
                : String.join(" ", employee.getUser().getLastName(), employee.getUser().getFirstName(), employee.getUser().getMiddleName() == null ? "" : employee.getUser().getMiddleName()).trim();
        boolean canReadSensitive = accessPolicy.canReadEmployeeSensitive(authentication, employeeId);
        List<String> maskedFields = canReadSensitive ? List.of() : List.of("personnelNumber", "username");
        securityAuditService.recordPersonalDataAccess(
                authentication,
                employeeId,
                requestUri,
                "READ",
                List.of("personnelNumber", "status", "username", "fullName", "department", "manager"),
                maskedFields,
                true
        );
        return new EmployeeDtos.EmployeeProfileResponse(
                employee.getId(),
                employee.getUser() == null ? null : employee.getUser().getId(),
                canReadSensitive ? employee.getPersonnelNumber() : maskPersonnelNumber(employee.getPersonnelNumber()),
                employee.getStatus(),
                canReadSensitive ? username : maskUsername(username),
                fullName,
                assignment == null ? null : assignment.getDepartment().getId(),
                assignment == null ? null : assignment.getDepartment().getName(),
                assignment == null || assignment.getManagerEmployee() == null ? null : assignment.getManagerEmployee().getId(),
                canReadSensitive == false
        );
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
}
