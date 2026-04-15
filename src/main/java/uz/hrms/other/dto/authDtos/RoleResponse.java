package uz.hrms.other.dto.authDtos;

import java.util.List;
import java.util.UUID;

public record RoleResponse(UUID id, String code, String name, String description, List<String> permissions) {
}
