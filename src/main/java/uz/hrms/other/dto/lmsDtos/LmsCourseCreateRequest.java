package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import uz.hrms.other.LmsCourseModuleRequest;
import uz.hrms.other.LmsCourseRequirementRequest;

import java.util.List;

record LmsCourseCreateRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        @Size(max = 100) String category,
        @NotNull LmsCourseLevel courseLevel,
        @NotNull LmsCourseStatus status,
        boolean mandatoryForAll,
        boolean introductoryCourse,
        @NotNull @Min(0) Integer estimatedMinutes,
        boolean certificateEnabled,
        @Size(max = 100) String certificateTemplateCode,
        @Valid @NotEmpty List<LmsCourseModuleRequest> modules,
        @Valid List<LmsCourseRequirementRequest> requirements
) {
}
