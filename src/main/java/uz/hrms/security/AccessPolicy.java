package uz.hrms.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import uz.hrms.auth.CurrentUser;
import uz.hrms.auth.entity.AccessDelegation;
import uz.hrms.auth.entity.RoleCode;
import uz.hrms.auth.entity.UserRoleAssignment;
import uz.hrms.auth.repository.AccessDelegationRepository;
import uz.hrms.auth.repository.RolePermissionRepository;
import uz.hrms.auth.repository.UserRoleAssignmentRepository;
import uz.hrms.employee.Department;
import uz.hrms.employee.DepartmentRepository;
import uz.hrms.employee.EmployeeAssignment;
import uz.hrms.employee.EmployeeAssignmentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("accessPolicy")
public class AccessPolicy {

    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AccessDelegationRepository accessDelegationRepository;
    private final EmployeeAssignmentRepository employeeAssignmentRepository;
    private final DepartmentRepository departmentRepository;

    public AccessPolicy(UserRoleAssignmentRepository userRoleAssignmentRepository,
                        RolePermissionRepository rolePermissionRepository,
                        AccessDelegationRepository accessDelegationRepository,
                        EmployeeAssignmentRepository employeeAssignmentRepository,
                        DepartmentRepository departmentRepository) {
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.accessDelegationRepository = accessDelegationRepository;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.departmentRepository = departmentRepository;
    }

    public boolean hasPermission(Authentication authentication, String module, String action) {
        CurrentUser currentUser = currentUser(authentication);
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getRoles().contains(RoleCode.SUPER_ADMIN.name())) {
            return true;
        }
        return currentUser.getPermissions().contains(authority(module, action));
    }

    public boolean canReadAudit(Authentication authentication) {
        return hasPermission(authentication, "AUDIT", "READ");
    }

    public boolean canReadEmployee(Authentication authentication, UUID employeeId) {
        CurrentUser currentUser = currentUser(authentication);
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getRoles().contains(RoleCode.SUPER_ADMIN.name())) {
            return true;
        }
        String requiredAuthority = authority("EMPLOYEE", "READ");
        if (currentUser.getPermissions().contains(requiredAuthority) == false) {
            return false;
        }
        List<UserRoleAssignment> assignments = activeAssignments(currentUser.getUserId());
        Set<UUID> roleIds = assignments.stream().map(item -> item.getRole().getId()).collect(Collectors.toSet());
        Set<String> authorities = roleIds.isEmpty() ? Set.of() : rolePermissionRepository.findAllByRoleIds(roleIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRole().getId(), Collectors.mapping(item -> item.getPermission().authority(), Collectors.toSet())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().contains(requiredAuthority))
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), java.util.Set::copyOf));

        for (UserRoleAssignment assignment : assignments) {
            if (!authorities.contains(assignment.getRole().getId())) {
                continue;
            }
            if (scopeAllows(currentUser, assignment.getScopeType(), assignment.getScopeDepartmentId(), null, employeeId)) {
                return true;
            }
        }

        for (AccessDelegation delegation : accessDelegationRepository.findActiveDelegations(currentUser.getUserId(), "EMPLOYEE", "READ")) {
            if (delegationIsActive(delegation) && scopeAllows(currentUser, delegation.getScopeType(), delegation.getScopeDepartmentId(), delegation.getEmployeeId(), employeeId)) {
                return true;
            }
        }
        return false;
    }

    public boolean canReadEmployeeSensitive(Authentication authentication, UUID employeeId) {
        CurrentUser currentUser = currentUser(authentication);
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getEmployeeId() != null && currentUser.getEmployeeId().equals(employeeId)) {
            return true;
        }
        if (currentUser.getRoles().contains(RoleCode.SUPER_ADMIN.name())) {
            return true;
        }
        if (currentUser.getPermissions().contains(authority("EMPLOYEE_SENSITIVE", "READ")) == false) {
            return false;
        }
        return canReadEmployee(authentication, employeeId);
    }

    private boolean scopeAllows(CurrentUser currentUser, AccessScopeType scopeType, UUID scopeDepartmentId, UUID scopeEmployeeId, UUID targetEmployeeId) {
        return switch (scopeType) {
            case GLOBAL -> true;
            case SELF -> currentUser.getEmployeeId() != null && currentUser.getEmployeeId().equals(targetEmployeeId);
            case EMPLOYEE -> scopeEmployeeId != null && scopeEmployeeId.equals(targetEmployeeId);
            case SUBORDINATES -> currentUser.getEmployeeId() != null && employeeAssignmentRepository.existsCurrentSubordinate(currentUser.getEmployeeId(), targetEmployeeId, LocalDate.now());
            case DEPARTMENT -> departmentInScope(scopeDepartmentId, targetEmployeeId);
        };
    }

    private boolean departmentInScope(UUID scopeDepartmentId, UUID targetEmployeeId) {
        if (scopeDepartmentId == null) {
            return false;
        }
        EmployeeAssignment assignment = employeeAssignmentRepository.findCurrentPrimaryAssignment(targetEmployeeId, LocalDate.now()).orElse(null);
        if (assignment == null) {
            return false;
        }
        Department current = assignment.getDepartment();
        while (current != null) {
            if (current.getId().equals(scopeDepartmentId)) {
                return true;
            }
            current = current.getParentDepartment();
            if (current == null) {
                break;
            }
            current = departmentRepository.findById(current.getId()).orElse(null);
        }
        return false;
    }

    private List<UserRoleAssignment> activeAssignments(UUID userId) {
        LocalDate today = LocalDate.now();
        return userRoleAssignmentRepository.findActiveAssignments(userId).stream()
                .filter(item -> item.getValidFrom() == null || item.getValidFrom().isEqual(today) || item.getValidFrom().isBefore(today))
                .filter(item -> item.getValidTo() == null || item.getValidTo().isEqual(today) || item.getValidTo().isAfter(today))
                .toList();
    }

    private boolean delegationIsActive(AccessDelegation delegation) {
        LocalDate today = LocalDate.now();
        if (delegation.isActive() == false) {
            return false;
        }

        if (delegation.getValidFrom() != null && delegation.getValidFrom().isAfter(today)) {
            return false;
        }

        return delegation.getValidTo() == null || delegation.getValidTo().isEqual(today) || delegation.getValidTo().isAfter(today);
    }

    private CurrentUser currentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (!(authentication.getPrincipal() instanceof CurrentUser currentUser)) {
            return null;
        }
        return currentUser;
    }

    private String authority(String module, String action) {
        return module + ":" + action;
    }
}
