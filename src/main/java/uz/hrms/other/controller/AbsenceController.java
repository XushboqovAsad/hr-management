package uz.hrms.other.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.*;

@RestController
@RequestMapping("/api/v1/absences")
@Validated
@Tag(name = "Absences")
class AbsenceController {

    private final AbsenceService absenceService;
    private final ProtectedFileAccessService protectedFileAccessService;
    private final AccessPolicy accessPolicy;

    AbsenceController(AbsenceService absenceService, ProtectedFileAccessService protectedFileAccessService, AccessPolicy accessPolicy) {
        this.absenceService = absenceService;
        this.protectedFileAccessService = protectedFileAccessService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping
    @Operation(summary = "List absences")
    List<AbsenceListItemResponse> list(
        Authentication authentication,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "status", required = false) AbsenceStatus status,
        @RequestParam(name = "absenceType", required = false) AbsenceType absenceType
    ) {
        ensureRead(authentication);
        return absenceService.list(employeeId).stream()
            .filter(item -> status == null || item.status() == status)
            .filter(item -> absenceType == null || item.absenceType() == absenceType)
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Absence card")
    AbsenceResponse get(Authentication authentication, @PathVariable UUID id) {
        ensureRead(authentication);
        return absenceService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create absence draft")
    AbsenceResponse create(Authentication authentication, @Valid @RequestBody AbsenceRequest request) {
        ensureWrite(authentication);
        return absenceService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update absence draft")
    AbsenceResponse update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody AbsenceRequest request) {
        ensureWrite(authentication);
        return absenceService.update(id, request);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit absence to HR")
    AbsenceResponse submit(Authentication authentication, @PathVariable UUID id) {
        ensureWrite(authentication);
        return absenceService.submit(id);
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Move absence to HR review")
    AbsenceResponse review(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) AbsenceDecisionRequest request) {
        ensureApprove(authentication);
        return absenceService.startReview(id, request == null ? null : request.hrComment());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve absence")
    AbsenceResponse approve(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) AbsenceDecisionRequest request) {
        ensureApprove(authentication);
        return absenceService.approve(id, request == null ? null : request.hrComment());
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject absence")
    AbsenceResponse reject(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) AbsenceDecisionRequest request) {
        ensureApprove(authentication);
        return absenceService.reject(id, request == null ? null : request.hrComment());
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close absence")
    AbsenceResponse close(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) AbsenceDecisionRequest request) {
        ensureApprove(authentication);
        return absenceService.close(id, request == null ? null : request.hrComment());
    }

    @PostMapping("/{id}/payroll-sync/sent")
    @Operation(summary = "Mark absence as sent to payroll")
    AbsenceResponse markPayrollSent(Authentication authentication, @PathVariable UUID id) {
        ensureApprove(authentication);
        return absenceService.markPayrollSent(id);
    }

    @GetMapping("/{id}/documents")
    @Operation(summary = "List absence documents")
    List<AbsenceDocumentResponse> documents(Authentication authentication, @PathVariable UUID id) {
        ensureRead(authentication);
        return absenceService.documents(id);
    }

    @PostMapping(path = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload absence document")
    AbsenceDocumentResponse uploadDocument(
        Authentication authentication,
        @PathVariable UUID id,
        @Valid @RequestPart("meta") AbsenceDocumentUploadRequest request,
        @RequestPart("file") MultipartFile file
    ) {
        ensureWrite(authentication);
        return absenceService.uploadDocument(id, request, file);
    }

    @GetMapping("/{id}/documents/{documentId}/download")
    @Operation(summary = "Download absence document")
    ResponseEntity<Resource> download(Authentication authentication, HttpServletRequest request, @PathVariable UUID id, @PathVariable UUID documentId) {
        ensureRead(authentication);
        AbsenceDocument document = absenceService.getDocumentEntity(id, documentId);
        return protectedFileAccessService.serve(authentication, request, "ABSENCE", id, documentId, document.getStorageKey(), document.getOriginalFileName(), document.getContentType(), false);
    }

    @GetMapping("/{id}/documents/{documentId}/preview")
    @Operation(summary = "Preview absence document")
    ResponseEntity<Resource> preview(Authentication authentication, HttpServletRequest request, @PathVariable UUID id, @PathVariable UUID documentId) {
        ensureRead(authentication);
        AbsenceDocument document = absenceService.getDocumentEntity(id, documentId);
        return protectedFileAccessService.serve(authentication, request, "ABSENCE", id, documentId, document.getStorageKey(), document.getOriginalFileName(), document.getContentType(), true);
    }

    @GetMapping("/timesheet")
    @Operation(summary = "Get attendance marks from absences")
    List<AttendanceDayMarkResponse> timesheet(
        Authentication authentication,
        @RequestParam UUID employeeId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ensureRead(authentication);
        return absenceService.timesheet(employeeId, from, to);
    }

    @GetMapping("/analytics/frequent")
    @Operation(summary = "Frequent absence analytics")
    List<FrequentAbsenceAnalyticsResponse> frequentAnalytics(
        Authentication authentication,
        @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(name = "threshold", defaultValue = "3") long threshold
    ) {
        ensureRead(authentication);
        return absenceService.frequentAnalytics(from, to, threshold);
    }

    private void ensureRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "ABSENCE", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "ABSENCE", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureApprove(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "ABSENCE", "APPROVE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
}
