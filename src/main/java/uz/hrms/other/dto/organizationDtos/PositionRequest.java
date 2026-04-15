package uz.hrms.other.dto.organizationDtos;

import jakarta.validation.constraints.NotBlank;

record PositionRequest(
        @NotBlank String code,
        @NotBlank String title,
        String category,
        Boolean active
) {
}
