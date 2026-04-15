package uz.hrms.other.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SecurityUserService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final EmployeeRepository employeeRepository;

    SecurityUserService(UserAccountRepository userAccountRepository,
                        UserRoleAssignmentRepository userRoleAssignmentRepository,
                        RolePermissionRepository rolePermissionRepository,
                        EmployeeRepository employeeRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.employeeRepository = employeeRepository;
    }

    CurrentUser loadByUserId(UUID userId) {
        UserAccount user = userAccountRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return toCurrentUser(user);
    }

    CurrentUser toCurrentUser(UserAccount user) {
        List<UserRoleAssignment> assignments = userRoleAssignmentRepository.findActiveAssignments(user.getId()).stream()
                .filter(this::isActiveNow)
                .toList();
        List<UUID> roleIds = assignments.stream().map(item -> item.getRole().getId()).toList();
        Set<String> permissions = roleIds.isEmpty()
                ? Set.of()
                : rolePermissionRepository.findAllByRoleIds(roleIds).stream().map(item -> item.getPermission().authority()).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> roles = assignments.stream().map(UserRoleAssignment::getRole).map(Role::getCode).map(Enum::name).collect(Collectors.toCollection(LinkedHashSet::new));
        UUID employeeId = employeeRepository.findByUserIdAndDeletedFalse(user.getId()).map(Employee::getId).orElse(null);
        return new CurrentUser(user.getId(), employeeId, user.getUsername(), user.getPasswordHash(), user.isActive(), roles, permissions);
    }

    private boolean isActiveNow(UserRoleAssignment item) {
        LocalDate today = LocalDate.now();
        boolean validFrom = item.getValidFrom() == null || item.getValidFrom().isBefore(today) || item.getValidFrom().isEqual(today);
        boolean validTo = item.getValidTo() == null || item.getValidTo().isAfter(today) || item.getValidTo().isEqual(today);
        return item.isActive() && validFrom && validTo;
    }
}
