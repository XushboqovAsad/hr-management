package uz.hrms.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.repository.JpaRepository;

@Entity
@Table(schema = "hr", name = "department_history")
class DepartmentHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "unit_type", nullable = false, length = 30)
    private String unitType;

    @Column(name = "parent_department_id")
    private UUID parentDepartmentId;

    @Column(name = "manager_employee_id")
    private UUID managerEmployeeId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json")
    private String payloadJson;

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public UUID getParentDepartmentId() {
        return parentDepartmentId;
    }

    public void setParentDepartmentId(UUID parentDepartmentId) {
        this.parentDepartmentId = parentDepartmentId;
    }

    public UUID getManagerEmployeeId() {
        return managerEmployeeId;
    }

    public void setManagerEmployeeId(UUID managerEmployeeId) {
        this.managerEmployeeId = managerEmployeeId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}

@Entity
@Table(schema = "hr", name = "position_history")
class PositionHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json")
    private String payloadJson;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}

@Entity
@Table(schema = "hr", name = "staffing_unit_history")
class StaffingUnitHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffing_unit_id", nullable = false)
    private StaffingUnit staffingUnit;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "planned_fte", nullable = false, precision = 10, scale = 2)
    private BigDecimal plannedFte;

    @Column(name = "occupied_fte", nullable = false, precision = 10, scale = 2)
    private BigDecimal occupiedFte;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "opened_at")
    private LocalDate openedAt;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json")
    private String payloadJson;

    public StaffingUnit getStaffingUnit() {
        return staffingUnit;
    }

    public void setStaffingUnit(StaffingUnit staffingUnit) {
        this.staffingUnit = staffingUnit;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public BigDecimal getPlannedFte() {
        return plannedFte;
    }

    public void setPlannedFte(BigDecimal plannedFte) {
        this.plannedFte = plannedFte;
    }

    public BigDecimal getOccupiedFte() {
        return occupiedFte;
    }

    public void setOccupiedFte(BigDecimal occupiedFte) {
        this.occupiedFte = occupiedFte;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDate openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDate getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDate closedAt) {
        this.closedAt = closedAt;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}

interface DepartmentHistoryRepository extends JpaRepository<DepartmentHistory, UUID> {
    long countByDepartmentId(UUID departmentId);

    List<DepartmentHistory> findAllByDepartmentIdOrderByVersionNoDesc(UUID departmentId);
}

interface PositionHistoryRepository extends JpaRepository<PositionHistory, UUID> {
    long countByPositionId(UUID positionId);

    List<PositionHistory> findAllByPositionIdOrderByVersionNoDesc(UUID positionId);
}

interface StaffingUnitHistoryRepository extends JpaRepository<StaffingUnitHistory, UUID> {
    long countByStaffingUnitId(UUID staffingUnitId);

    List<StaffingUnitHistory> findAllByStaffingUnitIdOrderByVersionNoDesc(UUID staffingUnitId);
}
