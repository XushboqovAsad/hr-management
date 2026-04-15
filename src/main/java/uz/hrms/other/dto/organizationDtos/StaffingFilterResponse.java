package uz.hrms.other.dto.organizationDtos;

import uz.hrms.other.StaffingUnitResponse;

import java.util.List;

record StaffingFilterResponse(
        List<StaffingUnitResponse> items,
        long total
) {
}
