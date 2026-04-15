package uz.hrms.other.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ScudEventBatchRequest(
        @NotEmpty List<ScudEventIngestRequest> events
) {
}
