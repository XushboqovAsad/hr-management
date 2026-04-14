package uz.hrms.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hrms.auth.entity.Role;
import uz.hrms.auth.repository.PermissionRepository;
import uz.hrms.auth.repository.RolePermissionRepository;
import uz.hrms.auth.repository.RoleRepository;
import uz.hrms.auth.web.AdminSecurityDtos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuthorizationQueryService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public AuthorizationQueryService(RoleRepository roleRepository,
                                     RolePermissionRepository rolePermissionRepository,
                                     PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<AdminSecurityDtos.RoleResponse> getRoles() {
        List<Role> roles = roleRepository.findAllByIsDeletedFalseOrderByNameAsc();
        List<UUID> roleIds = roles.stream().map(Role::getId).toList();
        Map<UUID, List<String>> permissionsByRole = new LinkedHashMap<>();
        rolePermissionRepository.findAllByRoleIds(roleIds).forEach(item ->
                permissionsByRole.computeIfAbsent(item.getRole().getId(), key -> new java.util.ArrayList<>())
                        .add(item.getPermission().authority())
        );
        return roles.stream()
                .map(role -> new AdminSecurityDtos.RoleResponse(
                        role.getId(),
                        role.getCode().name(),
                        role.getName(),
                        role.getDescription(),
                        permissionsByRole.getOrDefault(role.getId(), List.of()).stream().sorted().collect(Collectors.toList())
                ))
                .toList();
    }

    public List<AdminSecurityDtos.PermissionResponse> getPermissions() {
        return permissionRepository.findAllByIsDeletedFalseOrderByModuleCodeAscActionCodeAsc().stream()
                .map(permission -> new AdminSecurityDtos.PermissionResponse(
                        permission.getId(),
                        permission.getModuleCode(),
                        permission.getActionCode(),
                        permission.getName(),
                        permission.getDescription()
                ))
                .toList();
    }
}
