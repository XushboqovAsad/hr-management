package uz.hrms.auth.web;

import java.util.List;
import java.util.UUID;

public final class AdminSecurityDtos {

    private AdminSecurityDtos() {
    }

    public record RoleResponse(
            UUID id,
            String code,
            String name,
            String description,
            List<String> permissions
    ) {
    }

    public record PermissionResponse(
            UUID id,
            String moduleCode,
            String actionCode,
            String name,
            String description
    ) {
    }
}
