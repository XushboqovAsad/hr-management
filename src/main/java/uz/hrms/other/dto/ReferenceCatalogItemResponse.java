package uz.hrms.other.dto;

import java.util.Map;
import java.util.UUID;

public record ReferenceCatalogItemResponse(
        UUID id,
        String code,
        String name,
        String description,
        boolean active,
        Map<String, Object> attributes
) {
}
