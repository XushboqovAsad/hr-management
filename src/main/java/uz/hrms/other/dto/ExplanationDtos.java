package uz.hrms.other;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record ExplanationIncidentCreateRequest(
    @NotNull UUID employeeId,
    UUID departmentId,
    UUID managerEmployeeId,
    @NotNull ExplanationIncidentSource incidentSource,
    @NotBlank @Size(max = 40) String incidentType,
    @NotBlank @Size(max = 255) String title,
    @Size(max = 2000) String description,
    @NotNull OffsetDateTime occurredAt,
    OffsetDateTime dueAt
) {
}

record ExplanationSubmitRequest(
    @NotBlank @Size(max = 4000) String explanationText
) {
}

record ExplanationManagerReviewRequest(
    UUID managerReviewerEmployeeId,
    @NotBlank @Size(max = 2000) String managerComment
) {
}

record ExplanationDecisionRequest(
    UUID hrReviewerEmployeeId,
    @Size(max = 2000) String hrComment,
    UUID sourceOrderId,
    @Size(max = 100) String sourceOrderNumber
) {
}

record ExplanationDisciplinaryActionRequest(
    UUID hrReviewerEmployeeId,
    @NotNull DisciplinaryActionType actionType,
    @NotNull LocalDate actionDate,
    @NotBlank @Size(max = 2000) String reasonText,
    LocalDate validUntil,
    UUID sourceOrderId,
    @Size(max = 100) String sourceOrderNumber,
    @Size(max = 2000) String hrComment
) {
}

record ExplanationDocumentUploadRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 1000) String description
) {
}

record RewardActionRequest(
    @NotNull UUID employeeId,
    UUID departmentId,
    @NotNull RewardType rewardType,
    @NotNull LocalDate rewardDate,
    @DecimalMin("0.00") BigDecimal amount,
    @Size(max = 3) String currencyCode,
    @NotBlank @Size(max = 2000) String reasonText,
    UUID sourceOrderId,
    @Size(max = 100) String sourceOrderNumber
) {
}

record ExplanationInboxItemResponse(
    UUID incidentId,
    UUID employeeId,
    UUID departmentId,
    ExplanationIncidentSource incidentSource,
    String incidentType,
    String title,
    OffsetDateTime occurredAt,
    OffsetDateTime dueAt,
    ExplanationIncidentStatus incidentStatus,
    ExplanationStatus explanationStatus,
    boolean overdue,
    boolean disciplinaryActionCreated
) {
}

record ExplanationDocumentResponse(
    UUID id,
    String title,
    String originalFileName,
    String contentType,
    long sizeBytes,
    int versionNo,
    boolean current,
    String description
) {
}

record ExplanationHistoryResponse(
    UUID id,
    String actionType,
    String statusFrom,
    String statusTo,
    UUID actorUserId,
    String commentText,
    OffsetDateTime createdAt
) {
}

record DisciplinaryActionResponse(
    UUID id,
    UUID employeeId,
    UUID departmentId,
    UUID explanationIncidentId,
    UUID explanationId,
    DisciplinaryActionType actionType,
    LocalDate actionDate,
    String reasonText,
    DisciplinaryActionStatus status,
    LocalDate validUntil,
    UUID sourceOrderId,
    String sourceOrderNumber
) {
}

record RewardActionResponse(
    UUID id,
    UUID employeeId,
    UUID departmentId,
    RewardType rewardType,
    LocalDate rewardDate,
    BigDecimal amount,
    String currencyCode,
    String reasonText,
    RewardStatus status,
    UUID sourceOrderId,
    String sourceOrderNumber
) {
}

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

record DepartmentDisciplineReportResponse(
    UUID departmentId,
    long incidentCount,
    long submittedCount,
    long acceptedCount,
    long rejectedCount,
    long disciplinaryCount,
    long rewardCount,
    long remarkCount,
    long reprimandCount,
    long severeReprimandCount,
    long bonusCount
) {
}
