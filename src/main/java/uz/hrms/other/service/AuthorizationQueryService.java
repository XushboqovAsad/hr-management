package uz.hrms.other.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class AuthorizationQueryService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    AuthorizationQueryService(RoleRepository roleRepository,
                              RolePermissionRepository rolePermissionRepository,
                              PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    List<RoleResponse> getRoles() {
        List<Role> roles = roleRepository.findAllByDeletedFalseOrderByNameAsc();
        List<UUID> roleIds = roles.stream().map(Role::getId).toList();
        Map<UUID, List<String>> permissionsByRole = new LinkedHashMap<>();
        if (roleIds.isEmpty() == false) {
            rolePermissionRepository.findAllByRoleIds(roleIds).forEach(item -> permissionsByRole.computeIfAbsent(item.getRole().getId(), key -> new ArrayList<>()).add(item.getPermission().authority()));
        }
        return roles.stream().map(role -> new RoleResponse(role.getId(), role.getCode().name(), role.getName(), role.getDescription(), permissionsByRole.getOrDefault(role.getId(), List.of()).stream().sorted().toList())).toList();
    }

    List<PermissionResponse> getPermissions() {
        return permissionRepository.findAllByDeletedFalseOrderByModuleCodeAscActionCodeAsc().stream()
                .map(item -> new PermissionResponse(item.getId(), item.getModuleCode(), item.getActionCode(), item.getName(), item.getDescription()))
                .toList();
    }
}
