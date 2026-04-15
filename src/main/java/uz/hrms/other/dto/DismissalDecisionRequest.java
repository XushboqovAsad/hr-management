package uz.hrms.other.dto;

import jakarta.validation.constraints.Size;

public record DismissalDecisionRequest(
    @Size(max = 2000) String commentText
) {
}

