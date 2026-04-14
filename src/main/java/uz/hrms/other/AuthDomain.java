package uz.hrms.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.hrms.other.entity.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

enum RoleCode {
    SUPER_ADMIN,
    HR_ADMIN,
    HR_INSPECTOR,
    MANAGER,
    PAYROLL_SPECIALIST,
    SECURITY_OPERATOR,
    EMPLOYEE,
    AUDITOR,
    TOP_MANAGEMENT
}

enum AccessScopeType {
    GLOBAL,
    DEPARTMENT,
    SELF,
    SUBORDINATES,
    EMPLOYEE
}


@Entity
@Table(name = "roles", schema = "auth")
class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false)
    private RoleCode code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean systemRole = true;

    public RoleCode getCode() {
        return code;
    }

    public void setCode(RoleCode code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }
}

@Entity
@Table(name = "permissions", schema = "auth")
class Permission extends BaseEntity {

    @Column(name = "module_code", nullable = false)
    private String moduleCode;

    @Column(name = "action_code", nullable = false)
    private String actionCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String authority() {
        return moduleCode + ":" + actionCode;
    }
}

@Entity
@Table(name = "role_permissions", schema = "auth")
class RolePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}

@Entity
@Table(name = "refresh_tokens", schema = "auth")
class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}

@Entity
@Table(name = "user_roles", schema = "auth")
class UserRoleAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private AccessScopeType scopeType;

    @Column(name = "scope_department_id")
    private UUID scopeDepartmentId;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public AccessScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(AccessScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public UUID getScopeDepartmentId() {
        return scopeDepartmentId;
    }

    public void setScopeDepartmentId(UUID scopeDepartmentId) {
        this.scopeDepartmentId = scopeDepartmentId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }
}

@Entity
@Table(name = "access_delegations", schema = "auth")
class AccessDelegation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grantor_user_id", nullable = false)
    private UserAccount grantorUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grantee_user_id", nullable = false)
    private UserAccount granteeUser;

    @Column(name = "module_code", nullable = false)
    private String moduleCode;

    @Column(name = "action_code", nullable = false)
    private String actionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false)
    private AccessScopeType scopeType;

    @Column(name = "scope_department_id")
    private UUID scopeDepartmentId;

    @Column(name = "employee_id")
    private UUID employeeId;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public UserAccount getGrantorUser() {
        return grantorUser;
    }

    public void setGrantorUser(UserAccount grantorUser) {
        this.grantorUser = grantorUser;
    }

    public UserAccount getGranteeUser() {
        return granteeUser;
    }

    public void setGranteeUser(UserAccount granteeUser) {
        this.granteeUser = granteeUser;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public AccessScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(AccessScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public UUID getScopeDepartmentId() {
        return scopeDepartmentId;
    }

    public void setScopeDepartmentId(UUID scopeDepartmentId) {
        this.scopeDepartmentId = scopeDepartmentId;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByIdAndDeletedFalse(UUID id);
    Optional<UserAccount> findByUsernameIgnoreCaseAndDeletedFalse(String username);
}

interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);
}

interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCodeAndDeletedFalse(RoleCode code);
    List<Role> findAllByDeletedFalseOrderByNameAsc();
}

interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByModuleCodeAndActionCodeAndDeletedFalse(String moduleCode, String actionCode);
    List<Permission> findAllByDeletedFalseOrderByModuleCodeAscActionCodeAsc();
}

interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    @Query("""
            select rp
            from RolePermission rp
            join fetch rp.role r
            join fetch rp.permission p
            where rp.deleted = false
              and r.id in :roleIds
              and r.deleted = false
              and p.deleted = false
            """)
    List<RolePermission> findAllByRoleIds(@Param("roleIds") List<UUID> roleIds);
}

interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, UUID> {

    @Query("""
            select ura
            from UserRoleAssignment ura
            join fetch ura.role r
            where ura.deleted = false
              and ura.user.id = :userId
              and ura.active = true
              and r.deleted = false
            """)
    List<UserRoleAssignment> findActiveAssignments(@Param("userId") UUID userId);
}

interface AccessDelegationRepository extends JpaRepository<AccessDelegation, UUID> {

    @Query("""
            select ad
            from AccessDelegation ad
            where ad.deleted = false
              and ad.granteeUser.id = :granteeUserId
              and ad.moduleCode = :moduleCode
              and ad.actionCode = :actionCode
              and ad.active = true
            """)
    List<AccessDelegation> findActiveDelegations(@Param("granteeUserId") UUID granteeUserId,
                                                 @Param("moduleCode") String moduleCode,
                                                 @Param("actionCode") String actionCode);
}
