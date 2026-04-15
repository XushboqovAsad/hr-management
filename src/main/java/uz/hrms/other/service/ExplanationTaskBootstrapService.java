package uz.hrms.other.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.entity.*;
import uz.hrms.other.enums.*;
import uz.hrms.other.repository.*;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ExplanationTaskBootstrapService {

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
