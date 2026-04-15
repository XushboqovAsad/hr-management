package uz.hrms.other.dto.explanationDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

record ExplanationManagerReviewRequest(
        UUID managerReviewerEmployeeId,
        @NotBlank @Size(max = 2000) String managerComment
) {
}
