package uz.hrms.other.module;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import uz.hrms.audit.RequestAuditFilter;
import uz.hrms.auth.CurrentUser;
import uz.hrms.other.entity.AccessDelegation;
import uz.hrms.other.entity.Department;
import uz.hrms.other.entity.EmployeeAssignment;
import uz.hrms.other.entity.UserRoleAssignment;
import uz.hrms.other.enums.AccessScopeType;
import uz.hrms.other.enums.RoleCode;
import uz.hrms.other.repository.DepartmentRepository;
import uz.hrms.other.repository.EmployeeAssignmentRepository;
import uz.hrms.other.repository.*;
import uz.hrms.security.JwtAuthenticationFilter;
import uz.hrms.security.RateLimitingFilter;

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

    AccessPolicy(UserRoleAssignmentRepository userRoleAssignmentRepository,
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
        if (currentUser.roles().contains(RoleCode.SUPER_ADMIN.name())) {
            return true;
        }
        return currentUser.permissions().contains(authority(module, action));
    }

    boolean canReadAudit(Authentication authentication) {
        return hasPermission(authentication, "AUDIT", "READ");
    }

    boolean canReadEmployee(Authentication authentication, UUID employeeId) {
        CurrentUser currentUser = currentUser(authentication);
        if (currentUser == null) {
            return false;
        }
        if (currentUser.roles().contains(RoleCode.SUPER_ADMIN.name())) {
            return true;
        }
        String requiredAuthority = authority("EMPLOYEE", "READ");

        if (!currentUser.permissions().contains(requiredAuthority)) {
            return false;
        }

        List<UserRoleAssignment> assignments = activeAssignments(currentUser.userId());
        Set<UUID> roleIds = assignments.stream().map(item -> item.getRole().getId()).collect(Collectors.toSet());
        Set<UUID> allowedRoles = roleIds.isEmpty()
                ? Set.of()
                : rolePermissionRepository.findAllByRoleIds(List.copyOf(roleIds)).stream()
                .filter(item -> item.getPermission().authority().equals(requiredAuthority))
                .map(item -> item.getRole().getId())
                .collect(Collectors.toSet());

        for (UserRoleAssignment assignment : assignments) {
            if (!allowedRoles.contains(assignment.getRole().getId())) {
                continue;
            }
            if (scopeAllows(currentUser, assignment.getScopeType(), assignment.getScopeDepartmentId(), null, employeeId)) {
                return true;
            }
        }

        for (AccessDelegation delegation : accessDelegationRepository.findActiveDelegations(currentUser.userId(), "EMPLOYEE", "READ")) {
            if (delegationActive(delegation) && scopeAllows(currentUser, delegation.getScopeType(), delegation.getScopeDepartmentId(), delegation.getEmployeeId(), employeeId)) {
                return true;
            }
        }
        return false;
    }

    private boolean scopeAllows(CurrentUser currentUser, AccessScopeType scopeType, UUID scopeDepartmentId, UUID scopeEmployeeId, UUID targetEmployeeId) {
        return switch (scopeType) {
            case GLOBAL -> true;
            case SELF -> currentUser.employeeId() == null ? false : currentUser.employeeId().equals(targetEmployeeId);
            case EMPLOYEE -> scopeEmployeeId == null ? false : scopeEmployeeId.equals(targetEmployeeId);
            case SUBORDINATES -> currentUser.employeeId() == null ? false : employeeAssignmentRepository.existsCurrentSubordinate(currentUser.employeeId(), targetEmployeeId, LocalDate.now());
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
                continue;
            }
            current = departmentRepository.findById(current.getId()).orElse(null);
        }
        return false;
    }

    private List<UserRoleAssignment> activeAssignments(UUID userId) {
        LocalDate today = LocalDate.now();
        return userRoleAssignmentRepository.findActiveAssignments(userId).stream()
                .filter(item -> item.getValidFrom() == null || item.getValidFrom().isBefore(today) || item.getValidFrom().isEqual(today))
                .filter(item -> item.getValidTo() == null || item.getValidTo().isAfter(today) || item.getValidTo().isEqual(today))
                .toList();
    }

    private boolean delegationActive(AccessDelegation delegation) {
        LocalDate today = LocalDate.now();

        if (!delegation.isActive()) {
            return false;
        }
        if (delegation.getValidFrom() != null && delegation.getValidFrom().isAfter(today)) {
            return false;
        }
        return delegation.getValidTo() == null || delegation.getValidTo().isAfter(today) || delegation.getValidTo().isEqual(today);
    }

    private CurrentUser currentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CurrentUser)) {
            return null;
        }
        return (CurrentUser) principal;
    }

    private String authority(String module, String action) {
        return module + ":" + action;
    }
}

@Configuration
class SecurityModule {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtAuthenticationFilter,
                                            RateLimitingFilter rateLimitingFilter,
                                            RequestAuditFilter requestAuditFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestAuditFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
