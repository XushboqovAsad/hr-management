package uz.hrms.other.dto.attendanceDtos;

import java.time.OffsetDateTime;
import java.util.UUID;

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
