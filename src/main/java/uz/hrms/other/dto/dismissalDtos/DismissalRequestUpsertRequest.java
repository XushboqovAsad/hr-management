package uz.hrms.other.dto.dismissalDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record DismissalRequestUpsertRequest(
        @NotNull UUID employeeId,
        UUID initiatorEmployeeId,
        UUID departmentId,
        @NotNull DismissalReasonType reasonType,
        @NotBlank @Size(max = 2000) String reasonText,
        @NotNull LocalDate dismissalDate,
        @Size(max = 2000) String commentText
) {
}
