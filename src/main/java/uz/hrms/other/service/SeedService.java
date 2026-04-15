package uz.hrms.other.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hrms.other.BootstrapAdminProperties;
import uz.hrms.other.PermissionCatalog;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SeedService implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    SeedService(PermissionRepository permissionRepository,
                RoleRepository roleRepository,
                RolePermissionRepository rolePermissionRepository,
                UserAccountRepository userAccountRepository,
                UserRoleAssignmentRepository userRoleAssignmentRepository,
                PasswordEncoder passwordEncoder,
                BootstrapAdminProperties bootstrapAdminProperties) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userAccountRepository = userAccountRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminProperties = bootstrapAdminProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, Permission> permissions = seedPermissions();
        Map<RoleCode, Role> roles = seedRoles();
        seedRolePermissions(permissions, roles);
        seedBootstrapAdmin(roles);
    }

    private Map<String, Permission> seedPermissions() {
        Map<String, Permission> result = new LinkedHashMap<>();
        for (PermissionSeed definition : PermissionCatalog.DEFINITIONS) {
            Permission permission = permissionRepository.findByModuleCodeAndActionCodeAndDeletedFalse(definition.moduleCode(), definition.actionCode()).orElseGet(Permission::new);
            permission.setModuleCode(definition.moduleCode());
            permission.setActionCode(definition.actionCode());
            permission.setName(definition.name());
            permission.setDescription(definition.description());
            permissionRepository.save(permission);
            result.put(definition.authority(), permission);
        }
        return result;
    }

    private Map<RoleCode, Role> seedRoles() {
        Map<RoleCode, Role> result = new LinkedHashMap<>();
        for (RoleCode roleCode : RoleCode.values()) {
            Role role = roleRepository.findByCodeAndDeletedFalse(roleCode).orElseGet(Role::new);
            role.setCode(roleCode);
            role.setName(roleCode.name().replace('_', ' '));
            role.setDescription("System role " + roleCode.name());
            role.setSystemRole(true);
            roleRepository.save(role);
            result.put(roleCode, role);
        }
        return result;
    }

    private void seedRolePermissions(Map<String, Permission> permissionMap, Map<RoleCode, Role> roleMap) {
        Map<RoleCode, Set<String>> rolePermissions = PermissionCatalog.rolePermissions();
        for (Map.Entry<RoleCode, Set<String>> entry : rolePermissions.entrySet()) {
            Role role = roleMap.get(entry.getKey());
            List<RolePermission> existing = rolePermissionRepository.findAllByRoleIds(List.of(role.getId()));
            Set<String> existingAuthorities = existing.stream().map(item -> item.getPermission().authority()).collect(Collectors.toSet());
            for (String authority : entry.getValue()) {
                if (existingAuthorities.contains(authority)) {
                    continue;
                }
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermission(permissionMap.get(authority));
                rolePermissionRepository.save(rolePermission);
            }
        }
    }

    private void seedBootstrapAdmin(Map<RoleCode, Role> roleMap) {
        if (bootstrapAdminProperties.enabled() == false) {
            return;
        }
        UserAccount user = userAccountRepository.findByUsernameIgnoreCaseAndDeletedFalse(bootstrapAdminProperties.username()).orElseGet(UserAccount::new);
        user.setUsername(bootstrapAdminProperties.username());
        user.setPasswordHash(passwordEncoder.encode(bootstrapAdminProperties.password()));
        user.setEmail(bootstrapAdminProperties.email());
        user.setFirstName("System");
        user.setLastName("Administrator");
        user.setActive(true);
        userAccountRepository.save(user);

        boolean superAdminAssigned = userRoleAssignmentRepository.findActiveAssignments(user.getId()).stream().anyMatch(item -> item.getRole().getCode() == RoleCode.SUPER_ADMIN);
        if (superAdminAssigned) {
            return;
        }
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(roleMap.get(RoleCode.SUPER_ADMIN));
        assignment.setScopeType(AccessScopeType.GLOBAL);
        assignment.setValidFrom(LocalDate.now());
        assignment.setActive(true);
        userRoleAssignmentRepository.save(assignment);
    }
}
