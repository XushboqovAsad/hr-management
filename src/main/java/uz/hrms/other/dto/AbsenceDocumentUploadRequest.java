package uz.hrms.other.dto;

import jakarta.validation.constraints.NotBlank;

public record AbsenceDocumentUploadRequest(
        @NotBlank String title,
        String description
) {
}