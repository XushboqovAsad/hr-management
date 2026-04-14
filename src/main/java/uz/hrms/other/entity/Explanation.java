package uz.hrms.other.entity;

import jakarta.persistence.*;
import uz.hrms.other.enums.ExplanationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public
@Entity
@Table(schema = "hr", name = "explanations")
class Explanation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_incident_id", nullable = false)
    private ExplanationIncident explanationIncident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "explanation_text", length = 4000)
    private String explanationText;

    @Column(name = "employee_submitted_at")
    private OffsetDateTime employeeSubmittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_reviewer_employee_id")
    private Employee managerReviewerEmployee;

    @Column(name = "manager_review_comment", length = 2000)
    private String managerReviewComment;

    @Column(name = "manager_reviewed_at")
    private OffsetDateTime managerReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_reviewer_employee_id")
    private Employee hrReviewerEmployee;

    @Column(name = "hr_decision_comment", length = 2000)
    private String hrDecisionComment;

    @Column(name = "hr_decided_at")
    private OffsetDateTime hrDecidedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ExplanationStatus status = ExplanationStatus.DRAFT;

    @Column(name = "source_order_id")
    private UUID sourceOrderId;

    @Column(name = "source_order_number", length = 100)
    private String sourceOrderNumber;

    public ExplanationIncident getExplanationIncident() {
        return explanationIncident;
    }

    public void setExplanationIncident(ExplanationIncident explanationIncident) {
        this.explanationIncident = explanationIncident;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getExplanationText() {
        return explanationText;
    }

    public void setExplanationText(String explanationText) {
        this.explanationText = explanationText;
    }

    public OffsetDateTime getEmployeeSubmittedAt() {
        return employeeSubmittedAt;
    }

    public void setEmployeeSubmittedAt(OffsetDateTime employeeSubmittedAt) {
        this.employeeSubmittedAt = employeeSubmittedAt;
    }

    public Employee getManagerReviewerEmployee() {
        return managerReviewerEmployee;
    }

    public void setManagerReviewerEmployee(Employee managerReviewerEmployee) {
        this.managerReviewerEmployee = managerReviewerEmployee;
    }

    public String getManagerReviewComment() {
        return managerReviewComment;
    }

    public void setManagerReviewComment(String managerReviewComment) {
        this.managerReviewComment = managerReviewComment;
    }

    public OffsetDateTime getManagerReviewedAt() {
        return managerReviewedAt;
    }

    public void setManagerReviewedAt(OffsetDateTime managerReviewedAt) {
        this.managerReviewedAt = managerReviewedAt;
    }

    public Employee getHrReviewerEmployee() {
        return hrReviewerEmployee;
    }

    public void setHrReviewerEmployee(Employee hrReviewerEmployee) {
        this.hrReviewerEmployee = hrReviewerEmployee;
    }

    public String getHrDecisionComment() {
        return hrDecisionComment;
    }

    public void setHrDecisionComment(String hrDecisionComment) {
        this.hrDecisionComment = hrDecisionComment;
    }

    public OffsetDateTime getHrDecidedAt() {
        return hrDecidedAt;
    }

    public void setHrDecidedAt(OffsetDateTime hrDecidedAt) {
        this.hrDecidedAt = hrDecidedAt;
    }

    public ExplanationStatus getStatus() {
        return status;
    }

    public void setStatus(ExplanationStatus status) {
        this.status = status;
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
