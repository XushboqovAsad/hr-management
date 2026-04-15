package uz.hrms.other.dto.employeeDtos;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record EmployeeStatusChangeRequest(
        @NotBlank String status,
        LocalDate dismissalDate
) {
}
