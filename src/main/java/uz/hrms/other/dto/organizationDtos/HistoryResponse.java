package uz.hrms.other.dto.organizationDtos;

import java.time.OffsetDateTime;

record HistoryResponse(
        Integer versionNo,
        String actionType,
        OffsetDateTime changedAt,
        String payloadJson
) {
}
