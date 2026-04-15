package uz.hrms.other.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import uz.hrms.other.enums.BusinessTripApprovalRole;
import uz.hrms.other.enums.BusinessTripApprovalStatus;

import java.time.OffsetDateTime;

@Entity
@Table(schema = "hr", name = "business_trip_approvals")
public class BusinessTripApproval extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_trip_id", nullable = false)
    private BusinessTrip businessTrip;

    @Column(name = "step_no", nullable = false)
    private Integer stepNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_role", nullable = false, length = 50)
    private uz.hrms.other.enums.BusinessTripApprovalRole approvalRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id")
    private UserAccount approverUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private uz.hrms.other.enums.BusinessTripApprovalStatus status = uz.hrms.other.enums.BusinessTripApprovalStatus.PENDING;

    @Column(name = "decision_comment", length = 1000)
    private String decisionComment;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    public BusinessTrip getBusinessTrip() { return businessTrip; }
    public void setBusinessTrip(BusinessTrip businessTrip) { this.businessTrip = businessTrip; }
    public Integer getStepNo() { return stepNo; }
    public void setStepNo(Integer stepNo) { this.stepNo = stepNo; }
    public uz.hrms.other.enums.BusinessTripApprovalRole getApprovalRole() { return approvalRole; }
    public void setApprovalRole(BusinessTripApprovalRole approvalRole) { this.approvalRole = approvalRole; }
    public UserAccount getApproverUser() { return approverUser; }
    public void setApproverUser(UserAccount approverUser) { this.approverUser = approverUser; }
    public uz.hrms.other.enums.BusinessTripApprovalStatus getStatus() { return status; }
    public void setStatus(BusinessTripApprovalStatus status) { this.status = status; }
    public String getDecisionComment() { return decisionComment; }
    public void setDecisionComment(String decisionComment) { this.decisionComment = decisionComment; }
    public OffsetDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(OffsetDateTime decidedAt) { this.decidedAt = decidedAt; }
}

