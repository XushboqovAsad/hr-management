package uz.hrms.other.dto.dismissalDtos;

import java.time.LocalDate;
import java.util.UUID;

record DismissalListItemResponse(
        UUID id,
        UUID employeeId,
        UUID departmentId,
        DismissalReasonType reasonType,
        LocalDate dismissalDate,
        DismissalStatus status,
        String orderNumber,
        boolean archived
) {
}
