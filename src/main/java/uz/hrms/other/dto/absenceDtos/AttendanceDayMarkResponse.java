package uz.hrms.other.dto.absenceDtos;

import uz.hrms.other.enums.AttendanceMarkSource;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceDayMarkResponse(
        UUID id,
        UUID employeeId,
        LocalDate attendanceDate,
        AttendanceMarkSource markSource,
        String markStatus,
        String noteText
) {
}
