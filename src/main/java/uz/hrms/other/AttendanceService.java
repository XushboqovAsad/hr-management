package uz.hrms.other;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
class AttendanceService {

    private final ScudEventRepository scudEventRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeWorkScheduleRepository employeeWorkScheduleRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final AttendanceAdjustmentRepository attendanceAdjustmentRepository;
    private final AttendanceIncidentRepository attendanceIncidentRepository;
    private final AttendanceViolationRepository attendanceViolationRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditLogRepository auditLogRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ExplanationTaskBootstrapService explanationTaskBootstrapService;
    private final ZoneId zoneId = ZoneId.systemDefault();

    AttendanceService(
        ScudEventRepository scudEventRepository,
        WorkScheduleRepository workScheduleRepository,
        EmployeeWorkScheduleRepository employeeWorkScheduleRepository,
        AttendanceLogRepository attendanceLogRepository,
        AttendanceSummaryRepository attendanceSummaryRepository,
        AttendanceAdjustmentRepository attendanceAdjustmentRepository,
        AttendanceIncidentRepository attendanceIncidentRepository,
        AttendanceViolationRepository attendanceViolationRepository,
        EmployeeRepository employeeRepository,
        AuditLogRepository auditLogRepository,
        JdbcTemplate jdbcTemplate,
        ObjectMapper objectMapper,
        ExplanationTaskBootstrapService explanationTaskBootstrapService
    ) {
        this.scudEventRepository = scudEventRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.employeeWorkScheduleRepository = employeeWorkScheduleRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.attendanceSummaryRepository = attendanceSummaryRepository;
        this.attendanceAdjustmentRepository = attendanceAdjustmentRepository;
        this.attendanceIncidentRepository = attendanceIncidentRepository;
        this.attendanceViolationRepository = attendanceViolationRepository;
        this.employeeRepository = employeeRepository;
        this.auditLogRepository = auditLogRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.explanationTaskBootstrapService = explanationTaskBootstrapService;
    }

    ScudEventIngestRequest ingestEvent(ScudEventIngestRequest request) {
        if (StringUtils.hasText(request.externalEventId())) {
            ScudEvent existing = scudEventRepository.findByExternalEventIdAndDeletedFalse(request.externalEventId()).orElse(null);
            if (existing != null) {
                return request;
            }
        }
        ScudEvent event = new ScudEvent();
        event.setEmployee(request.employeeId() == null ? null : getEmployee(request.employeeId()));
        event.setBadgeNumber(trimToNull(request.badgeNumber()));
        event.setSourceSystem(StringUtils.hasText(request.sourceSystem()) ? request.sourceSystem().trim() : "SCUD");
        event.setExternalEventId(trimToNull(request.externalEventId()));
        event.setDeviceId(request.deviceId().trim());
        event.setDeviceName(trimToNull(request.deviceName()));
        event.setEventType(request.eventType());
        event.setEventAt(request.eventAt());
        event.setRawPayload(trimToNull(request.rawPayload()));
        event.setNormalizationStatus(ScudNormalizationStatus.NEW);
        scudEventRepository.save(event);
        writeAudit("SCUD_EVENT_INGESTED", "scud_events", event.getId(), eventPayload(event));
        return request;
    }

    List<ScudEventIngestRequest> ingestBatch(ScudEventBatchRequest request) {
        List<ScudEventIngestRequest> result = new ArrayList<>();
        for (ScudEventIngestRequest item : request.events()) {
            result.add(ingestEvent(item));
        }
        return result;
    }

    List<AttendanceSummaryResponse> processWorkDate(LocalDate workDate, UUID employeeId) {
        List<UUID> employeeIds = employeeId == null ? activeEmployeeIds(workDate) : List.of(employeeId);
        List<AttendanceSummaryResponse> processed = new ArrayList<>();
        boolean globalDataPresent = hasAnyScudData(workDate);
        for (UUID currentEmployeeId : employeeIds) {
            Employee employee = getEmployee(currentEmployeeId);
            WorkSchedule schedule = resolveSchedule(currentEmployeeId, workDate);
            AssignmentSnapshot assignment = resolveAssignment(currentEmployeeId, workDate);
            TimeWindow window = attendanceWindow(workDate, schedule);
            List<ScudEvent> events = scudEventRepository.findAllByEmployeeIdAndDeletedFalseAndEventAtBetweenOrderByEventAtAsc(currentEmployeeId, window.windowStart(), window.windowEnd());
            String overrideStatus = findManualOrAbsenceMark(currentEmployeeId, workDate);
            ProcessedAttendance processedAttendance = normalize(workDate, schedule, events, overrideStatus, globalDataPresent);
            AttendanceLog log = upsertLog(employee, schedule, workDate, processedAttendance);
            AttendanceSummary summary = upsertSummary(employee, assignment, schedule, log, workDate, processedAttendance);
            rebuildViolations(summary, processedAttendance, log);
            markEventsProcessed(events);
            processed.add(toSummaryResponse(summary));
        }
        return processed;
    }

    @Transactional(readOnly = true)
    AttendanceDashboardResponse dashboard(LocalDate from, LocalDate to, UUID departmentId, UUID employeeId, AttendanceViolationType violationType) {
        LocalDate rangeStart = from == null ? LocalDate.now().minusDays(30) : from;
        LocalDate rangeEnd = to == null ? LocalDate.now() : to;
        List<AttendanceSummary> summaries = employeeId == null
            ? attendanceSummaryRepository.findAllByDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(rangeStart, rangeEnd)
            : attendanceSummaryRepository.findAllByEmployeeIdAndDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(employeeId, rangeStart, rangeEnd);
        final Set<UUID> summaryIdsWithViolation;
        if (violationType != null) {
            Set<UUID> matchingIds = new HashSet<>();
            for (AttendanceViolation violation : attendanceViolationRepository.findAllByDeletedFalseOrderByCreatedAtDesc()) {
                if (violation.getViolationType() == violationType) {
                    matchingIds.add(violation.getAttendanceSummary().getId());
                }
            }
            summaryIdsWithViolation = matchingIds;
        } else {
            summaryIdsWithViolation = null;
        }
        List<AttendanceSummary> filtered = summaries.stream()
            .filter(item -> departmentId == null || (item.getDepartment() != null && departmentId.equals(item.getDepartment().getId())))
            .filter(item -> summaryIdsWithViolation == null || summaryIdsWithViolation.contains(item.getId()))
            .toList();
        List<AttendanceDashboardItemResponse> items = filtered.stream().map(this::toDashboardItem).toList();
        long lateCount = filtered.stream().filter(item -> item.getLateMinutes() > 0).count();
        long earlyLeaveCount = filtered.stream().filter(item -> item.getEarlyLeaveMinutes() > 0).count();
        long missingPunchCount = filtered.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.MISSING_PUNCH).count();
        long absenceCount = filtered.stream().filter(item -> item.getAttendanceStatus() == AttendanceStatus.ABSENT || item.getAttendanceStatus() == AttendanceStatus.NO_DATA).count();
        long overtimeCount = filtered.stream().filter(item -> item.getOvertimeMinutes() > 0).count();
        return new AttendanceDashboardResponse(filtered.size(), lateCount, earlyLeaveCount, missingPunchCount, absenceCount, overtimeCount, items);
    }

    @Transactional(readOnly = true)
    List<AttendanceSummaryResponse> employeeAttendance(UUID employeeId, LocalDate from, LocalDate to) {
        LocalDate rangeStart = from == null ? LocalDate.now().minusDays(30) : from;
        LocalDate rangeEnd = to == null ? LocalDate.now() : to;
        return attendanceSummaryRepository.findAllByEmployeeIdAndDeletedFalseAndWorkDateBetweenOrderByWorkDateDesc(employeeId, rangeStart, rangeEnd)
            .stream()
            .map(this::toSummaryResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    List<AttendanceViolationResponse> violations(LocalDate from, LocalDate to, UUID departmentId, UUID employeeId, AttendanceViolationType violationType) {
        LocalDate rangeStart = from == null ? LocalDate.now().minusDays(30) : from;
        LocalDate rangeEnd = to == null ? LocalDate.now() : to;
        return attendanceViolationRepository.findAllByDeletedFalseOrderByCreatedAtDesc().stream()
            .filter(item -> item.getAttendanceSummary().getWorkDate().isBefore(rangeStart) == false && item.getAttendanceSummary().getWorkDate().isAfter(rangeEnd) == false)
            .filter(item -> employeeId == null || item.getEmployee().getId().equals(employeeId))
            .filter(item -> departmentId == null || (item.getAttendanceSummary().getDepartment() != null && item.getAttendanceSummary().getDepartment().getId().equals(departmentId)))
            .filter(item -> violationType == null || item.getViolationType() == violationType)
            .map(this::toViolationResponse)
            .toList();
    }

    AttendanceSummaryResponse adjustSummary(UUID summaryId, AttendanceAdjustmentRequest request) {
        AttendanceSummary summary = attendanceSummaryRepository.findByIdAndDeletedFalse(summaryId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance summary not found"));
        AttendanceAdjustment adjustment = new AttendanceAdjustment();
        adjustment.setAttendanceSummary(summary);
        adjustment.setAdjustedStartAt(request.adjustedStartAt());
        adjustment.setAdjustedEndAt(request.adjustedEndAt());
        adjustment.setAdjustedStatus(request.adjustedStatus().trim());
        adjustment.setAdjustedReason(request.adjustedReason().trim());
        adjustment.setApprovedBy(request.approvedBy());
        adjustment.setApprovedAt(OffsetDateTime.now());
        attendanceAdjustmentRepository.save(adjustment);

        AttendanceLog log = summary.getAttendanceLog();
        if (log != null) {
            if (request.adjustedStartAt() != null) {
                log.setFirstInAt(request.adjustedStartAt());
            }
            if (request.adjustedEndAt() != null) {
                log.setLastOutAt(request.adjustedEndAt());
            }
            log.setLogStatus("MANUAL");
            attendanceLogRepository.save(log);
        }

        summary.setAttendanceStatus(parseAttendanceStatus(request.adjustedStatus()));
        summary.setManuallyAdjusted(true);
        summary.setAdjustedComment(request.adjustedReason().trim());
        summary.setFinalizedAt(OffsetDateTime.now());
        summary.setViolationCount(0);
        summary.setLateMinutes(0);
        summary.setEarlyLeaveMinutes(0);
        summary.setOvertimeMinutes(0);
        summary.setAbsenceMinutes(0);
        attendanceSummaryRepository.save(summary);
        softDeleteViolations(summary.getId());
        softDeleteIncidents(summary.getId());
        writeAudit("ATTENDANCE_ADJUSTED", "attendance_summaries", summary.getId(), summaryPayload(summary));
        return toSummaryResponse(summary);
    }

    private AttendanceLog upsertLog(Employee employee, WorkSchedule schedule, LocalDate workDate, ProcessedAttendance processedAttendance) {
        AttendanceLog log = attendanceLogRepository.findByEmployeeIdAndWorkDateAndDeletedFalse(employee.getId(), workDate).orElseGet(AttendanceLog::new);
        log.setEmployee(employee);
        log.setWorkDate(workDate);
        log.setWorkSchedule(schedule);
        log.setScheduledStartAt(processedAttendance.scheduledStart());
        log.setScheduledEndAt(processedAttendance.scheduledEnd());
        log.setFirstInAt(processedAttendance.firstIn());
        log.setLastOutAt(processedAttendance.lastOut());
        log.setWorkedMinutes(processedAttendance.workedMinutes());
        log.setRawEventCount(processedAttendance.rawEventCount());
        log.setMissingIn(processedAttendance.missingIn());
        log.setMissingOut(processedAttendance.missingOut());
        log.setNoScudData(processedAttendance.noScudData());
        log.setLogStatus(processedAttendance.manualStatus() ? "MANUAL" : "CALCULATED");
        return attendanceLogRepository.save(log);
    }

    private AttendanceSummary upsertSummary(Employee employee, AssignmentSnapshot assignment, WorkSchedule schedule, AttendanceLog log, LocalDate workDate, ProcessedAttendance processedAttendance) {
        AttendanceSummary summary = attendanceSummaryRepository.findByEmployeeIdAndWorkDateAndDeletedFalse(employee.getId(), workDate).orElseGet(AttendanceSummary::new);
        summary.setEmployee(employee);
        summary.setEmployeeAssignmentId(assignment.assignmentId());
        summary.setDepartment(assignment.departmentId() == null ? null : departmentReference(assignment.departmentId()));
        summary.setAttendanceLog(log);
        summary.setWorkDate(workDate);
        summary.setWorkSchedule(schedule);
        summary.setAttendanceStatus(processedAttendance.status());
        summary.setLateMinutes(processedAttendance.lateMinutes());
        summary.setEarlyLeaveMinutes(processedAttendance.earlyLeaveMinutes());
        summary.setOvertimeMinutes(processedAttendance.overtimeMinutes());
        summary.setAbsenceMinutes(processedAttendance.absenceMinutes());
        summary.setViolationCount(processedAttendance.violationCount());
        summary.setIncidentCreated(processedAttendance.violationCount() > 0);
        if (processedAttendance.manualStatus() == false) {
            summary.setManuallyAdjusted(false);
            summary.setAdjustedComment(null);
        }
        summary.setFinalizedAt(OffsetDateTime.now());
        AttendanceSummary saved = attendanceSummaryRepository.save(summary);
        writeAudit("ATTENDANCE_SUMMARY_UPSERTED", "attendance_summaries", saved.getId(), summaryPayload(saved));
        return saved;
    }

    private void rebuildViolations(AttendanceSummary summary, ProcessedAttendance processedAttendance, AttendanceLog log) {
        softDeleteViolations(summary.getId());
        softDeleteIncidents(summary.getId());
        if (processedAttendance.violationCount() == 0) {
            summary.setIncidentCreated(false);
            attendanceSummaryRepository.save(summary);
            return;
        }
        if (processedAttendance.lateMinutes() > 0) {
            createViolation(summary, AttendanceViolationType.LATENESS, processedAttendance.lateMinutes(), log.getScheduledStartAt(), log.getFirstInAt());
        }
        if (processedAttendance.earlyLeaveMinutes() > 0) {
            createViolation(summary, AttendanceViolationType.EARLY_LEAVE, processedAttendance.earlyLeaveMinutes(), log.getScheduledEndAt(), log.getLastOutAt());
        }
        if (processedAttendance.status() == AttendanceStatus.MISSING_PUNCH) {
            createViolation(summary, AttendanceViolationType.MISSING_PUNCH, 0, log.getScheduledStartAt(), log.getLastOutAt());
        }
        if (processedAttendance.status() == AttendanceStatus.ABSENT) {
            createViolation(summary, AttendanceViolationType.ABSENCE, processedAttendance.absenceMinutes(), log.getScheduledStartAt(), null);
        }
        if (processedAttendance.status() == AttendanceStatus.NO_DATA) {
            createViolation(summary, AttendanceViolationType.NO_DATA, processedAttendance.absenceMinutes(), log.getScheduledStartAt(), null);
        }
        if (processedAttendance.overtimeMinutes() > 0) {
            createViolation(summary, AttendanceViolationType.OVERTIME, processedAttendance.overtimeMinutes(), log.getScheduledEndAt(), log.getLastOutAt());
        }
        summary.setIncidentCreated(true);
        attendanceSummaryRepository.save(summary);
    }

    private void createViolation(AttendanceSummary summary, AttendanceViolationType type, int minutesValue, OffsetDateTime scheduledAt, OffsetDateTime actualAt) {
        AttendanceIncident incident = new AttendanceIncident();
        incident.setEmployee(summary.getEmployee());
        incident.setAttendanceSummary(summary);
        incident.setIncidentType(type.name());
        incident.setTitle("Attendance incident: " + type.name());
        incident.setDescription("Automatically created from attendance processing");
        incident.setStatus(AttendanceIncidentStatus.OPEN);
        incident.setExplanationRequired(true);
        incident.setDueAt(OffsetDateTime.now().plusDays(1));
        AttendanceIncident savedIncident = attendanceIncidentRepository.save(incident);

        AttendanceViolation violation = new AttendanceViolation();
        violation.setEmployee(summary.getEmployee());
        violation.setAttendanceSummary(summary);
        violation.setAttendanceIncident(savedIncident);
        violation.setViolationType(type);
        violation.setMinutesValue(minutesValue);
        violation.setScheduledAt(scheduledAt);
        violation.setActualAt(actualAt);
        violation.setStatus(AttendanceViolationStatus.EXPLANATION_REQUESTED);
        attendanceViolationRepository.save(violation);
        explanationTaskBootstrapService.bootstrapFromAttendanceIncident(savedIncident, summary, type);
    }

    private void softDeleteViolations(UUID summaryId) {
        for (AttendanceViolation item : attendanceViolationRepository.findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(summaryId)) {
            item.setDeleted(true);
            attendanceViolationRepository.save(item);
        }
    }

    private void softDeleteIncidents(UUID summaryId) {
        for (AttendanceIncident item : attendanceIncidentRepository.findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(summaryId)) {
            item.setDeleted(true);
            attendanceIncidentRepository.save(item);
        }
    }

    private void markEventsProcessed(List<ScudEvent> events) {
        for (ScudEvent event : events) {
            event.setNormalizationStatus(ScudNormalizationStatus.PROCESSED);
            event.setProcessedAt(OffsetDateTime.now());
            scudEventRepository.save(event);
        }
    }

    private ProcessedAttendance normalize(LocalDate workDate, WorkSchedule schedule, List<ScudEvent> events, String overrideStatus, boolean globalDataPresent) {
        TimeWindow window = attendanceWindow(workDate, schedule);
        if (StringUtils.hasText(overrideStatus)) {
            AttendanceStatus status = overrideAttendanceStatus(overrideStatus);
            int absenceMinutes = status == AttendanceStatus.PRESENT ? 0 : schedule.getRequiredMinutes();
            return new ProcessedAttendance(status, window.scheduledStart(), window.scheduledEnd(), null, null, 0, 0, 0, 0, absenceMinutes, 0, false, false, false, true);
        }

        OffsetDateTime firstIn = null;
        OffsetDateTime lastOut = null;
        OffsetDateTime openEntry = null;
        boolean missingIn = false;
        boolean missingOut = false;
        int workedMinutes = 0;
        for (ScudEvent event : events) {
            if (event.getEventType() == ScudEventType.ENTRY) {
                if (firstIn == null || event.getEventAt().isBefore(firstIn)) {
                    firstIn = event.getEventAt();
                }
                if (openEntry != null) {
                    missingOut = true;
                }
                openEntry = event.getEventAt();
            } else {
                lastOut = event.getEventAt();
                if (openEntry == null) {
                    missingIn = true;
                } else {
                    workedMinutes += minutesBetween(openEntry, event.getEventAt());
                    openEntry = null;
                }
            }
        }
        if (openEntry != null) {
            missingOut = true;
        }
        boolean noScudData = events.isEmpty();
        int lateMinutes = 0;
        int earlyLeaveMinutes = 0;
        int overtimeMinutes = 0;
        int absenceMinutes = 0;
        AttendanceStatus status;
        if (noScudData) {
            status = globalDataPresent ? AttendanceStatus.ABSENT : AttendanceStatus.NO_DATA;
            absenceMinutes = schedule.getRequiredMinutes();
        } else if (missingIn || missingOut) {
            status = AttendanceStatus.MISSING_PUNCH;
        } else {
            if (firstIn != null) {
                lateMinutes = Math.max(0, minutesBetween(window.scheduledStart(), firstIn) - schedule.getGraceMinutes());
            }
            if (lastOut != null) {
                earlyLeaveMinutes = Math.max(0, minutesBetween(lastOut, window.scheduledEnd()));
                overtimeMinutes = Math.max(0, workedMinutes - schedule.getRequiredMinutes());
            }
            if (lateMinutes > 0) {
                status = AttendanceStatus.LATE;
            } else if (earlyLeaveMinutes > 0) {
                status = AttendanceStatus.EARLY_LEAVE;
            } else if (overtimeMinutes > 0) {
                status = AttendanceStatus.OVERTIME;
            } else {
                status = AttendanceStatus.PRESENT;
            }
        }
        int violationCount = 0;
        if (lateMinutes > 0) {
            violationCount++;
        }
        if (earlyLeaveMinutes > 0) {
            violationCount++;
        }
        if (missingIn || missingOut) {
            violationCount++;
        }
        if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.NO_DATA) {
            violationCount++;
        }
        if (overtimeMinutes > 0) {
            violationCount++;
        }
        return new ProcessedAttendance(status, window.scheduledStart(), window.scheduledEnd(), firstIn, lastOut, workedMinutes, events.size(), lateMinutes, earlyLeaveMinutes, absenceMinutes, overtimeMinutes, missingIn, missingOut, noScudData, false);
    }

    private int minutesBetween(OffsetDateTime from, OffsetDateTime to) {
        if (from == null || to == null || to.isBefore(from)) {
            return 0;
        }
        return (int) Duration.between(from, to).toMinutes();
    }

    private TimeWindow attendanceWindow(LocalDate workDate, WorkSchedule schedule) {
        OffsetDateTime scheduledStart = ZonedDateTime.of(workDate, schedule.getShiftStartTime(), zoneId).toOffsetDateTime();
        LocalDate endDate = schedule.isCrossesMidnight() ? workDate.plusDays(1) : workDate;
        OffsetDateTime scheduledEnd = ZonedDateTime.of(endDate, schedule.getShiftEndTime(), zoneId).toOffsetDateTime();
        if (schedule.isCrossesMidnight() == false && scheduledEnd.isBefore(scheduledStart)) {
            scheduledEnd = scheduledEnd.plusDays(1);
        }
        return new TimeWindow(scheduledStart, scheduledEnd, scheduledStart.minusHours(6), scheduledEnd.plusHours(6));
    }

    private WorkSchedule resolveSchedule(UUID employeeId, LocalDate workDate) {
        EmployeeWorkSchedule assignment = employeeWorkScheduleRepository
            .findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(employeeId, workDate, workDate)
            .orElseGet(() -> employeeWorkScheduleRepository
                .findFirstByEmployeeIdAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(employeeId, workDate)
                .orElse(null));
        if (assignment != null) {
            return assignment.getWorkSchedule();
        }
        return workScheduleRepository.findByCodeAndDeletedFalse("STANDARD_0900_1800")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default work schedule not found"));
    }

    private AssignmentSnapshot resolveAssignment(UUID employeeId, LocalDate workDate) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "select id, department_id from hr.employee_assignments where employee_id = ? and is_deleted = false and started_at <= ? and (ended_at is null or ended_at >= ?) order by started_at desc limit 1",
            employeeId,
            workDate,
            workDate
        );
        if (rows.isEmpty()) {
            return new AssignmentSnapshot(null, null);
        }
        Map<String, Object> row = rows.get(0);
        return new AssignmentSnapshot((UUID) row.get("id"), (UUID) row.get("department_id"));
    }

    private List<UUID> activeEmployeeIds(LocalDate workDate) {
        return jdbcTemplate.queryForList(
            "select distinct employee_id from hr.employee_assignments where is_deleted = false and started_at <= ? and (ended_at is null or ended_at >= ?)",
            UUID.class,
            workDate,
            workDate
        );
    }

    private boolean hasAnyScudData(LocalDate workDate) {
        WorkSchedule schedule = workScheduleRepository.findByCodeAndDeletedFalse("STANDARD_0900_1800")
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default work schedule not found"));
        TimeWindow window = attendanceWindow(workDate, schedule);
        return scudEventRepository.findAllByDeletedFalseAndEventAtBetweenOrderByEventAtAsc(window.windowStart(), window.windowEnd()).isEmpty() == false;
    }

    private String findManualOrAbsenceMark(UUID employeeId, LocalDate workDate) {
        List<String> rows = jdbcTemplate.query(
            "select mark_status from hr.attendance_day_marks where employee_id = ? and attendance_date = ? and is_deleted = false order by created_at desc limit 1",
            (rs, rowNum) -> rs.getString(1),
            employeeId,
            workDate
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private AttendanceStatus overrideAttendanceStatus(String markStatus) {
        if (markStatus == null) {
            return AttendanceStatus.PRESENT;
        }
        return switch (markStatus) {
            case "REMOTE_WORK" -> AttendanceStatus.REMOTE_WORK;
            case "SICK_LEAVE" -> AttendanceStatus.SICK_LEAVE;
            case "BUSINESS_TRIP" -> AttendanceStatus.BUSINESS_TRIP;
            case "UNPAID_LEAVE" -> AttendanceStatus.UNPAID_LEAVE;
            case "EXCUSED_ABSENCE" -> AttendanceStatus.EXCUSED_ABSENCE;
            case "ABSENCE_UNEXCUSED" -> AttendanceStatus.ABSENCE_UNEXCUSED;
            case "DOWNTIME" -> AttendanceStatus.DOWNTIME;
            case "OTHER_ABSENCE" -> AttendanceStatus.OTHER;
            default -> AttendanceStatus.valueOf(markStatus);
        };
    }

    private AttendanceStatus parseAttendanceStatus(String adjustedStatus) {
        try {
            return AttendanceStatus.valueOf(adjustedStatus.trim());
        } catch (IllegalArgumentException exception) {
            return AttendanceStatus.MANUAL;
        }
    }

    private AttendanceSummaryResponse toSummaryResponse(AttendanceSummary summary) {
        AttendanceLog log = summary.getAttendanceLog();
        AttendanceLogResponse logResponse = log == null ? null : new AttendanceLogResponse(
            log.getId(),
            log.getEmployee().getId(),
            log.getWorkDate(),
            log.getWorkSchedule() == null ? null : log.getWorkSchedule().getId(),
            log.getScheduledStartAt(),
            log.getScheduledEndAt(),
            log.getFirstInAt(),
            log.getLastOutAt(),
            log.getWorkedMinutes(),
            log.getRawEventCount(),
            log.isMissingIn(),
            log.isMissingOut(),
            log.isNoScudData(),
            log.getLogStatus()
        );
        List<AttendanceViolationResponse> violations = attendanceViolationRepository.findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(summary.getId())
            .stream()
            .map(this::toViolationResponse)
            .toList();
        List<AttendanceIncidentResponse> incidents = attendanceIncidentRepository.findAllByAttendanceSummaryIdAndDeletedFalseOrderByCreatedAtDesc(summary.getId())
            .stream()
            .map(this::toIncidentResponse)
            .toList();
        return new AttendanceSummaryResponse(
            summary.getId(),
            summary.getEmployee().getId(),
            summary.getDepartment() == null ? null : summary.getDepartment().getId(),
            summary.getEmployeeAssignmentId(),
            summary.getWorkDate(),
            summary.getAttendanceStatus(),
            summary.getLateMinutes(),
            summary.getEarlyLeaveMinutes(),
            summary.getOvertimeMinutes(),
            summary.getAbsenceMinutes(),
            summary.getViolationCount(),
            summary.isIncidentCreated(),
            summary.isManuallyAdjusted(),
            summary.getAdjustedComment(),
            summary.getFinalizedAt(),
            logResponse,
            violations,
            incidents
        );
    }

    private AttendanceDashboardItemResponse toDashboardItem(AttendanceSummary summary) {
        return new AttendanceDashboardItemResponse(
            summary.getId(),
            summary.getEmployee().getId(),
            summary.getDepartment() == null ? null : summary.getDepartment().getId(),
            summary.getWorkDate(),
            summary.getAttendanceStatus(),
            summary.getLateMinutes(),
            summary.getEarlyLeaveMinutes(),
            summary.getOvertimeMinutes(),
            summary.getAbsenceMinutes(),
            summary.getViolationCount(),
            summary.isIncidentCreated(),
            summary.isManuallyAdjusted()
        );
    }

    private AttendanceViolationResponse toViolationResponse(AttendanceViolation violation) {
        return new AttendanceViolationResponse(
            violation.getId(),
            violation.getEmployee().getId(),
            violation.getAttendanceSummary().getId(),
            violation.getAttendanceIncident() == null ? null : violation.getAttendanceIncident().getId(),
            violation.getViolationType(),
            violation.getMinutesValue(),
            violation.getStatus(),
            violation.getScheduledAt(),
            violation.getActualAt()
        );
    }

    private AttendanceIncidentResponse toIncidentResponse(AttendanceIncident incident) {
        return new AttendanceIncidentResponse(
            incident.getId(),
            incident.getEmployee().getId(),
            incident.getAttendanceSummary().getId(),
            incident.getIncidentType(),
            incident.getTitle(),
            incident.getDescription(),
            incident.getStatus(),
            incident.isExplanationRequired(),
            incident.getDueAt()
        );
    }

    private Employee getEmployee(UUID id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private Department departmentReference(UUID id) {
        Department department = new Department();
        department.setId(id);
        return department;
    }

    private void writeAudit(String action, String entityTable, UUID entityId, String afterData) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntitySchema("hr");
        log.setEntityTable(entityTable);
        log.setEntityId(entityId);
        log.setOccurredAt(OffsetDateTime.now());
        log.setAfterData(afterData);
        auditLogRepository.save(log);
    }

    private String eventPayload(ScudEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", event.getId());
        payload.put("employeeId", event.getEmployee() == null ? null : event.getEmployee().getId());
        payload.put("eventType", event.getEventType());
        payload.put("eventAt", event.getEventAt());
        payload.put("deviceId", event.getDeviceId());
        return toJson(payload);
    }

    private String summaryPayload(AttendanceSummary summary) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", summary.getId());
        payload.put("employeeId", summary.getEmployee().getId());
        payload.put("workDate", summary.getWorkDate());
        payload.put("attendanceStatus", summary.getAttendanceStatus());
        payload.put("lateMinutes", summary.getLateMinutes());
        payload.put("earlyLeaveMinutes", summary.getEarlyLeaveMinutes());
        payload.put("overtimeMinutes", summary.getOvertimeMinutes());
        payload.put("absenceMinutes", summary.getAbsenceMinutes());
        return toJson(payload);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize attendance payload");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record TimeWindow(OffsetDateTime scheduledStart, OffsetDateTime scheduledEnd, OffsetDateTime windowStart, OffsetDateTime windowEnd) {
    }

    private record AssignmentSnapshot(UUID assignmentId, UUID departmentId) {
    }

    private record ProcessedAttendance(
        AttendanceStatus status,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        OffsetDateTime firstIn,
        OffsetDateTime lastOut,
        Integer workedMinutes,
        Integer rawEventCount,
        Integer lateMinutes,
        Integer earlyLeaveMinutes,
        Integer absenceMinutes,
        Integer overtimeMinutes,
        boolean missingIn,
        boolean missingOut,
        boolean noScudData,
        boolean manualStatus
    ) {
        int violationCount() {
            int count = 0;
            if (lateMinutes > 0) {
                count++;
            }
            if (earlyLeaveMinutes > 0) {
                count++;
            }
            if (missingIn || missingOut) {
                count++;
            }
            if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.NO_DATA) {
                count++;
            }
            if (overtimeMinutes > 0) {
                count++;
            }
            return count;
        }
    }
}
