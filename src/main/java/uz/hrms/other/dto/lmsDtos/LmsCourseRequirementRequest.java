package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record LmsCourseRequirementRequest(
        @NotNull LmsRequirementScopeType scopeType,
        UUID positionId,
        UUID departmentId,
        @NotNull @Min(0) Integer dueDays,
        boolean active
) {
}
