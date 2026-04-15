package uz.hrms.other.dto.explanationDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.hrms.other.enums.ExplanationIncidentSource;

import java.time.OffsetDateTime;
import java.util.UUID;

record ExplanationIncidentCreateRequest(
        @NotNull UUID employeeId,
        UUID departmentId,
        UUID managerEmployeeId,
        @NotNull ExplanationIncidentSource incidentSource,
        @NotBlank @Size(max = 40) String incidentType,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        @NotNull OffsetDateTime occurredAt,
        OffsetDateTime dueAt
) {
}
