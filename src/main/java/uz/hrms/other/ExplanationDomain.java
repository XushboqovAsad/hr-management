package uz.hrms.other;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.hrms.other.entity.*;
import uz.hrms.other.enums.DisciplinaryActionStatus;
import uz.hrms.other.enums.DisciplinaryActionType;
import uz.hrms.other.enums.NotificationStatus;

public final class ExplanationDomain {
    private ExplanationDomain() {
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
