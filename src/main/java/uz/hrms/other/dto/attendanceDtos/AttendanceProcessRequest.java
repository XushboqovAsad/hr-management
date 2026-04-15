package uz.hrms.other.dto.attendanceDtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

record AttendanceProcessRequest(
        @NotNull LocalDate workDate,
        UUID employeeId
) {
}
