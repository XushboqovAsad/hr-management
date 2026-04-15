package uz.hrms.other.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import uz.hrms.other.enums.ClearanceItemStatus;
import uz.hrms.other.enums.ClearanceReturnStatus;

public record DismissalChecklistItemUpdateRequest(
    @NotNull ClearanceItemStatus itemStatus,
    @NotNull ClearanceReturnStatus returnStatus,
    UUID responsibleUserId,
    OffsetDateTime dueAt,
    @Size(max = 100) String assetCode,
    @Size(max = 255) String assetName,
    @Size(max = 2000) String commentText
) {
}

