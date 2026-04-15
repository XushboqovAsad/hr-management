package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

record LmsCourseTestRequest(
        @NotBlank @Size(max = 255) String title,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal passScore,
        @NotNull @Min(1) @Max(20) Integer attemptLimit,
        boolean randomizeQuestions,
        @Valid @NotEmpty List<LmsCourseQuestionRequest> questions
) {
}
