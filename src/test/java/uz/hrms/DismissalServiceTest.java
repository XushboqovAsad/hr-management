package uz.hrms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class DismissalServiceTest {

    private DismissalRequestRepository dismissalRequestRepository;
    private ClearanceChecklistRepository clearanceChecklistRepository;
    private ClearanceChecklistItemRepository clearanceChecklistItemRepository;
    private DismissalHistoryRepository dismissalHistoryRepository;
    private DismissalEmployeeAssignmentRepository dismissalEmployeeAssignmentRepository;
    private EmployeeRepository employeeRepository;
    private DepartmentRepository departmentRepository;
    private UserAccountRepository userAccountRepository;
    private AuditLogRepository auditLogRepository;
    private DismissalService service;

    @BeforeEach
    void setUp() {
        dismissalRequestRepository = mock(DismissalRequestRepository.class);
        clearanceChecklistRepository = mock(ClearanceChecklistRepository.class);
        clearanceChecklistItemRepository = mock(ClearanceChecklistItemRepository.class);
        dismissalHistoryRepository = mock(DismissalHistoryRepository.class);
        dismissalEmployeeAssignmentRepository = mock(DismissalEmployeeAssignmentRepository.class);
        employeeRepository = mock(EmployeeRepository.class);
        departmentRepository = mock(DepartmentRepository.class);
        userAccountRepository = mock(UserAccountRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        service = new DismissalService(
            dismissalRequestRepository,
            clearanceChecklistRepository,
            clearanceChecklistItemRepository,
            dismissalHistoryRepository,
            dismissalEmployeeAssignmentRepository,
            employeeRepository,
            departmentRepository,
            userAccountRepository,
            auditLogRepository,
            new ObjectMapper()
        );
        when(dismissalRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(clearanceChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(clearanceChecklistItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dismissalHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(dismissalEmployeeAssignmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditLogRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void finalizeRequiresCompletedChecklist() {
        DismissalRequest dismissalRequest = dismissal();
        ClearanceChecklist checklist = checklist(dismissalRequest);
        ClearanceChecklistItem pendingItem = checklistItem(checklist, ClearanceItemType.PASS, ClearanceItemStatus.PENDING, ClearanceReturnStatus.PENDING);

        when(dismissalRequestRepository.findByIdAndDeletedFalse(dismissalRequest.getId())).thenReturn(Optional.of(dismissalRequest));
        when(clearanceChecklistRepository.findByDismissalRequestIdAndDeletedFalse(dismissalRequest.getId())).thenReturn(Optional.of(checklist));
        when(clearanceChecklistItemRepository.findAllByClearanceChecklistIdAndDeletedFalseOrderBySortOrderAsc(checklist.getId())).thenReturn(List.of(pendingItem));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.finalizeDismissal(dismissalRequest.getId(), "Finalize"));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void finalizeDismissalBlocksAccountAndClosesAssignments() {
        DismissalRequest dismissalRequest = dismissal();
        ClearanceChecklist checklist = checklist(dismissalRequest);
        ClearanceChecklistItem passItem = checklistItem(checklist, ClearanceItemType.PASS, ClearanceItemStatus.COMPLETED, ClearanceReturnStatus.RETURNED);
        ClearanceChecklistItem accountBlockItem = checklistItem(checklist, ClearanceItemType.ACCOUNT_BLOCK, ClearanceItemStatus.PENDING, ClearanceReturnStatus.NOT_REQUIRED);
        EmployeeAssignment assignment = new EmployeeAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setEmployee(dismissalRequest.getEmployee());
        assignment.setStartedAt(LocalDate.of(2025, 1, 1));

        when(dismissalRequestRepository.findByIdAndDeletedFalse(dismissalRequest.getId())).thenReturn(Optional.of(dismissalRequest));
        when(clearanceChecklistRepository.findByDismissalRequestIdAndDeletedFalse(dismissalRequest.getId())).thenReturn(Optional.of(checklist));
        when(clearanceChecklistItemRepository.findAllByClearanceChecklistIdAndDeletedFalseOrderBySortOrderAsc(checklist.getId())).thenReturn(List.of(passItem, accountBlockItem));
        when(dismissalEmployeeAssignmentRepository.findAllByEmployeeIdAndDeletedFalseAndEndedAtIsNullOrderByStartedAtDesc(dismissalRequest.getEmployee().getId())).thenReturn(List.of(assignment));

        DismissalCardResponse response = service.finalizeDismissal(dismissalRequest.getId(), "All done");

        assertEquals(DismissalStatus.FINALIZED, response.status());
        assertEquals("DISMISSED", dismissalRequest.getEmployee().getEmploymentStatus());
        assertEquals(dismissalRequest.getDismissalDate(), dismissalRequest.getEmployee().getDismissalDate());
        assertFalse(dismissalRequest.getEmployee().getUser().isActive());
        assertEquals(dismissalRequest.getDismissalDate(), assignment.getEndedAt());
    }

    private DismissalRequest dismissal() {
        UserAccount user = new UserAccount();
        user.setId(UUID.randomUUID());
        user.setUsername("employee1");
        user.setActive(true);

        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setPersonnelNumber("EMP-001");
        employee.setHireDate(LocalDate.of(2025, 1, 10));
        employee.setEmploymentStatus("ACTIVE");
        employee.setUser(user);

        DismissalRequest dismissalRequest = new DismissalRequest();
        dismissalRequest.setId(UUID.randomUUID());
        dismissalRequest.setEmployee(employee);
        dismissalRequest.setDismissalDate(LocalDate.of(2026, 4, 15));
        dismissalRequest.setReasonType(DismissalReasonType.RESIGNATION);
        dismissalRequest.setReasonText("By own will");
        dismissalRequest.setStatus(DismissalStatus.READY_FOR_FINALIZATION);
        return dismissalRequest;
    }

    private ClearanceChecklist checklist(DismissalRequest dismissalRequest) {
        ClearanceChecklist checklist = new ClearanceChecklist();
        checklist.setId(UUID.randomUUID());
        checklist.setDismissalRequest(dismissalRequest);
        checklist.setChecklistStatus(ClearanceChecklistStatus.IN_PROGRESS);
        return checklist;
    }

    private ClearanceChecklistItem checklistItem(ClearanceChecklist checklist, ClearanceItemType type, ClearanceItemStatus status, ClearanceReturnStatus returnStatus) {
        ClearanceChecklistItem item = new ClearanceChecklistItem();
        item.setId(UUID.randomUUID());
        item.setClearanceChecklist(checklist);
        item.setItemType(type);
        item.setItemName(type.name());
        item.setItemStatus(status);
        item.setReturnStatus(returnStatus);
        return item;
    }
}
