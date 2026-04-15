package uz.hrms.other.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceDashboardItemResponse(
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
