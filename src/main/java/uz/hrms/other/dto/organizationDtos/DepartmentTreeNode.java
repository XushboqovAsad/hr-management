package uz.hrms.other.dto.organizationDtos;

import java.util.List;
import java.util.UUID;

record DepartmentTreeNode(
        UUID id,
        String code,
        String name,
        DepartmentUnitType unitType,
        boolean active,
        String managerPersonnelNumber,
        List<DepartmentTreeNode> children
) {
}
