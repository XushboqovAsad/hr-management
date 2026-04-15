package uz.hrms.other.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ReferenceCatalogUpsertRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        Boolean active,
        Map<String, Object> attributes
) {
}
