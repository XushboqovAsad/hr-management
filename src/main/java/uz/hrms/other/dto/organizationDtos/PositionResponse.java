package uz.hrms.other.dto.organizationDtos;

import java.util.UUID;

record PositionResponse(
        UUID id,
        String code,
        String title,
        String category,
        boolean active,
        long staffingUnitsCount,
        long vacanciesCount
) {
}
