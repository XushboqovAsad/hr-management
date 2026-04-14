package uz.hrms.other;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.repository.AuditLogRepository;

@Service
@Transactional
class AbsenceService {

    private final AbsenceRecordRepository absenceRecordRepository;
    private final AbsenceDocumentRepository absenceDocumentRepository;
    private final AbsenceHistoryRepository absenceHistoryRepository;
    private final AttendanceDayMarkRepository attendanceDayMarkRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessTripRepository businessTripRepository;
    private final AuditLogRepository auditLogRepository;
    private final LocalFileStorageService localFileStorageService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    AbsenceService(
        AbsenceRecordRepository absenceRecordRepository,
        AbsenceDocumentRepository absenceDocumentRepository,
        AbsenceHistoryRepository absenceHistoryRepository,
        AttendanceDayMarkRepository attendanceDayMarkRepository,
        EmployeeRepository employeeRepository,
        BusinessTripRepository businessTripRepository,
        AuditLogRepository auditLogRepository,
        LocalFileStorageService localFileStorageService,
        JdbcTemplate jdbcTemplate,
        ObjectMapper objectMapper
    ) {
        this.absenceRecordRepository = absenceRecordRepository;
        this.absenceDocumentRepository = absenceDocumentRepository;
        this.absenceHistoryRepository = absenceHistoryRepository;
        this.attendanceDayMarkRepository = attendanceDayMarkRepository;
        this.employeeRepository = employeeRepository;
        this.businessTripRepository = businessTripRepository;
        this.auditLogRepository = auditLogRepository;
        this.localFileStorageService = localFileStorageService;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    List<AbsenceListItemResponse> list(UUID employeeId) {
        List<AbsenceRecord> items = employeeId == null
            ? absenceRecordRepository.findAllByDeletedFalseOrderByCreatedAtDesc()
            : absenceRecordRepository.findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(employeeId);
        return items.stream().map(this::toListItem).toList();
    }

    @Transactional(readOnly = true)
    AbsenceResponse get(UUID id) {
        return toResponse(getRecord(id));
    }

    AbsenceResponse create(AbsenceRequest request) {
        validateRequest(request, null);
        AbsenceRecord record = new AbsenceRecord();
        applyRecord(record, request);
        record.setStatus(AbsenceStatus.DRAFT);
        AbsenceRecord saved = absenceRecordRepository.save(record);
        writeHistory(saved, "CREATED", null, saved.getStatus(), "Draft created", recordPayload(saved));
        writeAudit("ABSENCE_CREATED", saved.getId(), null, recordPayload(saved));
        return toResponse(saved);
    }

    AbsenceResponse update(UUID id, AbsenceRequest request) {
        validateRequest(request, id);
        AbsenceRecord record = getRecord(id);
        if (record.getStatus() != AbsenceStatus.DRAFT && record.getStatus() != AbsenceStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft or rejected record can be updated");
        }
        String before = recordPayload(record);
        applyRecord(record, request);
        AbsenceRecord saved = absenceRecordRepository.save(record);
        writeHistory(saved, "UPDATED", saved.getStatus(), saved.getStatus(), "Record updated", recordPayload(saved));
        writeAudit("ABSENCE_UPDATED", saved.getId(), before, recordPayload(saved));
        return toResponse(saved);
    }

    AbsenceResponse submit(UUID id) {
        AbsenceRecord record = getRecord(id);
        if (record.getStatus() != AbsenceStatus.DRAFT && record.getStatus() != AbsenceStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft or rejected record can be submitted");
        }
        AbsenceStatus previous = record.getStatus();
        record.setStatus(AbsenceStatus.SUBMITTED);
        absenceRecordRepository.save(record);
        writeHistory(record, "SUBMITTED", previous, record.getStatus(), "Submitted for HR review", recordPayload(record));
        writeAudit("ABSENCE_SUBMITTED", record.getId(), null, recordPayload(record));
        return toResponse(record);
    }

    AbsenceResponse startReview(UUID id, String hrComment) {
        AbsenceRecord record = getRecord(id);
        if (record.getStatus() != AbsenceStatus.SUBMITTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only submitted record can be moved to review");
        }
        AbsenceStatus previous = record.getStatus();
        record.setStatus(AbsenceStatus.HR_REVIEW);
        record.setHrComment(trimToNull(hrComment));
        absenceRecordRepository.save(record);
        writeHistory(record, "HR_REVIEW_STARTED", previous, record.getStatus(), hrComment, recordPayload(record));
        writeAudit("ABSENCE_REVIEW_STARTED", record.getId(), null, recordPayload(record));
        return toResponse(record);
    }

    AbsenceResponse approve(UUID id, String hrComment) {
        AbsenceRecord record = getRecord(id);
        if (record.getStatus() != AbsenceStatus.SUBMITTED && record.getStatus() != AbsenceStatus.HR_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only submitted or under review record can be approved");
        }
        if (record.isDocumentRequired() && currentDocuments(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supporting document is required before approval");
        }
        AbsenceStatus previous = record.getStatus();
        record.setStatus(AbsenceStatus.APPROVED);
        record.setApprovedAt(OffsetDateTime.now());
        record.setHrComment(trimToNull(hrComment));
        absenceRecordRepository.save(record);
        syncAttendance(record);
        writeHistory(record, "APPROVED", previous, record.getStatus(), hrComment, recordPayload(record));
        writeAudit("ABSENCE_APPROVED", record.getId(), null, recordPayload(record));
        return toResponse(record);
    }

    AbsenceResponse reject(UUID id, String hrComment) {
        AbsenceRecord record = getRecord(id);
        if (record.getStatus() != AbsenceStatus.SUBMITTED && record.getStatus() != AbsenceStatus.HR_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only submitted or under review record can be rejected");
        }
        AbsenceStatus previous = record.getStatus();
        record.setStatus(AbsenceStatus.REJECTED);
        record.setHrComment(trimToNull(hrComment));
        absenceRecordRepository.save(record);
        removeAttendanceMarks(record.getId());
        writeHistory(record, "REJECTED", previous, record.getStatus(), hrComment, recordPayload(record));
        writeAudit("ABSENCE_REJECTED", record.getId(), null, recordPayload(record));
        return toResponse(record);
    }

    AbsenceResponse close(UUID id, String hrComment) {
        AbsenceRecord record = getRecord(id);
        if (record.getStatus() != AbsenceStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved record can be closed");
        }
        if (record.isDocumentRequired() && currentDocuments(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supporting document is required before close");
        }
        AbsenceStatus previous = record.getStatus();
        record.setStatus(AbsenceStatus.CLOSED);
        record.setClosedAt(OffsetDateTime.now());
        record.setHrComment(trimToNull(hrComment));
        absenceRecordRepository.save(record);
        writeHistory(record, "CLOSED", previous, record.getStatus(), hrComment, recordPayload(record));
        writeAudit("ABSENCE_CLOSED", record.getId(), null, recordPayload(record));
        return toResponse(record);
    }

    AbsenceResponse markPayrollSent(UUID id) {
        AbsenceRecord record = getRecord(id);
        if (record.getStatus() != AbsenceStatus.APPROVED && record.getStatus() != AbsenceStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only approved or closed record can be sent to payroll");
        }
        record.setPayrollSyncStatus(PayrollSyncStatus.SENT);
        absenceRecordRepository.save(record);
        writeHistory(record, "PAYROLL_SENT", record.getStatus(), record.getStatus(), "Sent to payroll", recordPayload(record));
        writeAudit("ABSENCE_PAYROLL_SENT", record.getId(), null, recordPayload(record));
        return toResponse(record);
    }

    AbsenceDocumentResponse uploadDocument(UUID id, AbsenceDocumentUploadRequest request, MultipartFile file) {
        AbsenceRecord record = getRecord(id);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        StoredFileDescriptor storedFile = localFileStorageService.store("absences/" + id, file);
        List<AbsenceDocument> existing = absenceDocumentRepository.findAllByAbsenceRecordIdAndTitleIgnoreCaseAndDeletedFalseOrderByVersionNoDesc(id, request.title());
        int nextVersion = existing.isEmpty() ? 1 : existing.get(0).getVersionNo() + 1;
        for (AbsenceDocument document : existing) {
            if (document.isCurrent()) {
                document.setCurrent(false);
                absenceDocumentRepository.save(document);
            }
        }
        AbsenceDocument document = new AbsenceDocument();
        document.setAbsenceRecord(record);
        document.setTitle(request.title().trim());
        document.setDescription(trimToNull(request.description()));
        document.setOriginalFileName(storedFile.fileName());
        document.setStorageKey(storedFile.storageKey());
        document.setContentType(storedFile.contentType());
        document.setSizeBytes(storedFile.sizeBytes());
        document.setVersionNo(nextVersion);
        document.setCurrent(true);
        document.setDocumentStatus(AbsenceDocumentStatus.ACTIVE);
        AbsenceDocument saved = absenceDocumentRepository.save(document);
        writeHistory(record, "DOCUMENT_UPLOADED", record.getStatus(), record.getStatus(), saved.getTitle(), documentPayload(saved));
        writeAudit("ABSENCE_DOCUMENT_UPLOADED", record.getId(), null, documentPayload(saved));
        return toDocumentResponse(saved);
    }

    List<AbsenceDocumentResponse> documents(UUID id) {
        getRecord(id);
        return absenceDocumentRepository.findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(id)
            .stream()
            .map(this::toDocumentResponse)
            .toList();
    }

    AbsenceDocument getDocumentEntity(UUID absenceId, UUID documentId) {
        AbsenceDocument document = absenceDocumentRepository.findByIdAndDeletedFalse(documentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
        if (document.getAbsenceRecord().getId().equals(absenceId) == false) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found for absence");
        }
        return document;
    }

    @Transactional(readOnly = true)
    List<FrequentAbsenceAnalyticsResponse> frequentAnalytics(LocalDate from, LocalDate to, long threshold) {
        List<AbsenceRecord> items = absenceRecordRepository.findAllByDeletedFalseOrderByCreatedAtDesc().stream()
            .filter(item -> item.getStatus() == AbsenceStatus.APPROVED || item.getStatus() == AbsenceStatus.CLOSED)
            .filter(item -> overlaps(item.getStartDate(), item.getEndDate(), from, to))
            .toList();
        Map<UUID, List<AbsenceRecord>> grouped = new LinkedHashMap<>();
        for (AbsenceRecord item : items) {
            grouped.computeIfAbsent(item.getEmployee().getId(), key -> new ArrayList<>()).add(item);
        }
        List<FrequentAbsenceAnalyticsResponse> result = new ArrayList<>();
        for (Map.Entry<UUID, List<AbsenceRecord>> entry : grouped.entrySet()) {
            long recordCount = entry.getValue().size();
            if (recordCount < threshold) {
                continue;
            }
            long totalDays = entry.getValue().stream().mapToLong(item -> ChronoUnit.DAYS.between(item.getStartDate(), item.getEndDate()) + 1).sum();
            List<AbsenceType> types = entry.getValue().stream().map(AbsenceRecord::getAbsenceType).distinct().toList();
            result.add(new FrequentAbsenceAnalyticsResponse(entry.getKey(), recordCount, totalDays, types));
        }
        return result;
    }

    @Transactional(readOnly = true)
    List<AttendanceDayMarkResponse> timesheet(UUID employeeId, LocalDate from, LocalDate to) {
        return attendanceDayMarkRepository.findAllByEmployeeIdAndAttendanceDateBetweenAndDeletedFalseOrderByAttendanceDateAsc(employeeId, from, to)
            .stream()
            .map(this::toAttendanceDayMarkResponse)
            .toList();
    }

    private void validateRequest(AbsenceRequest request, UUID currentId) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        Employee employee = getEmployeeEntity(request.employeeId());
        if (request.requesterEmployeeId() != null) {
            getEmployeeEntity(request.requesterEmployeeId());
        }
        if (hasAbsenceOverlap(employee.getId(), request.startDate(), request.endDate(), currentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period overlaps another absence");
        }
        if (hasBusinessTripOverlap(employee.getId(), request.startDate(), request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period overlaps approved or active business trip");
        }
        if (hasLeaveOverlap(employee.getId(), request.startDate(), request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Period overlaps approved leave");
        }
    }

    private boolean hasAbsenceOverlap(UUID employeeId, LocalDate from, LocalDate to, UUID currentId) {
        return absenceRecordRepository.findAllByEmployeeIdAndDeletedFalseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(employeeId, to, from)
            .stream()
            .filter(item -> item.getStatus() != AbsenceStatus.REJECTED && item.getStatus() != AbsenceStatus.CANCELLED)
            .anyMatch(item -> currentId == null || item.getId().equals(currentId) == false);
    }

    private boolean hasBusinessTripOverlap(UUID employeeId, LocalDate from, LocalDate to) {
        List<BusinessTripStatus> blockingStatuses = List.of(
            BusinessTripStatus.ON_APPROVAL,
            BusinessTripStatus.APPROVED,
            BusinessTripStatus.ORDER_CREATED,
            BusinessTripStatus.IN_PROGRESS,
            BusinessTripStatus.REPORT_PENDING,
            BusinessTripStatus.REPORT_SUBMITTED,
            BusinessTripStatus.OVERDUE
        );
        return businessTripRepository.findAllByEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(employeeId)
            .stream()
            .filter(item -> blockingStatuses.contains(item.getStatus()))
            .anyMatch(item -> overlaps(item.getStartDate(), item.getEndDate(), from, to));
    }

    private boolean hasLeaveOverlap(UUID employeeId, LocalDate from, LocalDate to) {
        Integer exists = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.tables where table_schema = 'hr' and table_name = 'leave_requests'",
            Integer.class
        );
        if (exists == null || exists == 0) {
            return false;
        }
        Integer overlapCount = jdbcTemplate.queryForObject(
            "select count(*) from hr.leave_requests where employee_id = ? and is_deleted = false and status in ('SUBMITTED','APPROVED','ONGOING') and start_date <= ? and end_date >= ?",
            Integer.class,
            employeeId,
            to,
            from
        );
        return overlapCount != null && overlapCount > 0;
    }

    private void applyRecord(AbsenceRecord record, AbsenceRequest request) {
        record.setEmployee(getEmployeeEntity(request.employeeId()));
        record.setRequesterEmployee(request.requesterEmployeeId() == null ? null : getEmployeeEntity(request.requesterEmployeeId()));
        record.setAbsenceType(request.absenceType());
        record.setReasonText(trimToNull(request.reasonText()));
        record.setStartDate(request.startDate());
        record.setEndDate(request.endDate());
        record.setDocumentRequired(request.documentRequired() != null && request.documentRequired());
    }

    private void syncAttendance(AbsenceRecord record) {
        removeAttendanceMarks(record.getId());
        LocalDate current = record.getStartDate();
        while (current.isAfter(record.getEndDate()) == false) {
            AttendanceDayMark mark = new AttendanceDayMark();
            mark.setEmployee(record.getEmployee());
            mark.setAttendanceDate(current);
            mark.setMarkSource(AttendanceMarkSource.ABSENCE);
            mark.setSourceRecordId(record.getId());
            mark.setMarkStatus(record.getAbsenceType().name());
            mark.setNoteText(record.getReasonText());
            attendanceDayMarkRepository.save(mark);
            current = current.plusDays(1);
        }
    }

    private void removeAttendanceMarks(UUID recordId) {
        List<AttendanceDayMark> marks = attendanceDayMarkRepository.findAllBySourceRecordIdAndMarkSourceAndDeletedFalse(recordId, AttendanceMarkSource.ABSENCE);
        for (AttendanceDayMark mark : marks) {
            mark.setDeleted(true);
            attendanceDayMarkRepository.save(mark);
        }
    }

    private List<AbsenceDocument> currentDocuments(UUID id) {
        return absenceDocumentRepository.findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(id)
            .stream()
            .filter(AbsenceDocument::isCurrent)
            .filter(item -> item.getDocumentStatus() == AbsenceDocumentStatus.ACTIVE)
            .toList();
    }

    private AbsenceResponse toResponse(AbsenceRecord record) {
        AbsenceRecord refreshed = getRecord(record.getId());
        List<AbsenceDocumentResponse> documents = documents(refreshed.getId());
        List<AbsenceHistoryResponse> history = absenceHistoryRepository.findAllByAbsenceRecordIdAndDeletedFalseOrderByCreatedAtDesc(refreshed.getId())
            .stream()
            .map(this::toHistoryResponse)
            .toList();
        List<AttendanceDayMarkResponse> marks = attendanceDayMarkRepository.findAllBySourceRecordIdAndMarkSourceAndDeletedFalse(refreshed.getId(), AttendanceMarkSource.ABSENCE)
            .stream()
            .map(this::toAttendanceDayMarkResponse)
            .toList();
        return new AbsenceResponse(
            refreshed.getId(),
            refreshed.getEmployee().getId(),
            refreshed.getRequesterEmployee() == null ? null : refreshed.getRequesterEmployee().getId(),
            refreshed.getAbsenceType(),
            refreshed.getReasonText(),
            refreshed.getStartDate(),
            refreshed.getEndDate(),
            refreshed.isDocumentRequired(),
            refreshed.getHrComment(),
            refreshed.getStatus(),
            refreshed.getPayrollSyncStatus(),
            refreshed.getApprovedAt(),
            refreshed.getClosedAt(),
            documents,
            history,
            marks
        );
    }

    private AbsenceListItemResponse toListItem(AbsenceRecord record) {
        long days = ChronoUnit.DAYS.between(record.getStartDate(), record.getEndDate()) + 1;
        return new AbsenceListItemResponse(
            record.getId(),
            record.getEmployee().getId(),
            record.getAbsenceType(),
            record.getStartDate(),
            record.getEndDate(),
            record.getStatus(),
            record.isDocumentRequired(),
            days >= 3
        );
    }

    private AbsenceDocumentResponse toDocumentResponse(AbsenceDocument document) {
        return new AbsenceDocumentResponse(
            document.getId(),
            document.getTitle(),
            document.getOriginalFileName(),
            document.getContentType(),
            document.getSizeBytes(),
            document.getVersionNo(),
            document.isCurrent(),
            document.getDocumentStatus(),
            document.getDescription()
        );
    }

    private AbsenceHistoryResponse toHistoryResponse(AbsenceHistory history) {
        return new AbsenceHistoryResponse(
            history.getId(),
            history.getActionType(),
            history.getStatusFrom(),
            history.getStatusTo(),
            history.getActorUser() == null ? null : history.getActorUser().getId(),
            history.getCommentText(),
            history.getCreatedAt()
        );
    }

    private AttendanceDayMarkResponse toAttendanceDayMarkResponse(AttendanceDayMark mark) {
        return new AttendanceDayMarkResponse(
            mark.getId(),
            mark.getEmployee().getId(),
            mark.getAttendanceDate(),
            mark.getMarkSource(),
            mark.getMarkStatus(),
            mark.getNoteText()
        );
    }

    private AbsenceRecord getRecord(UUID id) {
        return absenceRecordRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Absence record not found"));
    }

    private Employee getEmployeeEntity(UUID employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private void writeHistory(AbsenceRecord record, String actionType, AbsenceStatus from, AbsenceStatus to, String commentText, String payload) {
        AbsenceHistory history = new AbsenceHistory();
        history.setAbsenceRecord(record);
        history.setActionType(actionType);
        history.setStatusFrom(from == null ? null : from.name());
        history.setStatusTo(to == null ? null : to.name());
        history.setCommentText(trimToNull(commentText));
        history.setPayloadJson(payload);
        absenceHistoryRepository.save(history);
    }

    private void writeAudit(String action, UUID entityId, String beforeData, String afterData) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntitySchema("hr");
        auditLog.setEntityTable("absence_records");
        auditLog.setEntityId(entityId);
        auditLog.setOccurredAt(OffsetDateTime.now());
        auditLog.setBeforeData(beforeData);
        auditLog.setAfterData(afterData);
        auditLogRepository.save(auditLog);
    }

    private boolean overlaps(LocalDate leftStart, LocalDate leftEnd, LocalDate rightStart, LocalDate rightEnd) {
        return leftStart.isAfter(rightEnd) == false && leftEnd.isBefore(rightStart) == false;
    }

    private String recordPayload(AbsenceRecord record) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", record.getId());
        payload.put("employeeId", record.getEmployee().getId());
        payload.put("absenceType", record.getAbsenceType());
        payload.put("startDate", record.getStartDate());
        payload.put("endDate", record.getEndDate());
        payload.put("status", record.getStatus());
        payload.put("documentRequired", record.isDocumentRequired());
        payload.put("payrollSyncStatus", record.getPayrollSyncStatus());
        return toJson(payload);
    }

    private String documentPayload(AbsenceDocument document) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("documentId", document.getId());
        payload.put("title", document.getTitle());
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

    private String trimToNull(String value) {
        if (StringUtils.hasText(value)) {
            return value.trim();
        }
        return null;
    }
}
