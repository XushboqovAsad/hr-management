package uz.hrms.other.dto.organizationDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record DepartmentRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull DepartmentUnitType unitType,
        UUID parentDepartmentId,
        UUID managerEmployeeId,
        String phone,
        String email,
        String location,
        Boolean active
) {
}
