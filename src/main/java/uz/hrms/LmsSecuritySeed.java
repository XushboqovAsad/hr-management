package uz.hrms;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LmsSecuritySeed {

    @Bean
    ApplicationRunner lmsPermissionsSeeder(
        PermissionRepository permissionRepository,
        RoleRepository roleRepository,
        RolePermissionRepository rolePermissionRepository
    ) {
        return args -> {
            Permission read = upsertPermission(permissionRepository, "LMS", "READ", "Read learning courses and assignments");
            Permission write = upsertPermission(permissionRepository, "LMS", "WRITE", "Work with learning progress and content");
            Permission assign = upsertPermission(permissionRepository, "LMS", "ASSIGN", "Assign mandatory and manual courses");
            Permission report = upsertPermission(permissionRepository, "LMS", "REPORT", "Read LMS reports");

            Map<RoleCode, List<Permission>> mapping = new EnumMap<>(RoleCode.class);
            mapping.put(RoleCode.SUPER_ADMIN, List.of(read, write, assign, report));
            mapping.put(RoleCode.HR_ADMIN, List.of(read, write, assign, report));
            mapping.put(RoleCode.HR_INSPECTOR, List.of(read, assign, report));
            mapping.put(RoleCode.MANAGER, List.of(read, report));
            mapping.put(RoleCode.EMPLOYEE, List.of(read, write));
            mapping.put(RoleCode.TOP_MANAGEMENT, List.of(read, report));
            mapping.put(RoleCode.AUDITOR, List.of(read, report));
            mapping.put(RoleCode.PAYROLL_SPECIALIST, List.of(read));
            mapping.put(RoleCode.SECURITY_OPERATOR, List.of(read));

            for (Map.Entry<RoleCode, List<Permission>> entry : mapping.entrySet()) {
                Role role = roleRepository.findByCodeAndDeletedFalse(entry.getKey()).orElse(null);
                if (role == null) {
                    continue;
                }
                List<RolePermission> existing = rolePermissionRepository.findAllByRoleIds(List.of(role.getId()));
                for (Permission permission : entry.getValue()) {
                    boolean exists = existing.stream().anyMatch(item -> item.getPermission().authority().equals(permission.authority()));
                    if (exists) {
                        continue;
                    }
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRole(role);
                    rolePermission.setPermission(permission);
                    rolePermissionRepository.save(rolePermission);
                }
            }
        };
    }

    private Permission upsertPermission(PermissionRepository permissionRepository, String moduleCode, String actionCode, String description) {
        Permission existing = permissionRepository.findByModuleCodeAndActionCodeAndDeletedFalse(moduleCode, actionCode).orElse(null);
        if (existing != null) {
            return existing;
        }
        Permission permission = new Permission();
        permission.setModuleCode(moduleCode);
        permission.setActionCode(actionCode);
        permission.setName(moduleCode + ":" + actionCode);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }
}
