package uz.hrms.other;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record ScudEventIngestRequest(
    UUID employeeId,
    String badgeNumber,
    String sourceSystem,
    String externalEventId,
    @NotBlank String deviceId,
    String deviceName,
    @NotNull ScudEventType eventType,
    @NotNull OffsetDateTime eventAt,
    String rawPayload
) {
}

record ScudEventBatchRequest(
    @NotEmpty List<ScudEventIngestRequest> events
) {
}

record AttendanceProcessRequest(
    @NotNull LocalDate workDate,
    UUID employeeId
) {
}

record AttendanceAdjustmentRequest(
    OffsetDateTime adjustedStartAt,
    OffsetDateTime adjustedEndAt,
    @NotBlank String adjustedStatus,
    @NotBlank String adjustedReason,
    UUID approvedBy
) {
}

record AttendanceDashboardItemResponse(
    UUID summaryId,
    UUID employeeId,
    UUID departmentId,
    LocalDate workDate,
    AttendanceStatus attendanceStatus,
    Integer lateMinutes,
    Integer earlyLeaveMinutes,
    Integer overtimeMinutes,
    Integer absenceMinutes,
    Integer violationCount,
    boolean incidentCreated,
    boolean manuallyAdjusted
) {
}

record AttendanceViolationResponse(
    UUID id,
    UUID employeeId,
    UUID attendanceSummaryId,
    UUID attendanceIncidentId,
    AttendanceViolationType violationType,
    Integer minutesValue,
    AttendanceViolationStatus status,
    OffsetDateTime scheduledAt,
    OffsetDateTime actualAt
) {
}

record AttendanceIncidentResponse(
    UUID id,
    UUID employeeId,
    UUID attendanceSummaryId,
    String incidentType,
    String title,
    String description,
    AttendanceIncidentStatus status,
    boolean explanationRequired,
    OffsetDateTime dueAt
) {
}

record AttendanceLogResponse(
    UUID id,
    UUID employeeId,
    LocalDate workDate,
    UUID workScheduleId,
    OffsetDateTime scheduledStartAt,
    OffsetDateTime scheduledEndAt,
    OffsetDateTime firstInAt,
    OffsetDateTime lastOutAt,
    Integer workedMinutes,
    Integer rawEventCount,
    boolean missingIn,
    boolean missingOut,
    boolean noScudData,
    String logStatus
) {
}

record AttendanceSummaryResponse(
    UUID id,
    UUID employeeId,
    UUID departmentId,
    UUID employeeAssignmentId,
    LocalDate workDate,
    AttendanceStatus attendanceStatus,
    Integer lateMinutes,
    Integer earlyLeaveMinutes,
    Integer overtimeMinutes,
    Integer absenceMinutes,
    Integer violationCount,
    boolean incidentCreated,
    boolean manuallyAdjusted,
    String adjustedComment,
    OffsetDateTime finalizedAt,
    AttendanceLogResponse log,
    List<AttendanceViolationResponse> violations,
    List<AttendanceIncidentResponse> incidents
) {
}

record AttendanceDashboardResponse(
    long totalDays,
    long lateCount,
    long earlyLeaveCount,
    long missingPunchCount,
    long absenceCount,
    long overtimeCount,
    List<AttendanceDashboardItemResponse> items
) {
}
