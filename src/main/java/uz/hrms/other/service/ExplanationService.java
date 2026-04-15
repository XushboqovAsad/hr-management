package uz.hrms.other;

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
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.entity.*;
import uz.hrms.other.repository.AuditLogRepository;
import uz.hrms.other.repository.*;
import uz.hrms.other.enums.*;

@Service
@Transactional
class ExplanationTaskBootstrapService {

    private final ExplanationIncidentRepository explanationIncidentRepository;
    private final ExplanationRepository explanationRepository;
    private final ExplanationHistoryRepository explanationHistoryRepository;
    private final HrNotificationRepository hrNotificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    ExplanationTaskBootstrapService(
        ExplanationIncidentRepository explanationIncidentRepository,
        ExplanationRepository explanationRepository,
        ExplanationHistoryRepository explanationHistoryRepository,
        HrNotificationRepository hrNotificationRepository,
        AuditLogRepository auditLogRepository,
        ObjectMapper objectMapper
    ) {
        this.explanationIncidentRepository = explanationIncidentRepository;
        this.explanationRepository = explanationRepository;
        this.explanationHistoryRepository = explanationHistoryRepository;
        this.hrNotificationRepository = hrNotificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    void bootstrapFromAttendanceIncident(AttendanceIncident attendanceIncident, AttendanceSummary summary, AttendanceViolationType violationType) {
        ExplanationIncident existing = explanationIncidentRepository.findByAttendanceIncidentIdAndDeletedFalse(attendanceIncident.getId()).orElse(null);
        if (existing != null) {
            return;
        }
        ExplanationIncident incident = new ExplanationIncident();
        incident.setEmployee(attendanceIncident.getEmployee());
        incident.setDepartment(summary.getDepartment());
        incident.setAttendanceIncident(attendanceIncident);
        incident.setIncidentSource(ExplanationIncidentSource.SCUD);
        incident.setIncidentType(violationType.name());
        incident.setTitle("Предоставить объяснительную: " + violationType.name());
        incident.setDescription(StringUtils.hasText(attendanceIncident.getDescription()) ? attendanceIncident.getDescription() : "Автоматически создано по данным посещаемости");
        incident.setOccurredAt(summary.getFinalizedAt() == null ? OffsetDateTime.now() : summary.getFinalizedAt());
        incident.setStatus(ExplanationIncidentStatus.PENDING_EXPLANATION);
        incident.setExplanationRequired(true);
        incident.setDueAt(attendanceIncident.getDueAt());
        ExplanationIncident savedIncident = explanationIncidentRepository.save(incident);

        Explanation explanation = new Explanation();
        explanation.setExplanationIncident(savedIncident);
        explanation.setEmployee(savedIncident.getEmployee());
        explanation.setStatus(ExplanationStatus.DRAFT);
        Explanation savedExplanation = explanationRepository.save(explanation);

        writeHistory(savedIncident, savedExplanation, "TASK_CREATED", null, savedIncident.getStatus().name(), "Автоматическая задача на объяснительную", incidentPayload(savedIncident));
        notifyEmployee(savedIncident.getEmployee(), "EXPLANATION_TASK", "Нужно предоставить объяснительную", "По сотруднику создан инцидент, требуется объяснительная.", savedIncident.getId(), incidentPayload(savedIncident));
        writeAudit("EXPLANATION_TASK_CREATED", savedIncident.getId(), null, incidentPayload(savedIncident));
    }

    private void writeHistory(ExplanationIncident incident, Explanation explanation, String actionType, String statusFrom, String statusTo, String commentText, String payload) {
        ExplanationHistory history = new ExplanationHistory();
        history.setExplanationIncident(incident);
        history.setExplanation(explanation);
        history.setActionType(actionType);
        history.setStatusFrom(statusFrom);
        history.setStatusTo(statusTo);
        history.setCommentText(commentText);
        history.setPayloadJson(payload);
        explanationHistoryRepository.save(history);
    }

    private void notifyEmployee(Employee employee, String notificationType, String title, String body, UUID entityId, String payload) {
        HrNotification notification = new HrNotification();
        notification.setRecipientEmployee(employee);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setEntityType("EXPLANATION_INCIDENT");
        notification.setEntityId(entityId);
        notification.setStatus(NotificationStatus.NEW);
        notification.setPayloadJson(payload);
        hrNotificationRepository.save(notification);
    }

    private void writeAudit(String action, UUID entityId, String beforeData, String afterData) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntitySchema("hr");
        auditLog.setEntityTable("explanation_incidents");
        auditLog.setEntityId(entityId);
        auditLog.setOccurredAt(OffsetDateTime.now());
        auditLog.setBeforeData(beforeData);
        auditLog.setAfterData(afterData);
        auditLogRepository.save(auditLog);
    }

    private String incidentPayload(ExplanationIncident incident) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("incidentId", incident.getId());
        payload.put("employeeId", incident.getEmployee().getId());
        payload.put("departmentId", incident.getDepartment() == null ? null : incident.getDepartment().getId());
        payload.put("incidentType", incident.getIncidentType());
        payload.put("status", incident.getStatus());
        payload.put("dueAt", incident.getDueAt());
        return toJson(payload);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize payload");
        }
    }
}

@Service
@Transactional
class ExplanationService {

    private final ExplanationIncidentRepository explanationIncidentRepository;
    private final ExplanationRepository explanationRepository;
    private final ExplanationDocumentRepository explanationDocumentRepository;
    private final ExplanationHistoryRepository explanationHistoryRepository;
    private final DisciplinaryActionRepository disciplinaryActionRepository;
    private final RewardActionRepository rewardActionRepository;
    private final HrNotificationRepository hrNotificationRepository;
    private final AttendanceIncidentRepository attendanceIncidentRepository;
    private final AttendanceViolationRepository attendanceViolationRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final LocalFileStorageService localFileStorageService;
    private final ObjectMapper objectMapper;

    ExplanationService(
        ExplanationIncidentRepository explanationIncidentRepository,
        ExplanationRepository explanationRepository,
        ExplanationDocumentRepository explanationDocumentRepository,
        ExplanationHistoryRepository explanationHistoryRepository,
        DisciplinaryActionRepository disciplinaryActionRepository,
        RewardActionRepository rewardActionRepository,
        HrNotificationRepository hrNotificationRepository,
        AttendanceIncidentRepository attendanceIncidentRepository,
        AttendanceViolationRepository attendanceViolationRepository,
        EmployeeRepository employeeRepository,
        DepartmentRepository departmentRepository,
        AuditLogRepository auditLogRepository,
        LocalFileStorageService localFileStorageService,
        ObjectMapper objectMapper
    ) {
        this.explanationIncidentRepository = explanationIncidentRepository;
        this.explanationRepository = explanationRepository;
        this.explanationDocumentRepository = explanationDocumentRepository;
        this.explanationHistoryRepository = explanationHistoryRepository;
        this.disciplinaryActionRepository = disciplinaryActionRepository;
        this.rewardActionRepository = rewardActionRepository;
        this.hrNotificationRepository = hrNotificationRepository;
        this.attendanceIncidentRepository = attendanceIncidentRepository;
        this.attendanceViolationRepository = attendanceViolationRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.localFileStorageService = localFileStorageService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    List<ExplanationInboxItemResponse> inbox(UUID employeeId, UUID departmentId, ExplanationStatus explanationStatus) {
        List<ExplanationIncident> incidents = employeeId != null
            ? explanationIncidentRepository.findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(employeeId)
            : departmentId != null
                ? explanationIncidentRepository.findAllByDepartmentIdAndDeletedFalseOrderByCreatedAtDesc(departmentId)
                : explanationIncidentRepository.findAllByDeletedFalseOrderByCreatedAtDesc();
        List<ExplanationInboxItemResponse> result = new ArrayList<>();
        for (ExplanationIncident incident : incidents) {
            Explanation explanation = explanationRepository.findByExplanationIncidentIdAndDeletedFalse(incident.getId()).orElse(null);
            ExplanationStatus currentStatus = explanation == null ? ExplanationStatus.DRAFT : explanation.getStatus();
            if (explanationStatus != null && currentStatus != explanationStatus) {
                continue;
            }
            boolean disciplinaryCreated = disciplinaryActionRepository.findAllByEmployeeIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(incident.getEmployee().getId())
                .stream()
                .anyMatch(item -> item.getExplanationIncident() != null && item.getExplanationIncident().getId().equals(incident.getId()));
            boolean overdue = incident.getDueAt() != null && incident.getDueAt().isBefore(OffsetDateTime.now())
                && incident.getStatus() != ExplanationIncidentStatus.RESOLVED
                && incident.getStatus() != ExplanationIncidentStatus.WAIVED;
            result.add(new ExplanationInboxItemResponse(
                incident.getId(),
                incident.getEmployee().getId(),
                incident.getDepartment() == null ? null : incident.getDepartment().getId(),
                incident.getIncidentSource(),
                incident.getIncidentType(),
                incident.getTitle(),
                incident.getOccurredAt(),
                incident.getDueAt(),
                incident.getStatus(),
                currentStatus,
                overdue,
                disciplinaryCreated
            ));
        }
        return result;
    }

    @Transactional(readOnly = true)
    ExplanationCardResponse get(UUID incidentId) {
        return toCardResponse(getIncident(incidentId));
    }

    ExplanationCardResponse createManualIncident(ExplanationIncidentCreateRequest request) {
        if (request.incidentSource() == ExplanationIncidentSource.SCUD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SCUD incidents are created automatically");
        }
        Employee employee = getEmployeeEntity(request.employeeId());
        Department department = request.departmentId() == null ? null : getDepartmentEntity(request.departmentId());
        Employee manager = request.managerEmployeeId() == null ? null : getEmployeeEntity(request.managerEmployeeId());

        ExplanationIncident incident = new ExplanationIncident();
        incident.setEmployee(employee);
        incident.setDepartment(department);
        incident.setManagerEmployee(manager);
        incident.setIncidentSource(request.incidentSource());
        incident.setIncidentType(request.incidentType().trim());
        incident.setTitle(request.title().trim());
        incident.setDescription(trimToNull(request.description()));
        incident.setOccurredAt(request.occurredAt());
        incident.setStatus(ExplanationIncidentStatus.PENDING_EXPLANATION);
        incident.setExplanationRequired(true);
        incident.setDueAt(request.dueAt());
        ExplanationIncident savedIncident = explanationIncidentRepository.save(incident);

        Explanation explanation = new Explanation();
        explanation.setExplanationIncident(savedIncident);
        explanation.setEmployee(employee);
        explanation.setStatus(ExplanationStatus.DRAFT);
        Explanation savedExplanation = explanationRepository.save(explanation);

        writeHistory(savedIncident, savedExplanation, "INCIDENT_CREATED", null, savedIncident.getStatus().name(), "Создан ручной инцидент", incidentPayload(savedIncident));
        notifyEmployee(employee, "EXPLANATION_TASK", "Нужно предоставить объяснительную", savedIncident.getTitle(), savedIncident.getId(), incidentPayload(savedIncident));
        writeAudit("EXPLANATION_INCIDENT_CREATED", savedIncident.getId(), null, incidentPayload(savedIncident));
        return toCardResponse(savedIncident);
    }

    ExplanationCardResponse submit(UUID incidentId, ExplanationSubmitRequest request) {
        ExplanationIncident incident = getIncident(incidentId);
        Explanation explanation = ensureExplanation(incident);
        if (incident.getExplanationRequired() == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explanation is not required for this incident");
        }
        String before = explanationPayload(explanation);
        explanation.setExplanationText(request.explanationText().trim());
        explanation.setEmployeeSubmittedAt(OffsetDateTime.now());
        explanation.setStatus(ExplanationStatus.SUBMITTED);
        explanationRepository.save(explanation);

        ExplanationIncidentStatus previousStatus = incident.getStatus();
        incident.setStatus(ExplanationIncidentStatus.UNDER_REVIEW);
        explanationIncidentRepository.save(incident);
        syncAttendanceIncidentStatus(incident, AttendanceIncidentStatus.UNDER_REVIEW, AttendanceViolationStatus.EXPLAINED);

        writeHistory(incident, explanation, "EXPLANATION_SUBMITTED", previousStatus.name(), incident.getStatus().name(), "Сотрудник отправил объяснительную", explanationPayload(explanation));
        if (incident.getManagerEmployee() != null) {
            notifyEmployee(incident.getManagerEmployee(), "EXPLANATION_REVIEW", "Нужно рассмотреть объяснительную", incident.getTitle(), incident.getId(), explanationPayload(explanation));
        }
        writeAudit("EXPLANATION_SUBMITTED", incident.getId(), before, explanationPayload(explanation));
        return toCardResponse(incident);
    }

    ExplanationCardResponse managerReview(UUID incidentId, ExplanationManagerReviewRequest request) {
        ExplanationIncident incident = getIncident(incidentId);
        Explanation explanation = ensureSubmittedExplanation(incident);
        String before = explanationPayload(explanation);
        explanation.setManagerReviewerEmployee(request.managerReviewerEmployeeId() == null ? null : getEmployeeEntity(request.managerReviewerEmployeeId()));
        explanation.setManagerReviewComment(request.managerComment().trim());
        explanation.setManagerReviewedAt(OffsetDateTime.now());
        explanation.setStatus(ExplanationStatus.MANAGER_REVIEWED);
        explanationRepository.save(explanation);
        writeHistory(incident, explanation, "MANAGER_REVIEWED", incident.getStatus().name(), incident.getStatus().name(), request.managerComment(), explanationPayload(explanation));
        notifyEmployee(explanation.getEmployee(), "EXPLANATION_MANAGER_REVIEWED", "Объяснительная рассмотрена руководителем", incident.getTitle(), incident.getId(), explanationPayload(explanation));
        writeAudit("EXPLANATION_MANAGER_REVIEWED", incident.getId(), before, explanationPayload(explanation));
        return toCardResponse(incident);
    }

    ExplanationCardResponse accept(UUID incidentId, ExplanationDecisionRequest request) {
        ExplanationIncident incident = getIncident(incidentId);
        Explanation explanation = ensureReviewableExplanation(incident);
        String before = explanationPayload(explanation);
        applyHrDecision(explanation, request);
        explanation.setStatus(ExplanationStatus.ACCEPTED);
        explanationRepository.save(explanation);
        ExplanationIncidentStatus previousStatus = incident.getStatus();
        incident.setStatus(ExplanationIncidentStatus.WAIVED);
        explanationIncidentRepository.save(incident);
        syncAttendanceIncidentStatus(incident, AttendanceIncidentStatus.WAIVED, AttendanceViolationStatus.WAIVED);
        writeHistory(incident, explanation, "HR_ACCEPTED", previousStatus.name(), incident.getStatus().name(), request == null ? null : request.hrComment(), explanationPayload(explanation));
        notifyEmployee(explanation.getEmployee(), "EXPLANATION_ACCEPTED", "Объяснение принято", incident.getTitle(), incident.getId(), explanationPayload(explanation));
        writeAudit("EXPLANATION_ACCEPTED", incident.getId(), before, explanationPayload(explanation));
        return toCardResponse(incident);
    }

    ExplanationCardResponse reject(UUID incidentId, ExplanationDecisionRequest request) {
        ExplanationIncident incident = getIncident(incidentId);
        Explanation explanation = ensureReviewableExplanation(incident);
        String before = explanationPayload(explanation);
        applyHrDecision(explanation, request);
        explanation.setStatus(ExplanationStatus.REJECTED);
        explanationRepository.save(explanation);
        ExplanationIncidentStatus previousStatus = incident.getStatus();
        incident.setStatus(ExplanationIncidentStatus.RESOLVED);
        explanationIncidentRepository.save(incident);
        syncAttendanceIncidentStatus(incident, AttendanceIncidentStatus.RESOLVED, AttendanceViolationStatus.CLOSED);
        writeHistory(incident, explanation, "HR_REJECTED", previousStatus.name(), incident.getStatus().name(), request == null ? null : request.hrComment(), explanationPayload(explanation));
        notifyEmployee(explanation.getEmployee(), "EXPLANATION_REJECTED", "Объяснение отклонено", incident.getTitle(), incident.getId(), explanationPayload(explanation));
        writeAudit("EXPLANATION_REJECTED", incident.getId(), before, explanationPayload(explanation));
        return toCardResponse(incident);
    }

    ExplanationCardResponse closeNoConsequence(UUID incidentId, ExplanationDecisionRequest request) {
        ExplanationIncident incident = getIncident(incidentId);
        Explanation explanation = ensureReviewableExplanation(incident);
        String before = explanationPayload(explanation);
        applyHrDecision(explanation, request);
        explanation.setStatus(ExplanationStatus.CLOSED_NO_CONSEQUENCE);
        explanationRepository.save(explanation);
        ExplanationIncidentStatus previousStatus = incident.getStatus();
        incident.setStatus(ExplanationIncidentStatus.WAIVED);
        explanationIncidentRepository.save(incident);
        syncAttendanceIncidentStatus(incident, AttendanceIncidentStatus.WAIVED, AttendanceViolationStatus.WAIVED);
        writeHistory(incident, explanation, "CLOSED_NO_CONSEQUENCE", previousStatus.name(), incident.getStatus().name(), request == null ? null : request.hrComment(), explanationPayload(explanation));
        notifyEmployee(explanation.getEmployee(), "EXPLANATION_CLOSED", "Инцидент закрыт без последствий", incident.getTitle(), incident.getId(), explanationPayload(explanation));
        writeAudit("EXPLANATION_CLOSED_NO_CONSEQUENCE", incident.getId(), before, explanationPayload(explanation));
        return toCardResponse(incident);
    }

    ExplanationCardResponse createDisciplinaryAction(UUID incidentId, ExplanationDisciplinaryActionRequest request) {
        ExplanationIncident incident = getIncident(incidentId);
        Explanation explanation = ensureReviewableExplanation(incident);
        if (request.validUntil() != null && request.validUntil().isBefore(request.actionDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validUntil cannot be before actionDate");
        }
        DisciplinaryAction action = new DisciplinaryAction();
        action.setEmployee(explanation.getEmployee());
        action.setDepartment(incident.getDepartment());
        action.setExplanationIncident(incident);
        action.setExplanation(explanation);
        action.setActionType(request.actionType());
        action.setActionDate(request.actionDate());
        action.setReasonText(request.reasonText().trim());
        action.setStatus(DisciplinaryActionStatus.ACTIVE);
        action.setValidUntil(request.validUntil());
        action.setSourceOrderId(request.sourceOrderId());
        action.setSourceOrderNumber(trimToNull(request.sourceOrderNumber()));
        disciplinaryActionRepository.save(action);

        String before = explanationPayload(explanation);
        explanation.setHrReviewerEmployee(request.hrReviewerEmployeeId() == null ? null : getEmployeeEntity(request.hrReviewerEmployeeId()));
        explanation.setHrDecisionComment(trimToNull(request.hrComment()));
        explanation.setHrDecidedAt(OffsetDateTime.now());
        explanation.setStatus(ExplanationStatus.DISCIPLINARY_ACTION_CREATED);
        explanation.setSourceOrderId(request.sourceOrderId());
        explanation.setSourceOrderNumber(trimToNull(request.sourceOrderNumber()));
        explanationRepository.save(explanation);

        ExplanationIncidentStatus previousStatus = incident.getStatus();
        incident.setStatus(ExplanationIncidentStatus.RESOLVED);
        explanationIncidentRepository.save(incident);
        syncAttendanceIncidentStatus(incident, AttendanceIncidentStatus.RESOLVED, AttendanceViolationStatus.CLOSED);
        writeHistory(incident, explanation, "DISCIPLINARY_ACTION_CREATED", previousStatus.name(), incident.getStatus().name(), request.reasonText(), disciplinaryActionPayload(action));
        notifyEmployee(explanation.getEmployee(), "DISCIPLINARY_ACTION_CREATED", "Создано дисциплинарное взыскание", incident.getTitle(), incident.getId(), disciplinaryActionPayload(action));
        writeAudit("DISCIPLINARY_ACTION_CREATED", incident.getId(), before, disciplinaryActionPayload(action));
        return toCardResponse(incident);
    }

    ExplanationDocumentResponse uploadDocument(UUID incidentId, ExplanationDocumentUploadRequest request, MultipartFile file) {
        ExplanationIncident incident = getIncident(incidentId);
        Explanation explanation = ensureExplanation(incident);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        StoredFileDescriptor storedFile = localFileStorageService.store("explanations/" + incidentId, file);
        List<ExplanationDocument> previousVersions = explanationDocumentRepository.findAllByExplanationIdAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(explanation.getId(), request.title());
        int nextVersion = previousVersions.isEmpty() ? 1 : previousVersions.get(0).getVersionNo() + 1;
        for (ExplanationDocument previousVersion : previousVersions) {
            if (previousVersion.isCurrent()) {
                previousVersion.setCurrent(false);
                explanationDocumentRepository.save(previousVersion);
            }
        }
        ExplanationDocument document = new ExplanationDocument();
        document.setExplanation(explanation);
        document.setTitle(request.title().trim());
        document.setDescription(trimToNull(request.description()));
        document.setOriginalFileName(storedFile.fileName());
        document.setStorageKey(storedFile.storageKey());
        document.setContentType(storedFile.contentType());
        document.setSizeBytes(storedFile.sizeBytes());
        document.setVersionNo(nextVersion);
        document.setCurrent(true);
        ExplanationDocument saved = explanationDocumentRepository.save(document);
        writeHistory(incident, explanation, "DOCUMENT_UPLOADED", incident.getStatus().name(), incident.getStatus().name(), saved.getTitle(), documentPayload(saved));
        writeAudit("EXPLANATION_DOCUMENT_UPLOADED", incident.getId(), null, documentPayload(saved));
        return toDocumentResponse(saved);
    }

    @Transactional(readOnly = true)
    List<ExplanationDocumentResponse> documents(UUID incidentId) {
        Explanation explanation = ensureExplanation(getIncident(incidentId));
        return explanationDocumentRepository.findAllByExplanationIdAndDeletedFalseOrderByCreatedAtDesc(explanation.getId())
            .stream()
            .map(this::toDocumentResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    ExplanationDocument getDocumentEntity(UUID incidentId, UUID documentId) {
        ExplanationDocument document = explanationDocumentRepository.findByIdAndDeletedFalse(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (document.getExplanation().getExplanationIncident().getId().equals(incidentId) == false) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found for incident");
        }
        return document;
    }

    @Transactional(readOnly = true)
    List<DisciplinaryActionResponse> disciplinaryActions(UUID employeeId, UUID departmentId) {
        List<DisciplinaryAction> actions = employeeId != null
            ? disciplinaryActionRepository.findAllByEmployeeIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(employeeId)
            : departmentId != null
                ? disciplinaryActionRepository.findAllByDepartmentIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(departmentId)
                : disciplinaryActionRepository.findAllByDeletedFalseOrderByActionDateDescCreatedAtDesc();
        return actions.stream().map(this::toDisciplinaryResponse).toList();
    }

    @Transactional(readOnly = true)
    List<RewardActionResponse> rewards(UUID employeeId, UUID departmentId) {
        List<RewardAction> rewards = employeeId != null
            ? rewardActionRepository.findAllByEmployeeIdAndDeletedFalseOrderByRewardDateDescCreatedAtDesc(employeeId)
            : departmentId != null
                ? rewardActionRepository.findAllByDepartmentIdAndDeletedFalseOrderByRewardDateDescCreatedAtDesc(departmentId)
                : rewardActionRepository.findAllByDeletedFalseOrderByRewardDateDescCreatedAtDesc();
        return rewards.stream().map(this::toRewardResponse).toList();
    }

    RewardActionResponse createReward(RewardActionRequest request) {
        Employee employee = getEmployeeEntity(request.employeeId());
        Department department = request.departmentId() == null ? null : getDepartmentEntity(request.departmentId());
        if (request.amount() != null && request.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount cannot be negative");
        }
        RewardAction reward = new RewardAction();
        reward.setEmployee(employee);
        reward.setDepartment(department);
        reward.setRewardType(request.rewardType());
        reward.setRewardDate(request.rewardDate());
        reward.setAmount(request.amount());
        reward.setCurrencyCode(StringUtils.hasText(request.currencyCode()) ? request.currencyCode().trim() : "UZS");
        reward.setReasonText(request.reasonText().trim());
        reward.setStatus(RewardStatus.APPROVED);
        reward.setSourceOrderId(request.sourceOrderId());
        reward.setSourceOrderNumber(trimToNull(request.sourceOrderNumber()));
        RewardAction saved = rewardActionRepository.save(reward);
        notifyEmployee(employee, "REWARD_CREATED", "Создано поощрение", request.reasonText(), saved.getId(), rewardPayload(saved));
        writeAudit("REWARD_CREATED", saved.getId(), null, rewardPayload(saved), "reward_actions");
        return toRewardResponse(saved);
    }

    @Transactional(readOnly = true)
    List<DepartmentDisciplineReportResponse> reportByDepartment(LocalDate from, LocalDate to) {
        LocalDate start = from == null ? LocalDate.now().minusMonths(1) : from;
        LocalDate end = to == null ? LocalDate.now() : to;
        List<ExplanationIncident> incidents = explanationIncidentRepository.findAllByDeletedFalseOrderByCreatedAtDesc();
        List<DisciplinaryAction> actions = disciplinaryActionRepository.findAllByDeletedFalseOrderByActionDateDescCreatedAtDesc();
        List<RewardAction> rewards = rewardActionRepository.findAllByDeletedFalseOrderByRewardDateDescCreatedAtDesc();
        Map<UUID, DepartmentReportAccumulator> accumulators = new LinkedHashMap<>();

        for (ExplanationIncident incident : incidents) {
            if (incident.getOccurredAt().toLocalDate().isBefore(start) || incident.getOccurredAt().toLocalDate().isAfter(end)) {
                continue;
            }
            UUID departmentId = incident.getDepartment() == null ? null : incident.getDepartment().getId();
            DepartmentReportAccumulator accumulator = accumulators.computeIfAbsent(departmentId, key -> new DepartmentReportAccumulator());
            accumulator.incidentCount++;
            Explanation explanation = explanationRepository.findByExplanationIncidentIdAndDeletedFalse(incident.getId()).orElse(null);
            if (explanation != null) {
                if (explanation.getStatus() == ExplanationStatus.SUBMITTED || explanation.getStatus() == ExplanationStatus.MANAGER_REVIEWED) {
                    accumulator.submittedCount++;
                }
                if (explanation.getStatus() == ExplanationStatus.ACCEPTED || explanation.getStatus() == ExplanationStatus.CLOSED_NO_CONSEQUENCE) {
                    accumulator.acceptedCount++;
                }
                if (explanation.getStatus() == ExplanationStatus.REJECTED) {
                    accumulator.rejectedCount++;
                }
            }
        }

        for (DisciplinaryAction action : actions) {
            if (action.getActionDate().isBefore(start) || action.getActionDate().isAfter(end)) {
                continue;
            }
            UUID departmentId = action.getDepartment() == null ? null : action.getDepartment().getId();
            DepartmentReportAccumulator accumulator = accumulators.computeIfAbsent(departmentId, key -> new DepartmentReportAccumulator());
            accumulator.disciplinaryCount++;
            if (action.getActionType() == DisciplinaryActionType.REMARK) {
                accumulator.remarkCount++;
            }
            if (action.getActionType() == DisciplinaryActionType.REPRIMAND) {
                accumulator.reprimandCount++;
            }
            if (action.getActionType() == DisciplinaryActionType.SEVERE_REPRIMAND) {
                accumulator.severeReprimandCount++;
            }
        }

        for (RewardAction reward : rewards) {
            if (reward.getRewardDate().isBefore(start) || reward.getRewardDate().isAfter(end)) {
                continue;
            }
            UUID departmentId = reward.getDepartment() == null ? null : reward.getDepartment().getId();
            DepartmentReportAccumulator accumulator = accumulators.computeIfAbsent(departmentId, key -> new DepartmentReportAccumulator());
            accumulator.rewardCount++;
            if (reward.getRewardType() == RewardType.BONUS) {
                accumulator.bonusCount++;
            }
        }

        List<DepartmentDisciplineReportResponse> result = new ArrayList<>();
        for (Map.Entry<UUID, DepartmentReportAccumulator> entry : accumulators.entrySet()) {
            DepartmentReportAccumulator value = entry.getValue();
            result.add(new DepartmentDisciplineReportResponse(
                entry.getKey(),
                value.incidentCount,
                value.submittedCount,
                value.acceptedCount,
                value.rejectedCount,
                value.disciplinaryCount,
                value.rewardCount,
                value.remarkCount,
                value.reprimandCount,
                value.severeReprimandCount,
                value.bonusCount
            ));
        }
        return result;
    }

    private ExplanationCardResponse toCardResponse(ExplanationIncident incident) {
        Explanation explanation = ensureExplanation(incident);
        List<ExplanationDocumentResponse> documents = explanationDocumentRepository.findAllByExplanationIdAndDeletedFalseOrderByCreatedAtDesc(explanation.getId())
            .stream()
            .map(this::toDocumentResponse)
            .toList();
        List<ExplanationHistoryResponse> history = explanationHistoryRepository.findAllByExplanationIncidentIdAndDeletedFalseOrderByCreatedAtDesc(incident.getId())
            .stream()
            .map(this::toHistoryResponse)
            .toList();
        List<DisciplinaryActionResponse> disciplinaryActions = disciplinaryActionRepository.findAllByEmployeeIdAndDeletedFalseOrderByActionDateDescCreatedAtDesc(incident.getEmployee().getId())
            .stream()
            .filter(item -> item.getExplanationIncident() != null && item.getExplanationIncident().getId().equals(incident.getId()))
            .map(this::toDisciplinaryResponse)
            .toList();
        return new ExplanationCardResponse(
            incident.getId(),
            incident.getEmployee().getId(),
            incident.getDepartment() == null ? null : incident.getDepartment().getId(),
            incident.getManagerEmployee() == null ? null : incident.getManagerEmployee().getId(),
            incident.getAttendanceIncident() == null ? null : incident.getAttendanceIncident().getId(),
            incident.getIncidentSource(),
            incident.getIncidentType(),
            incident.getTitle(),
            incident.getDescription(),
            incident.getOccurredAt(),
            incident.getDueAt(),
            incident.getStatus(),
            explanation.getId(),
            explanation.getExplanationText(),
            explanation.getEmployeeSubmittedAt(),
            explanation.getManagerReviewerEmployee() == null ? null : explanation.getManagerReviewerEmployee().getId(),
            explanation.getManagerReviewComment(),
            explanation.getManagerReviewedAt(),
            explanation.getHrReviewerEmployee() == null ? null : explanation.getHrReviewerEmployee().getId(),
            explanation.getHrDecisionComment(),
            explanation.getHrDecidedAt(),
            explanation.getStatus(),
            documents,
            history,
            disciplinaryActions
        );
    }

    private Explanation ensureExplanation(ExplanationIncident incident) {
        Explanation existing = explanationRepository.findByExplanationIncidentIdAndDeletedFalse(incident.getId()).orElse(null);
        if (existing != null) {
            return existing;
        }
        Explanation explanation = new Explanation();
        explanation.setExplanationIncident(incident);
        explanation.setEmployee(incident.getEmployee());
        explanation.setStatus(ExplanationStatus.DRAFT);
        return explanationRepository.save(explanation);
    }

    private Explanation ensureSubmittedExplanation(ExplanationIncident incident) {
        Explanation explanation = ensureExplanation(incident);
        if (StringUtils.hasText(explanation.getExplanationText()) == false) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explanation text is required");
        }
        return explanation;
    }

    private Explanation ensureReviewableExplanation(ExplanationIncident incident) {
        Explanation explanation = ensureSubmittedExplanation(incident);
        if (explanation.getStatus() != ExplanationStatus.SUBMITTED && explanation.getStatus() != ExplanationStatus.MANAGER_REVIEWED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explanation must be submitted before HR decision");
        }
        return explanation;
    }

    private void applyHrDecision(Explanation explanation, ExplanationDecisionRequest request) {
        if (request != null) {
            explanation.setHrReviewerEmployee(request.hrReviewerEmployeeId() == null ? null : getEmployeeEntity(request.hrReviewerEmployeeId()));
            explanation.setHrDecisionComment(trimToNull(request.hrComment()));
            explanation.setSourceOrderId(request.sourceOrderId());
            explanation.setSourceOrderNumber(trimToNull(request.sourceOrderNumber()));
        }
        explanation.setHrDecidedAt(OffsetDateTime.now());
    }

    private void syncAttendanceIncidentStatus(ExplanationIncident incident, AttendanceIncidentStatus incidentStatus, AttendanceViolationStatus violationStatus) {
        if (incident.getAttendanceIncident() == null) {
            return;
        }
        AttendanceIncident attendanceIncident = attendanceIncidentRepository.findByIdAndDeletedFalse(incident.getAttendanceIncident().getId()).orElse(null);
        if (attendanceIncident != null) {
            attendanceIncident.setStatus(incidentStatus);
            attendanceIncidentRepository.save(attendanceIncident);
        }
        for (AttendanceViolation violation : attendanceViolationRepository.findAllByDeletedFalseOrderByCreatedAtDesc()) {
            if (violation.getAttendanceIncident() != null && violation.getAttendanceIncident().getId().equals(incident.getAttendanceIncident().getId())) {
                violation.setStatus(violationStatus);
                attendanceViolationRepository.save(violation);
            }
        }
    }

    private void writeHistory(ExplanationIncident incident, Explanation explanation, String actionType, String statusFrom, String statusTo, String commentText, String payload) {
        ExplanationHistory history = new ExplanationHistory();
        history.setExplanationIncident(incident);
        history.setExplanation(explanation);
        history.setActionType(actionType);
        history.setStatusFrom(statusFrom);
        history.setStatusTo(statusTo);
        history.setCommentText(trimToNull(commentText));
        history.setPayloadJson(payload);
        explanationHistoryRepository.save(history);
    }

    private void notifyEmployee(Employee employee, String notificationType, String title, String body, UUID entityId, String payload) {
        HrNotification notification = new HrNotification();
        notification.setRecipientEmployee(employee);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setEntityType("EXPLANATION_INCIDENT");
        notification.setEntityId(entityId);
        notification.setStatus(NotificationStatus.NEW);
        notification.setPayloadJson(payload);
        hrNotificationRepository.save(notification);
    }

    private ExplanationIncident getIncident(UUID incidentId) {
        return explanationIncidentRepository.findByIdAndDeletedFalse(incidentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Explanation incident not found"));
    }

    private Employee getEmployeeEntity(UUID employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private Department getDepartmentEntity(UUID departmentId) {
        return departmentRepository.findByIdAndDeletedFalse(departmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private ExplanationDocumentResponse toDocumentResponse(ExplanationDocument document) {
        return new ExplanationDocumentResponse(
            document.getId(),
            document.getTitle(),
            document.getOriginalFileName(),
            document.getContentType(),
            document.getSizeBytes(),
            document.getVersionNo(),
            document.isCurrent(),
            document.getDescription()
        );
    }

    private ExplanationHistoryResponse toHistoryResponse(ExplanationHistory history) {
        return new ExplanationHistoryResponse(
            history.getId(),
            history.getActionType(),
            history.getStatusFrom(),
            history.getStatusTo(),
            history.getActorUserId(),
            history.getCommentText(),
            history.getCreatedAt()
        );
    }

    private DisciplinaryActionResponse toDisciplinaryResponse(DisciplinaryAction action) {
        return new DisciplinaryActionResponse(
            action.getId(),
            action.getEmployee().getId(),
            action.getDepartment() == null ? null : action.getDepartment().getId(),
            action.getExplanationIncident() == null ? null : action.getExplanationIncident().getId(),
            action.getExplanation() == null ? null : action.getExplanation().getId(),
            action.getActionType(),
            action.getActionDate(),
            action.getReasonText(),
            action.getStatus(),
            action.getValidUntil(),
            action.getSourceOrderId(),
            action.getSourceOrderNumber()
        );
    }

    private RewardActionResponse toRewardResponse(RewardAction reward) {
        return new RewardActionResponse(
            reward.getId(),
            reward.getEmployee().getId(),
            reward.getDepartment() == null ? null : reward.getDepartment().getId(),
            reward.getRewardType(),
            reward.getRewardDate(),
            reward.getAmount(),
            reward.getCurrencyCode(),
            reward.getReasonText(),
            reward.getStatus(),
            reward.getSourceOrderId(),
            reward.getSourceOrderNumber()
        );
    }

    private void writeAudit(String action, UUID entityId, String beforeData, String afterData) {
        writeAudit(action, entityId, beforeData, afterData, "explanation_incidents");
    }

    private void writeAudit(String action, UUID entityId, String beforeData, String afterData, String entityTable) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntitySchema("hr");
        auditLog.setEntityTable(entityTable);
        auditLog.setEntityId(entityId);
        auditLog.setOccurredAt(OffsetDateTime.now());
        auditLog.setBeforeData(beforeData);
        auditLog.setAfterData(afterData);
        auditLogRepository.save(auditLog);
    }

    private String incidentPayload(ExplanationIncident incident) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("incidentId", incident.getId());
        payload.put("employeeId", incident.getEmployee().getId());
        payload.put("departmentId", incident.getDepartment() == null ? null : incident.getDepartment().getId());
        payload.put("incidentSource", incident.getIncidentSource());
        payload.put("incidentType", incident.getIncidentType());
        payload.put("status", incident.getStatus());
        payload.put("dueAt", incident.getDueAt());
        return toJson(payload);
    }

    private String explanationPayload(Explanation explanation) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("explanationId", explanation.getId());
        payload.put("incidentId", explanation.getExplanationIncident().getId());
        payload.put("employeeId", explanation.getEmployee().getId());
        payload.put("status", explanation.getStatus());
        payload.put("employeeSubmittedAt", explanation.getEmployeeSubmittedAt());
        payload.put("managerReviewedAt", explanation.getManagerReviewedAt());
        payload.put("hrDecidedAt", explanation.getHrDecidedAt());
        return toJson(payload);
    }

    private String documentPayload(ExplanationDocument document) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("documentId", document.getId());
        payload.put("explanationId", document.getExplanation().getId());
        payload.put("title", document.getTitle());
        payload.put("versionNo", document.getVersionNo());
        payload.put("current", document.isCurrent());
        return toJson(payload);
    }

    private String disciplinaryActionPayload(DisciplinaryAction action) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("disciplinaryActionId", action.getId());
        payload.put("employeeId", action.getEmployee().getId());
        payload.put("departmentId", action.getDepartment() == null ? null : action.getDepartment().getId());
        payload.put("actionType", action.getActionType());
        payload.put("actionDate", action.getActionDate());
        payload.put("status", action.getStatus());
        payload.put("sourceOrderNumber", action.getSourceOrderNumber());
        return toJson(payload);
    }

    private String rewardPayload(RewardAction reward) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("rewardActionId", reward.getId());
        payload.put("employeeId", reward.getEmployee().getId());
        payload.put("rewardType", reward.getRewardType());
        payload.put("rewardDate", reward.getRewardDate());
        payload.put("status", reward.getStatus());
        payload.put("amount", reward.getAmount());
        return toJson(payload);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize payload");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private static final class DepartmentReportAccumulator {
        long incidentCount;
        long submittedCount;
        long acceptedCount;
        long rejectedCount;
        long disciplinaryCount;
        long rewardCount;
        long remarkCount;
        long reprimandCount;
        long severeReprimandCount;
        long bonusCount;
    }
}
