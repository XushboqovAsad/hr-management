package uz.hrms.other.entity;

import jakarta.persistence.*;
import uz.hrms.other.AbsenceStatus;
import uz.hrms.other.AbsenceType;
import uz.hrms.other.PayrollSyncStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(schema = "hr", name = "absence_records")
class AbsenceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_employee_id")
    private Employee requesterEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "absence_type", nullable = false, length = 40)
    private AbsenceType absenceType;

    @Column(name = "reason_text", length = 2000)
    private String reasonText;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "document_required", nullable = false)
    private boolean documentRequired;

    @Column(name = "hr_comment", length = 2000)
    private String hrComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AbsenceStatus status = AbsenceStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_sync_status", nullable = false, length = 30)
    private PayrollSyncStatus payrollSyncStatus = PayrollSyncStatus.PENDING;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getRequesterEmployee() {
        return requesterEmployee;
    }

    public void setRequesterEmployee(Employee requesterEmployee) {
        this.requesterEmployee = requesterEmployee;
    }

    public AbsenceType getAbsenceType() {
        return absenceType;
    }

    public void setAbsenceType(AbsenceType absenceType) {
        this.absenceType = absenceType;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isDocumentRequired() {
        return documentRequired;
    }

    public void setDocumentRequired(boolean documentRequired) {
        this.documentRequired = documentRequired;
    }

    public String getHrComment() {
        return hrComment;
    }

    public void setHrComment(String hrComment) {
        this.hrComment = hrComment;
    }

    public AbsenceStatus getStatus() {
        return status;
    }

    public void setStatus(AbsenceStatus status) {
        this.status = status;
    }

    public PayrollSyncStatus getPayrollSyncStatus() {
        return payrollSyncStatus;
    }

    public void setPayrollSyncStatus(PayrollSyncStatus payrollSyncStatus) {
        this.payrollSyncStatus = payrollSyncStatus;
    }

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
