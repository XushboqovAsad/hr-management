package uz.hrms.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import uz.hrms.auth.CurrentUser;
import uz.hrms.other.entity.*;
import uz.hrms.other.repository.*;

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

    public SecurityUserService(UserAccountRepository userAccountRepository,
                               UserRoleAssignmentRepository userRoleAssignmentRepository,
                               RolePermissionRepository rolePermissionRepository,
                               EmployeeRepository employeeRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.employeeRepository = employeeRepository;
    }

    public CurrentUser loadByUserId(UUID userId) {
        UserAccount user = userAccountRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return toCurrentUser(user);
    }

    public CurrentUser loadByUsername(String username) {
        UserAccount user = userAccountRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return toCurrentUser(user);
    }

    public CurrentUser toCurrentUser(UserAccount user) {
        List<UserRoleAssignment> assignments = userRoleAssignmentRepository.findActiveAssignments(user.getId()).stream()
                .filter(this::isCurrentlyValid)
                .toList();

        List<UUID> roleIds = assignments.stream().map(item -> item.getRole().getId()).toList();
        Set<String> permissions = roleIds.isEmpty()
                ? Set.of()
                : rolePermissionRepository.findAllByRoleIds(roleIds).stream()
                    .map(rolePermission -> rolePermission.getPermission().authority())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> roles = assignments.stream()
                .map(UserRoleAssignment::getRole)
                .map(Role::getCode)
                .map(Enum::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        UUID employeeId = employeeRepository.findByUserIdAndDeletedFalse(user.getId())
                .map(BaseEntity::getId)
                .orElse(null);
        return new CurrentUser(user.getId(), employeeId, user.getUsername(), user.getPasswordHash(), user.isActive(), roles, permissions);
    }

    private boolean isCurrentlyValid(UserRoleAssignment assignment) {
        LocalDate today = LocalDate.now();
        if (assignment.isActive() == false) {
            return false;
        }
        if (assignment.getValidFrom() != null && assignment.getValidFrom().isAfter(today)) {
            return false;
        }
        return assignment.getValidTo() == null || assignment.getValidTo().isAfter(today) || assignment.getValidTo().isEqual(today);
    }
}
