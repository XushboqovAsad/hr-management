package uz.hrms.other.dto.attendanceDtos;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.UUID;

record AttendanceAdjustmentRequest(
        OffsetDateTime adjustedStartAt,
        OffsetDateTime adjustedEndAt,
        @NotBlank String adjustedStatus,
        @NotBlank String adjustedReason,
        UUID approvedBy
) {
}
