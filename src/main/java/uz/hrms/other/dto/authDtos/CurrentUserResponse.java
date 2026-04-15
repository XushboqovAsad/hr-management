package uz.hrms.other.dto.authDtos;

import java.util.Set;
import java.util.UUID;

public record CurrentUserResponse(UUID userId, UUID employeeId, String username, String fullName, Set<String> roles, Set<String> permissions) {
}
