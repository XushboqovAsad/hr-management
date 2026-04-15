package uz.hrms.other.dto.lmsDtos;

import java.util.List;
import java.util.UUID;

record LmsCourseResponse(
        UUID id,
        String code,
        String title,
        String description,
        String category,
        LmsCourseLevel courseLevel,
        LmsCourseStatus status,
        boolean mandatoryForAll,
        boolean introductoryCourse,
        Integer estimatedMinutes,
        boolean certificateEnabled,
        String certificateTemplateCode,
        List<LmsCourseModuleResponse> modules,
        List<LmsCourseRequirementResponse> requirements
) {
}
