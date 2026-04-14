package uz.hrms.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

@jakarta.persistence.MappedSuperclass
abstract class SecurityAuditBaseEntity {

    @Id
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

@Entity
@Table(schema = "auth", name = "login_audit_logs")
class LoginAuditLog extends SecurityAuditBaseEntity {

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(name = "result", nullable = false, length = 20)
    private String result;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}

@Entity
@Table(schema = "audit", name = "personal_data_access_logs")
class PersonalDataAccessLog extends SecurityAuditBaseEntity {

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_employee_id")
    private UUID actorEmployeeId;

    @Column(name = "target_employee_id", nullable = false)
    private UUID targetEmployeeId;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "access_type", nullable = false, length = 30)
    private String accessType;

    @Column(name = "fields_accessed")
    private String fieldsAccessed;

    @Column(name = "masked_fields")
    private String maskedFields;

    @Column(name = "access_allowed", nullable = false)
    private boolean accessAllowed;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public UUID getActorEmployeeId() {
        return actorEmployeeId;
    }

    public void setActorEmployeeId(UUID actorEmployeeId) {
        this.actorEmployeeId = actorEmployeeId;
    }

    public UUID getTargetEmployeeId() {
        return targetEmployeeId;
    }

    public void setTargetEmployeeId(UUID targetEmployeeId) {
        this.targetEmployeeId = targetEmployeeId;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getFieldsAccessed() {
        return fieldsAccessed;
    }

    public void setFieldsAccessed(String fieldsAccessed) {
        this.fieldsAccessed = fieldsAccessed;
    }

    public String getMaskedFields() {
        return maskedFields;
    }

    public void setMaskedFields(String maskedFields) {
        this.maskedFields = maskedFields;
    }

    public boolean isAccessAllowed() {
        return accessAllowed;
    }

    public void setAccessAllowed(boolean accessAllowed) {
        this.accessAllowed = accessAllowed;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}

@Entity
@Table(schema = "audit", name = "document_access_logs")
class DocumentAccessLog extends SecurityAuditBaseEntity {

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_employee_id")
    private UUID actorEmployeeId;

    @Column(name = "document_module", nullable = false, length = 100)
    private String documentModule;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "access_mode", nullable = false, length = 20)
    private String accessMode;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "access_allowed", nullable = false)
    private boolean accessAllowed;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public UUID getActorEmployeeId() {
        return actorEmployeeId;
    }

    public void setActorEmployeeId(UUID actorEmployeeId) {
        this.actorEmployeeId = actorEmployeeId;
    }

    public String getDocumentModule() {
        return documentModule;
    }

    public void setDocumentModule(String documentModule) {
        this.documentModule = documentModule;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public boolean isAccessAllowed() {
        return accessAllowed;
    }

    public void setAccessAllowed(boolean accessAllowed) {
        this.accessAllowed = accessAllowed;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}

@Entity
@Table(schema = "audit", name = "admin_action_logs")
class AdminActionLog extends SecurityAuditBaseEntity {

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_employee_id")
    private UUID actorEmployeeId;

    @Column(name = "request_method", nullable = false, length = 10)
    private String requestMethod;

    @Column(name = "request_uri", nullable = false, length = 500)
    private String requestUri;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "details_json")
    private String detailsJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public UUID getActorEmployeeId() {
        return actorEmployeeId;
    }

    public void setActorEmployeeId(UUID actorEmployeeId) {
        this.actorEmployeeId = actorEmployeeId;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}

@Entity
@Table(schema = "audit", name = "hr_decision_logs")
class HrDecisionLog extends SecurityAuditBaseEntity {

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_employee_id")
    private UUID actorEmployeeId;

    @Column(name = "module_code", nullable = false, length = 100)
    private String moduleCode;

    @Column(name = "decision_action", nullable = false, length = 100)
    private String decisionAction;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "request_uri", nullable = false, length = 500)
    private String requestUri;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "details_json")
    private String detailsJson;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public UUID getActorEmployeeId() {
        return actorEmployeeId;
    }

    public void setActorEmployeeId(UUID actorEmployeeId) {
        this.actorEmployeeId = actorEmployeeId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getDecisionAction() {
        return decisionAction;
    }

    public void setDecisionAction(String decisionAction) {
        this.decisionAction = decisionAction;
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

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}

interface LoginAuditLogRepository extends JpaRepository<LoginAuditLog, UUID> {
    Page<LoginAuditLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
}

interface PersonalDataAccessLogRepository extends JpaRepository<PersonalDataAccessLog, UUID> {
    Page<PersonalDataAccessLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
}

interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, UUID> {
    Page<DocumentAccessLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
}

interface AdminActionLogRepository extends JpaRepository<AdminActionLog, UUID> {
    Page<AdminActionLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
}

interface HrDecisionLogRepository extends JpaRepository<HrDecisionLog, UUID> {
    Page<HrDecisionLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
