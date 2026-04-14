package uz.hrms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.repository.JpaRepository;

public final class BusinessTripDomain {
    private BusinessTripDomain() {
    }
}

enum BusinessTripStatus {
    DRAFT,
    ON_APPROVAL,
    APPROVED,
    REJECTED,
    ORDER_CREATED,
    IN_PROGRESS,
    REPORT_PENDING,
    REPORT_SUBMITTED,
    CLOSED,
    CANCELLED,
    OVERDUE
}

enum BusinessTripDocumentKind {
    REQUEST_ATTACHMENT,
    ORDER_ATTACHMENT,
    REPORT_ATTACHMENT,
    CONFIRMING_DOCUMENT
}

enum BusinessTripApprovalRole {
    MANAGER,
    HR_ADMIN,
    HR_INSPECTOR
}

enum BusinessTripApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SKIPPED
}

enum PayrollSyncStatus {
    PENDING,
    SENT,
    ACKNOWLEDGED,
    FAILED
}

@Entity
@Table(schema = "hr", name = "business_trips")
class BusinessTrip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_employee_id")
    private Employee requesterEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_department_id")
    private Department approverDepartment;

    @Column(name = "destination_country", length = 100)
    private String destinationCountry;

    @Column(name = "destination_city", nullable = false, length = 150)
    private String destinationCity;

    @Column(name = "destination_address", length = 255)
    private String destinationAddress;

    @Column(name = "purpose", nullable = false, length = 1000)
    private String purpose;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "transport_type", length = 100)
    private String transportType;

    @Column(name = "accommodation_details", length = 1000)
    private String accommodationDetails;

    @Column(name = "daily_allowance", nullable = false, precision = 18, scale = 2)
    private BigDecimal dailyAllowance = BigDecimal.ZERO;

    @Column(name = "funding_source", length = 255)
    private String fundingSource;

    @Column(name = "comment_text", length = 2000)
    private String commentText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BusinessTripStatus status = BusinessTripStatus.DRAFT;

    @Column(name = "order_number", length = 100)
    private String orderNumber;

    @Column(name = "order_template_code", length = 100)
    private String orderTemplateCode;

    @Column(name = "order_generated_at")
    private OffsetDateTime orderGeneratedAt;

    @Column(name = "order_print_form_html")
    private String orderPrintFormHtml;

    @Column(name = "order_pdf_document_id")
    private UUID orderPdfDocumentId;

    @Column(name = "report_text")
    private String reportText;

    @Column(name = "report_submitted_at")
    private OffsetDateTime reportSubmittedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payroll_sync_status", nullable = false, length = 30)
    private PayrollSyncStatus payrollSyncStatus = PayrollSyncStatus.PENDING;

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

    public Department getApproverDepartment() {
        return approverDepartment;
    }

    public void setApproverDepartment(Department approverDepartment) {
        this.approverDepartment = approverDepartment;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
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

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getAccommodationDetails() {
        return accommodationDetails;
    }

    public void setAccommodationDetails(String accommodationDetails) {
        this.accommodationDetails = accommodationDetails;
    }

    public BigDecimal getDailyAllowance() {
        return dailyAllowance;
    }

    public void setDailyAllowance(BigDecimal dailyAllowance) {
        this.dailyAllowance = dailyAllowance;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public BusinessTripStatus getStatus() {
        return status;
    }

    public void setStatus(BusinessTripStatus status) {
        this.status = status;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderTemplateCode() {
        return orderTemplateCode;
    }

    public void setOrderTemplateCode(String orderTemplateCode) {
        this.orderTemplateCode = orderTemplateCode;
    }

    public OffsetDateTime getOrderGeneratedAt() {
        return orderGeneratedAt;
    }

    public void setOrderGeneratedAt(OffsetDateTime orderGeneratedAt) {
        this.orderGeneratedAt = orderGeneratedAt;
    }

    public String getOrderPrintFormHtml() {
        return orderPrintFormHtml;
    }

    public void setOrderPrintFormHtml(String orderPrintFormHtml) {
        this.orderPrintFormHtml = orderPrintFormHtml;
    }

    public UUID getOrderPdfDocumentId() {
        return orderPdfDocumentId;
    }

    public void setOrderPdfDocumentId(UUID orderPdfDocumentId) {
        this.orderPdfDocumentId = orderPdfDocumentId;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public OffsetDateTime getReportSubmittedAt() {
        return reportSubmittedAt;
    }

    public void setReportSubmittedAt(OffsetDateTime reportSubmittedAt) {
        this.reportSubmittedAt = reportSubmittedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public PayrollSyncStatus getPayrollSyncStatus() {
        return payrollSyncStatus;
    }

    public void setPayrollSyncStatus(PayrollSyncStatus payrollSyncStatus) {
        this.payrollSyncStatus = payrollSyncStatus;
    }
}

@Entity
@Table(schema = "hr", name = "business_trip_documents")
class BusinessTripDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_trip_id", nullable = false)
    private BusinessTrip businessTrip;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_kind", nullable = false, length = 40)
    private BusinessTripDocumentKind documentKind;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "content_type", nullable = false, length = 150)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @Column(name = "is_current", nullable = false)
    private boolean current = true;

    @Column(name = "description", length = 1000)
    private String description;

    public BusinessTrip getBusinessTrip() {
        return businessTrip;
    }

    public void setBusinessTrip(BusinessTrip businessTrip) {
        this.businessTrip = businessTrip;
    }

    public BusinessTripDocumentKind getDocumentKind() {
        return documentKind;
    }

    public void setDocumentKind(BusinessTripDocumentKind documentKind) {
        this.documentKind = documentKind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

@Entity
@Table(schema = "hr", name = "business_trip_approvals")
class BusinessTripApproval extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_trip_id", nullable = false)
    private BusinessTrip businessTrip;

    @Column(name = "step_no", nullable = false)
    private Integer stepNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_role", nullable = false, length = 50)
    private BusinessTripApprovalRole approvalRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id")
    private UserAccount approverUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BusinessTripApprovalStatus status = BusinessTripApprovalStatus.PENDING;

    @Column(name = "decision_comment", length = 1000)
    private String decisionComment;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    public BusinessTrip getBusinessTrip() {
        return businessTrip;
    }

    public void setBusinessTrip(BusinessTrip businessTrip) {
        this.businessTrip = businessTrip;
    }

    public Integer getStepNo() {
        return stepNo;
    }

    public void setStepNo(Integer stepNo) {
        this.stepNo = stepNo;
    }

    public BusinessTripApprovalRole getApprovalRole() {
        return approvalRole;
    }

    public void setApprovalRole(BusinessTripApprovalRole approvalRole) {
        this.approvalRole = approvalRole;
    }

    public UserAccount getApproverUser() {
        return approverUser;
    }

    public void setApproverUser(UserAccount approverUser) {
        this.approverUser = approverUser;
    }

    public BusinessTripApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(BusinessTripApprovalStatus status) {
        this.status = status;
    }

    public String getDecisionComment() {
        return decisionComment;
    }

    public void setDecisionComment(String decisionComment) {
        this.decisionComment = decisionComment;
    }

    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(OffsetDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}

@Entity
@Table(schema = "hr", name = "business_trip_history")
class BusinessTripHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_trip_id", nullable = false)
    private BusinessTrip businessTrip;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "status_from", length = 30)
    private String statusFrom;

    @Column(name = "status_to", length = 30)
    private String statusTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserAccount actorUser;

    @Column(name = "comment_text", length = 1000)
    private String commentText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json")
    private String payloadJson;

    public BusinessTrip getBusinessTrip() {
        return businessTrip;
    }

    public void setBusinessTrip(BusinessTrip businessTrip) {
        this.businessTrip = businessTrip;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getStatusFrom() {
        return statusFrom;
    }

    public void setStatusFrom(String statusFrom) {
        this.statusFrom = statusFrom;
    }

    public String getStatusTo() {
        return statusTo;
    }

    public void setStatusTo(String statusTo) {
        this.statusTo = statusTo;
    }

    public UserAccount getActorUser() {
        return actorUser;
    }

    public void setActorUser(UserAccount actorUser) {
        this.actorUser = actorUser;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}

interface BusinessTripRepository extends JpaRepository<BusinessTrip, UUID> {
    Optional<BusinessTrip> findByIdAndDeletedFalse(UUID id);

    List<BusinessTrip> findAllByDeletedFalseOrderByCreatedAtDesc();

    List<BusinessTrip> findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);

    List<BusinessTrip> findAllByStatusInAndEndDateBeforeAndDeletedFalse(List<BusinessTripStatus> statuses, LocalDate date);
}

interface BusinessTripDocumentRepository extends JpaRepository<BusinessTripDocument, UUID> {
    Optional<BusinessTripDocument> findByIdAndDeletedFalse(UUID id);

    List<BusinessTripDocument> findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(UUID businessTripId);

    List<BusinessTripDocument> findAllByBusinessTripIdAndDocumentKindAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(UUID businessTripId, BusinessTripDocumentKind documentKind, String title);
}

interface BusinessTripApprovalRepository extends JpaRepository<BusinessTripApproval, UUID> {
    Optional<BusinessTripApproval> findByIdAndDeletedFalse(UUID id);

    List<BusinessTripApproval> findAllByBusinessTripIdAndDeletedFalseOrderByStepNoAsc(UUID businessTripId);
}

interface BusinessTripHistoryRepository extends JpaRepository<BusinessTripHistory, UUID> {
    List<BusinessTripHistory> findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(UUID businessTripId);
}
