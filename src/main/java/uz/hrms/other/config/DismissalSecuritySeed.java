package uz.hrms.other;

import java.security.Permission;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.hrms.other.entity.RolePermission;
import uz.hrms.other.enums.RoleCode;
import uz.hrms.other.repository.PermissionRepository;
import uz.hrms.other.repository.RolePermissionRepository;
import uz.hrms.other.repository.RoleRepository;

import javax.management.relation.Role;

@Configuration
class DismissalSecuritySeed {

    @Bean
    ApplicationRunner dismissalPermissionsSeeder(
        PermissionRepository permissionRepository,
        RoleRepository roleRepository,
        RolePermissionRepository rolePermissionRepository
    ) {
        return args -> {
            Permission read = upsertPermission(permissionRepository, "DISMISSAL", "READ", "Read dismissal requests");
            Permission write = upsertPermission(permissionRepository, "DISMISSAL", "WRITE", "Create and update dismissal requests");
            Permission approve = upsertPermission(permissionRepository, "DISMISSAL", "APPROVE", "Approve dismissal requests");
            Permission finalizePermission = upsertPermission(permissionRepository, "DISMISSAL", "FINALIZE", "Finalize dismissal and block accounts");
            Permission archive = upsertPermission(permissionRepository, "DISMISSAL", "ARCHIVE", "Archive dismissed employee cards");

            Map<RoleCode, List<Permission>> mapping = new EnumMap<>(RoleCode.class);
            mapping.put(RoleCode.SUPER_ADMIN, List.of(read, write, approve, finalizePermission, archive));
            mapping.put(RoleCode.HR_ADMIN, List.of(read, write, approve, finalizePermission, archive));
            mapping.put(RoleCode.HR_INSPECTOR, List.of(read, write, approve, finalizePermission));
            mapping.put(RoleCode.MANAGER, List.of(read, write, approve));
            mapping.put(RoleCode.PAYROLL_SPECIALIST, List.of(read));
            mapping.put(RoleCode.AUDITOR, List.of(read));
            mapping.put(RoleCode.TOP_MANAGEMENT, List.of(read));
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
        PermissionRepository existing = permissionRepository.findByModuleCodeAndActionCodeAndDeletedFalse(moduleCode, actionCode).orElse(null);
        if (existing == null) {
            Permission permission = new Permission();
            permission.setModuleCode(moduleCode);
            permission.setActionCode(actionCode);
            permission.setName(moduleCode + ":" + actionCode);
            permission.setDescription(description);
            return permissionRepository.save(permission);
        }
        return existing;
    }
}
