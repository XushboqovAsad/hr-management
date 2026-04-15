package uz.hrms.other.dto.employeeDtos;

import java.util.UUID;

public record EmployeeProfileResponse(
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
