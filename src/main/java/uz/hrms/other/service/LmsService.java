package uz.hrms.other.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.LocalFileStorageService;
import uz.hrms.other.StoredFileDescriptor;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.entity.*;
import uz.hrms.other.repository.AuditLogRepository;
import uz.hrms.other.repository.*;

@Service
@Transactional
class LmsService {

    private final LmsCourseRepository courseRepository;
    private final LmsCourseModuleRepository courseModuleRepository;
    private final LmsLessonRepository lessonRepository;
    private final LmsTestRepository testRepository;
    private final LmsTestQuestionRepository testQuestionRepository;
    private final LmsTestOptionRepository testOptionRepository;
    private final LmsCourseRequirementRepository courseRequirementRepository;
    private final LmsCourseAssignmentRepository courseAssignmentRepository;
    private final LmsLessonProgressRepository lessonProgressRepository;
    private final LmsTestAttemptRepository testAttemptRepository;
    private final LmsTestAttemptAnswerRepository testAttemptAnswerRepository;
    private final LmsCertificateRepository certificateRepository;
    private final LmsLearningHistoryRepository learningHistoryRepository;
    private final LmsEmployeeRepository employeeRepository;
    private final LmsEmployeeAssignmentRepository employeeAssignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final HrNotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final LocalFileStorageService localFileStorageService;
    private final ObjectMapper objectMapper;

    LmsService(
        LmsCourseRepository courseRepository,
        LmsCourseModuleRepository courseModuleRepository,
        LmsLessonRepository lessonRepository,
        LmsTestRepository testRepository,
        LmsTestQuestionRepository testQuestionRepository,
        LmsTestOptionRepository testOptionRepository,
        LmsCourseRequirementRepository courseRequirementRepository,
        LmsCourseAssignmentRepository courseAssignmentRepository,
        LmsLessonProgressRepository lessonProgressRepository,
        LmsTestAttemptRepository testAttemptRepository,
        LmsTestAttemptAnswerRepository testAttemptAnswerRepository,
        LmsCertificateRepository certificateRepository,
        LmsLearningHistoryRepository learningHistoryRepository,
        LmsEmployeeRepository employeeRepository,
        LmsEmployeeAssignmentRepository employeeAssignmentRepository,
        DepartmentRepository departmentRepository,
        PositionRepository positionRepository,
        HrNotificationRepository notificationRepository,
        AuditLogRepository auditLogRepository,
        LocalFileStorageService localFileStorageService,
        ObjectMapper objectMapper
    ) {
        this.courseRepository = courseRepository;
        this.courseModuleRepository = courseModuleRepository;
        this.lessonRepository = lessonRepository;
        this.testRepository = testRepository;
        this.testQuestionRepository = testQuestionRepository;
        this.testOptionRepository = testOptionRepository;
        this.courseRequirementRepository = courseRequirementRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.testAttemptRepository = testAttemptRepository;
        this.testAttemptAnswerRepository = testAttemptAnswerRepository;
        this.certificateRepository = certificateRepository;
        this.learningHistoryRepository = learningHistoryRepository;
        this.employeeRepository = employeeRepository;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.localFileStorageService = localFileStorageService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    List<LmsCourseListItemResponse> listCourses(String query, LmsCourseStatus status, UUID employeeId) {
        String normalizedQuery = trimToNull(query);
        List<LmsCourse> courses = normalizedQuery == null
            ? (status == null ? courseRepository.findAllByDeletedFalseOrderByTitleAsc() : courseRepository.findAllByDeletedFalseAndStatusOrderByTitleAsc(status))
            : courseRepository.search(normalizedQuery, status);
        Set<UUID> assignedCourseIds = Set.of();
        Set<UUID> completedCourseIds = Set.of();
        Set<UUID> recommendedCourseIds = Set.of();
        if (employeeId != null) {
            List<LmsCourseAssignment> assignments = courseAssignmentRepository.findAllByEmployeeIdAndDeletedFalseOrderByAssignedAtDesc(employeeId);
            assignedCourseIds = assignments.stream().map(item -> item.getCourse().getId()).collect(Collectors.toSet());
            completedCourseIds = assignments.stream()
                .filter(item -> item.getStatus() == LmsAssignmentStatus.COMPLETED)
                .map(item -> item.getCourse().getId())
                .collect(Collectors.toSet());
            EmployeeAssignment currentAssignment = currentAssignment(employeeId).orElse(null);
            recommendedCourseIds = recommendedCourseIds(currentAssignment);
        }
        Set<UUID> finalAssignedCourseIds = assignedCourseIds;
        Set<UUID> finalCompletedCourseIds = completedCourseIds;
        Set<UUID> finalRecommendedCourseIds = recommendedCourseIds;
        return courses.stream()
            .map(course -> new LmsCourseListItemResponse(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getCategory(),
                course.getCourseLevel(),
                course.getStatus(),
                course.isMandatoryForAll(),
                course.isIntroductoryCourse(),
                course.getEstimatedMinutes(),
                finalAssignedCourseIds.contains(course.getId()),
                finalCompletedCourseIds.contains(course.getId()),
                finalRecommendedCourseIds.contains(course.getId())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    LmsCourseResponse getCourse(UUID courseId, UUID assignmentId) {
        LmsCourse course = getCourseEntity(courseId);
        Map<UUID, LmsLessonProgress> progressMap = assignmentId == null
            ? Map.of()
            : lessonProgressRepository.findAllByAssignmentIdAndDeletedFalseOrderByCreatedAtAsc(assignmentId)
                .stream()
                .collect(Collectors.toMap(item -> item.getLesson().getId(), Function.identity(), (left, right) -> right, LinkedHashMap::new));
        return toCourseResponse(course, progressMap);
    }

    LmsCourseResponse createCourse(LmsCourseCreateRequest request) {
        if (courseRepository.findByCodeIgnoreCaseAndDeletedFalse(request.code()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course code already exists");
        }
        validateCourseRequest(request);
        LmsCourse course = new LmsCourse();
        course.setCode(request.code().trim());
        course.setTitle(request.title().trim());
        course.setDescription(trimToNull(request.description()));
        course.setCategory(trimToNull(request.category()));
        course.setCourseLevel(request.courseLevel());
        course.setStatus(request.status());
        course.setMandatoryForAll(request.mandatoryForAll());
        course.setIntroductoryCourse(request.introductoryCourse());
        course.setEstimatedMinutes(request.estimatedMinutes());
        course.setCertificateEnabled(request.certificateEnabled());
        course.setCertificateTemplateCode(trimToNull(request.certificateTemplateCode()));
        course.setActive(true);
        LmsCourse saved = courseRepository.save(course);
        persistCourseStructure(saved, request.modules(), request.requirements());
        writeAudit("LMS_COURSE_CREATED", saved.getId(), null, courseJson(saved));
        return getCourse(saved.getId(), null);
    }

    LmsAssignmentResponse assignCourse(LmsAssignmentCreateRequest request, UUID actorUserId) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(request.employeeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        LmsCourse course = getCourseEntity(request.courseId());
        if (course.getStatus() != LmsCourseStatus.PUBLISHED || course.isActive() == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only published active courses can be assigned");
        }
        if (courseAssignmentRepository.existsByEmployeeIdAndCourseIdAndStatuses(
            employee.getId(),
            course.getId(),
            List.of(LmsAssignmentStatus.ASSIGNED, LmsAssignmentStatus.IN_PROGRESS, LmsAssignmentStatus.COMPLETED, LmsAssignmentStatus.OVERDUE, LmsAssignmentStatus.FAILED)
        )) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course already assigned to employee");
        }
        EmployeeAssignment currentAssignment = currentAssignment(employee.getId()).orElse(null);
        LmsCourseAssignment assignment = new LmsCourseAssignment();
        assignment.setEmployee(employee);
        assignment.setCourse(course);
        assignment.setAssignedByUserId(actorUserId);
        assignment.setAssignmentSource(request.assignmentSource());
        assignment.setDueDate(request.dueDate());
        assignment.setMandatory(request.mandatory());
        if (currentAssignment != null) {
            assignment.setCurrentDepartment(currentAssignment.getDepartment());
            assignment.setCurrentPosition(currentAssignment.getPosition());
        }
        LmsCourseAssignment saved = courseAssignmentRepository.save(assignment);
        writeHistory(employee, saved, course, "COURSE_ASSIGNED", Map.of("source", request.assignmentSource().name(), "dueDate", String.valueOf(request.dueDate())));
        notifyEmployee(employee, "LMS_ASSIGNMENT", "Назначен курс", "Вам назначен курс: " + course.getTitle(), course.getId(), saved.getId());
        writeAudit("LMS_ASSIGNMENT_CREATED", saved.getId(), null, assignmentJson(saved));
        return toAssignmentResponse(saved);
    }

    @Transactional(readOnly = true)
    List<LmsAssignmentResponse> listAssignments(UUID employeeId, LmsAssignmentStatus status, UUID departmentId, UUID positionId, LocalDate dueBefore) {
        return courseAssignmentRepository.search(employeeId, status, departmentId, positionId, dueBefore)
            .stream()
            .map(this::toAssignmentResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    LmsAssignmentResponse getAssignment(UUID assignmentId) {
        return toAssignmentResponse(getAssignmentEntity(assignmentId));
    }

    @Transactional(readOnly = true)
    UUID assignmentEmployeeId(UUID assignmentId) {
        return getAssignmentEntity(assignmentId).getEmployee().getId();
    }

    LmsAssignmentResponse startAssignment(UUID assignmentId) {
        LmsCourseAssignment assignment = getAssignmentEntity(assignmentId);
        if (assignment.getStatus() == LmsAssignmentStatus.COMPLETED || assignment.getStatus() == LmsAssignmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment is not active");
        }
        if (assignment.getStartedAt() == null) {
            assignment.setStartedAt(OffsetDateTime.now());
        }
        if (assignment.getStatus() == LmsAssignmentStatus.ASSIGNED || assignment.getStatus() == LmsAssignmentStatus.OVERDUE || assignment.getStatus() == LmsAssignmentStatus.FAILED) {
            assignment.setStatus(LmsAssignmentStatus.IN_PROGRESS);
        }
        courseAssignmentRepository.save(assignment);
        writeHistory(assignment.getEmployee(), assignment, assignment.getCourse(), "COURSE_STARTED", Map.of());
        writeAudit("LMS_ASSIGNMENT_STARTED", assignment.getId(), null, assignmentJson(assignment));
        return toAssignmentResponse(assignment);
    }

    LmsAssignmentResponse completeLesson(UUID assignmentId, UUID lessonId) {
        LmsCourseAssignment assignment = getAssignmentEntity(assignmentId);
        LmsLesson lesson = lessonRepository.findByIdAndDeletedFalse(lessonId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        if (lesson.getCourseModule().getCourse().getId().equals(assignment.getCourse().getId()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson does not belong to assignment course");
        }
        if (lesson.getContentType() == LmsLessonContentType.TEST && testRepository.findByLessonIdAndDeletedFalse(lessonId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use test submit endpoint for test lesson");
        }
        markLessonCompleted(assignment, lesson);
        writeHistory(assignment.getEmployee(), assignment, assignment.getCourse(), "LESSON_COMPLETED", Map.of("lessonId", lesson.getId().toString(), "lessonTitle", lesson.getTitle()));
        writeAudit("LMS_LESSON_COMPLETED", assignment.getId(), null, lesson.getId().toString());
        completeAssignmentIfEligible(assignment);
        return toAssignmentResponse(assignment);
    }

    LmsAttemptResultResponse submitTest(UUID assignmentId, UUID testId, LmsSubmitTestAttemptRequest request) {
        LmsCourseAssignment assignment = getAssignmentEntity(assignmentId);
        LmsTest test = testRepository.findByIdAndDeletedFalse(testId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found"));
        if (test.getLesson().getCourseModule().getCourse().getId().equals(assignment.getCourse().getId()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Test does not belong to assignment course");
        }
        List<LmsTestAttempt> previousAttempts = testAttemptRepository.findAllByAssignmentIdAndTestIdAndDeletedFalseOrderByAttemptNoDesc(assignmentId, testId);
        int nextAttemptNo = previousAttempts.isEmpty() ? 1 : previousAttempts.get(0).getAttemptNo() + 1;
        if (nextAttemptNo > test.getAttemptLimit()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attempt limit exceeded");
        }
        LmsTestAttempt attempt = new LmsTestAttempt();
        attempt.setAssignment(assignment);
        attempt.setTest(test);
        attempt.setAttemptNo(nextAttemptNo);
        attempt = testAttemptRepository.save(attempt);

        List<LmsTestQuestion> questions = testQuestionRepository.findAllByTestIdAndDeletedFalseOrderByQuestionOrderAsc(testId);
        Map<UUID, LmsTestQuestion> questionsById = questions.stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        Map<UUID, List<LmsTestAnswerRequest>> answersByQuestion = request.answers().stream().collect(Collectors.groupingBy(LmsTestAnswerRequest::questionId));
        Map<UUID, List<LmsTestOption>> optionsByQuestion = testOptionRepository.findAllByQuestionIdInAndDeletedFalseOrderByOptionOrderAsc(questions.stream().map(BaseEntity::getId).toList())
            .stream()
            .collect(Collectors.groupingBy(item -> item.getQuestion().getId()));

        BigDecimal totalPoints = questions.stream().map(LmsTestQuestion::getPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal earnedPoints = BigDecimal.ZERO;
        List<LmsTestAttemptAnswer> storedAnswers = new ArrayList<>();

        for (LmsTestQuestion question : questions) {
            List<LmsTestAnswerRequest> submitted = answersByQuestion.getOrDefault(question.getId(), List.of());
            EvaluationResult evaluation = evaluateQuestion(question, submitted, optionsByQuestion.getOrDefault(question.getId(), List.of()));
            earnedPoints = earnedPoints.add(evaluation.pointsAwarded());
            if (submitted.isEmpty()) {
                LmsTestAttemptAnswer emptyAnswer = new LmsTestAttemptAnswer();
                emptyAnswer.setAttempt(attempt);
                emptyAnswer.setQuestion(question);
                emptyAnswer.setCorrect(false);
                emptyAnswer.setPointsAwarded(BigDecimal.ZERO);
                storedAnswers.add(emptyAnswer);
            } else {
                for (LmsTestAnswerRequest answerRequest : submitted) {
                    LmsTestAttemptAnswer answer = new LmsTestAttemptAnswer();
                    answer.setAttempt(attempt);
                    answer.setQuestion(question);
                    if (answerRequest.selectedOptionId() != null) {
                        optionsByQuestion.getOrDefault(question.getId(), List.of()).stream()
                            .filter(option -> option.getId().equals(answerRequest.selectedOptionId()))
                            .findFirst()
                            .ifPresent(answer::setSelectedOption);
                    }
                    answer.setFreeTextAnswer(trimToNull(answerRequest.freeTextAnswer()));
                    answer.setCorrect(evaluation.correct());
                    answer.setPointsAwarded(evaluation.pointsAwarded());
                    storedAnswers.add(answer);
                }
            }
        }
        testAttemptAnswerRepository.saveAll(storedAnswers);

        BigDecimal score = totalPoints.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : earnedPoints.multiply(BigDecimal.valueOf(100)).divide(totalPoints, 2, RoundingMode.HALF_UP);
        boolean passed = score.compareTo(test.getPassScore()) >= 0;
        attempt.setSubmittedAt(OffsetDateTime.now());
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setStatus(LmsTestAttemptStatus.SUBMITTED);
        testAttemptRepository.save(attempt);

        if (passed) {
            markLessonCompleted(assignment, test.getLesson());
        } else if (nextAttemptNo >= test.getAttemptLimit()) {
            assignment.setStatus(LmsAssignmentStatus.FAILED);
            courseAssignmentRepository.save(assignment);
        }

        writeHistory(assignment.getEmployee(), assignment, assignment.getCourse(), "TEST_SUBMITTED", Map.of(
            "testId", test.getId().toString(),
            "score", score.toPlainString(),
            "passed", String.valueOf(passed)
        ));
        writeAudit("LMS_TEST_SUBMITTED", assignment.getId(), null, score.toPlainString());
        completeAssignmentIfEligible(assignment);
        UUID certificateId = certificateRepository.findByAssignmentIdAndDeletedFalse(assignment.getId()).map(BaseEntity::getId).orElse(null);
        return new LmsAttemptResultResponse(attempt.getId(), attempt.getAttemptNo(), attempt.getScore(), Boolean.TRUE.equals(attempt.getPassed()), attempt.getStatus(), certificateId, attempt.getSubmittedAt());
    }

    @Transactional(readOnly = true)
    List<LmsLearningHistoryResponse> history(UUID employeeId) {
        return learningHistoryRepository.findAllByEmployeeIdAndDeletedFalseOrderByActionAtDesc(employeeId).stream()
            .map(item -> new LmsLearningHistoryResponse(item.getId(), item.getActionType(), item.getDetailsJson(), item.getActionAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    List<LmsReportRowResponse> report(UUID employeeId, UUID departmentId, UUID positionId, LmsAssignmentStatus status, LocalDate dueBefore) {
        return courseAssignmentRepository.search(employeeId, status, departmentId, positionId, dueBefore)
            .stream()
            .map(this::toReportRow)
            .toList();
    }

    @Transactional(readOnly = true)
    LmsCertificate getCertificateEntity(UUID assignmentId) {
        return certificateRepository.findByAssignmentIdAndDeletedFalse(assignmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
    }

    LmsReminderSummaryResponse syncMandatoryAssignments() {
        int assignedCount = 0;
        LocalDate today = LocalDate.now();
        Map<UUID, EmployeeAssignment> currentAssignments = employeeAssignmentRepository.findAllCurrentAssignments(today)
            .stream()
            .collect(Collectors.toMap(item -> item.getEmployee().getId(), Function.identity(), (left, right) -> left.getStartedAt().isAfter(right.getStartedAt()) ? left : right, LinkedHashMap::new));

        List<LmsCourse> activeCourses = courseRepository.findAllByDeletedFalseAndStatusOrderByTitleAsc(LmsCourseStatus.PUBLISHED).stream()
            .filter(LmsCourse::isActive)
            .toList();
        List<LmsCourseRequirement> requirements = courseRequirementRepository.findAllByActiveTrueAndDeletedFalse();
        Map<UUID, List<LmsCourseRequirement>> requirementsByCourse = requirements.stream().collect(Collectors.groupingBy(item -> item.getCourse().getId()));

        for (Employee employee : employeeRepository.findAllForLearningSync()) {
            EmployeeAssignment currentAssignment = currentAssignments.get(employee.getId());
            Set<UUID> requiredCourseIds = new LinkedHashSet<>();
            for (LmsCourse course : activeCourses) {
                if (course.isMandatoryForAll() || course.isIntroductoryCourse()) {
                    requiredCourseIds.add(course.getId());
                }
                for (LmsCourseRequirement requirement : requirementsByCourse.getOrDefault(course.getId(), List.of())) {
                    if (matchesRequirement(requirement, currentAssignment)) {
                        requiredCourseIds.add(course.getId());
                    }
                }
            }
            for (UUID courseId : requiredCourseIds) {
                if (courseAssignmentRepository.existsByEmployeeIdAndCourseIdAndStatuses(
                    employee.getId(),
                    courseId,
                    List.of(LmsAssignmentStatus.ASSIGNED, LmsAssignmentStatus.IN_PROGRESS, LmsAssignmentStatus.COMPLETED, LmsAssignmentStatus.OVERDUE, LmsAssignmentStatus.FAILED)
                )) {
                    continue;
                }
                LmsCourse course = activeCourses.stream().filter(item -> item.getId().equals(courseId)).findFirst().orElse(null);
                if (course == null) {
                    continue;
                }
                LmsCourseRequirement matchedRequirement = requirementsByCourse.getOrDefault(courseId, List.of()).stream()
                    .filter(item -> matchesRequirement(item, currentAssignment))
                    .findFirst()
                    .orElse(null);
                LmsCourseAssignment assignment = new LmsCourseAssignment();
                assignment.setEmployee(employee);
                assignment.setCourse(course);
                assignment.setMandatory(true);
                assignment.setAssignmentSource(resolveSource(course, matchedRequirement));
                assignment.setDueDate(today.plusDays(matchedRequirement == null ? defaultDueDays(course) : matchedRequirement.getDueDays()));
                if (currentAssignment != null) {
                    assignment.setCurrentDepartment(currentAssignment.getDepartment());
                    assignment.setCurrentPosition(currentAssignment.getPosition());
                }
                courseAssignmentRepository.save(assignment);
                writeHistory(employee, assignment, course, "AUTO_ASSIGNED", Map.of("source", assignment.getAssignmentSource().name()));
                notifyEmployee(employee, "LMS_ASSIGNMENT", "Обязательный курс", "Вам автоматически назначен обязательный курс: " + course.getTitle(), course.getId(), assignment.getId());
                assignedCount++;
            }
        }
        writeAudit("LMS_MANDATORY_SYNC", null, null, "assigned=" + assignedCount);
        return new LmsReminderSummaryResponse(assignedCount, 0);
    }

    LmsReminderSummaryResponse sendOverdueReminders() {
        int reminderCount = 0;
        LocalDate today = LocalDate.now();
        for (LmsCourseAssignment assignment : courseAssignmentRepository.findAllByStatusInAndDueDateBeforeAndDeletedFalse(
            List.of(LmsAssignmentStatus.ASSIGNED, LmsAssignmentStatus.IN_PROGRESS, LmsAssignmentStatus.FAILED, LmsAssignmentStatus.OVERDUE),
            today
        )) {
            if (assignment.getLastReminderAt() != null && assignment.getLastReminderAt().toLocalDate().isEqual(today)) {
                continue;
            }
            assignment.setStatus(LmsAssignmentStatus.OVERDUE);
            assignment.setLastReminderAt(OffsetDateTime.now());
            courseAssignmentRepository.save(assignment);
            notifyEmployee(assignment.getEmployee(), "LMS_REMINDER", "Непройденный курс", "Просрочен срок прохождения курса: " + assignment.getCourse().getTitle(), assignment.getCourse().getId(), assignment.getId());
            writeHistory(assignment.getEmployee(), assignment, assignment.getCourse(), "REMINDER_SENT", Map.of("dueDate", String.valueOf(assignment.getDueDate())));
            reminderCount++;
        }
        writeAudit("LMS_REMINDERS_SENT", null, null, "reminders=" + reminderCount);
        return new LmsReminderSummaryResponse(0, reminderCount);
    }

    private void persistCourseStructure(LmsCourse course, List<LmsCourseModuleRequest> modules, List<LmsCourseRequirementRequest> requirements) {
        for (LmsCourseRequirementRequest requirementRequest : requirements == null ? List.<LmsCourseRequirementRequest>of() : requirements) {
            LmsCourseRequirement requirement = new LmsCourseRequirement();
            requirement.setCourse(course);
            requirement.setScopeType(requirementRequest.scopeType());
            requirement.setDueDays(requirementRequest.dueDays());
            requirement.setActive(requirementRequest.active());
            if (requirementRequest.positionId() != null) {
                requirement.setPosition(positionRepository.findByIdAndDeletedFalse(requirementRequest.positionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Position not found")));
            }
            if (requirementRequest.departmentId() != null) {
                requirement.setDepartment(departmentRepository.findByIdAndDeletedFalse(requirementRequest.departmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found")));
            }
            courseRequirementRepository.save(requirement);
        }

        for (LmsCourseModuleRequest moduleRequest : modules) {
            LmsCourseModule module = new LmsCourseModule();
            module.setCourse(course);
            module.setTitle(moduleRequest.title().trim());
            module.setDescription(trimToNull(moduleRequest.description()));
            module.setModuleOrder(moduleRequest.moduleOrder());
            module.setRequired(moduleRequest.required());
            LmsCourseModule savedModule = courseModuleRepository.save(module);
            for (LmsCourseLessonRequest lessonRequest : moduleRequest.lessons()) {
                if (lessonRequest.contentType() == LmsLessonContentType.TEST && lessonRequest.test() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Test lesson requires test definition");
                }
                LmsLesson lesson = new LmsLesson();
                lesson.setCourseModule(savedModule);
                lesson.setTitle(lessonRequest.title().trim());
                lesson.setDescription(trimToNull(lessonRequest.description()));
                lesson.setLessonOrder(lessonRequest.lessonOrder());
                lesson.setContentType(lessonRequest.contentType());
                lesson.setContentUrl(trimToNull(lessonRequest.contentUrl()));
                lesson.setContentText(trimToNull(lessonRequest.contentText()));
                lesson.setStorageKey(trimToNull(lessonRequest.storageKey()));
                lesson.setMimeType(trimToNull(lessonRequest.mimeType()));
                lesson.setDurationMinutes(lessonRequest.durationMinutes());
                lesson.setRequired(lessonRequest.required());
                LmsLesson savedLesson = lessonRepository.save(lesson);
                if (lessonRequest.test() != null) {
                    LmsTest test = new LmsTest();
                    test.setLesson(savedLesson);
                    test.setTitle(lessonRequest.test().title().trim());
                    test.setPassScore(lessonRequest.test().passScore());
                    test.setAttemptLimit(lessonRequest.test().attemptLimit());
                    test.setRandomizeQuestions(lessonRequest.test().randomizeQuestions());
                    LmsTest savedTest = testRepository.save(test);
                    for (LmsCourseQuestionRequest questionRequest : lessonRequest.test().questions()) {
                        LmsTestQuestion question = new LmsTestQuestion();
                        question.setTest(savedTest);
                        question.setQuestionOrder(questionRequest.questionOrder());
                        question.setQuestionType(questionRequest.questionType());
                        question.setQuestionText(questionRequest.questionText().trim());
                        question.setPoints(questionRequest.points());
                        LmsTestQuestion savedQuestion = testQuestionRepository.save(question);
                        for (LmsCourseOptionRequest optionRequest : questionRequest.options() == null ? List.<LmsCourseOptionRequest>of() : questionRequest.options()) {
                            LmsTestOption option = new LmsTestOption();
                            option.setQuestion(savedQuestion);
                            option.setOptionOrder(optionRequest.optionOrder());
                            option.setOptionText(optionRequest.optionText().trim());
                            option.setCorrect(optionRequest.correct());
                            testOptionRepository.save(option);
                        }
                    }
                }
            }
        }
    }

    private void validateCourseRequest(LmsCourseCreateRequest request) {
        Set<Integer> moduleOrders = new LinkedHashSet<>();
        for (LmsCourseModuleRequest module : request.modules()) {
            if (moduleOrders.add(module.moduleOrder()) == false) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module order must be unique within course");
            }
            Set<Integer> lessonOrders = new LinkedHashSet<>();
            for (LmsCourseLessonRequest lesson : module.lessons()) {
                if (lessonOrders.add(lesson.lessonOrder()) == false) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson order must be unique within module");
                }
            }
        }
    }

    private EvaluationResult evaluateQuestion(LmsTestQuestion question, List<LmsTestAnswerRequest> submitted, List<LmsTestOption> options) {
        return switch (question.getQuestionType()) {
            case SINGLE_CHOICE -> evaluateSingleChoice(question, submitted, options);
            case MULTIPLE_CHOICE -> evaluateMultipleChoice(question, submitted, options);
            case TEXT -> evaluateText(question, submitted);
        };
    }

    private EvaluationResult evaluateSingleChoice(LmsTestQuestion question, List<LmsTestAnswerRequest> submitted, List<LmsTestOption> options) {
        UUID correctId = options.stream().filter(LmsTestOption::isCorrect).map(BaseEntity::getId).findFirst().orElse(null);
        UUID selectedId = submitted.stream().map(LmsTestAnswerRequest::selectedOptionId).filter(Objects::nonNull).findFirst().orElse(null);
        boolean correct = correctId != null && correctId.equals(selectedId);
        return new EvaluationResult(correct, correct ? question.getPoints() : BigDecimal.ZERO);
    }

    private EvaluationResult evaluateMultipleChoice(LmsTestQuestion question, List<LmsTestAnswerRequest> submitted, List<LmsTestOption> options) {
        Set<UUID> expected = options.stream().filter(LmsTestOption::isCorrect).map(BaseEntity::getId).collect(Collectors.toSet());
        Set<UUID> selected = submitted.stream().map(LmsTestAnswerRequest::selectedOptionId).filter(Objects::nonNull).collect(Collectors.toSet());
        boolean correct = expected.equals(selected);
        return new EvaluationResult(correct, correct ? question.getPoints() : BigDecimal.ZERO);
    }

    private EvaluationResult evaluateText(LmsTestQuestion question, List<LmsTestAnswerRequest> submitted) {
        boolean correct = submitted.stream().map(LmsTestAnswerRequest::freeTextAnswer).anyMatch(StringUtils::hasText);
        return new EvaluationResult(correct, correct ? question.getPoints() : BigDecimal.ZERO);
    }

    private void markLessonCompleted(LmsCourseAssignment assignment, LmsLesson lesson) {
        LmsLessonProgress progress = lessonProgressRepository.findByAssignmentIdAndLessonIdAndDeletedFalse(assignment.getId(), lesson.getId()).orElseGet(LmsLessonProgress::new);
        progress.setAssignment(assignment);
        progress.setLesson(lesson);
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(OffsetDateTime.now());
        }
        progress.setProgressPercent(BigDecimal.valueOf(100));
        progress.setCompletedAt(OffsetDateTime.now());
        progress.setLastAccessedAt(OffsetDateTime.now());
        lessonProgressRepository.save(progress);
        if (assignment.getStartedAt() == null) {
            assignment.setStartedAt(OffsetDateTime.now());
        }
        if (assignment.getStatus() == LmsAssignmentStatus.ASSIGNED || assignment.getStatus() == LmsAssignmentStatus.FAILED || assignment.getStatus() == LmsAssignmentStatus.OVERDUE) {
            assignment.setStatus(LmsAssignmentStatus.IN_PROGRESS);
        }
        courseAssignmentRepository.save(assignment);
    }

    private void completeAssignmentIfEligible(LmsCourseAssignment assignment) {
        LmsCourse course = assignment.getCourse();
        List<LmsCourseModule> modules = courseModuleRepository.findAllByCourseIdAndDeletedFalseOrderByModuleOrderAsc(course.getId());
        List<LmsLesson> lessons = modules.isEmpty() ? List.of() : lessonRepository.findAllByCourseModuleIdInAndDeletedFalseOrderByLessonOrderAsc(modules.stream().map(BaseEntity::getId).toList());
        List<LmsLesson> requiredLessons = lessons.stream().filter(LmsLesson::isRequired).toList();
        Map<UUID, LmsLessonProgress> progressByLesson = lessonProgressRepository.findAllByAssignmentIdAndDeletedFalseOrderByCreatedAtAsc(assignment.getId())
            .stream()
            .collect(Collectors.toMap(item -> item.getLesson().getId(), Function.identity(), (left, right) -> right));
        boolean allCompleted = requiredLessons.stream().allMatch(lesson -> {
            LmsLessonProgress progress = progressByLesson.get(lesson.getId());
            return progress != null && progress.getCompletedAt() != null;
        });
        if (allCompleted) {
            assignment.setStatus(LmsAssignmentStatus.COMPLETED);
            assignment.setCompletedAt(OffsetDateTime.now());
            courseAssignmentRepository.save(assignment);
            generateCertificateIfNeeded(assignment);
            writeHistory(assignment.getEmployee(), assignment, assignment.getCourse(), "COURSE_COMPLETED", Map.of());
            notifyEmployee(assignment.getEmployee(), "LMS_COMPLETED", "Курс завершен", "Вы успешно завершили курс: " + assignment.getCourse().getTitle(), assignment.getCourse().getId(), assignment.getId());
            return;
        }
        if (assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDate.now()) && assignment.getStatus() != LmsAssignmentStatus.COMPLETED) {
            assignment.setStatus(LmsAssignmentStatus.OVERDUE);
            courseAssignmentRepository.save(assignment);
        }
    }

    private void generateCertificateIfNeeded(LmsCourseAssignment assignment) {
        if (assignment.getCourse().isCertificateEnabled() == false) {
            return;
        }
        if (certificateRepository.findByAssignmentIdAndDeletedFalse(assignment.getId()).isPresent()) {
            return;
        }
        String certificateNumber = "CERT-" + LocalDate.now().getYear() + "-" + String.format("%05d", Math.abs(assignment.getId().hashCode()) % 100000);
        String employeeName = fullName(assignment.getEmployee());
        String html = "<html><body><h1>Сертификат</h1><p>Сотрудник: " + employeeName + "</p><p>Курс: " + assignment.getCourse().getTitle() + "</p><p>Номер: " + certificateNumber + "</p><p>Дата: " + OffsetDateTime.now() + "</p></body></html>";
        StoredFileDescriptor file = localFileStorageService.storeText(
            "lms/certificates/" + assignment.getEmployee().getId(),
            certificateNumber + ".html",
            html,
            "text/html"
        );
        LmsCertificate certificate = new LmsCertificate();
        certificate.setAssignment(assignment);
        certificate.setEmployee(assignment.getEmployee());
        certificate.setCourse(assignment.getCourse());
        certificate.setCertificateNumber(certificateNumber);
        certificate.setFileName(file.fileName());
        certificate.setStorageKey(file.storageKey());
        certificate.setMimeType(file.contentType());
        certificateRepository.save(certificate);
        writeHistory(assignment.getEmployee(), assignment, assignment.getCourse(), "CERTIFICATE_ISSUED", Map.of("certificateNumber", certificateNumber));
        notifyEmployee(assignment.getEmployee(), "LMS_CERTIFICATE", "Сертификат готов", "Для курса \"" + assignment.getCourse().getTitle() + "\" сгенерирован сертификат.", assignment.getCourse().getId(), assignment.getId());
    }

    private boolean matchesRequirement(LmsCourseRequirement requirement, EmployeeAssignment currentAssignment) {
        return switch (requirement.getScopeType()) {
            case GLOBAL -> true;
            case INTRODUCTORY -> true;
            case POSITION -> currentAssignment != null && currentAssignment.getPosition() != null && requirement.getPosition() != null && currentAssignment.getPosition().getId().equals(requirement.getPosition().getId());
            case DEPARTMENT -> currentAssignment != null && currentAssignment.getDepartment() != null && requirement.getDepartment() != null && currentAssignment.getDepartment().getId().equals(requirement.getDepartment().getId());
        };
    }

    private LmsAssignmentSource resolveSource(LmsCourse course, LmsCourseRequirement requirement) {
        if (course.isIntroductoryCourse()) {
            return LmsAssignmentSource.INTRODUCTORY;
        }
        if (requirement == null) {
            return LmsAssignmentSource.GLOBAL;
        }
        return switch (requirement.getScopeType()) {
            case POSITION -> LmsAssignmentSource.POSITION;
            case DEPARTMENT -> LmsAssignmentSource.DEPARTMENT;
            case GLOBAL -> LmsAssignmentSource.GLOBAL;
            case INTRODUCTORY -> LmsAssignmentSource.INTRODUCTORY;
        };
    }

    private int defaultDueDays(LmsCourse course) {
        return course.isIntroductoryCourse() ? 7 : 30;
    }

    private Optional<EmployeeAssignment> currentAssignment(UUID employeeId) {
        return employeeAssignmentRepository.findCurrentAssignments(employeeId, LocalDate.now()).stream().findFirst();
    }

    private Set<UUID> recommendedCourseIds(EmployeeAssignment currentAssignment) {
        List<LmsCourseRequirement> requirements = courseRequirementRepository.findAllByActiveTrueAndDeletedFalse();
        Set<UUID> recommended = requirements.stream()
            .filter(item -> matchesRequirement(item, currentAssignment))
            .map(item -> item.getCourse().getId())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        recommended.addAll(courseRepository.search(null, LmsCourseStatus.PUBLISHED).stream()
            .filter(course -> course.isMandatoryForAll() || course.isIntroductoryCourse())
            .map(BaseEntity::getId)
            .toList());
        return recommended;
    }

    private LmsCourse getCourseEntity(UUID id) {
        return courseRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private LmsCourseAssignment getAssignmentEntity(UUID id) {
        return courseAssignmentRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
    }

    private LmsCourseResponse toCourseResponse(LmsCourse course, Map<UUID, LmsLessonProgress> progressMap) {
        List<LmsCourseModule> modules = courseModuleRepository.findAllByCourseIdAndDeletedFalseOrderByModuleOrderAsc(course.getId());
        Map<UUID, List<LmsLesson>> lessonsByModule = modules.isEmpty()
            ? Map.of()
            : lessonRepository.findAllByCourseModuleIdInAndDeletedFalseOrderByLessonOrderAsc(modules.stream().map(BaseEntity::getId).toList())
                .stream().collect(Collectors.groupingBy(item -> item.getCourseModule().getId(), LinkedHashMap::new, Collectors.toList()));
        List<LmsLesson> allLessons = lessonsByModule.values().stream().flatMap(List::stream).toList();
        Map<UUID, LmsTest> testsByLesson = allLessons.isEmpty()
            ? Map.of()
            : testRepository.findAllByLessonIdInAndDeletedFalse(allLessons.stream().map(BaseEntity::getId).toList())
                .stream().collect(Collectors.toMap(item -> item.getLesson().getId(), Function.identity()));
        List<LmsTest> tests = new ArrayList<>(testsByLesson.values());
        Map<UUID, List<LmsTestQuestion>> questionsByTest = tests.isEmpty()
            ? Map.of()
            : testQuestionRepository.findAllByTestIdInAndDeletedFalseOrderByQuestionOrderAsc(tests.stream().map(BaseEntity::getId).toList())
                .stream().collect(Collectors.groupingBy(item -> item.getTest().getId(), LinkedHashMap::new, Collectors.toList()));
        List<LmsTestQuestion> questions = questionsByTest.values().stream().flatMap(List::stream).toList();
        Map<UUID, List<LmsTestOption>> optionsByQuestion = questions.isEmpty()
            ? Map.of()
            : testOptionRepository.findAllByQuestionIdInAndDeletedFalseOrderByOptionOrderAsc(questions.stream().map(BaseEntity::getId).toList())
                .stream().collect(Collectors.groupingBy(item -> item.getQuestion().getId(), LinkedHashMap::new, Collectors.toList()));

        List<LmsCourseModuleResponse> moduleResponses = modules.stream().map(module -> new LmsCourseModuleResponse(
            module.getId(),
            module.getTitle(),
            module.getDescription(),
            module.getModuleOrder(),
            module.isRequired(),
            lessonsByModule.getOrDefault(module.getId(), List.of()).stream().map(lesson -> {
                LmsLessonProgress progress = progressMap.get(lesson.getId());
                LmsTest test = testsByLesson.get(lesson.getId());
                return new LmsLessonResponse(
                    lesson.getId(),
                    lesson.getTitle(),
                    lesson.getDescription(),
                    lesson.getLessonOrder(),
                    lesson.getContentType(),
                    lesson.getContentUrl(),
                    lesson.getContentText(),
                    lesson.getStorageKey(),
                    lesson.getMimeType(),
                    lesson.getDurationMinutes(),
                    lesson.isRequired(),
                    progress != null && progress.getCompletedAt() != null,
                    progress == null ? BigDecimal.ZERO : progress.getProgressPercent(),
                    test == null ? null : new LmsTestResponse(
                        test.getId(),
                        test.getTitle(),
                        test.getPassScore(),
                        test.getAttemptLimit(),
                        test.isRandomizeQuestions(),
                        questionsByTest.getOrDefault(test.getId(), List.of()).stream().map(question -> new LmsTestQuestionResponse(
                            question.getId(),
                            question.getQuestionOrder(),
                            question.getQuestionType(),
                            question.getQuestionText(),
                            question.getPoints(),
                            optionsByQuestion.getOrDefault(question.getId(), List.of()).stream().map(option -> new LmsTestOptionResponse(
                                option.getId(),
                                option.getOptionOrder(),
                                option.getOptionText()
                            )).toList()
                        )).toList()
                    )
                );
            }).toList()
        )).toList();

        List<LmsCourseRequirementResponse> requirements = courseRequirementRepository.findAllByCourseIdAndDeletedFalseOrderByCreatedAtAsc(course.getId()).stream()
            .map(item -> new LmsCourseRequirementResponse(
                item.getId(),
                item.getScopeType(),
                item.getPosition() == null ? null : item.getPosition().getId(),
                item.getPosition() == null ? null : item.getPosition().getTitle(),
                item.getDepartment() == null ? null : item.getDepartment().getId(),
                item.getDepartment() == null ? null : item.getDepartment().getName(),
                item.getDueDays(),
                item.isActive()
            ))
            .toList();
        return new LmsCourseResponse(
            course.getId(),
            course.getCode(),
            course.getTitle(),
            course.getDescription(),
            course.getCategory(),
            course.getCourseLevel(),
            course.getStatus(),
            course.isMandatoryForAll(),
            course.isIntroductoryCourse(),
            course.getEstimatedMinutes(),
            course.isCertificateEnabled(),
            course.getCertificateTemplateCode(),
            moduleResponses,
            requirements
        );
    }

    private LmsAssignmentResponse toAssignmentResponse(LmsCourseAssignment assignment) {
        BigDecimal progressPercent = progressPercent(assignment.getId());
        UUID certificateId = certificateRepository.findByAssignmentIdAndDeletedFalse(assignment.getId()).map(BaseEntity::getId).orElse(null);
        return new LmsAssignmentResponse(
            assignment.getId(),
            assignment.getEmployee().getId(),
            assignment.getCourse().getId(),
            assignment.getCourse().getCode(),
            assignment.getCourse().getTitle(),
            assignment.getCurrentDepartment() == null ? null : assignment.getCurrentDepartment().getId(),
            assignment.getCurrentDepartment() == null ? null : assignment.getCurrentDepartment().getName(),
            assignment.getCurrentPosition() == null ? null : assignment.getCurrentPosition().getId(),
            assignment.getCurrentPosition() == null ? null : assignment.getCurrentPosition().getTitle(),
            assignment.getAssignmentSource(),
            assignment.getDueDate(),
            assignment.getAssignedAt(),
            assignment.getStartedAt(),
            assignment.getCompletedAt(),
            assignment.getStatus(),
            assignment.isMandatory(),
            progressPercent,
            assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDate.now()) && assignment.getStatus() != LmsAssignmentStatus.COMPLETED,
            certificateId
        );
    }

    private LmsReportRowResponse toReportRow(LmsCourseAssignment assignment) {
        Employee employee = assignment.getEmployee();
        return new LmsReportRowResponse(
            assignment.getId(),
            employee.getId(),
            employee.getPersonnelNumber(),
            fullName(employee),
            assignment.getCurrentDepartment() == null ? null : assignment.getCurrentDepartment().getId(),
            assignment.getCurrentDepartment() == null ? null : assignment.getCurrentDepartment().getName(),
            assignment.getCurrentPosition() == null ? null : assignment.getCurrentPosition().getId(),
            assignment.getCurrentPosition() == null ? null : assignment.getCurrentPosition().getTitle(),
            assignment.getCourse().getId(),
            assignment.getCourse().getCode(),
            assignment.getCourse().getTitle(),
            assignment.getStatus(),
            assignment.getDueDate(),
            assignment.getCompletedAt(),
            progressPercent(assignment.getId()),
            assignment.getDueDate() != null && assignment.getDueDate().isBefore(LocalDate.now()) && assignment.getStatus() != LmsAssignmentStatus.COMPLETED,
            assignment.isMandatory()
        );
    }

    private BigDecimal progressPercent(UUID assignmentId) {
        LmsCourseAssignment assignment = getAssignmentEntity(assignmentId);
        List<LmsCourseModule> modules = courseModuleRepository.findAllByCourseIdAndDeletedFalseOrderByModuleOrderAsc(assignment.getCourse().getId());
        List<LmsLesson> lessons = modules.isEmpty() ? List.of() : lessonRepository.findAllByCourseModuleIdInAndDeletedFalseOrderByLessonOrderAsc(modules.stream().map(BaseEntity::getId).toList());
        List<LmsLesson> requiredLessons = lessons.stream().filter(LmsLesson::isRequired).toList();
        if (requiredLessons.isEmpty()) {
            return BigDecimal.valueOf(100);
        }
        long completedCount = lessonProgressRepository.findAllByAssignmentIdAndDeletedFalseOrderByCreatedAtAsc(assignmentId).stream()
            .filter(item -> item.getCompletedAt() != null)
            .map(item -> item.getLesson().getId())
            .filter(requiredLessons.stream().map(BaseEntity::getId).collect(Collectors.toSet())::contains)
            .distinct()
            .count();
        return BigDecimal.valueOf(completedCount)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(requiredLessons.size()), 2, RoundingMode.HALF_UP);
    }

    private void notifyEmployee(Employee employee, String type, String title, String body, UUID entityId, UUID assignmentId) {
        HrNotification notification = new HrNotification();
        notification.setRecipientEmployee(employee);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setEntityType("LMS_ASSIGNMENT");
        notification.setEntityId(assignmentId == null ? entityId : assignmentId);
        notification.setPayloadJson("{\"courseId\":\"" + entityId + "\",\"assignmentId\":\"" + assignmentId + "\"}");
        notificationRepository.save(notification);
    }

    private void writeHistory(Employee employee, LmsCourseAssignment assignment, LmsCourse course, String actionType, Map<String, String> details) {
        LmsLearningHistory history = new LmsLearningHistory();
        history.setEmployee(employee);
        history.setAssignment(assignment);
        history.setCourse(course);
        history.setActionType(actionType);
        history.setDetailsJson(asJson(details));
        learningHistoryRepository.save(history);
    }

    private void writeAudit(String action, UUID entityId, String before, String after) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntitySchema("hr");
        auditLog.setEntityTable("lms");
        auditLog.setEntityId(entityId);
        auditLog.setDetailsJson("{\"before\":" + quote(before) + ",\"after\":" + quote(after) + "}");
        auditLog.setOccurredAt(java.time.Instant.now());
        auditLogRepository.save(auditLog);
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    private String asJson(Map<String, String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private String courseJson(LmsCourse course) {
        return asJson(Map.of("code", course.getCode(), "title", course.getTitle(), "status", course.getStatus().name()));
    }

    private String assignmentJson(LmsCourseAssignment assignment) {
        Map<String, String> json = new LinkedHashMap<>();
        json.put("employeeId", assignment.getEmployee().getId().toString());
        json.put("courseId", assignment.getCourse().getId().toString());
        json.put("status", assignment.getStatus().name());
        return asJson(json);
    }

    private String fullName(Employee employee) {
        if (employee.getUser() == null) {
            return employee.getPersonnelNumber();
        }
        return String.join(
            " ",
            safe(employee.getUser().getLastName()),
            safe(employee.getUser().getFirstName()),
            safe(employee.getUser().getMiddleName())
        ).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record EvaluationResult(boolean correct, BigDecimal pointsAwarded) {
    }
}
