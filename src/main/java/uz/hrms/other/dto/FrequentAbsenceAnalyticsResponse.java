package uz.hrms.other.dto;

import uz.hrms.other.enums.AbsenceType;

import java.util.List;
import java.util.UUID;

public record FrequentAbsenceAnalyticsResponse(
        UUID employeeId,
        long recordCount,
        long totalDays,
        List<AbsenceType> absenceTypes
) {
}
