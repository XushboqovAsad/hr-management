package uz.hrms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", schema = "audit")
class AuditLog extends BaseEntity {

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_employee_id")
    private UUID actorEmployeeId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "entity_schema")
    private String entitySchema;

    @Column(name = "entity_table")
    private String entityTable;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "details_json")
    private String detailsJson;

    @jakarta.persistence.Transient
    private String beforeData;

    @jakarta.persistence.Transient
    private String afterData;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntitySchema() {
        return entitySchema;
    }

    public void setEntitySchema(String entitySchema) {
        this.entitySchema = entitySchema;
    }

    public String getEntityTable() {
        return entityTable;
    }

    public void setEntityTable(String entityTable) {
        this.entityTable = entityTable;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(OffsetDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt == null ? null : occurredAt.atOffset(java.time.ZoneOffset.UTC);
    }

    public String getBeforeData() {
        return beforeData;
    }

    public void setBeforeData(String beforeData) {
        this.beforeData = beforeData;
        rebuildDetailsJson();
    }

    public String getAfterData() {
        return afterData;
    }

    public void setAfterData(String afterData) {
        this.afterData = afterData;
        rebuildDetailsJson();
    }

    private void rebuildDetailsJson() {
        if (beforeData == null && afterData == null) {
            return;
        }
        StringBuilder value = new StringBuilder("{");
        boolean appended = false;
        if (beforeData != null) {
            value.append("\"before\":").append(quoted(beforeData));
            appended = true;
        }
        if (afterData != null) {
            if (appended) {
                value.append(",");
            }
            value.append("\"after\":").append(quoted(afterData));
        }
        value.append("}");
        this.detailsJson = value.toString();
    }

    private String quoted(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
