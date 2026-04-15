package uz.hrms.other.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorUserId,
        UUID actorEmployeeId,
        String action,
        String entitySchema,
        String entityTable,
        UUID entityId,
        String detailsJson,
        OffsetDateTime occurredAt
) {
}
