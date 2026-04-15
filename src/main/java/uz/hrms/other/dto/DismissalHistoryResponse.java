package uz.hrms.other.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DismissalHistoryResponse(
    UUID id,
    String actionType,
    String statusFrom,
    String statusTo,
    UUID actorUserId,
    String commentText,
    OffsetDateTime createdAt
) {
}

