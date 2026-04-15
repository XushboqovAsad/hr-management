package uz.hrms.other.dto.lmsDtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

record LmsReportRowResponse(
        UUID assignmentId,
        UUID employeeId,
        String personnelNumber,
        String employeeFullName,
        UUID departmentId,
        String departmentName,
        UUID positionId,
        String positionTitle,
        UUID courseId,
        String courseCode,
        String courseTitle,
        LmsAssignmentStatus status,
        LocalDate dueDate,
        OffsetDateTime completedAt,
        BigDecimal progressPercent,
        boolean overdue,
        boolean mandatory
) {
}
