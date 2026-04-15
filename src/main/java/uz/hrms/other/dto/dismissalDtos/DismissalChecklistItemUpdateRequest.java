package uz.hrms.other.dto.dismissalDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

record DismissalChecklistItemUpdateRequest(
        @NotNull ClearanceItemStatus itemStatus,
        @NotNull ClearanceReturnStatus returnStatus,
        UUID responsibleUserId,
        OffsetDateTime dueAt,
        @Size(max = 100) String assetCode,
        @Size(max = 255) String assetName,
        @Size(max = 2000) String commentText
) {
}
