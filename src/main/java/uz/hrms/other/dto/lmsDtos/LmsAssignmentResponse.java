package uz.hrms.other.dto.lmsDtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

record LmsAssignmentResponse(
        UUID id,
        UUID employeeId,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID currentDepartmentId,
        String currentDepartmentName,
        UUID currentPositionId,
        String currentPositionTitle,
        LmsAssignmentSource assignmentSource,
        LocalDate dueDate,
        OffsetDateTime assignedAt,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        LmsAssignmentStatus status,
        boolean mandatory,
        BigDecimal progressPercent,
        boolean overdue,
        UUID certificateId
) {
}
