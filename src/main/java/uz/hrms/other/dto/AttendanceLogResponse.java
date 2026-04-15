package uz.hrms.other.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AttendanceLogResponse(
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
