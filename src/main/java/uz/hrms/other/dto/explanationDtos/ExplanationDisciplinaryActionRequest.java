package uz.hrms.other.dto.explanationDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.hrms.other.enums.DisciplinaryActionType;

import java.time.LocalDate;
import java.util.UUID;

record ExplanationDisciplinaryActionRequest(
        UUID hrReviewerEmployeeId,
        @NotNull DisciplinaryActionType actionType,
        @NotNull LocalDate actionDate,
        @NotBlank @Size(max = 2000) String reasonText,
        LocalDate validUntil,
        UUID sourceOrderId,
        @Size(max = 100) String sourceOrderNumber,
        @Size(max = 2000) String hrComment
) {
}
