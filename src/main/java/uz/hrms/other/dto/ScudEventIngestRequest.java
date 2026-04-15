package uz.hrms.other.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uz.hrms.other.enums.ScudEventType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScudEventIngestRequest(
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
