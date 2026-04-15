package uz.hrms.other.dto.organizationDtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record StaffingUnitRequest(
        @NotBlank String code,
        @NotNull UUID departmentId,
        @NotNull UUID positionId,
        @NotNull @DecimalMin("0.01") BigDecimal plannedFte,
        @NotNull StaffingUnitStatus status,
        LocalDate openedAt,
        LocalDate closedAt,
        String notes
) {
}
