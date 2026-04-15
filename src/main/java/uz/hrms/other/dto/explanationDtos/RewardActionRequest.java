package uz.hrms.other.dto.explanationDtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uz.hrms.other.enums.RewardType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

record RewardActionRequest(
        @NotNull UUID employeeId,
        UUID departmentId,
        @NotNull RewardType rewardType,
        @NotNull LocalDate rewardDate,
        @DecimalMin("0.00") BigDecimal amount,
        @Size(max = 3) String currencyCode,
        @NotBlank @Size(max = 2000) String reasonText,
        UUID sourceOrderId,
        @Size(max = 100) String sourceOrderNumber
) {
}
