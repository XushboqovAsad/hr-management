package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

record LmsTestAnswerRequest(
        @NotNull UUID questionId,
        UUID selectedOptionId,
        @Size(max = 2000) String freeTextAnswer
) {
}
