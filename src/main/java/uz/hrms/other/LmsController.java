package uz.hrms;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/v1/lms")
@Validated
@Tag(name = "LMS")
@SecurityRequirement(name = "bearerAuth")
class LmsController {

    private final LmsService lmsService;
    private final AccessPolicy accessPolicy;
    private final LocalFileStorageService localFileStorageService;

    LmsController(LmsService lmsService, AccessPolicy accessPolicy, LocalFileStorageService localFileStorageService) {
        this.lmsService = lmsService;
        this.accessPolicy = accessPolicy;
        this.localFileStorageService = localFileStorageService;
    }

    @GetMapping("/courses")
    @Operation(summary = "List LMS courses")
    List<LmsCourseListItemResponse> listCourses(
        Authentication authentication,
        @RequestParam(name = "query", required = false) String query,
        @RequestParam(name = "status", required = false) LmsCourseStatus status,
        @RequestParam(name = "employeeId", required = false) UUID employeeId
    ) {
        ensureRead(authentication, employeeId);
        return lmsService.listCourses(query, status, employeeId);
    }

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Get LMS course card")
    LmsCourseResponse getCourse(Authentication authentication, @PathVariable UUID courseId, @RequestParam(name = "assignmentId", required = false) UUID assignmentId) {
        ensureRead(authentication, assignmentId == null ? null : lmsService.assignmentEmployeeId(assignmentId));
        return lmsService.getCourse(courseId, assignmentId);
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create LMS course")
    LmsCourseResponse createCourse(Authentication authentication, @Valid @RequestBody LmsCourseCreateRequest request) {
        ensureWrite(authentication);
        return lmsService.createCourse(request);
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign course to employee")
    LmsAssignmentResponse assignCourse(Authentication authentication, @Valid @RequestBody LmsAssignmentCreateRequest request) {
        ensureAssign(authentication);
        return lmsService.assignCourse(request, currentUser(authentication).userId());
    }

    @GetMapping("/assignments")
    @Operation(summary = "List LMS assignments")
    List<LmsAssignmentResponse> listAssignments(
        Authentication authentication,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "status", required = false) LmsAssignmentStatus status,
        @RequestParam(name = "departmentId", required = false) UUID departmentId,
        @RequestParam(name = "positionId", required = false) UUID positionId,
        @RequestParam(name = "dueBefore", required = false) LocalDate dueBefore
    ) {
        ensureReport(authentication, employeeId);
        return lmsService.listAssignments(employeeId, status, departmentId, positionId, dueBefore);
    }

    @GetMapping("/assignments/me")
    @Operation(summary = "List my LMS assignments")
    List<LmsAssignmentResponse> myAssignments(Authentication authentication) {
        CurrentUser currentUser = currentUser(authentication);
        if (currentUser.employeeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current user is not linked to employee");
        }
        return lmsService.listAssignments(currentUser.employeeId(), null, null, null, null);
    }

    @GetMapping("/assignments/{assignmentId}")
    @Operation(summary = "Get LMS assignment")
    LmsAssignmentResponse getAssignment(Authentication authentication, @PathVariable UUID assignmentId) {
        ensureSelfOrRead(authentication, lmsService.assignmentEmployeeId(assignmentId));
        return lmsService.getAssignment(assignmentId);
    }

    @PostMapping("/assignments/{assignmentId}/start")
    @Operation(summary = "Start assigned course")
    LmsAssignmentResponse startAssignment(Authentication authentication, @PathVariable UUID assignmentId) {
        ensureSelfOrWrite(authentication, lmsService.assignmentEmployeeId(assignmentId));
        return lmsService.startAssignment(assignmentId);
    }

    @PostMapping("/assignments/{assignmentId}/lessons/{lessonId}/complete")
    @Operation(summary = "Complete LMS lesson")
    LmsAssignmentResponse completeLesson(Authentication authentication, @PathVariable UUID assignmentId, @PathVariable UUID lessonId) {
        ensureSelfOrWrite(authentication, lmsService.assignmentEmployeeId(assignmentId));
        return lmsService.completeLesson(assignmentId, lessonId);
    }

    @PostMapping("/assignments/{assignmentId}/tests/{testId}/submit")
    @Operation(summary = "Submit LMS test")
    LmsAttemptResultResponse submitTest(
        Authentication authentication,
        @PathVariable UUID assignmentId,
        @PathVariable UUID testId,
        @Valid @RequestBody LmsSubmitTestAttemptRequest request
    ) {
        ensureSelfOrWrite(authentication, lmsService.assignmentEmployeeId(assignmentId));
        return lmsService.submitTest(assignmentId, testId, request);
    }

    @GetMapping("/assignments/{assignmentId}/certificate")
    @Operation(summary = "Download LMS certificate")
    ResponseEntity<Resource> downloadCertificate(Authentication authentication, @PathVariable UUID assignmentId) {
        ensureSelfOrRead(authentication, lmsService.assignmentEmployeeId(assignmentId));
        LmsCertificate certificate = lmsService.getCertificateEntity(assignmentId);
        Resource resource = localFileStorageService.load(certificate.getStorageKey());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(certificate.getMimeType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(certificate.getFileName()).build().toString())
            .body(resource);
    }

    @GetMapping("/history/{employeeId}")
    @Operation(summary = "Learning history by employee")
    List<LmsLearningHistoryResponse> history(Authentication authentication, @PathVariable UUID employeeId) {
        ensureRead(authentication, employeeId);
        return lmsService.history(employeeId);
    }

    @GetMapping("/reports/progress")
    @Operation(summary = "HR progress report")
    List<LmsReportRowResponse> report(
        Authentication authentication,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "departmentId", required = false) UUID departmentId,
        @RequestParam(name = "positionId", required = false) UUID positionId,
        @RequestParam(name = "status", required = false) LmsAssignmentStatus status,
        @RequestParam(name = "dueBefore", required = false) LocalDate dueBefore
    ) {
        ensureReport(authentication, employeeId);
        return lmsService.report(employeeId, departmentId, positionId, status, dueBefore);
    }

    @PostMapping("/jobs/sync-mandatory")
    @Operation(summary = "Synchronize mandatory course assignments")
    LmsReminderSummaryResponse syncMandatory(Authentication authentication) {
        ensureAssign(authentication);
        return lmsService.syncMandatoryAssignments();
    }

    @PostMapping("/jobs/send-reminders")
    @Operation(summary = "Send overdue learning reminders")
    LmsReminderSummaryResponse sendReminders(Authentication authentication) {
        ensureAssign(authentication);
        return lmsService.sendOverdueReminders();
    }

    private void ensureRead(Authentication authentication, UUID employeeId) {
        if (employeeId != null && isSelf(authentication, employeeId)) {
            return;
        }
        if (accessPolicy.hasPermission(authentication, "LMS", "READ")) {
            if (employeeId == null || accessPolicy.canReadEmployee(authentication, employeeId)) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureReport(Authentication authentication, UUID employeeId) {
        if (employeeId != null && isSelf(authentication, employeeId)) {
            return;
        }
        if (accessPolicy.hasPermission(authentication, "LMS", "REPORT")) {
            if (employeeId == null || accessPolicy.canReadEmployee(authentication, employeeId)) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "LMS", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureAssign(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "LMS", "ASSIGN")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureSelfOrWrite(Authentication authentication, UUID employeeId) {
        if (isSelf(authentication, employeeId)) {
            return;
        }
        if (accessPolicy.hasPermission(authentication, "LMS", "WRITE") && accessPolicy.canReadEmployee(authentication, employeeId)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureSelfOrRead(Authentication authentication, UUID employeeId) {
        if (isSelf(authentication, employeeId)) {
            return;
        }
        if (accessPolicy.hasPermission(authentication, "LMS", "READ") && accessPolicy.canReadEmployee(authentication, employeeId)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private boolean isSelf(Authentication authentication, UUID employeeId) {
        CurrentUser currentUser = currentUser(authentication);
        return currentUser.employeeId() != null && currentUser.employeeId().equals(employeeId);
    }

    private CurrentUser currentUser(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Object principal = authentication.getPrincipal();
        if ((principal instanceof CurrentUser) == false) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return (CurrentUser) principal;
    }
}
