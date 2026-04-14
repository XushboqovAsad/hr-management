package uz.hrms.other;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record DismissalRequestUpsertRequest(
    @NotNull UUID employeeId,
    UUID initiatorEmployeeId,
    UUID departmentId,
    @NotNull DismissalReasonType reasonType,
    @NotBlank @Size(max = 2000) String reasonText,
    @NotNull LocalDate dismissalDate,
    @Size(max = 2000) String commentText
) {
}

record DismissalDecisionRequest(
    @Size(max = 2000) String commentText
) {
}

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

record DismissalListItemResponse(
    UUID id,
    UUID employeeId,
    UUID departmentId,
    DismissalReasonType reasonType,
    LocalDate dismissalDate,
    DismissalStatus status,
    String orderNumber,
    boolean archived
) {
}

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

record DismissalHistoryResponse(
    UUID id,
    String actionType,
    String statusFrom,
    String statusTo,
    UUID actorUserId,
    String commentText,
    OffsetDateTime createdAt
) {
}

record DismissalCardResponse(
    UUID id,
    UUID employeeId,
    UUID initiatorEmployeeId,
    UUID departmentId,
    DismissalReasonType reasonType,
    String reasonText,
    LocalDate dismissalDate,
    DismissalStatus status,
    String orderNumber,
    String orderTemplateCode,
    OffsetDateTime orderGeneratedAt,
    String orderPrintFormHtml,
    OffsetDateTime approvedAt,
    OffsetDateTime finalizedAt,
    OffsetDateTime archivedAt,
    OffsetDateTime accountBlockedAt,
    String finalPayrollSyncStatus,
    String commentText,
    ClearanceChecklistStatus checklistStatus,
    List<ClearanceChecklistItemResponse> checklistItems,
    List<DismissalHistoryResponse> history
) {
}

record DismissalPrintFormResponse(
    UUID dismissalRequestId,
    String orderNumber,
    String orderTemplateCode,
    String html
) {
}
