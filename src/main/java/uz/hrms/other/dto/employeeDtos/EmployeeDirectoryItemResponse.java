package uz.hrms.other.dto.employeeDtos;

import java.util.UUID;

public record EmployeeDirectoryItemResponse(
        UUID employeeId,
        String personnelNumber,
        String fullName,
        String email,
        UUID departmentId,
        String departmentName,
        UUID managerEmployeeId,
        String managerName,
        String employmentStatus
) {
}
