package uz.hrms.other.dto.organizationDtos;

import java.time.LocalDate;
import java.util.UUID;

record AssignmentResponse(
        UUID id,
        UUID employeeId,
        UUID departmentId,
        UUID positionId,
        UUID staffingUnitId,
        UUID managerEmployeeId,
        LocalDate startedAt,
        LocalDate endedAt
) {
}
