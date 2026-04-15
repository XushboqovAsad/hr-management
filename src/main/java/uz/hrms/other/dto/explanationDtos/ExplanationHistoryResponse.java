package uz.hrms.other.dto.explanationDtos;

import java.time.OffsetDateTime;
import java.util.UUID;

record ExplanationHistoryResponse(
        UUID id,
        String actionType,
        String statusFrom,
        String statusTo,
        UUID actorUserId,
        String commentText,
        OffsetDateTime createdAt
) {
}
