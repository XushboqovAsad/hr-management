package uz.hrms.other.dto.lmsDtos;

import uz.hrms.other.LmsTestResponse;

import java.math.BigDecimal;
import java.util.UUID;

record LmsLessonResponse(
        UUID id,
        String title,
        String description,
        Integer lessonOrder,
        LmsLessonContentType contentType,
        String contentUrl,
        String contentText,
        String storageKey,
        String mimeType,
        Integer durationMinutes,
        boolean required,
        boolean completed,
        BigDecimal progressPercent,
        LmsTestResponse test
) {
}
