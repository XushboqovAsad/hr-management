package uz.hrms.audit;

import java.time.Instant;
import java.util.UUID;

public final class AuditDtos {

    private AuditDtos() {
    }

    public record AuditLogResponse(
            UUID id,
            UUID actorUserId,
            UUID actorEmployeeId,
            String action,
            String entitySchema,
            String entityTable,
            UUID entityId,
            String detailsJson,
            Instant occurredAt
    ) {
    }
}
