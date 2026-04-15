package uz.hrms.other.dto;

import java.time.LocalDate;
import java.util.UUID;
import uz.hrms.other.enums.DismissalReasonType;
import uz.hrms.other.enums.DismissalStatus;

public record DismissalListItemResponse(
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

