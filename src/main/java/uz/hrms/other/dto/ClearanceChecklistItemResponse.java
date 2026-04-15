package uz.hrms.other.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import uz.hrms.other.enums.ClearanceItemStatus;
import uz.hrms.other.enums.ClearanceItemType;
import uz.hrms.other.enums.ClearanceReturnStatus;

public record ClearanceChecklistItemResponse(
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

