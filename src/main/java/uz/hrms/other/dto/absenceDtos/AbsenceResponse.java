package uz.hrms.other.dto.absenceDtos;

import uz.hrms.other.enums.AbsenceStatus;
import uz.hrms.other.enums.AbsenceType;
import uz.hrms.other.enums.PayrollSyncStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AbsenceResponse(
        UUID id,
        UUID employeeId,
        UUID requesterEmployeeId,
        AbsenceType absenceType,
        String reasonText,
        LocalDate startDate,
        LocalDate endDate,
        boolean documentRequired,
        String hrComment,
        AbsenceStatus status,
        PayrollSyncStatus payrollSyncStatus,
        OffsetDateTime approvedAt,
        OffsetDateTime closedAt,
        List<AbsenceDocumentResponse> documents,
        List<AbsenceHistoryResponse> history,
        List<AttendanceDayMarkResponse> attendanceMarks
) {
}
