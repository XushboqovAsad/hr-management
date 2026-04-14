package uz.hrms.other.entity;

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
import uz.hrms.other.enums.StaffingUnitStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(schema = "hr", name = "staffing_units")
public class StaffingUnit extends BaseEntity {

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
