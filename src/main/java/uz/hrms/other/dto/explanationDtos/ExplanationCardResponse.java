package uz.hrms.other.dto.explanationDtos;

import uz.hrms.other.ExplanationDocumentResponse;
import uz.hrms.other.ExplanationHistoryResponse;
import uz.hrms.other.enums.ExplanationIncidentSource;
import uz.hrms.other.enums.ExplanationIncidentStatus;
import uz.hrms.other.enums.ExplanationStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record ExplanationCardResponse(
        UUID incidentId,
        UUID employeeId,
        UUID departmentId,
        UUID managerEmployeeId,
        UUID attendanceIncidentId,
        ExplanationIncidentSource incidentSource,
        String incidentType,
        String title,
        String description,
        OffsetDateTime occurredAt,
        OffsetDateTime dueAt,
        ExplanationIncidentStatus incidentStatus,
        UUID explanationId,
        String explanationText,
        OffsetDateTime employeeSubmittedAt,
        UUID managerReviewerEmployeeId,
        String managerReviewComment,
        OffsetDateTime managerReviewedAt,
        UUID hrReviewerEmployeeId,
        String hrDecisionComment,
        OffsetDateTime hrDecidedAt,
        ExplanationStatus explanationStatus,
        List<ExplanationDocumentResponse> documents,
        List<ExplanationHistoryResponse> history,
        List<DisciplinaryActionResponse> disciplinaryActions
) {
}
