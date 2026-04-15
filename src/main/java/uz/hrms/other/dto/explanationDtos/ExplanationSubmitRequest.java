package uz.hrms.other.dto.explanationDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ExplanationSubmitRequest(
        @NotBlank @Size(max = 4000) String explanationText
) {
}
