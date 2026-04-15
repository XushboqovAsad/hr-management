package uz.hrms.other.dto.explanationDtos;

import uz.hrms.other.enums.RewardStatus;
import uz.hrms.other.enums.RewardType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RewardActionResponse(
        UUID id,
        UUID employeeId,
        UUID departmentId,
        RewardType rewardType,
        LocalDate rewardDate,
        BigDecimal amount,
        String currencyCode,
        String reasonText,
        RewardStatus status,
        UUID sourceOrderId,
        String sourceOrderNumber
) {
}
