package uz.hrms.other;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.hrms.other.entity.BaseEntity;

public final class LmsDomain {
    private LmsDomain() {
    }
}

enum LmsCourseLevel {
    INTRODUCTORY,
    BASIC,
    ADVANCED
}

enum LmsCourseStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED
}

enum LmsLessonContentType {
    VIDEO,
    PDF,
    TEXT,
    LINK,
    TEST
}

enum LmsQuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    TEXT
}

enum LmsRequirementScopeType {
    INTRODUCTORY,
    POSITION,
    DEPARTMENT,
    GLOBAL
}

enum LmsAssignmentSource {
    HIRE,
    POSITION,
    DEPARTMENT,
    GLOBAL,
    MANUAL,
    INTRODUCTORY
}

enum LmsAssignmentStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    OVERDUE,
    CANCELLED
}

enum LmsTestAttemptStatus {
    STARTED,
    SUBMITTED,
    AUTO_FAILED
}

enum LmsCertificateStatus {
    ACTIVE,
    REVOKED
}

@Entity
@Table(schema = "hr", name = "lms_courses")
class LmsCourse extends BaseEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_level", nullable = false, length = 30)
    private LmsCourseLevel courseLevel = LmsCourseLevel.BASIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LmsCourseStatus status = LmsCourseStatus.DRAFT;

    @Column(name = "mandatory_for_all", nullable = false)
    private boolean mandatoryForAll;

    @Column(name = "introductory_course", nullable = false)
    private boolean introductoryCourse;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes = 0;

    @Column(name = "certificate_enabled", nullable = false)
    private boolean certificateEnabled = true;

    @Column(name = "certificate_template_code", length = 100)
    private String certificateTemplateCode;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LmsCourseLevel getCourseLevel() { return courseLevel; }
    public void setCourseLevel(LmsCourseLevel courseLevel) { this.courseLevel = courseLevel; }
    public LmsCourseStatus getStatus() { return status; }
    public void setStatus(LmsCourseStatus status) { this.status = status; }
    public boolean isMandatoryForAll() { return mandatoryForAll; }
    public void setMandatoryForAll(boolean mandatoryForAll) { this.mandatoryForAll = mandatoryForAll; }
    public boolean isIntroductoryCourse() { return introductoryCourse; }
    public void setIntroductoryCourse(boolean introductoryCourse) { this.introductoryCourse = introductoryCourse; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public boolean isCertificateEnabled() { return certificateEnabled; }
    public void setCertificateEnabled(boolean certificateEnabled) { this.certificateEnabled = certificateEnabled; }
    public String getCertificateTemplateCode() { return certificateTemplateCode; }
    public void setCertificateTemplateCode(String certificateTemplateCode) { this.certificateTemplateCode = certificateTemplateCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

@Entity
@Table(schema = "hr", name = "lms_course_modules")
class LmsCourseModule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private LmsCourse course;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "module_order", nullable = false)
    private Integer moduleOrder;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    public LmsCourse getCourse() { return course; }
    public void setCourse(LmsCourse course) { this.course = course; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getModuleOrder() { return moduleOrder; }
    public void setModuleOrder(Integer moduleOrder) { this.moduleOrder = moduleOrder; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}

@Entity
@Table(schema = "hr", name = "lms_lessons")
class LmsLesson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_module_id", nullable = false)
    private LmsCourseModule courseModule;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "lesson_order", nullable = false)
    private Integer lessonOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private LmsLessonContentType contentType;

    @Column(name = "content_url", length = 1000)
    private String contentUrl;

    @Column(name = "content_text")
    private String contentText;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "mime_type", length = 150)
    private String mimeType;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 0;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    public LmsCourseModule getCourseModule() { return courseModule; }
    public void setCourseModule(LmsCourseModule courseModule) { this.courseModule = courseModule; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getLessonOrder() { return lessonOrder; }
    public void setLessonOrder(Integer lessonOrder) { this.lessonOrder = lessonOrder; }
    public LmsLessonContentType getContentType() { return contentType; }
    public void setContentType(LmsLessonContentType contentType) { this.contentType = contentType; }
    public String getContentUrl() { return contentUrl; }
    public void setContentUrl(String contentUrl) { this.contentUrl = contentUrl; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
}

@Entity
@Table(schema = "hr", name = "lms_tests")
class LmsTest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LmsLesson lesson;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "pass_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal passScore;

    @Column(name = "attempt_limit", nullable = false)
    private Integer attemptLimit = 3;

    @Column(name = "randomize_questions", nullable = false)
    private boolean randomizeQuestions;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public LmsLesson getLesson() { return lesson; }
    public void setLesson(LmsLesson lesson) { this.lesson = lesson; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getPassScore() { return passScore; }
    public void setPassScore(BigDecimal passScore) { this.passScore = passScore; }
    public Integer getAttemptLimit() { return attemptLimit; }
    public void setAttemptLimit(Integer attemptLimit) { this.attemptLimit = attemptLimit; }
    public boolean isRandomizeQuestions() { return randomizeQuestions; }
    public void setRandomizeQuestions(boolean randomizeQuestions) { this.randomizeQuestions = randomizeQuestions; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

@Entity
@Table(schema = "hr", name = "lms_test_questions")
class LmsTestQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private LmsTest test;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private LmsQuestionType questionType;

    @Column(name = "question_text", nullable = false, length = 2000)
    private String questionText;

    @Column(name = "points", nullable = false, precision = 8, scale = 2)
    private BigDecimal points = BigDecimal.ONE;

    public LmsTest getTest() { return test; }
    public void setTest(LmsTest test) { this.test = test; }
    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
    public LmsQuestionType getQuestionType() { return questionType; }
    public void setQuestionType(LmsQuestionType questionType) { this.questionType = questionType; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public BigDecimal getPoints() { return points; }
    public void setPoints(BigDecimal points) { this.points = points; }
}

@Entity
@Table(schema = "hr", name = "lms_test_options")
class LmsTestOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private LmsTestQuestion question;

    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    @Column(name = "option_text", nullable = false, length = 1000)
    private String optionText;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    public LmsTestQuestion getQuestion() { return question; }
    public void setQuestion(LmsTestQuestion question) { this.question = question; }
    public Integer getOptionOrder() { return optionOrder; }
    public void setOptionOrder(Integer optionOrder) { this.optionOrder = optionOrder; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}

@Entity
@Table(schema = "hr", name = "lms_course_requirements")
class LmsCourseRequirement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private LmsCourse course;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private LmsRequirementScopeType scopeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "due_days", nullable = false)
    private Integer dueDays = 30;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public LmsCourse getCourse() { return course; }
    public void setCourse(LmsCourse course) { this.course = course; }
    public LmsRequirementScopeType getScopeType() { return scopeType; }
    public void setScopeType(LmsRequirementScopeType scopeType) { this.scopeType = scopeType; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public Integer getDueDays() { return dueDays; }
    public void setDueDays(Integer dueDays) { this.dueDays = dueDays; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

@Entity
@Table(schema = "hr", name = "lms_course_assignments")
class LmsCourseAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private LmsCourse course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_department_id")
    private Department currentDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_position_id")
    private Position currentPosition;

    @Column(name = "assigned_by_user_id")
    private UUID assignedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_source", nullable = false, length = 20)
    private LmsAssignmentSource assignmentSource;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt = OffsetDateTime.now();

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "last_reminder_at")
    private OffsetDateTime lastReminderAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LmsAssignmentStatus status = LmsAssignmentStatus.ASSIGNED;

    @Column(name = "mandatory", nullable = false)
    private boolean mandatory;

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LmsCourse getCourse() { return course; }
    public void setCourse(LmsCourse course) { this.course = course; }
    public Department getCurrentDepartment() { return currentDepartment; }
    public void setCurrentDepartment(Department currentDepartment) { this.currentDepartment = currentDepartment; }
    public Position getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(Position currentPosition) { this.currentPosition = currentPosition; }
    public UUID getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(UUID assignedByUserId) { this.assignedByUserId = assignedByUserId; }
    public LmsAssignmentSource getAssignmentSource() { return assignmentSource; }
    public void setAssignmentSource(LmsAssignmentSource assignmentSource) { this.assignmentSource = assignmentSource; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(OffsetDateTime assignedAt) { this.assignedAt = assignedAt; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public OffsetDateTime getLastReminderAt() { return lastReminderAt; }
    public void setLastReminderAt(OffsetDateTime lastReminderAt) { this.lastReminderAt = lastReminderAt; }
    public LmsAssignmentStatus getStatus() { return status; }
    public void setStatus(LmsAssignmentStatus status) { this.status = status; }
    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }
}

@Entity
@Table(schema = "hr", name = "lms_lesson_progress")
class LmsLessonProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LmsCourseAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LmsLesson lesson;

    @Column(name = "progress_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressPercent = BigDecimal.ZERO;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "last_accessed_at")
    private OffsetDateTime lastAccessedAt;

    public LmsCourseAssignment getAssignment() { return assignment; }
    public void setAssignment(LmsCourseAssignment assignment) { this.assignment = assignment; }
    public LmsLesson getLesson() { return lesson; }
    public void setLesson(LmsLesson lesson) { this.lesson = lesson; }
    public BigDecimal getProgressPercent() { return progressPercent; }
    public void setProgressPercent(BigDecimal progressPercent) { this.progressPercent = progressPercent; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public OffsetDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(OffsetDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
}

@Entity
@Table(schema = "hr", name = "lms_test_attempts")
class LmsTestAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LmsCourseAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private LmsTest test;

    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "passed")
    private Boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LmsTestAttemptStatus status = LmsTestAttemptStatus.STARTED;

    public LmsCourseAssignment getAssignment() { return assignment; }
    public void setAssignment(LmsCourseAssignment assignment) { this.assignment = assignment; }
    public LmsTest getTest() { return test; }
    public void setTest(LmsTest test) { this.test = test; }
    public Integer getAttemptNo() { return attemptNo; }
    public void setAttemptNo(Integer attemptNo) { this.attemptNo = attemptNo; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(OffsetDateTime submittedAt) { this.submittedAt = submittedAt; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public LmsTestAttemptStatus getStatus() { return status; }
    public void setStatus(LmsTestAttemptStatus status) { this.status = status; }
}

@Entity
@Table(schema = "hr", name = "lms_test_attempt_answers")
class LmsTestAttemptAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private LmsTestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private LmsTestQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private LmsTestOption selectedOption;

    @Column(name = "free_text_answer", length = 2000)
    private String freeTextAnswer;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "points_awarded", precision = 8, scale = 2)
    private BigDecimal pointsAwarded;

    public LmsTestAttempt getAttempt() { return attempt; }
    public void setAttempt(LmsTestAttempt attempt) { this.attempt = attempt; }
    public LmsTestQuestion getQuestion() { return question; }
    public void setQuestion(LmsTestQuestion question) { this.question = question; }
    public LmsTestOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(LmsTestOption selectedOption) { this.selectedOption = selectedOption; }
    public String getFreeTextAnswer() { return freeTextAnswer; }
    public void setFreeTextAnswer(String freeTextAnswer) { this.freeTextAnswer = freeTextAnswer; }
    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }
    public BigDecimal getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(BigDecimal pointsAwarded) { this.pointsAwarded = pointsAwarded; }
}

@Entity
@Table(schema = "hr", name = "lms_certificates")
class LmsCertificate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private LmsCourseAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private LmsCourse course;

    @Column(name = "certificate_number", nullable = false, length = 100)
    private String certificateNumber;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt = OffsetDateTime.now();

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "mime_type", nullable = false, length = 150)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LmsCertificateStatus status = LmsCertificateStatus.ACTIVE;

    public LmsCourseAssignment getAssignment() { return assignment; }
    public void setAssignment(LmsCourseAssignment assignment) { this.assignment = assignment; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LmsCourse getCourse() { return course; }
    public void setCourse(LmsCourse course) { this.course = course; }
    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    public OffsetDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(OffsetDateTime issuedAt) { this.issuedAt = issuedAt; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public LmsCertificateStatus getStatus() { return status; }
    public void setStatus(LmsCertificateStatus status) { this.status = status; }
}

@Entity
@Table(schema = "hr", name = "lms_learning_history")
class LmsLearningHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private LmsCourseAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private LmsCourse course;

    @Column(name = "action_type", nullable = false, length = 40)
    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json")
    private String detailsJson;

    @Column(name = "action_at", nullable = false)
    private OffsetDateTime actionAt = OffsetDateTime.now();

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public LmsCourseAssignment getAssignment() { return assignment; }
    public void setAssignment(LmsCourseAssignment assignment) { this.assignment = assignment; }
    public LmsCourse getCourse() { return course; }
    public void setCourse(LmsCourse course) { this.course = course; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
    public OffsetDateTime getActionAt() { return actionAt; }
    public void setActionAt(OffsetDateTime actionAt) { this.actionAt = actionAt; }
}

interface LmsCourseRepository extends JpaRepository<LmsCourse, UUID> {
    Optional<LmsCourse> findByIdAndDeletedFalse(UUID id);
    Optional<LmsCourse> findByCodeIgnoreCaseAndDeletedFalse(String code);
    List<LmsCourse> findAllByDeletedFalseOrderByTitleAsc();
    List<LmsCourse> findAllByDeletedFalseAndStatusOrderByTitleAsc(LmsCourseStatus status);

    @Query(
        "select c from LmsCourse c where c.deleted = false and (:query is null or lower(c.title) like lower(concat('%', :query, '%')) " +
            "or lower(c.code) like lower(concat('%', :query, '%')) or lower(coalesce(c.category, '')) like lower(concat('%', :query, '%'))) " +
            "and (:status is null or c.status = :status) order by c.title asc"
    )
    List<LmsCourse> search(@Param("query") String query, @Param("status") LmsCourseStatus status);
}

interface LmsCourseModuleRepository extends JpaRepository<LmsCourseModule, UUID> {
    List<LmsCourseModule> findAllByCourseIdAndDeletedFalseOrderByModuleOrderAsc(UUID courseId);
}

interface LmsLessonRepository extends JpaRepository<LmsLesson, UUID> {
    Optional<LmsLesson> findByIdAndDeletedFalse(UUID id);
    List<LmsLesson> findAllByCourseModuleIdInAndDeletedFalseOrderByLessonOrderAsc(Collection<UUID> moduleIds);
    List<LmsLesson> findAllByCourseModuleIdAndDeletedFalseOrderByLessonOrderAsc(UUID moduleId);
}

interface LmsTestRepository extends JpaRepository<LmsTest, UUID> {
    Optional<LmsTest> findByIdAndDeletedFalse(UUID id);
    Optional<LmsTest> findByLessonIdAndDeletedFalse(UUID lessonId);
    List<LmsTest> findAllByLessonIdInAndDeletedFalse(Collection<UUID> lessonIds);
}

interface LmsTestQuestionRepository extends JpaRepository<LmsTestQuestion, UUID> {
    List<LmsTestQuestion> findAllByTestIdAndDeletedFalseOrderByQuestionOrderAsc(UUID testId);
    List<LmsTestQuestion> findAllByTestIdInAndDeletedFalseOrderByQuestionOrderAsc(Collection<UUID> testIds);
}

interface LmsTestOptionRepository extends JpaRepository<LmsTestOption, UUID> {
    List<LmsTestOption> findAllByQuestionIdAndDeletedFalseOrderByOptionOrderAsc(UUID questionId);
    List<LmsTestOption> findAllByQuestionIdInAndDeletedFalseOrderByOptionOrderAsc(Collection<UUID> questionIds);
}

interface LmsCourseRequirementRepository extends JpaRepository<LmsCourseRequirement, UUID> {
    List<LmsCourseRequirement> findAllByCourseIdAndDeletedFalseOrderByCreatedAtAsc(UUID courseId);
    List<LmsCourseRequirement> findAllByActiveTrueAndDeletedFalse();
}

interface LmsCourseAssignmentRepository extends JpaRepository<LmsCourseAssignment, UUID> {
    Optional<LmsCourseAssignment> findByIdAndDeletedFalse(UUID id);

    @Query(
        "select a from LmsCourseAssignment a join fetch a.course c left join fetch a.currentDepartment d left join fetch a.currentPosition p " +
            "where a.deleted = false and (:employeeId is null or a.employee.id = :employeeId) " +
            "and (:status is null or a.status = :status) " +
            "and (:departmentId is null or d.id = :departmentId) " +
            "and (:positionId is null or p.id = :positionId) " +
            "and (:dueBefore is null or a.dueDate <= :dueBefore) order by a.assignedAt desc"
    )
    List<LmsCourseAssignment> search(
        @Param("employeeId") UUID employeeId,
        @Param("status") LmsAssignmentStatus status,
        @Param("departmentId") UUID departmentId,
        @Param("positionId") UUID positionId,
        @Param("dueBefore") LocalDate dueBefore
    );

    @Query(
        "select a from LmsCourseAssignment a join fetch a.course c where a.deleted = false and a.employee.id = :employeeId order by a.assignedAt desc"
    )
    List<LmsCourseAssignment> findAllByEmployeeIdAndDeletedFalseOrderByAssignedAtDesc(@Param("employeeId") UUID employeeId);

    @Query(
        "select case when count(a) > 0 then true else false end from LmsCourseAssignment a where a.deleted = false and a.employee.id = :employeeId and a.course.id = :courseId and a.status in :statuses"
    )
    boolean existsByEmployeeIdAndCourseIdAndStatuses(
        @Param("employeeId") UUID employeeId,
        @Param("courseId") UUID courseId,
        @Param("statuses") Collection<LmsAssignmentStatus> statuses
    );

    List<LmsCourseAssignment> findAllByStatusInAndDueDateBeforeAndDeletedFalse(Collection<LmsAssignmentStatus> statuses, LocalDate dueDate);
}

interface LmsLessonProgressRepository extends JpaRepository<LmsLessonProgress, UUID> {
    Optional<LmsLessonProgress> findByAssignmentIdAndLessonIdAndDeletedFalse(UUID assignmentId, UUID lessonId);
    List<LmsLessonProgress> findAllByAssignmentIdAndDeletedFalseOrderByCreatedAtAsc(UUID assignmentId);
}

interface LmsTestAttemptRepository extends JpaRepository<LmsTestAttempt, UUID> {
    List<LmsTestAttempt> findAllByAssignmentIdAndTestIdAndDeletedFalseOrderByAttemptNoDesc(UUID assignmentId, UUID testId);
}

interface LmsTestAttemptAnswerRepository extends JpaRepository<LmsTestAttemptAnswer, UUID> {
    List<LmsTestAttemptAnswer> findAllByAttemptIdAndDeletedFalse(UUID attemptId);
}

interface LmsCertificateRepository extends JpaRepository<LmsCertificate, UUID> {
    Optional<LmsCertificate> findByIdAndDeletedFalse(UUID id);
    Optional<LmsCertificate> findByAssignmentIdAndDeletedFalse(UUID assignmentId);
}

interface LmsLearningHistoryRepository extends JpaRepository<LmsLearningHistory, UUID> {
    List<LmsLearningHistory> findAllByEmployeeIdAndDeletedFalseOrderByActionAtDesc(UUID employeeId);
}

interface LmsEmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByIdAndDeletedFalse(UUID id);

    @Query("select e from Employee e left join fetch e.user where e.deleted = false and e.employmentStatus in ('ACTIVE', 'ONBOARDING')")
    List<Employee> findAllForLearningSync();
}

interface LmsEmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, UUID> {
    @Query(
        "select a from EmployeeAssignment a left join fetch a.department left join fetch a.position where a.deleted = false and a.employee.id = :employeeId and a.startedAt <= :today and (a.endedAt is null or a.endedAt >= :today) order by a.startedAt desc"
    )
    List<EmployeeAssignment> findCurrentAssignments(@Param("employeeId") UUID employeeId, @Param("today") LocalDate today);

    @Query(
        "select a from EmployeeAssignment a left join fetch a.department left join fetch a.position where a.deleted = false and a.startedAt <= :today and (a.endedAt is null or a.endedAt >= :today) order by a.employee.id asc, a.startedAt desc"
    )
    List<EmployeeAssignment> findAllCurrentAssignments(@Param("today") LocalDate today);
}
