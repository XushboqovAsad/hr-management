package uz.hrms.other.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "hr", name = "disciplinary_actions")
public class DisciplinaryAction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_incident_id")
    private ExplanationIncident explanationIncident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_id")
    private Explanation explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private DisciplinaryActionType actionType;

    @Column(name = "action_date", nullable = false)
    private LocalDate actionDate;

    @Column(name = "reason_text", nullable = false, length = 2000)
    private String reasonText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DisciplinaryActionStatus status = DisciplinaryActionStatus.ACTIVE;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "source_order_id")
    private UUID sourceOrderId;

    @Column(name = "source_order_number", length = 100)
    private String sourceOrderNumber;

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

    public ExplanationIncident getExplanationIncident() {
        return explanationIncident;
    }

    public void setExplanationIncident(ExplanationIncident explanationIncident) {
        this.explanationIncident = explanationIncident;
    }

    public Explanation getExplanation() {
        return explanation;
    }

    public void setExplanation(Explanation explanation) {
        this.explanation = explanation;
    }

    public DisciplinaryActionType getActionType() {
        return actionType;
    }

    public void setActionType(DisciplinaryActionType actionType) {
        this.actionType = actionType;
    }

    public LocalDate getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDate actionDate) {
        this.actionDate = actionDate;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public DisciplinaryActionStatus getStatus() {
        return status;
    }

    public void setStatus(DisciplinaryActionStatus status) {
        this.status = status;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public UUID getSourceOrderId() {
        return sourceOrderId;
    }

    public void setSourceOrderId(UUID sourceOrderId) {
        this.sourceOrderId = sourceOrderId;
    }

    public String getSourceOrderNumber() {
        return sourceOrderNumber;
    }

    public void setSourceOrderNumber(String sourceOrderNumber) {
        this.sourceOrderNumber = sourceOrderNumber;
    }
}

