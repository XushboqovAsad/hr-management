package uz.hrms.other.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.*;

@RestController
@RequestMapping("/api/v1/attendance")
@Validated
@Tag(name = "Attendance")
class AttendanceController {

    private final AttendanceService attendanceService;
    private final AccessPolicy accessPolicy;

    AttendanceController(AttendanceService attendanceService, AccessPolicy accessPolicy) {
        this.attendanceService = attendanceService;
        this.accessPolicy = accessPolicy;
    }

    @PostMapping("/scud-events")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ingest raw SCUD event")
    ScudEventIngestRequest ingestEvent(Authentication authentication, @Valid @RequestBody ScudEventIngestRequest request) {
        ensureWrite(authentication);
        return attendanceService.ingestEvent(request);
    }

    @PostMapping("/scud-events/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Batch ingest raw SCUD events")
    List<ScudEventIngestRequest> ingestBatch(Authentication authentication, @Valid @RequestBody ScudEventBatchRequest request) {
        ensureWrite(authentication);
        return attendanceService.ingestBatch(request);
    }

    @PostMapping("/process")
    @Operation(summary = "Process attendance for work date")
    List<AttendanceSummaryResponse> process(Authentication authentication, @Valid @RequestBody AttendanceProcessRequest request) {
        ensureWrite(authentication);
        return attendanceService.processWorkDate(request.workDate(), request.employeeId());
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Attendance dashboard")
    <AttendanceViolationType>
    AttendanceDashboardResponse dashboard(
        Authentication authentication,
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(name = "departmentId", required = false) UUID departmentId,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "violationType", required = false) AttendanceViolationType violationType
    ) {
        ensureRead(authentication);
        return attendanceService.dashboard(from, to, departmentId, employeeId, violationType);
    }

    @GetMapping("/employees/{employeeId}")
    @Operation(summary = "Attendance detail by employee")
    List<AttendanceSummaryResponse> employeeAttendance(
        Authentication authentication,
        @PathVariable UUID employeeId,
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ensureRead(authentication);
        return attendanceService.employeeAttendance(employeeId, from, to);
    }

    @GetMapping("/violations")
    @Operation(summary = "Attendance violations")
    List<AttendanceViolationResponse> violations(
        Authentication authentication,
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(name = "departmentId", required = false) UUID departmentId,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "violationType", required = false) AttendanceViolationType violationType
    ) {
        ensureRead(authentication);
        return attendanceService.violations(from, to, departmentId, employeeId, violationType);
    }

    @PostMapping("/summaries/{summaryId}/adjust")
    @Operation(summary = "Manual HR adjustment for attendance summary")
    AttendanceSummaryResponse adjust(Authentication authentication, @PathVariable UUID summaryId, @Valid @RequestBody AttendanceAdjustmentRequest request) {
        ensureWrite(authentication);
        return attendanceService.adjustSummary(summaryId, request);
    }

    private void ensureRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "ATTENDANCE", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "ATTENDANCE", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
}
