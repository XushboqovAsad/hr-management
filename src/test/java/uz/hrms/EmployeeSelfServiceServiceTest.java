package uz.hrms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.*;

class EmployeeSelfServiceServiceTest {

    private EmployeeSelfServiceEmployeeRepository employeeRepository;
    private EmployeeSelfServiceAssignmentRepository assignmentRepository;
    private EmployeeSelfServiceUserAccountRepository userAccountRepository;
    private HrNotificationRepository hrNotificationRepository;
    private AttendanceService attendanceService;
    private AbsenceService absenceService;
    private BusinessTripService businessTripService;
    private ExplanationService explanationService;
    private DismissalService dismissalService;
    private AuditLogRepository auditLogRepository;
    private EmployeeSelfService service;

    @BeforeEach
    void setUp() {
        employeeRepository = mock(EmployeeSelfServiceEmployeeRepository.class);
        assignmentRepository = mock(EmployeeSelfServiceAssignmentRepository.class);
        userAccountRepository = mock(EmployeeSelfServiceUserAccountRepository.class);
        hrNotificationRepository = mock(HrNotificationRepository.class);
        attendanceService = mock(AttendanceService.class);
        absenceService = mock(AbsenceService.class);
        businessTripService = mock(BusinessTripService.class);
        explanationService = mock(ExplanationService.class);
        dismissalService = mock(DismissalService.class);
        auditLogRepository = mock(AuditLogRepository.class);
        service = new EmployeeSelfService(
            employeeRepository,
            assignmentRepository,
            userAccountRepository,
            hrNotificationRepository,
            attendanceService,
            absenceService,
            businessTripService,
            explanationService,
            dismissalService,
            auditLogRepository
        );
        when(userAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(hrNotificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updateProfileChangesOnlyAllowedFields() {
        CurrentUser currentUser = currentUser();
        Employee employee = employee(currentUser);
        when(employeeRepository.findByUserIdAndDeletedFalse(currentUser.userId())).thenReturn(Optional.of(employee));
        when(assignmentRepository.findCurrentAssignments(employee.getId(), LocalDate.now())).thenReturn(List.of());

        EmployeeSelfProfileResponse response = service.updateProfile(currentUser, new EmployeeSelfProfileUpdateRequest("new@email.uz", "Karimovich"));

        assertEquals("new@email.uz", employee.getUser().getEmail());
        assertEquals("Karimovich", employee.getUser().getMiddleName());
        assertEquals("new@email.uz", response.email());
    }

    @Test
    void markNotificationReadRejectsForeignNotification() {
        CurrentUser currentUser = currentUser();
        Employee employee = employee(currentUser);
        HrNotification notification = new HrNotification();
        notification.setId(UUID.randomUUID());
        Employee foreignEmployee = new Employee();
        foreignEmployee.setId(UUID.randomUUID());
        notification.setRecipientEmployee(foreignEmployee);

        when(employeeRepository.findByUserIdAndDeletedFalse(currentUser.userId())).thenReturn(Optional.of(employee));
        when(hrNotificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.markNotificationRead(currentUser, notification.getId()));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void dashboardAggregatesSelfModules() {
        CurrentUser currentUser = currentUser();
        Employee employee = employee(currentUser);
        HrNotification notification = new HrNotification();
        notification.setId(UUID.randomUUID());
        notification.setRecipientEmployee(employee);
        notification.setNotificationType("INFO");
        notification.setTitle("Title");
        notification.setBody("Body");
        notification.setStatus(NotificationStatus.NEW);
        notification.setCreatedAt(OffsetDateTime.now());

        when(employeeRepository.findByUserIdAndDeletedFalse(currentUser.userId())).thenReturn(Optional.of(employee));
        when(assignmentRepository.findCurrentAssignments(employee.getId(), LocalDate.now())).thenReturn(List.of());
        when(hrNotificationRepository.findAllByRecipientEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(employee.getId())).thenReturn(List.of(notification));
        when(attendanceService.dashboard(any(), any(), any(), any(), any())).thenReturn(new AttendanceDashboardResponse(0, 0, 0, 0, 0, 0, List.of()));
        when(absenceService.list(employee.getId())).thenReturn(List.of());
        when(businessTripService.list(employee.getId())).thenReturn(List.of());
        when(explanationService.inbox(employee.getId(), null, null)).thenReturn(List.of());
        when(dismissalService.list(employee.getId(), null, null)).thenReturn(List.of());

        EmployeeSelfDashboardResponse dashboard = service.dashboard(currentUser);

        assertEquals(employee.getId(), dashboard.profile().employeeId());
        assertEquals(1, dashboard.unreadNotificationsCount());
        assertEquals(true, dashboard.features().directoryAvailable());
        assertEquals(false, dashboard.features().leaveAvailable());
    }

    private CurrentUser currentUser() {
        return new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), "employee", "hash", true, Set.of("EMPLOYEE"), Set.of("ESS:READ", "ESS:WRITE"));
    }

    private Employee employee(CurrentUser currentUser) {
        UserAccount user = new UserAccount();
        user.setId(currentUser.userId());
        user.setUsername(currentUser.username());
        user.setEmail("old@email.uz");
        user.setFirstName("Ali");
        user.setLastName("Valiyev");
        user.setMiddleName("Old");
        user.setActive(true);

        Employee employee = new Employee();
        employee.setId(currentUser.employeeId());
        employee.setUser(user);
        employee.setPersonnelNumber("EMP-100");
        employee.setEmploymentStatus("ACTIVE");
        employee.setHireDate(LocalDate.of(2025, 1, 1));
        return employee;
    }
}
