package uz.hrms.other.dto.employeeDtos;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeListItemResponse(
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
