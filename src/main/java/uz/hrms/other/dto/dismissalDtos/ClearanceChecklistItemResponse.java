package uz.hrms.other.dto.dismissalDtos;

import java.time.OffsetDateTime;
import java.util.UUID;

record ClearanceChecklistItemResponse(
        UUID id,
        ClearanceItemType itemType,
        String itemName,
        ClearanceItemStatus itemStatus,
        ClearanceReturnStatus returnStatus,
        String responsibleRole,
        UUID responsibleUserId,
        OffsetDateTime dueAt,
        OffsetDateTime completedAt,
        String assetCode,
        String assetName,
        String commentText,
        int sortOrder
) {
}
