package uz.hrms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public final class HrDomain {
    private HrDomain() {
    }
}

enum DepartmentUnitType {
    BRANCH,
    DEPARTMENT,
    MANAGEMENT,
    DIVISION,
    SECTOR
}

enum StaffingUnitStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}

@Entity
@Table(schema = "hr", name = "departments")
class Department extends BaseEntity {

    @NotBlank
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @NotBlank
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_type", nullable = false, length = 30)
    private DepartmentUnitType unitType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_employee_id")
    private Employee managerEmployee;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

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

    public DepartmentUnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(DepartmentUnitType unitType) {
        this.unitType = unitType;
    }

    public Department getParentDepartment() {
        return parentDepartment;
    }

    public void setParentDepartment(Department parentDepartment) {
        this.parentDepartment = parentDepartment;
    }

    public Employee getManagerEmployee() {
        return managerEmployee;
    }

    public void setManagerEmployee(Employee managerEmployee) {
        this.managerEmployee = managerEmployee;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }
}

@Entity
@Table(schema = "hr", name = "positions")
class Position extends BaseEntity {

    @NotBlank
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @NotBlank
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "active", nullable = false)
    private boolean active = true;

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
}

@Entity
@Table(schema = "hr", name = "staffing_units")
class StaffingUnit extends BaseEntity {

    @NotBlank
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "planned_fte", nullable = false, precision = 10, scale = 2)
    private BigDecimal plannedFte;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "occupied_fte", nullable = false, precision = 10, scale = 2)
    private BigDecimal occupiedFte = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StaffingUnitStatus status = StaffingUnitStatus.ACTIVE;

    @Column(name = "opened_at")
    private LocalDate openedAt;

    @Column(name = "closed_at")
    private LocalDate closedAt;

    @Column(name = "notes", length = 1000)
    private String notes;

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

    public StaffingUnitStatus getStatus() {
        return status;
    }

    public void setStatus(StaffingUnitStatus status) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

@Entity
@Table(schema = "hr", name = "employees")
class Employee extends BaseEntity {

    @Column(name = "personnel_number", nullable = false, length = 50)
    private String personnelNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "dismissal_date")
    private LocalDate dismissalDate;

    @Column(name = "status", nullable = false, length = 30)
    private String employmentStatus;

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public void setPersonnelNumber(String personnelNumber) {
        this.personnelNumber = personnelNumber;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getDismissalDate() {
        return dismissalDate;
    }

    public void setDismissalDate(LocalDate dismissalDate) {
        this.dismissalDate = dismissalDate;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getStatus() {
        return employmentStatus;
    }

    public void setStatus(String status) {
        this.employmentStatus = status;
    }
}

@Entity
@Table(schema = "hr", name = "employee_assignments")
class EmployeeAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffing_unit_id")
    private StaffingUnit staffingUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_employee_id")
    private Employee managerEmployee;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryAssignment = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate startedAt;

    @Column(name = "effective_to")
    private LocalDate endedAt;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    public StaffingUnit getStaffingUnit() {
        return staffingUnit;
    }

    public void setStaffingUnit(StaffingUnit staffingUnit) {
        this.staffingUnit = staffingUnit;
    }

    public Employee getManagerEmployee() {
        return managerEmployee;
    }

    public void setManagerEmployee(Employee managerEmployee) {
        this.managerEmployee = managerEmployee;
    }

    public boolean isPrimaryAssignment() {
        return primaryAssignment;
    }

    public void setPrimaryAssignment(boolean primaryAssignment) {
        this.primaryAssignment = primaryAssignment;
    }

    public LocalDate getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDate startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDate getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDate endedAt) {
        this.endedAt = endedAt;
    }
}

interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Optional<Department> findByIdAndDeletedFalse(UUID id);

    List<Department> findAllByDeletedFalseOrderByNameAsc();

    List<Department> findAllByParentDepartmentIdAndDeletedFalseOrderByNameAsc(UUID parentDepartmentId);

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdAndDeletedFalse(String code, UUID id);

    boolean existsByNameIgnoreCaseAndParentDepartmentIdAndDeletedFalse(String name, UUID parentDepartmentId);

    @Query("select d from Department d where d.deleted = false and d.parentDepartment is null order by d.name asc")
    List<Department> findRootDepartments();
}

interface PositionRepository extends JpaRepository<Position, UUID> {
    Optional<Position> findByIdAndDeletedFalse(UUID id);

    List<Position> findAllByDeletedFalseOrderByTitleAsc();

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdAndDeletedFalse(String code, UUID id);
}

interface StaffingUnitRepository extends JpaRepository<StaffingUnit, UUID> {
    Optional<StaffingUnit> findByIdAndDeletedFalse(UUID id);

    List<StaffingUnit> findAllByDeletedFalseOrderByCodeAsc();

    boolean existsByCodeIgnoreCaseAndDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdAndDeletedFalse(String code, UUID id);

    @Query(
        "select s from StaffingUnit s " +
            "join fetch s.department d " +
            "join fetch s.position p " +
            "where s.deleted = false " +
            "and (:departmentIds is null or d.id in :departmentIds) " +
            "and (:positionId is null or p.id = :positionId) " +
            "and (:status is null or s.status = :status) " +
            "order by d.name asc, p.title asc, s.code asc"
    )
    List<StaffingUnit> search(
        @Param("departmentIds") List<UUID> departmentIds,
        @Param("positionId") UUID positionId,
        @Param("status") StaffingUnitStatus status
    );

    @Query(
        "select s from StaffingUnit s " +
            "join fetch s.department d " +
            "join fetch s.position p " +
            "where s.deleted = false " +
            "and s.status = uz.hrms.StaffingUnitStatus.ACTIVE " +
            "and s.occupiedFte < s.plannedFte " +
            "and (:departmentIds is null or d.id in :departmentIds) " +
            "and (:positionId is null or p.id = :positionId) " +
            "order by d.name asc, p.title asc, s.code asc"
    )
    List<StaffingUnit> findVacancies(
        @Param("departmentIds") List<UUID> departmentIds,
        @Param("positionId") UUID positionId
    );
}

interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByIdAndDeletedFalse(UUID id);
    Optional<Employee> findByUserIdAndDeletedFalse(UUID userId);
    List<Employee> findAllByDeletedFalseOrderByCreatedAtDesc();
    boolean existsByPersonnelNumberIgnoreCaseAndDeletedFalse(String personnelNumber);
    boolean existsByPersonnelNumberIgnoreCaseAndIdNotAndDeletedFalse(String personnelNumber, UUID id);
}

interface EmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    @Query(
        "select case when count(a) > 0 then true else false end from EmployeeAssignment a " +
            "where a.deleted = false and a.endedAt is null and a.managerEmployee.id = :managerEmployeeId and a.employee.id = :employeeId"
    )
    boolean existsCurrentSubordinate(@Param("managerEmployeeId") UUID managerEmployeeId, @Param("employeeId") UUID employeeId);

    default boolean existsCurrentSubordinate(UUID managerEmployeeId, UUID employeeId, LocalDate ignoredDate) {
        return existsCurrentSubordinate(managerEmployeeId, employeeId);
    }

    @Query(
        "select a from EmployeeAssignment a " +
            "where a.deleted = false and a.employee.id = :employeeId and a.primaryAssignment = true " +
            "and a.startedAt <= :targetDate and (a.endedAt is null or a.endedAt >= :targetDate) " +
            "order by a.startedAt desc"
    )
    Optional<EmployeeAssignment> findCurrentPrimaryAssignment(@Param("employeeId") UUID employeeId, @Param("targetDate") LocalDate targetDate);

    long countByStaffingUnitIdAndDeletedFalseAndEndedAtIsNull(UUID staffingUnitId);

    List<EmployeeAssignment> findAllByDepartmentIdAndDeletedFalseAndEndedAtIsNullOrderByStartedAtDesc(UUID departmentId);

    List<EmployeeAssignment> findAllByEmployeeIdAndDeletedFalseOrderByStartedAtDesc(UUID employeeId);
}
