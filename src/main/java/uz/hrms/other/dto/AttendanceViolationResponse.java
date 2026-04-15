package uz.hrms.other.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AttendanceViolationResponse(
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
