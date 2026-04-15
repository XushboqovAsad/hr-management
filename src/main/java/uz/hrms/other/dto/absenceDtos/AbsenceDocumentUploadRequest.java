package uz.hrms.other.dto.absenceDtos;

import jakarta.validation.constraints.NotBlank;

public record AbsenceDocumentUploadRequest(
        @NotBlank String title,
        String description
) {
}
