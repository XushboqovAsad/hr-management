package uz.hrms.other;

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

public final class ExplanationDomain {
    private ExplanationDomain() {
    }
}

enum ExplanationIncidentSource {
    SCUD,
    MANUAL,
    MANAGER
}

enum ExplanationIncidentStatus {
    OPEN,
    PENDING_EXPLANATION,
    UNDER_REVIEW,
    RESOLVED,
    WAIVED
}

enum ExplanationStatus {
    DRAFT,
    SUBMITTED,
    MANAGER_REVIEWED,
    ACCEPTED,
    REJECTED,
    DISCIPLINARY_ACTION_CREATED,
    CLOSED_NO_CONSEQUENCE
}

enum DisciplinaryActionType {
    REMARK,
    REPRIMAND,
    SEVERE_REPRIMAND
}

enum DisciplinaryActionStatus {
    ACTIVE,
    CANCELLED,
    CLOSED
}

enum RewardType {
    THANKS,
    AWARD,
    CERTIFICATE,
    BONUS
}

enum RewardStatus {
    DRAFT,
    APPROVED,
    GRANTED,
    CANCELLED
}

enum NotificationStatus {
    NEW,
    READ,
    ARCHIVED
}

@Entity
@Table(schema = "hr", name = "explanation_incidents")
class ExplanationIncident extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_incident_id")
    private AttendanceIncident attendanceIncident;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_source", nullable = false, length = 30)
    private ExplanationIncidentSource incidentSource;

    @Column(name = "incident_type", nullable = false, length = 40)
    private String incidentType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_employee_id")
    private Employee managerEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ExplanationIncidentStatus status = ExplanationIncidentStatus.PENDING_EXPLANATION;

    @Column(name = "explanation_required", nullable = false)
    private boolean explanationRequired = true;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

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

    public AttendanceIncident getAttendanceIncident() {
        return attendanceIncident;
    }

    public void setAttendanceIncident(AttendanceIncident attendanceIncident) {
        this.attendanceIncident = attendanceIncident;
    }

    public ExplanationIncidentSource getIncidentSource() {
        return incidentSource;
    }

    public void setIncidentSource(ExplanationIncidentSource incidentSource) {
        this.incidentSource = incidentSource;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Employee getManagerEmployee() {
        return managerEmployee;
    }

    public void setManagerEmployee(Employee managerEmployee) {
        this.managerEmployee = managerEmployee;
    }

    public ExplanationIncidentStatus getStatus() {
        return status;
    }

    public void setStatus(ExplanationIncidentStatus status) {
        this.status = status;
    }

    public boolean isExplanationRequired() {
        return explanationRequired;
    }

    public boolean getExplanationRequired() {
        return explanationRequired;
    }

    public void setExplanationRequired(boolean explanationRequired) {
        this.explanationRequired = explanationRequired;
    }

    public OffsetDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(OffsetDateTime dueAt) {
        this.dueAt = dueAt;
    }
}

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

@Entity
@Table(schema = "hr", name = "explanation_documents")
class ExplanationDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_id", nullable = false)
    private Explanation explanation;

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

    public Explanation getExplanation() {
        return explanation;
    }

    public void setExplanation(Explanation explanation) {
        this.explanation = explanation;
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
@Table(schema = "hr", name = "explanation_history")
class ExplanationHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_incident_id", nullable = false)
    private ExplanationIncident explanationIncident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_id")
    private Explanation explanation;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "status_from", length = 40)
    private String statusFrom;

    @Column(name = "status_to", length = 40)
    private String statusTo;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "comment_text", length = 2000)
    private String commentText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json")
    private String payloadJson;

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

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
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

@Entity
@Table(schema = "hr", name = "disciplinary_actions")
class DisciplinaryAction extends BaseEntity {

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

@Entity
@Table(schema = "hr", name = "reward_actions")
class RewardAction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    private RewardType rewardType;

    @Column(name = "reward_date", nullable = false)
    private LocalDate rewardDate;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "reason_text", nullable = false, length = 2000)
    private String reasonText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RewardStatus status = RewardStatus.DRAFT;

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

    public RewardType getRewardType() {
        return rewardType;
    }

    public void setRewardType(RewardType rewardType) {
        this.rewardType = rewardType;
    }

    public LocalDate getRewardDate() {
        return rewardDate;
    }

    public void setRewardDate(LocalDate rewardDate) {
        this.rewardDate = rewardDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public RewardStatus getStatus() {
        return status;
    }

    public void setStatus(RewardStatus status) {
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

@Entity
@Table(schema = "hr", name = "notifications")
class HrNotification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_employee_id")
    private Employee recipientEmployee;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false, length = 2000)
    private String body;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.NEW;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json")
    private String payloadJson;

    public Employee getRecipientEmployee() {
        return recipientEmployee;
    }

    public void setRecipientEmployee(Employee recipientEmployee) {
        this.recipientEmployee = recipientEmployee;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(OffsetDateTime readAt) {
        this.readAt = readAt;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}

interface ExplanationIncidentRepository extends JpaRepository<ExplanationIncident, UUID> {
    Optional<ExplanationIncident> findByIdAndDeletedFalse(UUID id);

    Optional<ExplanationIncident> findByAttendanceIncidentIdAndDeletedFalse(UUID attendanceIncidentId);

    List<ExplanationIncident> findAllByDeletedFalseOrderByCreatedAtDesc();

    List<ExplanationIncident> findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);

    List<ExplanationIncident> findAllByDepartmentIdAndDeletedFalseOrderByCreatedAtDesc(UUID departmentId);
}

interface ExplanationRepository extends JpaRepository<Explanation, UUID> {
    Optional<Explanation> findByIdAndDeletedFalse(UUID id);

    Optional<Explanation> findByExplanationIncidentIdAndDeletedFalse(UUID explanationIncidentId);
}

interface ExplanationDocumentRepository extends JpaRepository<ExplanationDocument, UUID> {
    Optional<ExplanationDocument> findByIdAndDeletedFalse(UUID id);

    List<ExplanationDocument> findAllByExplanationIdAndDeletedFalseOrderByCreatedAtDesc(UUID explanationId);

    List<ExplanationDocument> findAllByExplanationIdAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(UUID explanationId, String title);
}

interface ExplanationHistoryRepository extends JpaRepository<ExplanationHistory, UUID> {
    List<ExplanationHistory> findAllByExplanationIncidentIdAndDeletedFalseOrderByCreatedAtDesc(UUID explanationIncidentId);
}

interface DisciplinaryActionRepository extends JpaRepository<DisciplinaryAction, UUID> {
    List<DisciplinaryAction> findAllByDeletedFalseOrderByActionDateDescCreatedAtDesc();

    List<DisciplinaryAction> findAllByEmployeeIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(UUID employeeId);

    List<DisciplinaryAction> findAllByDepartmentIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(UUID departmentId);
}

interface RewardActionRepository extends JpaRepository<RewardAction, UUID> {
    List<RewardAction> findAllByDeletedFalseOrderByRewardDateDescCreatedAtDesc();

    List<RewardAction> findAllByEmployeeIdAndDeletedFalseOrderByRewardDateDescCreatedAtDesc(UUID employeeId);

    List<RewardAction> findAllByDepartmentIdAndDeletedFalseOrderByRewardDateDescCreatedAtDesc(UUID departmentId);
}

interface HrNotificationRepository extends JpaRepository<HrNotification, UUID> {
    List<HrNotification> findAllByRecipientEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(UUID employeeId);
}
