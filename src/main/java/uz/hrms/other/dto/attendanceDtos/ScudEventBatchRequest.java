package uz.hrms.other.dto.attendanceDtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

record ScudEventBatchRequest(
        @NotEmpty List<ScudEventIngestRequest> events
) {
}
