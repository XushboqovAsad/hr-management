package uz.hrms.other.dto.businessTripDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record BusinessTripDocumentUploadRequest(
        @NotNull uz.hrms.other.enums.BusinessTripDocumentKind documentKind,
        @NotBlank String title,
        String description
) {
}
