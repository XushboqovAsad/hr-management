package uz.hrms.other;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

record LoginRequest(@NotBlank String username, @NotBlank String password) {
}

record RefreshRequest(@NotBlank String refreshToken) {
}

record LogoutRequest(@NotBlank String refreshToken) {
}

record CurrentUserResponse(UUID userId, UUID employeeId, String username, String fullName, Set<String> roles, Set<String> permissions) {
}

record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds, CurrentUserResponse user) {
}

record RoleResponse(UUID id, String code, String name, String description, List<String> permissions) {
}

record PermissionResponse(UUID id, String moduleCode, String actionCode, String name, String description) {
}

record PermissionSeed(String moduleCode, String actionCode, String name, String description) {
    String authority() {
        return moduleCode + ":" + actionCode;
    }
}

final class PermissionCatalog {

    private PermissionCatalog() {
    }

    static final List<PermissionSeed> DEFINITIONS = List.of(
            new PermissionSeed("AUTH", "READ", "Read auth profile", "Allows reading current auth context"),
            new PermissionSeed("ROLE", "READ", "Read roles", "Allows reading roles and permissions"),
            new PermissionSeed("ROLE", "WRITE", "Manage roles", "Allows managing roles and permissions"),
            new PermissionSeed("EMPLOYEE", "READ", "Read employees", "Allows reading employee data"),
            new PermissionSeed("EMPLOYEE", "WRITE", "Manage employees", "Allows creating and updating employee data"),
            new PermissionSeed("DEPARTMENT", "READ", "Read departments", "Allows reading departments"),
            new PermissionSeed("DEPARTMENT", "WRITE", "Manage departments", "Allows managing departments"),
            new PermissionSeed("POSITION", "READ", "Read positions", "Allows reading positions"),
            new PermissionSeed("POSITION", "WRITE", "Manage positions", "Allows managing positions"),
            new PermissionSeed("STAFFING", "READ", "Read staffing", "Allows reading staffing units"),
            new PermissionSeed("STAFFING", "WRITE", "Manage staffing", "Allows managing staffing units"),
            new PermissionSeed("DOCUMENT", "READ", "Read documents", "Allows reading documents"),
            new PermissionSeed("DOCUMENT", "WRITE", "Manage documents", "Allows managing documents"),
            new PermissionSeed("PAYROLL", "READ", "Read payroll", "Allows reading payroll data"),
            new PermissionSeed("PAYROLL", "WRITE", "Manage payroll", "Allows managing payroll events"),
            new PermissionSeed("ATTENDANCE", "READ", "Read attendance", "Allows reading attendance data"),
            new PermissionSeed("ATTENDANCE", "WRITE", "Manage attendance", "Allows managing attendance data"),
            new PermissionSeed("AUDIT", "READ", "Read audit logs", "Allows reading audit logs"),
            new PermissionSeed("NOTIFICATION", "READ", "Read notifications", "Allows reading notifications")
    );

    static Map<RoleCode, Set<String>> rolePermissions() {
        Map<RoleCode, Set<String>> mapping = new LinkedHashMap<>();
        Set<String> allPermissions = DEFINITIONS.stream().map(PermissionSeed::authority).collect(Collectors.toCollection(LinkedHashSet::new));
        mapping.put(RoleCode.SUPER_ADMIN, allPermissions);
        mapping.put(RoleCode.HR_ADMIN, Set.of("AUTH:READ", "ROLE:READ", "EMPLOYEE:READ", "EMPLOYEE:WRITE", "DEPARTMENT:READ", "DEPARTMENT:WRITE", "POSITION:READ", "POSITION:WRITE", "STAFFING:READ", "STAFFING:WRITE", "DOCUMENT:READ", "DOCUMENT:WRITE", "AUDIT:READ", "ATTENDANCE:READ", "PAYROLL:READ", "NOTIFICATION:READ"));
        mapping.put(RoleCode.HR_INSPECTOR, Set.of("AUTH:READ", "EMPLOYEE:READ", "EMPLOYEE:WRITE", "DEPARTMENT:READ", "POSITION:READ", "POSITION:WRITE", "STAFFING:READ", "STAFFING:WRITE", "DOCUMENT:READ", "DOCUMENT:WRITE", "ATTENDANCE:READ", "NOTIFICATION:READ"));
        mapping.put(RoleCode.MANAGER, Set.of("AUTH:READ", "EMPLOYEE:READ", "DEPARTMENT:READ", "POSITION:READ", "STAFFING:READ", "DOCUMENT:READ", "ATTENDANCE:READ", "NOTIFICATION:READ"));
        mapping.put(RoleCode.PAYROLL_SPECIALIST, Set.of("AUTH:READ", "EMPLOYEE:READ", "POSITION:READ", "STAFFING:READ", "PAYROLL:READ", "PAYROLL:WRITE", "NOTIFICATION:READ"));
        mapping.put(RoleCode.SECURITY_OPERATOR, Set.of("AUTH:READ", "EMPLOYEE:READ", "ATTENDANCE:READ", "ATTENDANCE:WRITE"));
        mapping.put(RoleCode.EMPLOYEE, Set.of("AUTH:READ", "EMPLOYEE:READ", "DOCUMENT:READ", "NOTIFICATION:READ"));
        mapping.put(RoleCode.AUDITOR, Set.of("AUTH:READ", "AUDIT:READ", "DOCUMENT:READ", "EMPLOYEE:READ", "POSITION:READ", "STAFFING:READ", "DEPARTMENT:READ"));
        mapping.put(RoleCode.TOP_MANAGEMENT, Set.of("AUTH:READ", "EMPLOYEE:READ", "DEPARTMENT:READ", "POSITION:READ", "STAFFING:READ", "DOCUMENT:READ", "AUDIT:READ", "PAYROLL:READ", "ATTENDANCE:READ"));
        return mapping;
    }
}

@Service
@Transactional(readOnly = true)
class SecurityUserService {

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

@Service
@Transactional
class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityUserService securityUserService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    AuthService(UserAccountRepository userAccountRepository,
                RefreshTokenRepository refreshTokenRepository,
                SecurityUserService securityUserService,
                JwtService jwtService,
                PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityUserService = securityUserService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    TokenResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        UserAccount user = userAccountRepository.findByUsernameIgnoreCaseAndDeletedFalse(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.isActive() == false) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is inactive");
        }
        if (passwordEncoder.matches(request.password(), user.getPasswordHash()) == false) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        user.setLastLoginAt(Instant.now());
        CurrentUser currentUser = securityUserService.toCurrentUser(user);
        return issueTokens(user, currentUser, httpServletRequest);
    }

    TokenResponse refresh(RefreshRequest request, HttpServletRequest httpServletRequest) {
        String tokenHash = hash(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid"));
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshToken.setRevokedAt(Instant.now());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        refreshToken.setRevokedAt(Instant.now());
        CurrentUser currentUser = securityUserService.toCurrentUser(refreshToken.getUser());
        return issueTokens(refreshToken.getUser(), currentUser, httpServletRequest);
    }

    void logout(LogoutRequest request) {
        String tokenHash = hash(request.refreshToken());
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash).ifPresent(token -> token.setRevokedAt(Instant.now()));
    }

    CurrentUserResponse me(CurrentUser currentUser) {
        UserAccount user = userAccountRepository.findByIdAndDeletedFalse(currentUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        String fullName = String.join(" ", user.getLastName(), user.getFirstName(), user.getMiddleName() == null ? "" : user.getMiddleName()).trim();
        return new CurrentUserResponse(currentUser.userId(), currentUser.employeeId(), currentUser.username(), fullName, currentUser.roles(), currentUser.permissions());
    }

    private TokenResponse issueTokens(UserAccount user, CurrentUser currentUser, HttpServletRequest httpServletRequest) {
        String rawRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(30L * 24 * 60 * 60));
        refreshToken.setIpAddress(httpServletRequest.getRemoteAddr());
        refreshToken.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        refreshTokenRepository.save(refreshToken);
        return new TokenResponse(jwtService.generateAccessToken(currentUser), rawRefreshToken, jwtService.expiresInSeconds(), me(currentUser));
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}

@Service
@Transactional(readOnly = true)
class AuthorizationQueryService {

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

@Component
class SeedService implements ApplicationRunner {

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

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.login(request, httpServletRequest));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.refresh(request, httpServletRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token")
    ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user context")
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<CurrentUserResponse> me(@AuthenticationPrincipal CurrentUser currentUser) {
        return ResponseEntity.ok(authService.me(currentUser));
    }
}

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Security Administration")
@SecurityRequirement(name = "bearerAuth")
class AdminSecurityController {

    private final AuthorizationQueryService authorizationQueryService;

    AdminSecurityController(AuthorizationQueryService authorizationQueryService) {
        this.authorizationQueryService = authorizationQueryService;
    }

    @GetMapping("/roles")
    @Operation(summary = "List roles with permissions")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.hasPermission(authentication, 'ROLE', 'READ')")
    ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(authorizationQueryService.getRoles());
    }

    @GetMapping("/permissions")
    @Operation(summary = "List permissions")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.hasPermission(authentication, 'ROLE', 'READ')")
    ResponseEntity<List<PermissionResponse>> getPermissions() {
        return ResponseEntity.ok(authorizationQueryService.getPermissions());
    }
}
