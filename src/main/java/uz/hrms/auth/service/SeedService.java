package uz.hrms.auth.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uz.hrms.other.Permission;
import uz.hrms.other.Role;
import uz.hrms.other.RoleCode;
import uz.hrms.other.RolePermission;
import uz.hrms.other.UserAccount;
import uz.hrms.other.UserRoleAssignment;
import uz.hrms.other.PermissionRepository;
import uz.hrms.other.RolePermissionRepository;
import uz.hrms.other.RoleRepository;
import uz.hrms.other.UserAccountRepository;
import uz.hrms.other.UserRoleAssignmentRepository;
import uz.hrms.config.BootstrapAdminProperties;
import uz.hrms.security.AccessScopeType;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SeedService implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    public SeedService(PermissionRepository permissionRepository,
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
        Map<String, Permission> permissionMap = seedPermissions();
        Map<RoleCode, Role> roleMap = seedRoles();
        seedRolePermissions(permissionMap, roleMap);
        seedBootstrapAdmin(roleMap);
    }

    private Map<String, Permission> seedPermissions() {
        Map<String, Permission> result = new LinkedHashMap<>();
        for (PermissionCatalog.PermissionDefinition definition : PermissionCatalog.DEFINITIONS) {
            Permission permission = permissionRepository.findByModuleCodeAndActionCodeAndIsDeletedFalse(definition.moduleCode(), definition.actionCode())
                    .orElseGet(Permission::new);
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
            Role role = roleRepository.findByCodeAndIsDeletedFalse(roleCode).orElseGet(Role::new);
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
        Map<RoleCode, java.util.Set<String>> mapping = PermissionCatalog.rolePermissions();
        for (Map.Entry<RoleCode, java.util.Set<String>> entry : mapping.entrySet()) {
            Role role = roleMap.get(entry.getKey());
            for (String authority : entry.getValue()) {
                boolean alreadyExists = rolePermissionRepository.findAllByRoleIds(java.util.List.of(role.getId())).stream()
                        .anyMatch(item -> item.getPermission().authority().equals(authority));
                if (alreadyExists) {
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
        UserAccount user = userAccountRepository.findByUsernameIgnoreCaseAndIsDeletedFalse(bootstrapAdminProperties.username())
                .orElseGet(UserAccount::new);
        user.setUsername(bootstrapAdminProperties.username());
        user.setPasswordHash(passwordEncoder.encode(bootstrapAdminProperties.password()));
        user.setEmail(bootstrapAdminProperties.email());
        user.setFirstName("System");
        user.setLastName("Administrator");
        user.setActive(true);
        userAccountRepository.save(user);

        boolean assignmentExists = userRoleAssignmentRepository.findActiveAssignments(user.getId()).stream()
                .anyMatch(item -> item.getRole().getCode() == RoleCode.SUPER_ADMIN);
        if (assignmentExists) {
            return;
        }
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(roleMap.get(RoleCode.SUPER_ADMIN));
        assignment.setScopeType(AccessScopeType.GLOBAL);
        assignment.setActive(true);
        userRoleAssignmentRepository.save(assignment);
    }
}
