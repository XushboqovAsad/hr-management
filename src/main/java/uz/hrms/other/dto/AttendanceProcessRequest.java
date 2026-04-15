package uz.hrms.other.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceProcessRequest(
        @NotNull LocalDate workDate,
        UUID employeeId
) {
}
