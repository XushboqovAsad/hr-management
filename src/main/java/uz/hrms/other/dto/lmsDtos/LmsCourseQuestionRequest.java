package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

record LmsCourseQuestionRequest(
        @NotNull @Min(1) Integer questionOrder,
        @NotNull LmsQuestionType questionType,
        @NotBlank @Size(max = 2000) String questionText,
        @NotNull @DecimalMin("0.01") BigDecimal points,
        @Valid List<LmsCourseOptionRequest> options
) {
}
