package uz.hrms.other.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import uz.hrms.other.enums.DismissalReasonType;
import uz.hrms.other.enums.DismissalStatus;

@Entity
@Table(schema = "hr", name = "dismissal_requests")
public class DismissalRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_employee_id")
    private Employee initiatorEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_user_id")
    private UserAccount initiatorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, length = 40)
    private DismissalReasonType reasonType;

    @Column(name = "reason_text", nullable = false, length = 2000)
    private String reasonText;

    @Column(name = "dismissal_date", nullable = false)
    private LocalDate dismissalDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private DismissalStatus status = DismissalStatus.DRAFT;

    @Column(name = "order_number", length = 100)
    private String orderNumber;

    @Column(name = "order_template_code", length = 100)
    private String orderTemplateCode;

    @Column(name = "order_generated_at")
    private OffsetDateTime orderGeneratedAt;

    @Column(name = "order_print_form_html")
    private String orderPrintFormHtml;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "finalized_at")
    private OffsetDateTime finalizedAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

    @Column(name = "account_blocked_at")
    private OffsetDateTime accountBlockedAt;

    @Column(name = "final_payroll_sync_status", nullable = false, length = 30)
    private String finalPayrollSyncStatus = "PENDING";

    @Column(name = "comment_text", length = 2000)
    private String commentText;

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Employee getInitiatorEmployee() { return initiatorEmployee; }
    public void setInitiatorEmployee(Employee initiatorEmployee) { this.initiatorEmployee = initiatorEmployee; }
    public UserAccount getInitiatorUser() { return initiatorUser; }
    public void setInitiatorUser(UserAccount initiatorUser) { this.initiatorUser = initiatorUser; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public DismissalReasonType getReasonType() { return reasonType; }
    public void setReasonType(DismissalReasonType reasonType) { this.reasonType = reasonType; }
    public String getReasonText() { return reasonText; }
    public void setReasonText(String reasonText) { this.reasonText = reasonText; }
    public LocalDate getDismissalDate() { return dismissalDate; }
    public void setDismissalDate(LocalDate dismissalDate) { this.dismissalDate = dismissalDate; }
    public DismissalStatus getStatus() { return status; }
    public void setStatus(DismissalStatus status) { this.status = status; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getOrderTemplateCode() { return orderTemplateCode; }
    public void setOrderTemplateCode(String orderTemplateCode) { this.orderTemplateCode = orderTemplateCode; }
    public OffsetDateTime getOrderGeneratedAt() { return orderGeneratedAt; }
    public void setOrderGeneratedAt(OffsetDateTime orderGeneratedAt) { this.orderGeneratedAt = orderGeneratedAt; }
    public String getOrderPrintFormHtml() { return orderPrintFormHtml; }
    public void setOrderPrintFormHtml(String orderPrintFormHtml) { this.orderPrintFormHtml = orderPrintFormHtml; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
    public OffsetDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(OffsetDateTime finalizedAt) { this.finalizedAt = finalizedAt; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(OffsetDateTime archivedAt) { this.archivedAt = archivedAt; }
    public OffsetDateTime getAccountBlockedAt() { return accountBlockedAt; }
    public void setAccountBlockedAt(OffsetDateTime accountBlockedAt) { this.accountBlockedAt = accountBlockedAt; }
    public String getFinalPayrollSyncStatus() { return finalPayrollSyncStatus; }
    public void setFinalPayrollSyncStatus(String finalPayrollSyncStatus) { this.finalPayrollSyncStatus = finalPayrollSyncStatus; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
}

