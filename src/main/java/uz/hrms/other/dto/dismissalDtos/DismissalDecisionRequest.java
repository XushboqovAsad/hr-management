package uz.hrms.other.dto.dismissalDtos;

import jakarta.validation.constraints.Size;

record DismissalDecisionRequest(
        @Size(max = 2000) String commentText
) {
}
