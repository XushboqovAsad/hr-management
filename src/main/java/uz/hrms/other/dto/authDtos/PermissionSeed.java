package uz.hrms.other.dto.authDtos;

public record PermissionSeed(String moduleCode, String actionCode, String name, String description) {
    String authority() {
        return moduleCode + ":" + actionCode;
    }
}
