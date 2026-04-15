package uz.hrms.other.dto.organizationDtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record StaffingUnitResponse(
        UUID id,
        String code,
        UUID departmentId,
        String departmentName,
        UUID positionId,
        String positionTitle,
        BigDecimal plannedFte,
        BigDecimal occupiedFte,
        BigDecimal vacantFte,
        StaffingUnitStatus status,
        LocalDate openedAt,
        LocalDate closedAt,
        String notes
) {
}
