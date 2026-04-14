package uz.hrms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.*;
import uz.hrms.other.repository.*;
import uz.hrms.other.enums.ExplanationIncidentStatus;
import uz.hrms.other.enums.ExplanationStatus;
import uz.hrms.other.repository.AuditLogRepository;

class ExplanationServiceTest {

    private ExplanationIncidentRepository explanationIncidentRepository;
    private ExplanationRepository explanationRepository;
    private ExplanationDocumentRepository explanationDocumentRepository;
    private ExplanationHistoryRepository explanationHistoryRepository;
    private DisciplinaryActionRepository disciplinaryActionRepository;
    private RewardActionRepository rewardActionRepository;
    private HrNotificationRepository hrNotificationRepository;
    private AttendanceIncidentRepository attendanceIncidentRepository;
    private AttendanceViolationRepository attendanceViolationRepository;
    private EmployeeRepository employeeRepository;
    private DepartmentRepository departmentRepository;
    private AuditLogRepository auditLogRepository;
    private LocalFileStorageService localFileStorageService;
    private ExplanationService service;

    @BeforeEach
    void setUp() {
        explanationIncidentRepository = mock(ExplanationIncidentRepository.class);
        explanationRepository = mock(ExplanationRepository.class);
        explanationDocumentRepository = mock(ExplanationDocumentRepository.class);
        explanationHistoryRepository = mock(ExplanationHistoryRepository.class);
        disciplinaryActionRepository = mock(DisciplinaryActionRepository.class);
        rewardActionRepository = mock(RewardActionRepository.class);
        hrNotificationRepository = mock(HrNotificationRepository.class);
        attendanceIncidentRepository = mock(AttendanceIncidentRepository.class);
        attendanceViolationRepository = mock(AttendanceViolationRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        localFileStorageService = mock(LocalFileStorageService.class);

        service = new ExplanationService(
            explanationIncidentRepository,
            explanationRepository,
            explanationDocumentRepository,
            explanationHistoryRepository,
            disciplinaryActionRepository,
            rewardActionRepository,
            hrNotificationRepository,
            attendanceIncidentRepository,
            attendanceViolationRepository,
            employeeRepository,
            departmentRepository,
            auditLogRepository,
            localFileStorageService,
            new ObjectMapper()
        );

        when(explanationIncidentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(explanationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(explanationHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(disciplinaryActionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(rewardActionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(hrNotificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceIncidentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(explanationDocumentRepository.findAllByExplanationIdAndDeletedFalseOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(explanationHistoryRepository.findAllByExplanationIncidentIdAndDeletedFalseOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(disciplinaryActionRepository.findAllByEmployeeIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(any())).thenReturn(List.of());
        when(rewardActionRepository.findAllByDeletedFalseOrderByRewardDateDescCreatedAtDesc()).thenReturn(List.of());
        when(attendanceViolationRepository.findAllByDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of());
    }

    @Test
    void createManualIncidentCreatesDraftExplanation() {
        Employee employee = employee("EMP-EXP-1");
        Department department = department();
        when(employeeRepository.findByIdAndDeletedFalse(employee.getId())).thenReturn(Optional.of(employee));
        when(departmentRepository.findByIdAndDeletedFalse(department.getId())).thenReturn(Optional.of(department));
        when(explanationRepository.findByExplanationIncidentIdAndDeletedFalse(any())).thenReturn(Optional.empty());

        ExplanationCardResponse response = service.createManualIncident(new ExplanationIncidentCreateRequest(
            employee.getId(),
            department.getId(),
            null,
            ExplanationIncidentSource.MANUAL,
            "MANUAL_INCIDENT",
            "Нарушение регламента",
            "Описание инцидента",
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(1)
        ));

        assertEquals(employee.getId(), response.employeeId());
        assertEquals(ExplanationStatus.DRAFT, response.explanationStatus());
        assertEquals(ExplanationIncidentSource.MANUAL, response.incidentSource());
    }

    @Test
    void createDisciplinaryActionRejectsInvalidDateRange() {
        Employee employee = employee("EMP-EXP-2");
        ExplanationIncident incident = incident(employee);
        Explanation explanation = submittedExplanation(incident, employee);
        when(explanationIncidentRepository.findByIdAndDeletedFalse(incident.getId())).thenReturn(Optional.of(incident));
        when(explanationRepository.findByExplanationIncidentIdAndDeletedFalse(incident.getId())).thenReturn(Optional.of(explanation));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.createDisciplinaryAction(
            incident.getId(),
            new ExplanationDisciplinaryActionRequest(
                null,
                DisciplinaryActionType.REMARK,
                LocalDate.of(2026, 4, 10),
                "Repeated lateness",
                LocalDate.of(2026, 4, 9),
                null,
                null,
                "comment"
            )
        ));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private Employee employee(String personnelNumber) {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setPersonnelNumber(personnelNumber);
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setEmploymentStatus("ACTIVE");
        return employee;
    }

    private Department department() {
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setCode("DEP-1");
        department.setName("HR");
        department.setUnitType(DepartmentUnitType.DEPARTMENT);
        return department;
    }

    private ExplanationIncident incident(Employee employee) {
        ExplanationIncident incident = new ExplanationIncident();
        incident.setId(UUID.randomUUID());
        incident.setEmployee(employee);
        incident.setIncidentSource(ExplanationIncidentSource.SCUD);
        incident.setIncidentType("LATENESS");
        incident.setTitle("Late arrival");
        incident.setOccurredAt(OffsetDateTime.now().minusDays(1));
        incident.setStatus(ExplanationIncidentStatus.UNDER_REVIEW);
        return incident;
    }

    private Explanation submittedExplanation(ExplanationIncident incident, Employee employee) {
        Explanation explanation = new Explanation();
        explanation.setId(UUID.randomUUID());
        explanation.setExplanationIncident(incident);
        explanation.setEmployee(employee);
        explanation.setExplanationText("Traffic accident");
        explanation.setEmployeeSubmittedAt(OffsetDateTime.now().minusHours(2));
        explanation.setStatus(ExplanationStatus.SUBMITTED);
        return explanation;
    }
}
