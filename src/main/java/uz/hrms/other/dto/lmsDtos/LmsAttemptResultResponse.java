package uz.hrms.other.dto.lmsDtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

record LmsAttemptResultResponse(
        UUID attemptId,
        Integer attemptNo,
        BigDecimal score,
        boolean passed,
        LmsTestAttemptStatus status,
        UUID certificateId,
        OffsetDateTime submittedAt
) {
}
