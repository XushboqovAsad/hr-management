package uz.hrms.other.dto.organizationDtos;

import java.math.BigDecimal;
import java.util.UUID;

public record VacancyResponse(
        UUID staffingUnitId,
        String staffingCode,
        UUID departmentId,
        String departmentName,
        UUID positionId,
        String positionTitle,
        BigDecimal plannedFte,
        BigDecimal occupiedFte,
        BigDecimal vacantFte
) {
}
