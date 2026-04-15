package uz.hrms.other.dto.employeeDtos;

import java.time.LocalDate;
import java.util.UUID;

public record EmployeeHistoryItemResponse(
        String entryType,
        String status,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        UUID departmentId,
        String departmentName,
        UUID positionId,
        String positionTitle,
        UUID staffingUnitId,
        String staffingUnitCode,
        UUID managerEmployeeId,
        String managerFullName
) {
}
