package uz.hrms.other.dto.explanationDtos;

import uz.hrms.other.enums.DisciplinaryActionStatus;
import uz.hrms.other.enums.DisciplinaryActionType;

import java.time.LocalDate;
import java.util.UUID;

record DisciplinaryActionResponse(
        UUID id,
        UUID employeeId,
        UUID departmentId,
        UUID explanationIncidentId,
        UUID explanationId,
        DisciplinaryActionType actionType,
        LocalDate actionDate,
        String reasonText,
        DisciplinaryActionStatus status,
        LocalDate validUntil,
        UUID sourceOrderId,
        String sourceOrderNumber
) {
}
