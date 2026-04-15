package uz.hrms.other.dto.lmsDtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LmsTestResponse(
        UUID id,
        String title,
        BigDecimal passScore,
        Integer attemptLimit,
        boolean randomizeQuestions,
        List<LmsTestQuestionResponse> questions
) {
}
