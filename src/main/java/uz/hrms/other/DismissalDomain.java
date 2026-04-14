package uz.hrms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public final class DismissalDomain {
    private DismissalDomain() {
    }
}

enum DismissalReasonType {
    RESIGNATION,
    MUTUAL_AGREEMENT,
    CONTRACT_EXPIRY,
    REDUCTION,
    DISCIPLINARY,
    OTHER
}

enum DismissalStatus {
    DRAFT,
    ON_APPROVAL,
    APPROVED,
    REJECTED,
    ORDER_CREATED,
    CLEARANCE_IN_PROGRESS,
    READY_FOR_FINALIZATION,
    FINALIZED,
    ARCHIVED,
    CANCELLED
}

enum ClearanceChecklistStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    BLOCKED
}

enum ClearanceItemType {
    PASS,
    LAPTOP,
    PHONE,
    SIM_CARD,
    DOCUMENTS,
    BOOKS,
    OTHER_ASSET,
    ACTIVE_TASKS,
    FINAL_PAYROLL,
    LMS_ACCESS,
    ACCOUNT_BLOCK
}

enum ClearanceItemStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    BLOCKED,
    WAIVED
}

enum ClearanceReturnStatus {
    PENDING,
    RETURNED,
    WAIVED,
    NOT_REQUIRED
}

@Entity
@Table(schema = "hr", name = "dismissal_requests")
class DismissalRequest extends BaseEntity {

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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getInitiatorEmployee() {
        return initiatorEmployee;
    }

    public void setInitiatorEmployee(Employee initiatorEmployee) {
        this.initiatorEmployee = initiatorEmployee;
    }

    public UserAccount getInitiatorUser() {
        return initiatorUser;
    }

    public void setInitiatorUser(UserAccount initiatorUser) {
        this.initiatorUser = initiatorUser;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public DismissalReasonType getReasonType() {
        return reasonType;
    }

    public void setReasonType(DismissalReasonType reasonType) {
        this.reasonType = reasonType;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public LocalDate getDismissalDate() {
        return dismissalDate;
    }

    public void setDismissalDate(LocalDate dismissalDate) {
        this.dismissalDate = dismissalDate;
    }

    public DismissalStatus getStatus() {
        return status;
    }

    public void setStatus(DismissalStatus status) {
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

    public OffsetDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(OffsetDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public OffsetDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(OffsetDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(OffsetDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public OffsetDateTime getAccountBlockedAt() {
        return accountBlockedAt;
    }

    public void setAccountBlockedAt(OffsetDateTime accountBlockedAt) {
        this.accountBlockedAt = accountBlockedAt;
    }

    public String getFinalPayrollSyncStatus() {
        return finalPayrollSyncStatus;
    }

    public void setFinalPayrollSyncStatus(String finalPayrollSyncStatus) {
        this.finalPayrollSyncStatus = finalPayrollSyncStatus;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}

@Entity
@Table(schema = "hr", name = "clearance_checklists")
class ClearanceChecklist extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissal_request_id", nullable = false)
    private DismissalRequest dismissalRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "checklist_status", nullable = false, length = 30)
    private ClearanceChecklistStatus checklistStatus = ClearanceChecklistStatus.OPEN;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public DismissalRequest getDismissalRequest() {
        return dismissalRequest;
    }

    public void setDismissalRequest(DismissalRequest dismissalRequest) {
        this.dismissalRequest = dismissalRequest;
    }

    public ClearanceChecklistStatus getChecklistStatus() {
        return checklistStatus;
    }

    public void setChecklistStatus(ClearanceChecklistStatus checklistStatus) {
        this.checklistStatus = checklistStatus;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

@Entity
@Table(schema = "hr", name = "clearance_checklist_items")
class ClearanceChecklistItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clearance_checklist_id", nullable = false)
    private ClearanceChecklist clearanceChecklist;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 40)
    private ClearanceItemType itemType;

    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_status", nullable = false, length = 30)
    private ClearanceItemStatus itemStatus = ClearanceItemStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status", nullable = false, length = 30)
    private ClearanceReturnStatus returnStatus = ClearanceReturnStatus.PENDING;

    @Column(name = "responsible_role", length = 50)
    private String responsibleRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_user_id")
    private UserAccount responsibleUser;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "asset_code", length = 100)
    private String assetCode;

    @Column(name = "asset_name", length = 255)
    private String assetName;

    @Column(name = "comment_text", length = 2000)
    private String commentText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ClearanceChecklist getClearanceChecklist() {
        return clearanceChecklist;
    }

    public void setClearanceChecklist(ClearanceChecklist clearanceChecklist) {
        this.clearanceChecklist = clearanceChecklist;
    }

    public ClearanceItemType getItemType() {
        return itemType;
    }

    public void setItemType(ClearanceItemType itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public ClearanceItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(ClearanceItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }

    public ClearanceReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(ClearanceReturnStatus returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getResponsibleRole() {
        return responsibleRole;
    }

    public void setResponsibleRole(String responsibleRole) {
        this.responsibleRole = responsibleRole;
    }

    public UserAccount getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(UserAccount responsibleUser) {
        this.responsibleUser = responsibleUser;
    }

    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(OffsetDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}

@Entity
@Table(schema = "hr", name = "dismissal_history")
class DismissalHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissal_request_id", nullable = false)
    private DismissalRequest dismissalRequest;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "status_from", length = 40)
    private String statusFrom;

    @Column(name = "status_to", length = 40)
    private String statusTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserAccount actorUser;

    @Column(name = "comment_text", length = 2000)
    private String commentText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json")
    private String payloadJson;

    public DismissalRequest getDismissalRequest() {
        return dismissalRequest;
    }

    public void setDismissalRequest(DismissalRequest dismissalRequest) {
        this.dismissalRequest = dismissalRequest;
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

interface DismissalRequestRepository extends JpaRepository<DismissalRequest, UUID> {
    Optional<DismissalRequest> findByIdAndDeletedFalse(UUID id);

    @Query(
        "select dr from DismissalRequest dr " +
            "join fetch dr.employee e " +
            "left join fetch dr.department d " +
            "where dr.deleted = false " +
            "and (:employeeId is null or e.id = :employeeId) " +
            "and (:departmentId is null or d.id = :departmentId) " +
            "and (:status is null or dr.status = :status) " +
            "order by dr.createdAt desc"
    )
    List<DismissalRequest> search(@Param("employeeId") UUID employeeId, @Param("departmentId") UUID departmentId, @Param("status") DismissalStatus status);
}

interface ClearanceChecklistRepository extends JpaRepository<ClearanceChecklist, UUID> {
    Optional<ClearanceChecklist> findByDismissalRequestIdAndDeletedFalse(UUID dismissalRequestId);
}

interface ClearanceChecklistItemRepository extends JpaRepository<ClearanceChecklistItem, UUID> {
    Optional<ClearanceChecklistItem> findByIdAndDeletedFalse(UUID id);

    List<ClearanceChecklistItem> findAllByClearanceChecklistIdAndDeletedFalseOrderBySortOrderAsc(UUID checklistId);
}

interface DismissalHistoryRepository extends JpaRepository<DismissalHistory, UUID> {
    List<DismissalHistory> findAllByDismissalRequestIdAndDeletedFalseOrderByCreatedAtDesc(UUID dismissalRequestId);
}

interface DismissalEmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    List<EmployeeAssignment> findAllByEmployeeIdAndDeletedFalseAndEndedAtIsNullOrderByStartedAtDesc(UUID employeeId);
}
