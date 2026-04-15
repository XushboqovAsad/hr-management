package uz.hrms.other.dto.lmsDtos;

import uz.hrms.other.LmsLessonResponse;

import java.util.List;
import java.util.UUID;

record LmsCourseModuleResponse(
        UUID id,
        String title,
        String description,
        Integer moduleOrder,
        boolean required,
        List<LmsLessonResponse> lessons
) {
}
