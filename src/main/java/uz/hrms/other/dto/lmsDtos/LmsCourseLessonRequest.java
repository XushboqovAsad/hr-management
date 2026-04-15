package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.hrms.other.LmsCourseTestRequest;

record LmsCourseLessonRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 1000) String description,
        @NotNull @Min(1) Integer lessonOrder,
        @NotNull LmsLessonContentType contentType,
        @Size(max = 1000) String contentUrl,
        String contentText,
        @Size(max = 500) String storageKey,
        @Size(max = 150) String mimeType,
        @NotNull @Min(0) Integer durationMinutes,
        boolean required,
        @Valid LmsCourseTestRequest test
) {
}
