package uz.hrms.other.dto.businessTripDtos;

import java.time.OffsetDateTime;
import java.util.UUID;

record BusinessTripHistoryResponse(
        UUID id,
        String actionType,
        String statusFrom,
        String statusTo,
        UUID actorUserId,
        String commentText,
        OffsetDateTime createdAt
) {
}
