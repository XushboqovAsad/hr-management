package uz.hrms.other;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/explanations")
@Validated
@Tag(name = "Explanations and discipline")
class ExplanationController {

    private final ExplanationService explanationService;
    private final ProtectedFileAccessService protectedFileAccessService;
    private final AccessPolicy accessPolicy;

    ExplanationController(ExplanationService explanationService, ProtectedFileAccessService protectedFileAccessService, AccessPolicy accessPolicy) {
        this.explanationService = explanationService;
        this.protectedFileAccessService = protectedFileAccessService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping("/inbox")
    @Operation(summary = "Explanation inbox")
    List<ExplanationInboxItemResponse> inbox(
        Authentication authentication,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "departmentId", required = false) UUID departmentId,
        @RequestParam(name = "explanationStatus", required = false) ExplanationStatus explanationStatus
    ) {
        ensureExplanationRead(authentication);
        return explanationService.inbox(employeeId, departmentId, explanationStatus);
    }

    @GetMapping("/incidents/{incidentId}")
    @Operation(summary = "Explanation incident card")
    ExplanationCardResponse get(Authentication authentication, @PathVariable UUID incidentId) {
        ensureExplanationRead(authentication);
        return explanationService.get(incidentId);
    }

    @PostMapping("/incidents/manual")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create manual or manager incident")
    ExplanationCardResponse createManualIncident(Authentication authentication, @Valid @RequestBody ExplanationIncidentCreateRequest request) {
        ensureExplanationWrite(authentication);
        return explanationService.createManualIncident(request);
    }

    @PostMapping("/incidents/{incidentId}/submit")
    @Operation(summary = "Submit explanation")
    ExplanationCardResponse submit(Authentication authentication, @PathVariable UUID incidentId, @Valid @RequestBody ExplanationSubmitRequest request) {
        ensureExplanationWrite(authentication);
        return explanationService.submit(incidentId, request);
    }

    @PostMapping("/incidents/{incidentId}/manager-review")
    @Operation(summary = "Manager review explanation")
    ExplanationCardResponse managerReview(Authentication authentication, @PathVariable UUID incidentId, @Valid @RequestBody ExplanationManagerReviewRequest request) {
        ensureExplanationReview(authentication);
        return explanationService.managerReview(incidentId, request);
    }

    @PostMapping("/incidents/{incidentId}/accept")
    @Operation(summary = "HR accepts explanation")
    ExplanationCardResponse accept(Authentication authentication, @PathVariable UUID incidentId, @RequestBody(required = false) ExplanationDecisionRequest request) {
        ensureExplanationDecide(authentication);
        return explanationService.accept(incidentId, request);
    }

    @PostMapping("/incidents/{incidentId}/reject")
    @Operation(summary = "HR rejects explanation")
    ExplanationCardResponse reject(Authentication authentication, @PathVariable UUID incidentId, @RequestBody(required = false) ExplanationDecisionRequest request) {
        ensureExplanationDecide(authentication);
        return explanationService.reject(incidentId, request);
    }

    @PostMapping("/incidents/{incidentId}/close-no-consequence")
    @Operation(summary = "Close explanation without consequence")
    ExplanationCardResponse closeNoConsequence(Authentication authentication, @PathVariable UUID incidentId, @RequestBody(required = false) ExplanationDecisionRequest request) {
        ensureExplanationDecide(authentication);
        return explanationService.closeNoConsequence(incidentId, request);
    }

    @PostMapping("/incidents/{incidentId}/disciplinary-actions")
    @Operation(summary = "Create disciplinary action from explanation")
    ExplanationCardResponse createDisciplinaryAction(Authentication authentication, @PathVariable UUID incidentId, @Valid @RequestBody ExplanationDisciplinaryActionRequest request) {
        ensureDisciplineWrite(authentication);
        return explanationService.createDisciplinaryAction(incidentId, request);
    }

    @GetMapping("/incidents/{incidentId}/documents")
    @Operation(summary = "List explanation documents")
    List<ExplanationDocumentResponse> documents(Authentication authentication, @PathVariable UUID incidentId) {
        ensureExplanationRead(authentication);
        return explanationService.documents(incidentId);
    }

    @PostMapping(path = "/incidents/{incidentId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload explanation document")
    ExplanationDocumentResponse uploadDocument(
        Authentication authentication,
        @PathVariable UUID incidentId,
        @Valid @RequestPart("meta") ExplanationDocumentUploadRequest request,
        @RequestPart("file") MultipartFile file
    ) {
        ensureExplanationWrite(authentication);
        return explanationService.uploadDocument(incidentId, request, file);
    }

    @GetMapping("/incidents/{incidentId}/documents/{documentId}/download")
    @Operation(summary = "Download explanation document")
    ResponseEntity<Resource> download(Authentication authentication, HttpServletRequest request, @PathVariable UUID incidentId, @PathVariable UUID documentId) {
        ensureExplanationRead(authentication);
        ExplanationDocument document = explanationService.getDocumentEntity(incidentId, documentId);
        return protectedFileAccessService.serve(authentication, request, "EXPLANATION", incidentId, documentId, document.getStorageKey(), document.getOriginalFileName(), document.getContentType(), false);
    }

    @GetMapping("/incidents/{incidentId}/documents/{documentId}/preview")
    @Operation(summary = "Preview explanation document")
    ResponseEntity<Resource> preview(Authentication authentication, HttpServletRequest request, @PathVariable UUID incidentId, @PathVariable UUID documentId) {
        ensureExplanationRead(authentication);
        ExplanationDocument document = explanationService.getDocumentEntity(incidentId, documentId);
        return protectedFileAccessService.serve(authentication, request, "EXPLANATION", incidentId, documentId, document.getStorageKey(), document.getOriginalFileName(), document.getContentType(), true);
    }

    @GetMapping("/disciplinary-actions")
    @Operation(summary = "List disciplinary actions")
    List<DisciplinaryActionResponse> disciplinaryActions(
        Authentication authentication,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "departmentId", required = false) UUID departmentId
    ) {
        ensureDisciplineRead(authentication);
        return explanationService.disciplinaryActions(employeeId, departmentId);
    }

    @GetMapping("/rewards")
    @Operation(summary = "List rewards")
    List<RewardActionResponse> rewards(
        Authentication authentication,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "departmentId", required = false) UUID departmentId
    ) {
        ensureRewardRead(authentication);
        return explanationService.rewards(employeeId, departmentId);
    }

    @PostMapping("/rewards")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create reward")
    RewardActionResponse createReward(Authentication authentication, @Valid @RequestBody RewardActionRequest request) {
        ensureRewardWrite(authentication);
        return explanationService.createReward(request);
    }

    @GetMapping("/reports/by-department")
    @Operation(summary = "Department discipline and reward report")
    List<DepartmentDisciplineReportResponse> departmentReport(
        Authentication authentication,
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        ensureDisciplineRead(authentication);
        return explanationService.reportByDepartment(from, to);
    }

    private void ensureExplanationRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "EXPLANATION", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureExplanationWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "EXPLANATION", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureExplanationReview(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "EXPLANATION", "REVIEW")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureExplanationDecide(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "EXPLANATION", "DECIDE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureDisciplineRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DISCIPLINE", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureDisciplineWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DISCIPLINE", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureRewardRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "REWARD", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureRewardWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "REWARD", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
}
