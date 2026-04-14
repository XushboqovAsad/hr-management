package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

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
