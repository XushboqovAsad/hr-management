package uz.hrms.auth.web.dtos;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String moduleCode,
        String actionCode,
        String name,
        String description
) {
}
