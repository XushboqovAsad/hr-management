package uz.hrms.other.dto.attendanceDtos;

import java.util.List;

public record AttendanceDashboardResponse(
        long totalDays,
        long lateCount,
        long earlyLeaveCount,
        long missingPunchCount,
        long absenceCount,
        long overtimeCount,
        List<AttendanceDashboardItemResponse> items
) {
}
