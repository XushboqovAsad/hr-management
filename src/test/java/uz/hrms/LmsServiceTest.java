package uz.hrms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.*;
import uz.hrms.other.repository.AuditLogRepository;

class LmsServiceTest {

    private LmsCourseRepository courseRepository;
    private LmsCourseModuleRepository courseModuleRepository;
    private LmsLessonRepository lessonRepository;
    private LmsTestRepository testRepository;
    private LmsTestQuestionRepository testQuestionRepository;
    private LmsTestOptionRepository testOptionRepository;
    private LmsCourseRequirementRepository courseRequirementRepository;
    private LmsCourseAssignmentRepository courseAssignmentRepository;
    private LmsLessonProgressRepository lessonProgressRepository;
    private LmsTestAttemptRepository testAttemptRepository;
    private LmsTestAttemptAnswerRepository testAttemptAnswerRepository;
    private LmsCertificateRepository certificateRepository;
    private LmsLearningHistoryRepository learningHistoryRepository;
    private LmsEmployeeRepository employeeRepository;
    private LmsEmployeeAssignmentRepository employeeAssignmentRepository;
    private DepartmentRepository departmentRepository;
    private PositionRepository positionRepository;
    private HrNotificationRepository notificationRepository;
    private AuditLogRepository auditLogRepository;
    private LocalFileStorageService localFileStorageService;
    private LmsService service;

    @BeforeEach
    void setUp() {
        courseRepository = mock(LmsCourseRepository.class);
        courseModuleRepository = mock(LmsCourseModuleRepository.class);
        lessonRepository = mock(LmsLessonRepository.class);
        testRepository = mock(LmsTestRepository.class);
        testQuestionRepository = mock(LmsTestQuestionRepository.class);
        testOptionRepository = mock(LmsTestOptionRepository.class);
        courseRequirementRepository = mock(LmsCourseRequirementRepository.class);
        courseAssignmentRepository = mock(LmsCourseAssignmentRepository.class);
        lessonProgressRepository = mock(LmsLessonProgressRepository.class);
        testAttemptRepository = mock(LmsTestAttemptRepository.class);
        testAttemptAnswerRepository = mock(LmsTestAttemptAnswerRepository.class);
        certificateRepository = mock(LmsCertificateRepository.class);
        learningHistoryRepository = mock(LmsLearningHistoryRepository.class);
        employeeRepository = mock(LmsEmployeeRepository.class);
        employeeAssignmentRepository = mock(LmsEmployeeAssignmentRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        positionRepository = mock(PositionRepository.class);
        notificationRepository = mock(HrNotificationRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        localFileStorageService = mock(LocalFileStorageService.class);
        service = new LmsService(
            courseRepository,
            courseModuleRepository,
            lessonRepository,
            testRepository,
            testQuestionRepository,
            testOptionRepository,
            courseRequirementRepository,
            courseAssignmentRepository,
            lessonProgressRepository,
            testAttemptRepository,
            testAttemptAnswerRepository,
            certificateRepository,
            learningHistoryRepository,
            employeeRepository,
            employeeAssignmentRepository,
            departmentRepository,
            positionRepository,
            notificationRepository,
            auditLogRepository,
            localFileStorageService,
            new ObjectMapper()
        );
        when(courseAssignmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(learningHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void assignCourseRejectsDuplicateAssignments() {
        Employee employee = employee();
        LmsCourse course = publishedCourse();
        when(employeeRepository.findByIdAndDeletedFalse(employee.getId())).thenReturn(Optional.of(employee));
        when(courseRepository.findByIdAndDeletedFalse(course.getId())).thenReturn(Optional.of(course));
        when(courseAssignmentRepository.existsByEmployeeIdAndCourseIdAndStatuses(any(), any(), any())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.assignCourse(
            new LmsAssignmentCreateRequest(employee.getId(), course.getId(), LmsAssignmentSource.MANUAL, LocalDate.now().plusDays(10), false),
            UUID.randomUUID()
        ));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void syncMandatoryAssignmentsAssignsPositionCourse() {
        Employee employee = employee();
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setName("IT");
        Position position = new Position();
        position.setId(UUID.randomUUID());
        position.setTitle("Engineer");
        EmployeeAssignment assignment = new EmployeeAssignment();
        assignment.setEmployee(employee);
        assignment.setDepartment(department);
        assignment.setPosition(position);
        assignment.setStartedAt(LocalDate.now().minusDays(1));

        LmsCourse course = publishedCourse();
        course.setMandatoryForAll(false);
        course.setIntroductoryCourse(false);

        LmsCourseRequirement requirement = new LmsCourseRequirement();
        requirement.setCourse(course);
        requirement.setScopeType(LmsRequirementScopeType.POSITION);
        requirement.setPosition(position);
        requirement.setDueDays(14);
        requirement.setActive(true);

        when(employeeAssignmentRepository.findAllCurrentAssignments(LocalDate.now())).thenReturn(List.of(assignment));
        when(employeeRepository.findAllForLearningSync()).thenReturn(List.of(employee));
        when(courseRepository.search(null, LmsCourseStatus.PUBLISHED)).thenReturn(List.of(course));
        when(courseRequirementRepository.findAllByActiveTrueAndDeletedFalse()).thenReturn(List.of(requirement));
        when(courseAssignmentRepository.existsByEmployeeIdAndCourseIdAndStatuses(any(), any(), any())).thenReturn(false);

        LmsReminderSummaryResponse result = service.syncMandatoryAssignments();

        assertEquals(1, result.assignedCount());
    }

    private Employee employee() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setPersonnelNumber("EMP-001");
        employee.setHireDate(LocalDate.now().minusMonths(1));
        employee.setEmploymentStatus("ACTIVE");
        return employee;
    }

    private LmsCourse publishedCourse() {
        LmsCourse course = new LmsCourse();
        course.setId(UUID.randomUUID());
        course.setCode("INTRO-001");
        course.setTitle("Intro");
        course.setStatus(LmsCourseStatus.PUBLISHED);
        course.setActive(true);
        course.setCertificateEnabled(true);
        course.setEstimatedMinutes(60);
        return course;
    }
}
