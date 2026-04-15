package uz.hrms.other.dto.employeeDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeUpsertRequest(
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
