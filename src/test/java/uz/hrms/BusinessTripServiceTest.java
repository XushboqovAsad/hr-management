package uz.hrms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.*;

class BusinessTripServiceTest {

    private BusinessTripRepository businessTripRepository;
    private BusinessTripDocumentRepository businessTripDocumentRepository;
    private BusinessTripApprovalRepository businessTripApprovalRepository;
    private BusinessTripHistoryRepository businessTripHistoryRepository;
    private EmployeeRepository employeeRepository;
    private DepartmentRepository departmentRepository;
    private AuditLogRepository auditLogRepository;
    private LocalFileStorageService localFileStorageService;
    private BusinessTripService service;

    @BeforeEach
    void setUp() {
        businessTripRepository = mock(BusinessTripRepository.class);
        businessTripDocumentRepository = mock(BusinessTripDocumentRepository.class);
        businessTripApprovalRepository = mock(BusinessTripApprovalRepository.class);
        businessTripHistoryRepository = mock(BusinessTripHistoryRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        localFileStorageService = mock(LocalFileStorageService.class);
        service = new BusinessTripService(
            businessTripRepository,
            businessTripDocumentRepository,
            businessTripApprovalRepository,
            businessTripHistoryRepository,
            employeeRepository,
            departmentRepository,
            auditLogRepository,
            localFileStorageService,
            new ObjectMapper()
        );
        when(businessTripHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(businessTripRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void generateOrderRequiresApprovedTrip() {
        BusinessTrip trip = trip(BusinessTripStatus.DRAFT);
        when(businessTripRepository.findByIdAndDeletedFalse(trip.getId())).thenReturn(Optional.of(trip));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.generateOrder(trip.getId(), null));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void closeRequiresSupportingDocuments() {
        BusinessTrip trip = trip(BusinessTripStatus.REPORT_SUBMITTED);
        trip.setReportSubmittedAt(java.time.OffsetDateTime.now());
        trip.setReportText("Report");
        when(businessTripRepository.findByIdAndDeletedFalse(trip.getId())).thenReturn(Optional.of(trip));
        when(businessTripDocumentRepository.findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(trip.getId())).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.close(trip.getId()));

        assertEquals(400, exception.getStatusCode().value());
    }

    private BusinessTrip trip(BusinessTripStatus status) {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setPersonnelNumber("EMP-1");
        employee.setHireDate(LocalDate.now().minusYears(1));
        employee.setEmploymentStatus("ACTIVE");

        BusinessTrip trip = new BusinessTrip();
        trip.setId(UUID.randomUUID());
        trip.setEmployee(employee);
        trip.setDestinationCity("Tashkent");
        trip.setPurpose("Meeting");
        trip.setStartDate(LocalDate.now().plusDays(1));
        trip.setEndDate(LocalDate.now().plusDays(2));
        trip.setDailyAllowance(BigDecimal.TEN);
        trip.setStatus(status);
        return trip;
    }
}
