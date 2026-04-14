package uz.hrms.other.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uz.hrms.other.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(schema = "hr", name = "employee_assignments")
public class EmployeeAssignment extends BaseEntity {

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
