package uz.hrms.other;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ExplanationSecuritySeed {

    @Bean
    ApplicationRunner explanationPermissionsSeeder(
        PermissionRepository permissionRepository,
        RoleRepository roleRepository,
        RolePermissionRepository rolePermissionRepository
    ) {
        return args -> {
            Permission explanationRead = upsertPermission(permissionRepository, "EXPLANATION", "READ", "Read explanations and incidents");
            Permission explanationWrite = upsertPermission(permissionRepository, "EXPLANATION", "WRITE", "Submit explanations and create manual incidents");
            Permission explanationReview = upsertPermission(permissionRepository, "EXPLANATION", "REVIEW", "Review explanations as manager");
            Permission explanationDecide = upsertPermission(permissionRepository, "EXPLANATION", "DECIDE", "Make HR decisions on explanations");
            Permission disciplineRead = upsertPermission(permissionRepository, "DISCIPLINE", "READ", "Read disciplinary actions and reports");
            Permission disciplineWrite = upsertPermission(permissionRepository, "DISCIPLINE", "WRITE", "Create disciplinary actions");
            Permission rewardRead = upsertPermission(permissionRepository, "REWARD", "READ", "Read rewards");
            Permission rewardWrite = upsertPermission(permissionRepository, "REWARD", "WRITE", "Create rewards");

            Map<RoleCode, List<Permission>> mapping = new EnumMap<>(RoleCode.class);
            mapping.put(RoleCode.SUPER_ADMIN, List.of(explanationRead, explanationWrite, explanationReview, explanationDecide, disciplineRead, disciplineWrite, rewardRead, rewardWrite));
            mapping.put(RoleCode.HR_ADMIN, List.of(explanationRead, explanationWrite, explanationReview, explanationDecide, disciplineRead, disciplineWrite, rewardRead, rewardWrite));
            mapping.put(RoleCode.HR_INSPECTOR, List.of(explanationRead, explanationWrite, explanationReview, explanationDecide, disciplineRead, disciplineWrite, rewardRead, rewardWrite));
            mapping.put(RoleCode.MANAGER, List.of(explanationRead, explanationWrite, explanationReview, disciplineRead, rewardRead));
            mapping.put(RoleCode.EMPLOYEE, List.of(explanationRead, explanationWrite, disciplineRead, rewardRead));
            mapping.put(RoleCode.AUDITOR, List.of(explanationRead, disciplineRead, rewardRead));
            mapping.put(RoleCode.TOP_MANAGEMENT, List.of(explanationRead, disciplineRead, rewardRead));
            mapping.put(RoleCode.PAYROLL_SPECIALIST, List.of(explanationRead, disciplineRead, rewardRead));
            mapping.put(RoleCode.SECURITY_OPERATOR, List.of(explanationRead, explanationWrite, disciplineRead));

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
