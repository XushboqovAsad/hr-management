package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

record LmsCourseModuleRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        @NotNull @Min(1) Integer moduleOrder,
        boolean required,
        @Valid @NotEmpty List<LmsCourseLessonRequest> lessons
) {
}
