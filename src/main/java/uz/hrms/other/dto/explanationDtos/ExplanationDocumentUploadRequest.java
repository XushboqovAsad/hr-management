package uz.hrms.other.dto.explanationDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ExplanationDocumentUploadRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description
) {
}
