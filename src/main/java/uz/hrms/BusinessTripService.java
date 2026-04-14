package uz.hrms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
class BusinessTripService {

    private final BusinessTripRepository businessTripRepository;
    private final BusinessTripDocumentRepository businessTripDocumentRepository;
    private final BusinessTripApprovalRepository businessTripApprovalRepository;
    private final BusinessTripHistoryRepository businessTripHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final LocalFileStorageService localFileStorageService;
    private final ObjectMapper objectMapper;

    BusinessTripService(
        BusinessTripRepository businessTripRepository,
        BusinessTripDocumentRepository businessTripDocumentRepository,
        BusinessTripApprovalRepository businessTripApprovalRepository,
        BusinessTripHistoryRepository businessTripHistoryRepository,
        EmployeeRepository employeeRepository,
        DepartmentRepository departmentRepository,
        AuditLogRepository auditLogRepository,
        LocalFileStorageService localFileStorageService,
        ObjectMapper objectMapper
    ) {
        this.businessTripRepository = businessTripRepository;
        this.businessTripDocumentRepository = businessTripDocumentRepository;
        this.businessTripApprovalRepository = businessTripApprovalRepository;
        this.businessTripHistoryRepository = businessTripHistoryRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.localFileStorageService = localFileStorageService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    List<BusinessTripListItemResponse> list(UUID employeeId) {
        List<BusinessTrip> trips = employeeId == null
            ? businessTripRepository.findAllByDeletedFalseOrderByCreatedAtDesc()
            : businessTripRepository.findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(employeeId);
        return trips.stream().map(this::toListItem).toList();
    }

    @Transactional(readOnly = true)
    List<BusinessTripListItemResponse> listOverdueReports() {
        List<BusinessTripStatus> statuses = List.of(BusinessTripStatus.ORDER_CREATED, BusinessTripStatus.IN_PROGRESS, BusinessTripStatus.REPORT_PENDING, BusinessTripStatus.OVERDUE);
        return businessTripRepository.findAllByStatusInAndEndDateBeforeAndDeletedFalse(statuses, LocalDate.now())
            .stream()
            .filter(trip -> trip.getReportSubmittedAt() == null)
            .map(this::toListItem)
            .toList();
    }

    @Transactional(readOnly = true)
    BusinessTripResponse get(UUID id) {
        BusinessTrip trip = getTrip(id);
        return toResponse(trip);
    }

    BusinessTripResponse create(BusinessTripRequest request) {
        validateRequest(request);
        BusinessTrip trip = new BusinessTrip();
        applyTrip(trip, request);
        trip.setStatus(BusinessTripStatus.DRAFT);
        BusinessTrip saved = businessTripRepository.save(trip);
        writeHistory(saved, "CREATED", null, saved.getStatus(), "Draft created", tripPayload(saved));
        writeAudit("BUSINESS_TRIP_CREATED", saved.getId(), null, tripPayload(saved));
        return toResponse(saved);
    }

    BusinessTripResponse update(UUID id, BusinessTripRequest request) {
        validateRequest(request);
        BusinessTrip trip = getTrip(id);
        if (trip.getStatus() != BusinessTripStatus.DRAFT && trip.getStatus() != BusinessTripStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft or rejected trip can be updated");
        }
        String before = tripPayload(trip);
        applyTrip(trip, request);
        BusinessTrip saved = businessTripRepository.save(trip);
        writeHistory(saved, "UPDATED", saved.getStatus(), saved.getStatus(), "Trip updated", tripPayload(saved));
        writeAudit("BUSINESS_TRIP_UPDATED", saved.getId(), before, tripPayload(saved));
        return toResponse(saved);
    }

    BusinessTripResponse submit(UUID id) {
        BusinessTrip trip = getTrip(id);
        if (trip.getStatus() != BusinessTripStatus.DRAFT && trip.getStatus() != BusinessTripStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft or rejected trip can be submitted");
        }
        BusinessTripStatus previousStatus = trip.getStatus();
        trip.setStatus(BusinessTripStatus.ON_APPROVAL);
        BusinessTrip saved = businessTripRepository.save(trip);
        resetApprovals(saved);
        writeHistory(saved, "SUBMITTED", previousStatus, saved.getStatus(), "Trip submitted for approval", tripPayload(saved));
        writeAudit("BUSINESS_TRIP_SUBMITTED", saved.getId(), null, tripPayload(saved));
        return toResponse(saved);
    }

    BusinessTripResponse approve(UUID tripId, UUID approvalId, String commentText) {
        BusinessTrip trip = getTrip(tripId);
        BusinessTripApproval approval = getApproval(tripId, approvalId);
        if (approval.getStatus() != BusinessTripApprovalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approval step already decided");
        }
        approval.setStatus(BusinessTripApprovalStatus.APPROVED);
        approval.setDecisionComment(trimToNull(commentText));
        approval.setDecidedAt(OffsetDateTime.now());
        businessTripApprovalRepository.save(approval);
        boolean allApproved = businessTripApprovalRepository.findAllByBusinessTripIdAndDeletedFalseOrderByStepNoAsc(tripId)
            .stream()
            .allMatch(step -> step.getStatus() == BusinessTripApprovalStatus.APPROVED || step.getStatus() == BusinessTripApprovalStatus.SKIPPED);
        BusinessTripStatus previousStatus = trip.getStatus();
        if (allApproved) {
            trip.setStatus(BusinessTripStatus.APPROVED);
            businessTripRepository.save(trip);
        }
        writeHistory(trip, "APPROVED_STEP", previousStatus, trip.getStatus(), commentText, approvalPayload(approval));
        writeAudit("BUSINESS_TRIP_APPROVED_STEP", trip.getId(), null, approvalPayload(approval));
        return toResponse(trip);
    }

    BusinessTripResponse reject(UUID tripId, UUID approvalId, String commentText) {
        BusinessTrip trip = getTrip(tripId);
        BusinessTripApproval approval = getApproval(tripId, approvalId);
        if (approval.getStatus() != BusinessTripApprovalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approval step already decided");
        }
        approval.setStatus(BusinessTripApprovalStatus.REJECTED);
        approval.setDecisionComment(trimToNull(commentText));
        approval.setDecidedAt(OffsetDateTime.now());
        businessTripApprovalRepository.save(approval);
        BusinessTripStatus previousStatus = trip.getStatus();
        trip.setStatus(BusinessTripStatus.REJECTED);
        businessTripRepository.save(trip);
        writeHistory(trip, "REJECTED", previousStatus, trip.getStatus(), commentText, approvalPayload(approval));
        writeAudit("BUSINESS_TRIP_REJECTED", trip.getId(), null, approvalPayload(approval));
        return toResponse(trip);
    }

    BusinessTripPrintFormResponse generateOrder(UUID id, String templateCode) {
        BusinessTrip trip = getTrip(id);
        if (trip.getStatus() != BusinessTripStatus.APPROVED && trip.getStatus() != BusinessTripStatus.ORDER_CREATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trip must be approved before order generation");
        }
        String orderNumber = trip.getOrderNumber() == null ? nextOrderNumber() : trip.getOrderNumber();
        trip.setOrderNumber(orderNumber);
        trip.setOrderTemplateCode(StringUtils.hasText(templateCode) ? templateCode : "BUSINESS_TRIP_DEFAULT");
        trip.setOrderGeneratedAt(OffsetDateTime.now());
        trip.setOrderPrintFormHtml(buildPrintForm(trip));
        if (trip.getStatus() == BusinessTripStatus.APPROVED) {
            trip.setStatus(BusinessTripStatus.ORDER_CREATED);
        }
        businessTripRepository.save(trip);
        writeHistory(trip, "ORDER_GENERATED", BusinessTripStatus.APPROVED, trip.getStatus(), "Order generated", tripPayload(trip));
        writeAudit("BUSINESS_TRIP_ORDER_GENERATED", trip.getId(), null, tripPayload(trip));
        return new BusinessTripPrintFormResponse(trip.getId(), trip.getOrderNumber(), trip.getOrderTemplateCode(), trip.getOrderPrintFormHtml());
    }

    BusinessTripResponse submitReport(UUID id, BusinessTripReportRequest request) {
        BusinessTrip trip = getTrip(id);
        if (trip.getStatus() == BusinessTripStatus.CLOSED || trip.getStatus() == BusinessTripStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Closed or cancelled trip cannot accept report");
        }
        BusinessTripStatus previousStatus = trip.getStatus();
        trip.setReportText(request.reportText().trim());
        trip.setReportSubmittedAt(OffsetDateTime.now());
        trip.setStatus(BusinessTripStatus.REPORT_SUBMITTED);
        businessTripRepository.save(trip);
        writeHistory(trip, "REPORT_SUBMITTED", previousStatus, trip.getStatus(), "Report submitted", tripPayload(trip));
        writeAudit("BUSINESS_TRIP_REPORT_SUBMITTED", trip.getId(), null, tripPayload(trip));
        return toResponse(trip);
    }

    BusinessTripDocumentResponse uploadDocument(UUID tripId, BusinessTripDocumentUploadRequest request, MultipartFile file) {
        BusinessTrip trip = getTrip(tripId);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        StoredFileDescriptor storedFile = localFileStorageService.store("business-trips/" + tripId, file);
        List<BusinessTripDocument> previousVersions = businessTripDocumentRepository
            .findAllByBusinessTripIdAndDocumentKindAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(tripId, request.documentKind(), request.title());
        int nextVersion = previousVersions.isEmpty() ? 1 : previousVersions.get(0).getVersionNo() + 1;
        for (BusinessTripDocument previousVersion : previousVersions) {
            if (previousVersion.isCurrent()) {
                previousVersion.setCurrent(false);
                businessTripDocumentRepository.save(previousVersion);
            }
        }
        BusinessTripDocument document = new BusinessTripDocument();
        document.setBusinessTrip(trip);
        document.setDocumentKind(request.documentKind());
        document.setTitle(request.title().trim());
        document.setDescription(trimToNull(request.description()));
        document.setOriginalFileName(storedFile.fileName());
        document.setStorageKey(storedFile.storageKey());
        document.setContentType(storedFile.contentType());
        document.setSizeBytes(storedFile.sizeBytes());
        document.setVersionNo(nextVersion);
        document.setCurrent(true);
        BusinessTripDocument saved = businessTripDocumentRepository.save(document);
        writeHistory(trip, "DOCUMENT_UPLOADED", trip.getStatus(), trip.getStatus(), saved.getTitle(), documentPayload(saved));
        writeAudit("BUSINESS_TRIP_DOCUMENT_UPLOADED", trip.getId(), null, documentPayload(saved));
        return toDocumentResponse(saved);
    }

    List<BusinessTripDocumentResponse> getDocuments(UUID tripId) {
        getTrip(tripId);
        return businessTripDocumentRepository.findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(tripId)
            .stream()
            .map(this::toDocumentResponse)
            .toList();
    }

    BusinessTripDocument getDocumentEntity(UUID tripId, UUID documentId) {
        BusinessTripDocument document = businessTripDocumentRepository.findByIdAndDeletedFalse(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (document.getBusinessTrip().getId().equals(tripId) == false) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found for trip");
        }
        return document;
    }

    BusinessTripResponse close(UUID id) {
        BusinessTrip trip = getTrip(id);
        if (trip.getReportSubmittedAt() == null || StringUtils.hasText(trip.getReportText()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trip report is required before close");
        }
        boolean hasSupportingDocuments = businessTripDocumentRepository.findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(id)
            .stream()
            .anyMatch(document -> document.getDocumentKind() == BusinessTripDocumentKind.REPORT_ATTACHMENT || document.getDocumentKind() == BusinessTripDocumentKind.CONFIRMING_DOCUMENT);
        if (hasSupportingDocuments == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supporting documents are required before close");
        }
        BusinessTripStatus previousStatus = trip.getStatus();
        trip.setClosedAt(OffsetDateTime.now());
        trip.setStatus(BusinessTripStatus.CLOSED);
        businessTripRepository.save(trip);
        writeHistory(trip, "CLOSED", previousStatus, trip.getStatus(), "Trip closed", tripPayload(trip));
        writeAudit("BUSINESS_TRIP_CLOSED", trip.getId(), null, tripPayload(trip));
        return toResponse(trip);
    }

    private void validateRequest(BusinessTripRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        if (request.dailyAllowance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Daily allowance cannot be negative");
        }
        getEmployeeEntity(request.employeeId());
        if (request.requesterEmployeeId() != null) {
            getEmployeeEntity(request.requesterEmployeeId());
        }
        if (request.approverDepartmentId() != null) {
            getDepartmentEntity(request.approverDepartmentId());
        }
    }

    private void applyTrip(BusinessTrip trip, BusinessTripRequest request) {
        trip.setEmployee(getEmployeeEntity(request.employeeId()));
        trip.setRequesterEmployee(request.requesterEmployeeId() == null ? null : getEmployeeEntity(request.requesterEmployeeId()));
        trip.setApproverDepartment(request.approverDepartmentId() == null ? null : getDepartmentEntity(request.approverDepartmentId()));
        trip.setDestinationCountry(trimToNull(request.destinationCountry()));
        trip.setDestinationCity(request.destinationCity().trim());
        trip.setDestinationAddress(trimToNull(request.destinationAddress()));
        trip.setPurpose(request.purpose().trim());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());
        trip.setTransportType(trimToNull(request.transportType()));
        trip.setAccommodationDetails(trimToNull(request.accommodationDetails()));
        trip.setDailyAllowance(request.dailyAllowance());
        trip.setFundingSource(trimToNull(request.fundingSource()));
        trip.setCommentText(trimToNull(request.commentText()));
    }

    private void resetApprovals(BusinessTrip trip) {
        List<BusinessTripApproval> existing = businessTripApprovalRepository.findAllByBusinessTripIdAndDeletedFalseOrderByStepNoAsc(trip.getId());
        for (BusinessTripApproval item : existing) {
            item.setDeleted(true);
            businessTripApprovalRepository.save(item);
        }
        createApprovalStep(trip, 1, BusinessTripApprovalRole.MANAGER);
        createApprovalStep(trip, 2, BusinessTripApprovalRole.HR_ADMIN);
    }

    private void createApprovalStep(BusinessTrip trip, int stepNo, BusinessTripApprovalRole role) {
        BusinessTripApproval approval = new BusinessTripApproval();
        approval.setBusinessTrip(trip);
        approval.setStepNo(stepNo);
        approval.setApprovalRole(role);
        approval.setStatus(BusinessTripApprovalStatus.PENDING);
        businessTripApprovalRepository.save(approval);
    }

    private BusinessTripResponse toResponse(BusinessTrip trip) {
        BusinessTrip refreshed = getTrip(trip.getId());
        List<BusinessTripApprovalResponse> approvals = businessTripApprovalRepository.findAllByBusinessTripIdAndDeletedFalseOrderByStepNoAsc(refreshed.getId())
            .stream()
            .map(this::toApprovalResponse)
            .toList();
        List<BusinessTripDocumentResponse> documents = businessTripDocumentRepository.findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(refreshed.getId())
            .stream()
            .map(this::toDocumentResponse)
            .toList();
        List<BusinessTripHistoryResponse> history = businessTripHistoryRepository.findAllByBusinessTripIdAndDeletedFalseOrderByCreatedAtDesc(refreshed.getId())
            .stream()
            .map(this::toHistoryResponse)
            .toList();
        return new BusinessTripResponse(
            refreshed.getId(),
            refreshed.getEmployee().getId(),
            refreshed.getRequesterEmployee() == null ? null : refreshed.getRequesterEmployee().getId(),
            refreshed.getApproverDepartment() == null ? null : refreshed.getApproverDepartment().getId(),
            refreshed.getDestinationCountry(),
            refreshed.getDestinationCity(),
            refreshed.getDestinationAddress(),
            refreshed.getPurpose(),
            refreshed.getStartDate(),
            refreshed.getEndDate(),
            refreshed.getTransportType(),
            refreshed.getAccommodationDetails(),
            refreshed.getDailyAllowance(),
            refreshed.getFundingSource(),
            refreshed.getCommentText(),
            refreshed.getStatus(),
            refreshed.getOrderNumber(),
            refreshed.getOrderGeneratedAt(),
            refreshed.getReportSubmittedAt(),
            refreshed.getClosedAt(),
            refreshed.getPayrollSyncStatus(),
            approvals,
            documents,
            history,
            refreshed.getOrderPrintFormHtml()
        );
    }

    private BusinessTripListItemResponse toListItem(BusinessTrip trip) {
        boolean overdueReport = trip.getEndDate().isBefore(LocalDate.now()) && trip.getReportSubmittedAt() == null && trip.getStatus() != BusinessTripStatus.CLOSED && trip.getStatus() != BusinessTripStatus.CANCELLED;
        return new BusinessTripListItemResponse(
            trip.getId(),
            trip.getEmployee().getId(),
            trip.getDestinationCity(),
            trip.getPurpose(),
            trip.getStartDate(),
            trip.getEndDate(),
            trip.getStatus(),
            overdueReport
        );
    }

    private BusinessTripDocumentResponse toDocumentResponse(BusinessTripDocument document) {
        return new BusinessTripDocumentResponse(
            document.getId(),
            document.getDocumentKind(),
            document.getTitle(),
            document.getOriginalFileName(),
            document.getContentType(),
            document.getSizeBytes(),
            document.getVersionNo(),
            document.isCurrent(),
            document.getDescription()
        );
    }

    private BusinessTripApprovalResponse toApprovalResponse(BusinessTripApproval approval) {
        return new BusinessTripApprovalResponse(
            approval.getId(),
            approval.getStepNo(),
            approval.getApprovalRole(),
            approval.getApproverUser() == null ? null : approval.getApproverUser().getId(),
            approval.getStatus(),
            approval.getDecisionComment(),
            approval.getDecidedAt()
        );
    }

    private BusinessTripHistoryResponse toHistoryResponse(BusinessTripHistory history) {
        return new BusinessTripHistoryResponse(
            history.getId(),
            history.getActionType(),
            history.getStatusFrom(),
            history.getStatusTo(),
            history.getActorUser() == null ? null : history.getActorUser().getId(),
            history.getCommentText(),
            history.getCreatedAt()
        );
    }

    private BusinessTrip getTrip(UUID id) {
        return businessTripRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business trip not found"));
    }

    private BusinessTripApproval getApproval(UUID tripId, UUID approvalId) {
        BusinessTripApproval approval = businessTripApprovalRepository.findByIdAndDeletedFalse(approvalId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval not found"));
        if (approval.getBusinessTrip().getId().equals(tripId) == false) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval not found for trip");
        }
        return approval;
    }

    private Employee getEmployeeEntity(UUID employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private Department getDepartmentEntity(UUID departmentId) {
        return departmentRepository.findByIdAndDeletedFalse(departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private void writeHistory(BusinessTrip trip, String actionType, BusinessTripStatus statusFrom, BusinessTripStatus statusTo, String commentText, String payload) {
        BusinessTripHistory history = new BusinessTripHistory();
        history.setBusinessTrip(trip);
        history.setActionType(actionType);
        history.setStatusFrom(statusFrom == null ? null : statusFrom.name());
        history.setStatusTo(statusTo == null ? null : statusTo.name());
        history.setCommentText(trimToNull(commentText));
        history.setPayloadJson(payload);
        businessTripHistoryRepository.save(history);
    }

    private void writeAudit(String action, UUID tripId, String beforeData, String afterData) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntitySchema("hr");
        auditLog.setEntityTable("business_trips");
        auditLog.setEntityId(tripId);
        auditLog.setOccurredAt(OffsetDateTime.now());
        auditLog.setBeforeData(beforeData);
        auditLog.setAfterData(afterData);
        auditLogRepository.save(auditLog);
    }

    private String tripPayload(BusinessTrip trip) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", trip.getId());
        payload.put("employeeId", trip.getEmployee().getId());
        payload.put("destinationCity", trip.getDestinationCity());
        payload.put("purpose", trip.getPurpose());
        payload.put("startDate", trip.getStartDate());
        payload.put("endDate", trip.getEndDate());
        payload.put("status", trip.getStatus());
        payload.put("orderNumber", trip.getOrderNumber());
        payload.put("reportSubmittedAt", trip.getReportSubmittedAt());
        payload.put("closedAt", trip.getClosedAt());
        return toJson(payload);
    }

    private String approvalPayload(BusinessTripApproval approval) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("approvalId", approval.getId());
        payload.put("stepNo", approval.getStepNo());
        payload.put("role", approval.getApprovalRole());
        payload.put("status", approval.getStatus());
        payload.put("decisionComment", approval.getDecisionComment());
        payload.put("decidedAt", approval.getDecidedAt());
        return toJson(payload);
    }

    private String documentPayload(BusinessTripDocument document) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("documentId", document.getId());
        payload.put("title", document.getTitle());
        payload.put("kind", document.getDocumentKind());
        payload.put("versionNo", document.getVersionNo());
        payload.put("current", document.isCurrent());
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
        long sequence = businessTripRepository.count() + 1;
        return "BT-" + LocalDate.now().getYear() + "-" + String.format("%05d", sequence);
    }

    private String buildPrintForm(BusinessTrip trip) {
        return "<html><body><h1>Приказ о командировке</h1>" +
            "<p>Номер: " + trip.getOrderNumber() + "</p>" +
            "<p>Сотрудник ID: " + trip.getEmployee().getId() + "</p>" +
            "<p>Место: " + trip.getDestinationCity() + "</p>" +
            "<p>Цель: " + trip.getPurpose() + "</p>" +
            "<p>Период: " + trip.getStartDate() + " - " + trip.getEndDate() + "</p>" +
            "<p>Транспорт: " + trip.getTransportType() + "</p>" +
            "<p>Проживание: " + trip.getAccommodationDetails() + "</p>" +
            "<p>Источник финансирования: " + trip.getFundingSource() + "</p>" +
            "<p>Суточные: " + trip.getDailyAllowance() + "</p>" +
            "</body></html>";
    }

    private String trimToNull(String value) {
        if (StringUtils.hasText(value)) {
            return value.trim();
        }
        return null;
    }
}
