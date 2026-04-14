package uz.hrms.other;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uz.hrms.other.enums.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record AbsenceRequest(
    @NotNull UUID employeeId,
    UUID requesterEmployeeId,
    @NotNull AbsenceType absenceType,
    String reasonText,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    Boolean documentRequired
) {
}

record AbsenceDecisionRequest(
    String hrComment
) {
}

record AbsenceDocumentUploadRequest(
    @NotBlank String title,
    String description
) {
}

record AbsenceListItemResponse(
    UUID id,
    UUID employeeId,
    AbsenceType absenceType,
    LocalDate startDate,
    LocalDate endDate,
    AbsenceStatus status,
    boolean documentRequired,
    boolean frequentAbsenceMarker
) {
}

record AbsenceDocumentResponse(
    UUID id,
    String title,
    String originalFileName,
    String contentType,
    Long sizeBytes,
    Integer versionNo,
    boolean current,
    AbsenceDocumentStatus documentStatus,
    String description
) {
}

record AbsenceHistoryResponse(
    UUID id,
    String actionType,
    String statusFrom,
    String statusTo,
    UUID actorUserId,
    String commentText,
    OffsetDateTime createdAt
) {
}

record AttendanceDayMarkResponse(
    UUID id,
    UUID employeeId,
    LocalDate attendanceDate,
    AttendanceMarkSource markSource,
    String markStatus,
    String noteText
) {
}

record FrequentAbsenceAnalyticsResponse(
    UUID employeeId,
    long recordCount,
    long totalDays,
    List<AbsenceType> absenceTypes
) {
}

record AbsenceResponse(
    UUID id,
    UUID employeeId,
    UUID requesterEmployeeId,
    AbsenceType absenceType,
    String reasonText,
    LocalDate startDate,
    LocalDate endDate,
    boolean documentRequired,
    String hrComment,
    AbsenceStatus status,
    PayrollSyncStatus payrollSyncStatus,
    OffsetDateTime approvedAt,
    OffsetDateTime closedAt,
    List<AbsenceDocumentResponse> documents,
    List<AbsenceHistoryResponse> history,
    List<AttendanceDayMarkResponse> attendanceMarks
) {
}
