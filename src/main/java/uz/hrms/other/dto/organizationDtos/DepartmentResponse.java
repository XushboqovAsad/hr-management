package uz.hrms.other.dto.organizationDtos;

import java.time.OffsetDateTime;
import java.util.UUID;

record DepartmentResponse(
        UUID id,
        String code,
        String name,
        DepartmentUnitType unitType,
        UUID parentDepartmentId,
        String parentDepartmentName,
        UUID managerEmployeeId,
        String managerPersonnelNumber,
        String phone,
        String email,
        String location,
        boolean active,
        OffsetDateTime closedAt,
        long directChildrenCount,
        long staffingUnitsCount,
        long vacanciesCount
) {
}
