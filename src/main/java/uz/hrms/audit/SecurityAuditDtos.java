package uz.hrms.audit;

import java.time.Instant;
import java.util.UUID;

public final class SecurityAuditDtos {

    private SecurityAuditDtos() {
    }

    public record LoginAuditLogResponse(
            UUID id,
            UUID actorUserId,
            String username,
            String eventType,
            String result,
            String failureReason,
            String ipAddress,
            String userAgent,
            Instant occurredAt
    ) {
    }

    public record PersonalDataAccessLogResponse(
            UUID id,
            UUID actorUserId,
            UUID actorEmployeeId,
            UUID targetEmployeeId,
            String requestUri,
            String accessType,
            String fieldsAccessed,
            String maskedFields,
            boolean accessAllowed,
            Instant occurredAt
    ) {
    }

    public record DocumentAccessLogResponse(
            UUID id,
            UUID actorUserId,
            UUID actorEmployeeId,
            String documentModule,
            UUID entityId,
            UUID documentId,
            String storageKey,
            String accessMode,
            String requestUri,
            boolean accessAllowed,
            Instant occurredAt
    ) {
    }

    public record AdminActionLogResponse(
            UUID id,
            UUID actorUserId,
            UUID actorEmployeeId,
            String requestMethod,
            String requestUri,
            int statusCode,
            String detailsJson,
            Instant occurredAt
    ) {
    }

    public record HrDecisionLogResponse(
            UUID id,
            UUID actorUserId,
            UUID actorEmployeeId,
            String moduleCode,
            String decisionAction,
            String entityType,
            UUID entityId,
            String requestUri,
            int statusCode,
            String detailsJson,
            Instant occurredAt
    ) {
    }
}
