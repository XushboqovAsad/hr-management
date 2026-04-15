package uz.hrms.other.dto.lmsDtos;

import java.time.OffsetDateTime;
import java.util.UUID;

record LmsLearningHistoryResponse(
        UUID id,
        String actionType,
        String detailsJson,
        OffsetDateTime actionAt
) {
}
