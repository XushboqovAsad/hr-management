package uz.hrms.other.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

record AbsenceHistoryResponse(
        UUID id,
        String actionType,
        String statusFrom,
        String statusTo,
        UUID actorUserId,
        String commentText,
        OffsetDateTime createdAt
) {
}
