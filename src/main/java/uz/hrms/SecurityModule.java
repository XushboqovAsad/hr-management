package uz.hrms;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

record CurrentUser(
        UUID userId,
        UUID employeeId,
        String username,
        String passwordHash,
        boolean active,
        Set<String> roles,
        Set<String> permissions
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> values = new LinkedHashSet<>();
        roles.forEach(role -> values.add("ROLE_" + role));
        values.addAll(permissions);
        return values.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public String getPassword() { return passwordHash; }
    @Override
    public String getUsername() { return username; }
    @Override
    public boolean isAccountNonExpired() { return active; }
    @Override
    public boolean isAccountNonLocked() { return active; }
    @Override
    public boolean isCredentialsNonExpired() { return active; }
    @Override
    public boolean isEnabled() { return active; }
}

@Component
class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    String generateAccessToken(CurrentUser currentUser) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(currentUser.username())
                .claim("uid", currentUser.userId().toString())
                .claim("roles", currentUser.roles())
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(now.plus(jwtProperties.accessTokenTtl())))
                .signWith(secretKey);
        if (currentUser.employeeId() == null) {
            return builder.compact();
        }
        builder.claim("eid", currentUser.employeeId().toString());
        return builder.compact();
    }

    UUID extractUserId(String token) {
        return UUID.fromString(parse(token).getPayload().get("uid", String.class));
    }

    long expiresInSeconds() {
        return jwtProperties.accessTokenTtl().toSeconds();
    }

    Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parser().verifyWith(secretKey).requireIssuer(jwtProperties.issuer()).build().parseSignedClaims(token);
    }
}

@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SecurityUserService securityUserService;

    JwtAuthenticationFilter(JwtService jwtService, SecurityUserService securityUserService) {
        this.jwtService = jwtService;
        this.securityUserService = securityUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (header.startsWith("Bearer ")) {
            try {
                UUID userId = jwtService.extractUserId(header.substring(7));
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    CurrentUser currentUser = securityUserService.loadByUserId(userId);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}

@Component("accessPolicy")
class AccessPolicy {

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

    boolean hasPermission(Authentication authentication, String module, String action) {
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
        if (currentUser.permissions().contains(requiredAuthority) == false) {
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
            if (allowedRoles.contains(assignment.getRole().getId()) == false) {
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
        if (delegation.isActive() == false) {
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
        if ((principal instanceof CurrentUser) == false) {
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
