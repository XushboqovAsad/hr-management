package uz.hrms.other.dto.businessTripDtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record BusinessTripRequest(
        @NotNull UUID employeeId,
        UUID requesterEmployeeId,
        UUID approverDepartmentId,
        String destinationCountry,
        @NotBlank String destinationCity,
        String destinationAddress,
        @NotBlank String purpose,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String transportType,
        String accommodationDetails,
        @NotNull @DecimalMin("0.00") BigDecimal dailyAllowance,
        String fundingSource,
        String commentText
) {
}
