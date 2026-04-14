package uz.hrms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.*;
import uz.hrms.other.enums.AbsenceDocumentStatus;
import uz.hrms.other.enums.AbsenceStatus;
import uz.hrms.other.enums.BusinessTripStatus;
import uz.hrms.other.repository.AuditLogRepository;

class AbsenceServiceTest {

    private AbsenceRecordRepository absenceRecordRepository;
    private AbsenceDocumentRepository absenceDocumentRepository;
    private AbsenceHistoryRepository absenceHistoryRepository;
    private AttendanceDayMarkRepository attendanceDayMarkRepository;
    private EmployeeRepository employeeRepository;
    private BusinessTripRepository businessTripRepository;
    private AuditLogRepository auditLogRepository;
    private LocalFileStorageService localFileStorageService;
    private JdbcTemplate jdbcTemplate;
    private AbsenceService service;

    @BeforeEach
    void setUp() {
        absenceRecordRepository = mock(AbsenceRecordRepository.class);
        absenceDocumentRepository = mock(AbsenceDocumentRepository.class);
        absenceHistoryRepository = mock(AbsenceHistoryRepository.class);
        attendanceDayMarkRepository = mock(AttendanceDayMarkRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        businessTripRepository = mock(BusinessTripRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        localFileStorageService = mock(LocalFileStorageService.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new AbsenceService(
            absenceRecordRepository,
            absenceDocumentRepository,
            absenceHistoryRepository,
            attendanceDayMarkRepository,
            employeeRepository,
            businessTripRepository,
            auditLogRepository,
            localFileStorageService,
            jdbcTemplate,
            new ObjectMapper()
        );
        when(absenceRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(absenceHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceDayMarkRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(absenceDocumentRepository.findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(attendanceDayMarkRepository.findAllBySourceRecordIdAndMarkSourceAndDeletedFalse(any(), eq(AttendanceMarkSource.ABSENCE))).thenReturn(List.of());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
    }

    @Test
    void createRejectsOverlapWithBusinessTrip() {
        Employee employee = employee();
        when(employeeRepository.findByIdAndDeletedFalse(employee.getId())).thenReturn(Optional.of(employee));
        when(absenceRecordRepository.findAllByEmployeeIdAndDeletedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any(), any())).thenReturn(List.of());
        when(businessTripRepository.findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(employee.getId())).thenReturn(List.of(activeTrip(employee)));

        AbsenceRequest request = new AbsenceRequest(
            employee.getId(),
            null,
            AbsenceType.SICK_LEAVE,
            null,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            Boolean.TRUE
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.create(request));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void closeRequiresDocumentWhenMandatory() {
        AbsenceRecord record = approvedRecord(true);
        when(absenceRecordRepository.findByIdAndDeletedFalse(record.getId())).thenReturn(Optional.of(record));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.close(record.getId(), "close"));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void approveCreatesAttendanceMarks() {
        AbsenceRecord record = reviewingRecord();
        when(absenceRecordRepository.findByIdAndDeletedFalse(record.getId())).thenReturn(Optional.of(record));
        when(absenceDocumentRepository.findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(record.getId())).thenReturn(List.of(document(record)));
        when(attendanceDayMarkRepository.findAllByEmployeeIdAndAttendanceDateBetweenAndDeletedFalseOrderByAttendanceDateAsc(
            eq(record.getEmployee().getId()),
            eq(record.getStartDate()),
            eq(record.getEndDate())
        )).thenReturn(List.of());

        AbsenceResponse response = service.approve(record.getId(), "ok");

        assertEquals(AbsenceStatus.APPROVED, response.status());
    }

    private Employee employee() {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setPersonnelNumber("EMP-ABS-1");
        employee.setHireDate(LocalDate.now().minusYears(2));
        employee.setEmploymentStatus("ACTIVE");
        return employee;
    }

    private BusinessTrip activeTrip(Employee employee) {
        BusinessTrip trip = new BusinessTrip();
        trip.setId(UUID.randomUUID());
        trip.setEmployee(employee);
        trip.setDestinationCity("Samarkand");
        trip.setPurpose("Audit");
        trip.setStartDate(LocalDate.now().plusDays(1));
        trip.setEndDate(LocalDate.now().plusDays(3));
        trip.setStatus(BusinessTripStatus.APPROVED);
        return trip;
    }

    private AbsenceRecord approvedRecord(boolean documentRequired) {
        AbsenceRecord record = new AbsenceRecord();
        record.setId(UUID.randomUUID());
        record.setEmployee(employee());
        record.setAbsenceType(AbsenceType.SICK_LEAVE);
        record.setStartDate(LocalDate.now().minusDays(2));
        record.setEndDate(LocalDate.now().minusDays(1));
        record.setStatus(AbsenceStatus.APPROVED);
        record.setDocumentRequired(documentRequired);
        return record;
    }

    private AbsenceRecord reviewingRecord() {
        AbsenceRecord record = new AbsenceRecord();
        record.setId(UUID.randomUUID());
        record.setEmployee(employee());
        record.setAbsenceType(AbsenceType.SICK_LEAVE);
        record.setStartDate(LocalDate.now().minusDays(1));
        record.setEndDate(LocalDate.now());
        record.setStatus(AbsenceStatus.HR_REVIEW);
        record.setDocumentRequired(true);
        return record;
    }

    private AbsenceDocument document(AbsenceRecord record) {
        AbsenceDocument document = new AbsenceDocument();
        document.setId(UUID.randomUUID());
        document.setAbsenceRecord(record);
        document.setTitle("Sick leave certificate");
        document.setOriginalFileName("sick.pdf");
        document.setStorageKey("absences/test/sick.pdf");
        document.setContentType("application/pdf");
        document.setSizeBytes(128L);
        document.setVersionNo(1);
        document.setCurrent(true);
        document.setDocumentStatus(AbsenceDocumentStatus.ACTIVE);
        return document;
    }
}
