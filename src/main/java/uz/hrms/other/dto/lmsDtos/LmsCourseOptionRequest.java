package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record LmsCourseOptionRequest(
        @NotNull @Min(1) Integer optionOrder,
        @NotBlank @Size(max = 1000) String optionText,
        boolean correct
) {
}
