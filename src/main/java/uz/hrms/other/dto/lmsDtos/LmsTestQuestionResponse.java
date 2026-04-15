package uz.hrms.other.dto.lmsDtos;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

record LmsTestQuestionResponse(
        UUID id,
        Integer questionOrder,
        LmsQuestionType questionType,
        String questionText,
        BigDecimal points,
        List<LmsTestOptionResponse> options
) {
}
