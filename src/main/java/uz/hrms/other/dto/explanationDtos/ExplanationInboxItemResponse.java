package uz.hrms.other.dto.explanationDtos;

import uz.hrms.other.enums.ExplanationIncidentSource;
import uz.hrms.other.enums.ExplanationIncidentStatus;
import uz.hrms.other.enums.ExplanationStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

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
