package uz.hrms.other.dto.businessTripDtos;

import jakarta.validation.constraints.NotBlank;

record BusinessTripReportRequest(
        @NotBlank String reportText
) {
}
