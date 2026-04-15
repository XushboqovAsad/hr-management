package uz.hrms.other.dto.lmsDtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

record LmsAssignmentCreateRequest(
        @NotNull UUID employeeId,
        @NotNull UUID courseId,
        @NotNull LmsAssignmentSource assignmentSource,
        LocalDate dueDate,
        boolean mandatory
) {
}
