package uz.hrms.other.dto.lmsDtos;

import java.util.UUID;

record LmsCourseRequirementResponse(
        UUID id,
        LmsRequirementScopeType scopeType,
        UUID positionId,
        String positionTitle,
        UUID departmentId,
        String departmentName,
        Integer dueDays,
        boolean active
) {
}
