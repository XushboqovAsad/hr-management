package uz.hrms.other.dto.lmsDtos;

import java.util.UUID;

record LmsCourseListItemResponse(
        UUID id,
        String code,
        String title,
        String category,
        LmsCourseLevel courseLevel,
        LmsCourseStatus status,
        boolean mandatoryForAll,
        boolean introductoryCourse,
        Integer estimatedMinutes,
        boolean assigned,
        boolean completed,
        boolean recommended
) {
}
