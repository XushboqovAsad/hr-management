package uz.hrms.auth.web.dtos;

import java.util.Set;
import java.util.UUID;

public record CurrentUserResponse(
        UUID userId,
        UUID employeeId,
        String username,
        String fullName,
        Set<String> roles,
        Set<String> permissions
) {
}
