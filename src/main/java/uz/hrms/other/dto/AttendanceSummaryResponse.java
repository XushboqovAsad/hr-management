package uz.hrms.other.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AttendanceSummaryResponse(
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
