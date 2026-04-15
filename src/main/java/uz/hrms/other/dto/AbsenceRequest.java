package uz.hrms.other.dto;

import jakarta.validation.constraints.NotNull;
import uz.hrms.other.enums.AbsenceType;

import java.time.LocalDate;
import java.util.UUID;

public record AbsenceRequest(
        @NotNull UUID employeeId,
        UUID requesterEmployeeId,
        @NotNull AbsenceType absenceType,
        String reasonText,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        Boolean documentRequired
) {
}