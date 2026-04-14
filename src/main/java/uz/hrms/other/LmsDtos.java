package uz.hrms.other;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

record LmsCourseOptionRequest(
    @NotNull @Min(1) Integer optionOrder,
    @NotBlank @Size(max = 1000) String optionText,
    boolean correct
) {
}

record LmsCourseQuestionRequest(
    @NotNull @Min(1) Integer questionOrder,
    @NotNull LmsQuestionType questionType,
    @NotBlank @Size(max = 2000) String questionText,
    @NotNull @DecimalMin("0.01") BigDecimal points,
    @Valid List<LmsCourseOptionRequest> options
) {
}

record LmsCourseTestRequest(
    @NotBlank @Size(max = 255) String title,
    @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal passScore,
    @NotNull @Min(1) @Max(20) Integer attemptLimit,
    boolean randomizeQuestions,
    @Valid @NotEmpty List<LmsCourseQuestionRequest> questions
) {
}

record LmsCourseLessonRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 1000) String description,
    @NotNull @Min(1) Integer lessonOrder,
    @NotNull LmsLessonContentType contentType,
    @Size(max = 1000) String contentUrl,
    String contentText,
    @Size(max = 500) String storageKey,
    @Size(max = 150) String mimeType,
    @NotNull @Min(0) Integer durationMinutes,
    boolean required,
    @Valid LmsCourseTestRequest test
) {
}

record LmsCourseModuleRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 1000) String description,
    @NotNull @Min(1) Integer moduleOrder,
    boolean required,
    @Valid @NotEmpty List<LmsCourseLessonRequest> lessons
) {
}

record LmsCourseRequirementRequest(
    @NotNull LmsRequirementScopeType scopeType,
    UUID positionId,
    UUID departmentId,
    @NotNull @Min(0) Integer dueDays,
    boolean active
) {
}

record LmsCourseCreateRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 255) String title,
    @Size(max = 2000) String description,
    @Size(max = 100) String category,
    @NotNull LmsCourseLevel courseLevel,
    @NotNull LmsCourseStatus status,
    boolean mandatoryForAll,
    boolean introductoryCourse,
    @NotNull @Min(0) Integer estimatedMinutes,
    boolean certificateEnabled,
    @Size(max = 100) String certificateTemplateCode,
    @Valid @NotEmpty List<LmsCourseModuleRequest> modules,
    @Valid List<LmsCourseRequirementRequest> requirements
) {
}

record LmsAssignmentCreateRequest(
    @NotNull UUID employeeId,
    @NotNull UUID courseId,
    @NotNull LmsAssignmentSource assignmentSource,
    LocalDate dueDate,
    boolean mandatory
) {
}

record LmsTestAnswerRequest(
    @NotNull UUID questionId,
    UUID selectedOptionId,
    @Size(max = 2000) String freeTextAnswer
) {
}

record LmsSubmitTestAttemptRequest(
    @Valid @NotEmpty List<LmsTestAnswerRequest> answers
) {
}

record LmsCourseListItemResponse(
    UUID id,
    String code,
    String title,
    String category,
    LmsCourseLevel courseLevel,
    LmsCourseStatus status,
    boolean mandatoryForAll,
    boolean introductoryCourse,
    Integer estimatedMinutes,
    boolean assigned,
    boolean completed,
    boolean recommended
) {
}

record LmsTestOptionResponse(
    UUID id,
    Integer optionOrder,
    String optionText
) {
}

record LmsTestQuestionResponse(
    UUID id,
    Integer questionOrder,
    LmsQuestionType questionType,
    String questionText,
    BigDecimal points,
    List<LmsTestOptionResponse> options
) {
}

record LmsTestResponse(
    UUID id,
    String title,
    BigDecimal passScore,
    Integer attemptLimit,
    boolean randomizeQuestions,
    List<LmsTestQuestionResponse> questions
) {
}

record LmsLessonResponse(
    UUID id,
    String title,
    String description,
    Integer lessonOrder,
    LmsLessonContentType contentType,
    String contentUrl,
    String contentText,
    String storageKey,
    String mimeType,
    Integer durationMinutes,
    boolean required,
    boolean completed,
    BigDecimal progressPercent,
    LmsTestResponse test
) {
}

record LmsCourseModuleResponse(
    UUID id,
    String title,
    String description,
    Integer moduleOrder,
    boolean required,
    List<LmsLessonResponse> lessons
) {
}

record LmsCourseRequirementResponse(
    UUID id,
    LmsRequirementScopeType scopeType,
    UUID positionId,
    String positionTitle,
    UUID departmentId,
    String departmentName,
    Integer dueDays,
    boolean active
) {
}

record LmsCourseResponse(
    UUID id,
    String code,
    String title,
    String description,
    String category,
    LmsCourseLevel courseLevel,
    LmsCourseStatus status,
    boolean mandatoryForAll,
    boolean introductoryCourse,
    Integer estimatedMinutes,
    boolean certificateEnabled,
    String certificateTemplateCode,
    List<LmsCourseModuleResponse> modules,
    List<LmsCourseRequirementResponse> requirements
) {
}

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

record LmsCertificateResponse(
    UUID id,
    UUID assignmentId,
    UUID employeeId,
    UUID courseId,
    String certificateNumber,
    OffsetDateTime issuedAt,
    String fileName,
    String mimeType,
    LmsCertificateStatus status
) {
}

record LmsAttemptResultResponse(
    UUID attemptId,
    Integer attemptNo,
    BigDecimal score,
    boolean passed,
    LmsTestAttemptStatus status,
    UUID certificateId,
    OffsetDateTime submittedAt
) {
}

record LmsLearningHistoryResponse(
    UUID id,
    String actionType,
    String detailsJson,
    OffsetDateTime actionAt
) {
}

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

record LmsReminderSummaryResponse(
    int assignedCount,
    int reminderCount
) {
}
