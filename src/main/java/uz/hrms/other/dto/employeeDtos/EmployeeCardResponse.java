package uz.hrms.other.dto.employeeDtos;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeCardResponse(
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
