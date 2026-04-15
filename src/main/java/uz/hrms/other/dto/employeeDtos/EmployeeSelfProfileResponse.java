package uz.hrms.other.dto.employeeDtos;

import java.util.UUID;

public record EmployeeSelfProfileResponse(
        UUID employeeId,
        UUID userId,
        String personnelNumber,
        String employmentStatus,
        String username,
        String firstName,
        String lastName,
        String middleName,
        String fullName,
        String email,
        UUID departmentId,
        String departmentName,
        UUID managerEmployeeId,
        String managerName
) {
}
