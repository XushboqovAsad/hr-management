package uz.hrms.other.dto.absenceDtos;

import uz.hrms.other.enums.AbsenceStatus;
import uz.hrms.other.enums.AbsenceType;

import java.time.LocalDate;
import java.util.UUID;

public record AbsenceListItemResponse(
        UUID id,
        UUID employeeId,
        AbsenceType absenceType,
        LocalDate startDate,
        LocalDate endDate,
        AbsenceStatus status,
        boolean documentRequired,
        boolean frequentAbsenceMarker
) {
}
