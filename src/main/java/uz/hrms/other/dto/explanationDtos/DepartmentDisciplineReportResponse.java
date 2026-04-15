package uz.hrms.other.dto.explanationDtos;

import java.util.UUID;

record DepartmentDisciplineReportResponse(
        UUID departmentId,
        long incidentCount,
        long submittedCount,
        long acceptedCount,
        long rejectedCount,
        long disciplinaryCount,
        long rewardCount,
        long remarkCount,
        long reprimandCount,
        long severeReprimandCount,
        long bonusCount
) {
}
