package uz.hrms.other.dto.dismissalDtos;

import uz.hrms.other.DismissalHistoryResponse;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
