package uz.hrms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uz.hrms.other.*;
import uz.hrms.other.repository.*;

class AttendanceServiceTest {

    private static final String ASSIGNMENT_QUERY = "select id, department_id from hr.employee_assignments where employee_id = ? and is_deleted = false and started_at <= ? and (ended_at is null or ended_at >= ?) order by started_at desc limit 1";

    private ScudEventRepository scudEventRepository;
    private WorkScheduleRepository workScheduleRepository;
    private EmployeeWorkScheduleRepository employeeWorkScheduleRepository;
    private AttendanceLogRepository attendanceLogRepository;
    private AttendanceSummaryRepository attendanceSummaryRepository;
    private AttendanceAdjustmentRepository attendanceAdjustmentRepository;
    private AttendanceIncidentRepository attendanceIncidentRepository;
    private AttendanceViolationRepository attendanceViolationRepository;
    private EmployeeRepository employeeRepository;
    private AuditLogRepository auditLogRepository;
    private JdbcTemplate jdbcTemplate;
    private ExplanationTaskBootstrapService explanationTaskBootstrapService;
    private AttendanceService service;

    @BeforeEach
    void setUp() {
        scudEventRepository = mock(ScudEventRepository.class);
        workScheduleRepository = mock(WorkScheduleRepository.class);
        employeeWorkScheduleRepository = mock(EmployeeWorkScheduleRepository.class);
        attendanceLogRepository = mock(AttendanceLogRepository.class);
        attendanceSummaryRepository = mock(AttendanceSummaryRepository.class);
        attendanceAdjustmentRepository = mock(AttendanceAdjustmentRepository.class);
        attendanceIncidentRepository = mock(AttendanceIncidentRepository.class);
        attendanceViolationRepository = mock(AttendanceViolationRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        explanationTaskBootstrapService = mock(ExplanationTaskBootstrapService.class);
        service = new AttendanceService(
            scudEventRepository,
            workScheduleRepository,
            employeeWorkScheduleRepository,
            attendanceLogRepository,
            attendanceSummaryRepository,
            attendanceAdjustmentRepository,
            attendanceIncidentRepository,
            attendanceViolationRepository,
            employeeRepository,
            auditLogRepository,
            jdbcTemplate,
            new ObjectMapper(),
            explanationTaskBootstrapService
        );

        when(attendanceLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceSummaryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceIncidentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceViolationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceViolationRepository.findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(attendanceIncidentRepository.findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(jdbcTemplate.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<String>>any(), any(), any())).thenReturn(List.of());
    }

    @Test
    void processWorkDateCalculatesWorkedMinutesForMultiplePunches() {
        LocalDate workDate = LocalDate.of(2026, 4, 2);
        Employee employee = employee();
        WorkSchedule schedule = standardSchedule();
        UUID employeeId = employee.getId();

        when(employeeRepository.findByIdAndDeletedFalse(employeeId)).thenReturn(Optional.of(employee));
        when(employeeWorkScheduleRepository.findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(employeeId, workDate, workDate)).thenReturn(Optional.empty());
        when(employeeWorkScheduleRepository.findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(employeeId, workDate)).thenReturn(Optional.empty());
        when(workScheduleRepository.findByCodeAndDeletedFalse("STANDARD_0900_1800")).thenReturn(Optional.of(schedule));
        when(scudEventRepository.findAllByDeletedFalseAndEventAtBetweenOrderByEventAtAsc(any(), any())).thenReturn(List.of(event(employee, ScudEventType.ENTRY, 9, 0)));
        when(scudEventRepository.findAllByEmployeeIdAndDeletedFalseAndEventAtBetweenOrderByEventAtAsc(eq(employeeId), any(), any())).thenReturn(List.of(
            event(employee, ScudEventType.ENTRY, 9, 0),
            event(employee, ScudEventType.EXIT, 12, 0),
            event(employee, ScudEventType.ENTRY, 13, 0),
            event(employee, ScudEventType.EXIT, 18, 0)
        ));
        when(attendanceLogRepository.findByEmployeeIdAndWorkDateAndDeletedFalse(employeeId, workDate)).thenReturn(Optional.empty());
        when(attendanceSummaryRepository.findByEmployeeIdAndWorkDateAndDeletedFalse(employeeId, workDate)).thenReturn(Optional.empty());
        when(jdbcTemplate.queryForList(ASSIGNMENT_QUERY, employeeId, workDate, workDate)).thenReturn(List.of(Map.of()));

        AttendanceSummaryResponse response = service.processWorkDate(workDate, employeeId).get(0);

        assertEquals(AttendanceStatus.PRESENT, response.attendanceStatus());
        assertEquals(480, response.log().workedMinutes());
        assertEquals(4, response.log().rawEventCount());
    }

    @Test
    void processWorkDateSupportsNightShift() {
        LocalDate workDate = LocalDate.of(2026, 4, 2);
        Employee employee = employee();
        WorkSchedule schedule = nightSchedule();
        EmployeeWorkSchedule assignment = new EmployeeWorkSchedule();
        assignment.setEmployee(employee);
        assignment.setWorkSchedule(schedule);
        assignment.setEffectiveFrom(workDate.minusDays(10));

        when(employeeRepository.findByIdAndDeletedFalse(employee.getId())).thenReturn(Optional.of(employee));
        when(employeeWorkScheduleRepository.findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(employee.getId(), workDate, workDate)).thenReturn(Optional.empty());
        when(employeeWorkScheduleRepository.findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(employee.getId(), workDate)).thenReturn(Optional.of(assignment));
        when(workScheduleRepository.findByCodeAndDeletedFalse("STANDARD_0900_1800")).thenReturn(Optional.of(standardSchedule()));
        when(scudEventRepository.findAllByDeletedFalseAndEventAtBetweenOrderByEventAtAsc(any(), any())).thenReturn(List.of(event(employee, ScudEventType.ENTRY, 22, 0)));
        when(scudEventRepository.findAllByEmployeeIdAndDeletedFalseAndEventAtBetweenOrderByEventAtAsc(eq(employee.getId()), any(), any())).thenReturn(List.of(
            event(employee, ScudEventType.ENTRY, 22, 0),
            event(employee, ScudEventType.EXIT, 6, 0, workDate.plusDays(1))
        ));
        when(attendanceLogRepository.findByEmployeeIdAndWorkDateAndDeletedFalse(employee.getId(), workDate)).thenReturn(Optional.empty());
        when(attendanceSummaryRepository.findByEmployeeIdAndWorkDateAndDeletedFalse(employee.getId(), workDate)).thenReturn(Optional.empty());
        when(jdbcTemplate.queryForList(ASSIGNMENT_QUERY, employee.getId(), workDate, workDate)).thenReturn(List.of(Map.of()));

        AttendanceSummaryResponse response = service.processWorkDate(workDate, employee.getId()).get(0);

        assertEquals(AttendanceStatus.PRESENT, response.attendanceStatus());
        assertEquals(480, response.log().workedMinutes());
    }

    @Test
    void processWorkDateMarksNoDataWhenScudFeedMissing() {
        LocalDate workDate = LocalDate.of(2026, 4, 2);
        Employee employee = employee();
        WorkSchedule schedule = standardSchedule();

        when(employeeRepository.findByIdAndDeletedFalse(employee.getId())).thenReturn(Optional.of(employee));
        when(employeeWorkScheduleRepository.findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(employee.getId(), workDate, workDate)).thenReturn(Optional.empty());
        when(employeeWorkScheduleRepository.findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(employee.getId(), workDate)).thenReturn(Optional.empty());
        when(workScheduleRepository.findByCodeAndDeletedFalse("STANDARD_0900_1800")).thenReturn(Optional.of(schedule));
        when(scudEventRepository.findAllByDeletedFalseAndEventAtBetweenOrderByEventAtAsc(any(), any())).thenReturn(List.of());
        when(scudEventRepository.findAllByEmployeeIdAndDeletedFalseAndEventAtBetweenOrderByEventAtAsc(eq(employee.getId()), any(), any())).thenReturn(List.of());
        when(attendanceLogRepository.findByEmployeeIdAndWorkDateAndDeletedFalse(employee.getId(), workDate)).thenReturn(Optional.empty());
        when(attendanceSummaryRepository.findByEmployeeIdAndWorkDateAndDeletedFalse(employee.getId(), workDate)).thenReturn(Optional.empty());
        when(jdbcTemplate.queryForList(ASSIGNMENT_QUERY, employee.getId(), workDate, workDate)).thenReturn(List.of(Map.of()));

        AttendanceSummaryResponse response = service.processWorkDate(workDate, employee.getId()).get(0);

        assertEquals(AttendanceStatus.NO_DATA, response.attendanceStatus());
        assertEquals(480, response.absenceMinutes());
    }

    private Employee employee() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setPersonnelNumber("EMP-ATT-1");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setEmploymentStatus("ACTIVE");
        return employee;
    }

    private WorkSchedule standardSchedule() {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setId(UUID.randomUUID());
        schedule.setCode("STANDARD_0900_1800");
        schedule.setName("Standard");
        schedule.setShiftStartTime(LocalTime.of(9, 0));
        schedule.setShiftEndTime(LocalTime.of(18, 0));
        schedule.setCrossesMidnight(false);
        schedule.setGraceMinutes(10);
        schedule.setRequiredMinutes(480);
        return schedule;
    }

    private WorkSchedule nightSchedule() {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setId(UUID.randomUUID());
        schedule.setCode("NIGHT_2200_0600");
        schedule.setName("Night");
        schedule.setShiftStartTime(LocalTime.of(22, 0));
        schedule.setShiftEndTime(LocalTime.of(6, 0));
        schedule.setCrossesMidnight(true);
        schedule.setGraceMinutes(10);
        schedule.setRequiredMinutes(480);
        return schedule;
    }

    private ScudEvent event(Employee employee, ScudEventType type, int hour, int minute) {
        return event(employee, type, hour, minute, LocalDate.of(2026, 4, 2));
    }

    private ScudEvent event(Employee employee, ScudEventType type, int hour, int minute, LocalDate date) {
        ScudEvent event = new ScudEvent();
        event.setId(UUID.randomUUID());
        event.setEmployee(employee);
        event.setDeviceId("TURNSTILE-1");
        event.setEventType(type);
        event.setEventAt(OffsetDateTime.of(date.atTime(hour, minute), ZoneOffset.ofHours(5)));
        event.setNormalizationStatus(ScudNormalizationStatus.NEW);
        return event;
    }
}
