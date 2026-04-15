package uz.hrms.other.dto.organizationDtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

record AssignmentRequest(
        @NotNull UUID employeeId,
        @NotNull UUID departmentId,
        @NotNull UUID positionId,
        @NotNull UUID staffingUnitId,
        UUID managerEmployeeId,
        @NotNull LocalDate startedAt,
        LocalDate endedAt
) {
}
