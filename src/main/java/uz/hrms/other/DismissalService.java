package uz.hrms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
class DismissalService {

    private final DismissalRequestRepository dismissalRequestRepository;
    private final ClearanceChecklistRepository clearanceChecklistRepository;
    private final ClearanceChecklistItemRepository clearanceChecklistItemRepository;
    private final DismissalHistoryRepository dismissalHistoryRepository;
    private final DismissalEmployeeAssignmentRepository dismissalEmployeeAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    DismissalService(
        DismissalRequestRepository dismissalRequestRepository,
        ClearanceChecklistRepository clearanceChecklistRepository,
        ClearanceChecklistItemRepository clearanceChecklistItemRepository,
        DismissalHistoryRepository dismissalHistoryRepository,
        DismissalEmployeeAssignmentRepository dismissalEmployeeAssignmentRepository,
        EmployeeRepository employeeRepository,
        DepartmentRepository departmentRepository,
        UserAccountRepository userAccountRepository,
        AuditLogRepository auditLogRepository,
        ObjectMapper objectMapper
    ) {
        this.dismissalRequestRepository = dismissalRequestRepository;
        this.clearanceChecklistRepository = clearanceChecklistRepository;
        this.clearanceChecklistItemRepository = clearanceChecklistItemRepository;
        this.dismissalHistoryRepository = dismissalHistoryRepository;
        this.dismissalEmployeeAssignmentRepository = dismissalEmployeeAssignmentRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    List<DismissalListItemResponse> list(UUID employeeId, UUID departmentId, DismissalStatus status) {
        return dismissalRequestRepository.search(employeeId, departmentId, status)
            .stream()
            .map(this::toListItem)
            .toList();
    }

    @Transactional(readOnly = true)
    DismissalCardResponse get(UUID id) {
        return toResponse(getDismissal(id));
    }

    DismissalCardResponse create(DismissalRequestUpsertRequest request) {
        Employee employee = getEmployeeEntity(request.employeeId());
        validateRequest(employee, request);
        DismissalRequest dismissalRequest = new DismissalRequest();
        applyDismissal(dismissalRequest, request, employee);
        dismissalRequest.setStatus(DismissalStatus.DRAFT);
        DismissalRequest saved = dismissalRequestRepository.save(dismissalRequest);
        writeHistory(saved, "CREATED", null, saved.getStatus(), "Dismissal draft created", dismissalPayload(saved));
        writeAudit("DISMISSAL_CREATED", saved.getId(), null, dismissalPayload(saved));
        return toResponse(saved);
    }

    DismissalCardResponse update(UUID id, DismissalRequestUpsertRequest request) {
        DismissalRequest dismissalRequest = getDismissal(id);
        if (dismissalRequest.getStatus() != DismissalStatus.DRAFT && dismissalRequest.getStatus() != DismissalStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft or rejected dismissal can be updated");
        }
        Employee employee = getEmployeeEntity(request.employeeId());
        validateRequest(employee, request);
        String before = dismissalPayload(dismissalRequest);
        applyDismissal(dismissalRequest, request, employee);
        DismissalRequest saved = dismissalRequestRepository.save(dismissalRequest);
        writeHistory(saved, "UPDATED", saved.getStatus(), saved.getStatus(), "Dismissal updated", dismissalPayload(saved));
        writeAudit("DISMISSAL_UPDATED", saved.getId(), before, dismissalPayload(saved));
        return toResponse(saved);
    }

    DismissalCardResponse submit(UUID id, String commentText) {
        DismissalRequest dismissalRequest = getDismissal(id);
        if (dismissalRequest.getStatus() != DismissalStatus.DRAFT && dismissalRequest.getStatus() != DismissalStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft or rejected dismissal can be submitted");
        }
        DismissalStatus previousStatus = dismissalRequest.getStatus();
        dismissalRequest.setStatus(DismissalStatus.ON_APPROVAL);
        dismissalRequest.setCommentText(trimToNull(commentText));
        dismissalRequestRepository.save(dismissalRequest);
        writeHistory(dismissalRequest, "SUBMITTED", previousStatus, dismissalRequest.getStatus(), commentText, dismissalPayload(dismissalRequest));
        writeAudit("DISMISSAL_SUBMITTED", dismissalRequest.getId(), null, dismissalPayload(dismissalRequest));
        return toResponse(dismissalRequest);
    }

    DismissalCardResponse approve(UUID id, String commentText) {
        DismissalRequest dismissalRequest = getDismissal(id);
        if (dismissalRequest.getStatus() != DismissalStatus.ON_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only dismissal on approval can be approved");
        }
        DismissalStatus previousStatus = dismissalRequest.getStatus();
        dismissalRequest.setStatus(DismissalStatus.APPROVED);
        dismissalRequest.setApprovedAt(OffsetDateTime.now());
        dismissalRequest.setCommentText(trimToNull(commentText));
        dismissalRequestRepository.save(dismissalRequest);
        writeHistory(dismissalRequest, "APPROVED", previousStatus, dismissalRequest.getStatus(), commentText, dismissalPayload(dismissalRequest));
        writeAudit("DISMISSAL_APPROVED", dismissalRequest.getId(), null, dismissalPayload(dismissalRequest));
        return toResponse(dismissalRequest);
    }

    DismissalCardResponse reject(UUID id, String commentText) {
        DismissalRequest dismissalRequest = getDismissal(id);
        if (dismissalRequest.getStatus() != DismissalStatus.ON_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only dismissal on approval can be rejected");
        }
        DismissalStatus previousStatus = dismissalRequest.getStatus();
        dismissalRequest.setStatus(DismissalStatus.REJECTED);
        dismissalRequest.setCommentText(trimToNull(commentText));
        dismissalRequestRepository.save(dismissalRequest);
        writeHistory(dismissalRequest, "REJECTED", previousStatus, dismissalRequest.getStatus(), commentText, dismissalPayload(dismissalRequest));
        writeAudit("DISMISSAL_REJECTED", dismissalRequest.getId(), null, dismissalPayload(dismissalRequest));
        return toResponse(dismissalRequest);
    }

    DismissalPrintFormResponse generateOrder(UUID id, String templateCode) {
        DismissalRequest dismissalRequest = getDismissal(id);
        if (dismissalRequest.getStatus() != DismissalStatus.APPROVED && dismissalRequest.getStatus() != DismissalStatus.ORDER_CREATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dismissal must be approved before order generation");
        }
        if (dismissalRequest.getOrderNumber() == null) {
            dismissalRequest.setOrderNumber(nextOrderNumber());
        }
        dismissalRequest.setOrderTemplateCode(StringUtils.hasText(templateCode) ? templateCode : "DISMISSAL_DEFAULT");
        dismissalRequest.setOrderGeneratedAt(OffsetDateTime.now());
        dismissalRequest.setOrderPrintFormHtml(buildPrintForm(dismissalRequest));
        dismissalRequest.setStatus(DismissalStatus.ORDER_CREATED);
        dismissalRequestRepository.save(dismissalRequest);
        writeHistory(dismissalRequest, "ORDER_GENERATED", DismissalStatus.APPROVED, dismissalRequest.getStatus(), "Dismissal order generated", dismissalPayload(dismissalRequest));
        writeAudit("DISMISSAL_ORDER_GENERATED", dismissalRequest.getId(), null, dismissalPayload(dismissalRequest));
        return new DismissalPrintFormResponse(
            dismissalRequest.getId(),
            dismissalRequest.getOrderNumber(),
            dismissalRequest.getOrderTemplateCode(),
            dismissalRequest.getOrderPrintFormHtml()
        );
    }

    DismissalCardResponse startClearance(UUID id) {
        DismissalRequest dismissalRequest = getDismissal(id);
        if (dismissalRequest.getStatus() != DismissalStatus.ORDER_CREATED && dismissalRequest.getStatus() != DismissalStatus.CLEARANCE_IN_PROGRESS && dismissalRequest.getStatus() != DismissalStatus.READY_FOR_FINALIZATION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clearance can be started only after order creation");
        }
        ClearanceChecklist checklist = clearanceChecklistRepository.findByDismissalRequestIdAndDeletedFalse(id).orElseGet(() -> createChecklist(dismissalRequest));
        checklist.setChecklistStatus(ClearanceChecklistStatus.IN_PROGRESS);
        if (checklist.getStartedAt() == null) {
            checklist.setStartedAt(OffsetDateTime.now());
        }
        clearanceChecklistRepository.save(checklist);
        dismissalRequest.setStatus(DismissalStatus.CLEARANCE_IN_PROGRESS);
        dismissalRequestRepository.save(dismissalRequest);
        writeHistory(dismissalRequest, "CLEARANCE_STARTED", DismissalStatus.ORDER_CREATED, dismissalRequest.getStatus(), "Clearance started", checklistPayload(checklist));
        writeAudit("DISMISSAL_CLEARANCE_STARTED", dismissalRequest.getId(), null, checklistPayload(checklist));
        return toResponse(dismissalRequest);
    }

    @Transactional(readOnly = true)
    List<ClearanceChecklistItemResponse> checklist(UUID id) {
        ClearanceChecklist checklist = getChecklistEntity(id);
        return clearanceChecklistItemRepository.findAllByClearanceChecklistIdAndDeletedFalseOrderBySortOrderAsc(checklist.getId())
            .stream()
            .map(this::toChecklistItemResponse)
            .toList();
    }

    DismissalCardResponse updateChecklistItem(UUID dismissalId, UUID itemId, DismissalChecklistItemUpdateRequest request) {
        DismissalRequest dismissalRequest = getDismissal(dismissalId);
        ClearanceChecklistItem item = clearanceChecklistItemRepository.findByIdAndDeletedFalse(itemId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist item not found"));
        if (item.getClearanceChecklist().getDismissalRequest().getId().equals(dismissalId) == false) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist item not found for dismissal");
        }
        String before = checklistItemPayload(item);
        item.setItemStatus(request.itemStatus());
        item.setReturnStatus(request.returnStatus());
        item.setResponsibleUser(request.responsibleUserId() == null ? null : getUserEntity(request.responsibleUserId()));
        item.setDueAt(request.dueAt());
        item.setAssetCode(trimToNull(request.assetCode()));
        item.setAssetName(trimToNull(request.assetName()));
        item.setCommentText(trimToNull(request.commentText()));
        item.setCompletedAt(request.itemStatus() == ClearanceItemStatus.COMPLETED || request.itemStatus() == ClearanceItemStatus.WAIVED ? OffsetDateTime.now() : null);
        clearanceChecklistItemRepository.save(item);
        refreshChecklistState(item.getClearanceChecklist(), dismissalRequest);
        writeHistory(dismissalRequest, "CHECKLIST_ITEM_UPDATED", dismissalRequest.getStatus(), dismissalRequest.getStatus(), request.commentText(), checklistItemPayload(item));
        writeAudit("DISMISSAL_CHECKLIST_ITEM_UPDATED", dismissalRequest.getId(), before, checklistItemPayload(item));
        return toResponse(dismissalRequest);
    }

    DismissalCardResponse finalizeDismissal(UUID id, String commentText) {
        DismissalRequest dismissalRequest = getDismissal(id);
        ClearanceChecklist checklist = getChecklistEntity(id);
        List<ClearanceChecklistItem> items = clearanceChecklistItemRepository.findAllByClearanceChecklistIdAndDeletedFalseOrderBySortOrderAsc(checklist.getId());
        boolean ready = items.stream()
            .filter(item -> item.getItemType() != ClearanceItemType.ACCOUNT_BLOCK)
            .allMatch(item -> item.getItemStatus() == ClearanceItemStatus.COMPLETED || item.getItemStatus() == ClearanceItemStatus.WAIVED);
        if (ready == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All checklist items must be completed before finalization");
        }
        DismissalStatus previousStatus = dismissalRequest.getStatus();
        dismissalRequest.setStatus(DismissalStatus.FINALIZED);
        dismissalRequest.setFinalizedAt(OffsetDateTime.now());
        dismissalRequest.setCommentText(trimToNull(commentText));

        Employee employee = dismissalRequest.getEmployee();
        employee.setDismissalDate(dismissalRequest.getDismissalDate());
        employee.setEmploymentStatus("DISMISSED");
        employeeRepository.save(employee);

        if (employee.getUser() != null) {
            employee.getUser().setActive(false);
            dismissalRequest.setAccountBlockedAt(OffsetDateTime.now());
        }

        for (EmployeeAssignment assignment : dismissalEmployeeAssignmentRepository.findAllByEmployeeIdAndDeletedFalseAndEndedAtIsNullOrderByStartedAtDesc(employee.getId())) {
            assignment.setEndedAt(dismissalRequest.getDismissalDate());
            dismissalEmployeeAssignmentRepository.save(assignment);
        }

        for (ClearanceChecklistItem item : items) {
            if (item.getItemType() == ClearanceItemType.ACCOUNT_BLOCK) {
                item.setItemStatus(ClearanceItemStatus.COMPLETED);
                item.setReturnStatus(ClearanceReturnStatus.NOT_REQUIRED);
                item.setCompletedAt(OffsetDateTime.now());
                clearanceChecklistItemRepository.save(item);
            }
        }
        checklist.setChecklistStatus(ClearanceChecklistStatus.COMPLETED);
        checklist.setCompletedAt(OffsetDateTime.now());
        clearanceChecklistRepository.save(checklist);
        dismissalRequestRepository.save(dismissalRequest);

        writeHistory(dismissalRequest, "FINALIZED", previousStatus, dismissalRequest.getStatus(), commentText, dismissalPayload(dismissalRequest));
        writeAudit("DISMISSAL_FINALIZED", dismissalRequest.getId(), null, dismissalPayload(dismissalRequest));
        return toResponse(dismissalRequest);
    }

    DismissalCardResponse archive(UUID id, String commentText) {
        DismissalRequest dismissalRequest = getDismissal(id);
        if (dismissalRequest.getStatus() != DismissalStatus.FINALIZED && dismissalRequest.getStatus() != DismissalStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only finalized dismissal can be archived");
        }
        DismissalStatus previousStatus = dismissalRequest.getStatus();
        dismissalRequest.setStatus(DismissalStatus.ARCHIVED);
        dismissalRequest.setArchivedAt(OffsetDateTime.now());
        dismissalRequest.setCommentText(trimToNull(commentText));
        dismissalRequest.getEmployee().setEmploymentStatus("ARCHIVED");
        employeeRepository.save(dismissalRequest.getEmployee());
        dismissalRequestRepository.save(dismissalRequest);
        writeHistory(dismissalRequest, "ARCHIVED", previousStatus, dismissalRequest.getStatus(), commentText, dismissalPayload(dismissalRequest));
        writeAudit("DISMISSAL_ARCHIVED", dismissalRequest.getId(), null, dismissalPayload(dismissalRequest));
        return toResponse(dismissalRequest);
    }

    private void validateRequest(Employee employee, DismissalRequestUpsertRequest request) {
        if (request.dismissalDate().isBefore(employee.getHireDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dismissal date cannot be before hire date");
        }
        if (StringUtils.hasText(request.reasonText()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is required");
        }
        if (request.initiatorEmployeeId() != null) {
            getEmployeeEntity(request.initiatorEmployeeId());
        }
        if (request.departmentId() != null) {
            getDepartmentEntity(request.departmentId());
        }
    }

    private void applyDismissal(DismissalRequest dismissalRequest, DismissalRequestUpsertRequest request, Employee employee) {
        dismissalRequest.setEmployee(employee);
        dismissalRequest.setInitiatorEmployee(request.initiatorEmployeeId() == null ? null : getEmployeeEntity(request.initiatorEmployeeId()));
        dismissalRequest.setDepartment(request.departmentId() == null ? null : getDepartmentEntity(request.departmentId()));
        dismissalRequest.setReasonType(request.reasonType());
        dismissalRequest.setReasonText(request.reasonText().trim());
        dismissalRequest.setDismissalDate(request.dismissalDate());
        dismissalRequest.setCommentText(trimToNull(request.commentText()));
    }

    private ClearanceChecklist createChecklist(DismissalRequest dismissalRequest) {
        ClearanceChecklist checklist = new ClearanceChecklist();
        checklist.setDismissalRequest(dismissalRequest);
        checklist.setChecklistStatus(ClearanceChecklistStatus.OPEN);
        checklist.setStartedAt(OffsetDateTime.now());
        ClearanceChecklist savedChecklist = clearanceChecklistRepository.save(checklist);

        seedChecklistItem(savedChecklist, 1, ClearanceItemType.PASS, "Возврат пропуска", "SECURITY_OPERATOR");
        seedChecklistItem(savedChecklist, 2, ClearanceItemType.LAPTOP, "Возврат ноутбука", "SECURITY_OPERATOR");
        seedChecklistItem(savedChecklist, 3, ClearanceItemType.PHONE, "Возврат телефона", "SECURITY_OPERATOR");
        seedChecklistItem(savedChecklist, 4, ClearanceItemType.SIM_CARD, "Возврат SIM-карты", "SECURITY_OPERATOR");
        seedChecklistItem(savedChecklist, 5, ClearanceItemType.DOCUMENTS, "Возврат документов", "HR_INSPECTOR");
        seedChecklistItem(savedChecklist, 6, ClearanceItemType.BOOKS, "Возврат книг библиотеки", "HR_ADMIN");
        seedChecklistItem(savedChecklist, 7, ClearanceItemType.OTHER_ASSET, "Возврат прочих ТМЦ", "HR_ADMIN");
        seedChecklistItem(savedChecklist, 8, ClearanceItemType.ACTIVE_TASKS, "Закрытие активных задач", "MANAGER");
        seedChecklistItem(savedChecklist, 9, ClearanceItemType.FINAL_PAYROLL, "Финальный расчет", "PAYROLL_SPECIALIST");
        seedChecklistItem(savedChecklist, 10, ClearanceItemType.LMS_ACCESS, "Закрытие LMS-доступа", "HR_ADMIN");
        seedChecklistItem(savedChecklist, 11, ClearanceItemType.ACCOUNT_BLOCK, "Блокировка учетной записи", "SUPER_ADMIN");
        return savedChecklist;
    }

    private void seedChecklistItem(ClearanceChecklist checklist, int sortOrder, ClearanceItemType itemType, String itemName, String responsibleRole) {
        ClearanceChecklistItem item = new ClearanceChecklistItem();
        item.setClearanceChecklist(checklist);
        item.setSortOrder(sortOrder);
        item.setItemType(itemType);
        item.setItemName(itemName);
        item.setItemStatus(ClearanceItemStatus.PENDING);
        item.setReturnStatus(
            itemType == ClearanceItemType.FINAL_PAYROLL || itemType == ClearanceItemType.LMS_ACCESS || itemType == ClearanceItemType.ACCOUNT_BLOCK || itemType == ClearanceItemType.ACTIVE_TASKS
                ? ClearanceReturnStatus.NOT_REQUIRED
                : ClearanceReturnStatus.PENDING
        );
        item.setResponsibleRole(responsibleRole);
        clearanceChecklistItemRepository.save(item);
    }

    private void refreshChecklistState(ClearanceChecklist checklist, DismissalRequest dismissalRequest) {
        List<ClearanceChecklistItem> items = clearanceChecklistItemRepository.findAllByClearanceChecklistIdAndDeletedFalseOrderBySortOrderAsc(checklist.getId());
        boolean blocked = items.stream().anyMatch(item -> item.getItemStatus() == ClearanceItemStatus.BLOCKED);
        boolean ready = items.stream()
            .filter(item -> item.getItemType() != ClearanceItemType.ACCOUNT_BLOCK)
            .allMatch(item -> item.getItemStatus() == ClearanceItemStatus.COMPLETED || item.getItemStatus() == ClearanceItemStatus.WAIVED);
        if (blocked) {
            checklist.setChecklistStatus(ClearanceChecklistStatus.BLOCKED);
        } else if (ready) {
            checklist.setChecklistStatus(ClearanceChecklistStatus.IN_PROGRESS);
            dismissalRequest.setStatus(DismissalStatus.READY_FOR_FINALIZATION);
        } else {
            checklist.setChecklistStatus(ClearanceChecklistStatus.IN_PROGRESS);
            dismissalRequest.setStatus(DismissalStatus.CLEARANCE_IN_PROGRESS);
        }
        clearanceChecklistRepository.save(checklist);
        dismissalRequestRepository.save(dismissalRequest);
    }

    private DismissalCardResponse toResponse(DismissalRequest dismissalRequest) {
        DismissalRequest refreshed = getDismissal(dismissalRequest.getId());
        ClearanceChecklist checklist = clearanceChecklistRepository.findByDismissalRequestIdAndDeletedFalse(refreshed.getId()).orElse(null);
        List<ClearanceChecklistItemResponse> checklistItems = checklist == null
            ? List.of()
            : clearanceChecklistItemRepository.findAllByClearanceChecklistIdAndDeletedFalseOrderBySortOrderAsc(checklist.getId())
                .stream()
                .map(this::toChecklistItemResponse)
                .toList();
        List<DismissalHistoryResponse> history = dismissalHistoryRepository.findAllByDismissalRequestIdAndDeletedFalseOrderByCreatedAtDesc(refreshed.getId())
            .stream()
            .map(this::toHistoryResponse)
            .toList();
        return new DismissalCardResponse(
            refreshed.getId(),
            refreshed.getEmployee().getId(),
            refreshed.getInitiatorEmployee() == null ? null : refreshed.getInitiatorEmployee().getId(),
            refreshed.getDepartment() == null ? null : refreshed.getDepartment().getId(),
            refreshed.getReasonType(),
            refreshed.getReasonText(),
            refreshed.getDismissalDate(),
            refreshed.getStatus(),
            refreshed.getOrderNumber(),
            refreshed.getOrderTemplateCode(),
            refreshed.getOrderGeneratedAt(),
            refreshed.getOrderPrintFormHtml(),
            refreshed.getApprovedAt(),
            refreshed.getFinalizedAt(),
            refreshed.getArchivedAt(),
            refreshed.getAccountBlockedAt(),
            refreshed.getFinalPayrollSyncStatus(),
            refreshed.getCommentText(),
            checklist == null ? null : checklist.getChecklistStatus(),
            checklistItems,
            history
        );
    }

    private DismissalListItemResponse toListItem(DismissalRequest dismissalRequest) {
        return new DismissalListItemResponse(
            dismissalRequest.getId(),
            dismissalRequest.getEmployee().getId(),
            dismissalRequest.getDepartment() == null ? null : dismissalRequest.getDepartment().getId(),
            dismissalRequest.getReasonType(),
            dismissalRequest.getDismissalDate(),
            dismissalRequest.getStatus(),
            dismissalRequest.getOrderNumber(),
            dismissalRequest.getArchivedAt() != null
        );
    }

    private ClearanceChecklistItemResponse toChecklistItemResponse(ClearanceChecklistItem item) {
        return new ClearanceChecklistItemResponse(
            item.getId(),
            item.getItemType(),
            item.getItemName(),
            item.getItemStatus(),
            item.getReturnStatus(),
            item.getResponsibleRole(),
            item.getResponsibleUser() == null ? null : item.getResponsibleUser().getId(),
            item.getDueAt(),
            item.getCompletedAt(),
            item.getAssetCode(),
            item.getAssetName(),
            item.getCommentText(),
            item.getSortOrder()
        );
    }

    private DismissalHistoryResponse toHistoryResponse(DismissalHistory history) {
        return new DismissalHistoryResponse(
            history.getId(),
            history.getActionType(),
            history.getStatusFrom(),
            history.getStatusTo(),
            history.getActorUser() == null ? null : history.getActorUser().getId(),
            history.getCommentText(),
            history.getCreatedAt()
        );
    }

    private DismissalRequest getDismissal(UUID id) {
        return dismissalRequestRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dismissal request not found"));
    }

    private ClearanceChecklist getChecklistEntity(UUID dismissalRequestId) {
        return clearanceChecklistRepository.findByDismissalRequestIdAndDeletedFalse(dismissalRequestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clearance checklist not found"));
    }

    private Employee getEmployeeEntity(UUID employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private Department getDepartmentEntity(UUID departmentId) {
        return departmentRepository.findByIdAndDeletedFalse(departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private UserAccount getUserEntity(UUID userId) {
        return userAccountRepository.findByIdAndDeletedFalse(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void writeHistory(DismissalRequest dismissalRequest, String actionType, DismissalStatus statusFrom, DismissalStatus statusTo, String commentText, String payload) {
        DismissalHistory history = new DismissalHistory();
        history.setDismissalRequest(dismissalRequest);
        history.setActionType(actionType);
        history.setStatusFrom(statusFrom == null ? null : statusFrom.name());
        history.setStatusTo(statusTo == null ? null : statusTo.name());
        history.setCommentText(trimToNull(commentText));
        history.setPayloadJson(payload);
        dismissalHistoryRepository.save(history);
    }

    private void writeAudit(String action, UUID dismissalRequestId, String beforeData, String afterData) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntitySchema("hr");
        auditLog.setEntityTable("dismissal_requests");
        auditLog.setEntityId(dismissalRequestId);
        auditLog.setOccurredAt(OffsetDateTime.now());
        auditLog.setBeforeData(beforeData);
        auditLog.setAfterData(afterData);
        auditLogRepository.save(auditLog);
    }

    private String dismissalPayload(DismissalRequest dismissalRequest) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", dismissalRequest.getId());
        payload.put("employeeId", dismissalRequest.getEmployee().getId());
        payload.put("departmentId", dismissalRequest.getDepartment() == null ? null : dismissalRequest.getDepartment().getId());
        payload.put("reasonType", dismissalRequest.getReasonType());
        payload.put("dismissalDate", dismissalRequest.getDismissalDate());
        payload.put("status", dismissalRequest.getStatus());
        payload.put("orderNumber", dismissalRequest.getOrderNumber());
        payload.put("finalizedAt", dismissalRequest.getFinalizedAt());
        payload.put("archivedAt", dismissalRequest.getArchivedAt());
        return toJson(payload);
    }

    private String checklistPayload(ClearanceChecklist checklist) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("checklistId", checklist.getId());
        payload.put("dismissalRequestId", checklist.getDismissalRequest().getId());
        payload.put("checklistStatus", checklist.getChecklistStatus());
        payload.put("startedAt", checklist.getStartedAt());
        payload.put("completedAt", checklist.getCompletedAt());
        return toJson(payload);
    }

    private String checklistItemPayload(ClearanceChecklistItem item) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("itemId", item.getId());
        payload.put("itemType", item.getItemType());
        payload.put("itemStatus", item.getItemStatus());
        payload.put("returnStatus", item.getReturnStatus());
        payload.put("assetCode", item.getAssetCode());
        payload.put("assetName", item.getAssetName());
        return toJson(payload);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize payload");
        }
    }

    private String nextOrderNumber() {
        long sequence = dismissalRequestRepository.count() + 1;
        return "DISM-" + LocalDate.now().getYear() + "-" + String.format("%05d", sequence);
    }

    private String buildPrintForm(DismissalRequest dismissalRequest) {
        return "<html><body><h1>Приказ об увольнении</h1>" +
            "<p>Номер: " + dismissalRequest.getOrderNumber() + "</p>" +
            "<p>Сотрудник ID: " + dismissalRequest.getEmployee().getId() + "</p>" +
            "<p>Основание: " + dismissalRequest.getReasonType() + "</p>" +
            "<p>Причина: " + dismissalRequest.getReasonText() + "</p>" +
            "<p>Дата увольнения: " + dismissalRequest.getDismissalDate() + "</p>" +
            "</body></html>";
    }

    private String trimToNull(String value) {
        if (StringUtils.hasText(value) == false) {
            return null;
        }
        return value.trim();
    }
}
