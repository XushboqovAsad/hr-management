package uz.hrms.other.dto.explanationDtos;

import jakarta.validation.constraints.Size;

import java.util.UUID;

record ExplanationDecisionRequest(
        UUID hrReviewerEmployeeId,
        @Size(max = 2000) String hrComment,
        UUID sourceOrderId,
        @Size(max = 100) String sourceOrderNumber
) {
}
