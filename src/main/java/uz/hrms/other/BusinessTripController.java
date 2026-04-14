package uz.hrms.other;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
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

@RestController
@RequestMapping("/api/v1/business-trips")
@Validated
@Tag(name = "Business trips")
class BusinessTripController {

    private final BusinessTripService businessTripService;
    private final ProtectedFileAccessService protectedFileAccessService;
    private final AccessPolicy accessPolicy;

    BusinessTripController(BusinessTripService businessTripService, ProtectedFileAccessService protectedFileAccessService, AccessPolicy accessPolicy) {
        this.businessTripService = businessTripService;
        this.protectedFileAccessService = protectedFileAccessService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping
    @Operation(summary = "List business trips")
    List<BusinessTripListItemResponse> list(Authentication authentication, @RequestParam(name = "employeeId", required = false) UUID employeeId) {
        ensureRead(authentication);
        return businessTripService.list(employeeId);
    }

    @GetMapping("/overdue-reports")
    @Operation(summary = "List overdue trip reports")
    List<BusinessTripListItemResponse> overdueReports(Authentication authentication) {
        ensureRead(authentication);
        return businessTripService.listOverdueReports();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Business trip card")
    BusinessTripResponse get(Authentication authentication, @PathVariable UUID id) {
        ensureRead(authentication);
        return businessTripService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create business trip draft")
    BusinessTripResponse create(Authentication authentication, @Valid @RequestBody BusinessTripRequest request) {
        ensureWrite(authentication);
        return businessTripService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update business trip draft")
    BusinessTripResponse update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody BusinessTripRequest request) {
        ensureWrite(authentication);
        return businessTripService.update(id, request);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit for approval")
    BusinessTripResponse submit(Authentication authentication, @PathVariable UUID id) {
        ensureWrite(authentication);
        return businessTripService.submit(id);
    }

    @PostMapping("/{id}/approvals/{approvalId}/approve")
    @Operation(summary = "Approve business trip step")
    BusinessTripResponse approve(Authentication authentication, @PathVariable UUID id, @PathVariable UUID approvalId, @RequestBody(required = false) BusinessTripApprovalDecisionRequest request) {
        ensureApprove(authentication);
        return businessTripService.approve(id, approvalId, request == null ? null : request.commentText());
    }

    @PostMapping("/{id}/approvals/{approvalId}/reject")
    @Operation(summary = "Reject business trip step")
    BusinessTripResponse reject(Authentication authentication, @PathVariable UUID id, @PathVariable UUID approvalId, @RequestBody(required = false) BusinessTripApprovalDecisionRequest request) {
        ensureApprove(authentication);
        return businessTripService.reject(id, approvalId, request == null ? null : request.commentText());
    }

    @PostMapping("/{id}/order")
    @Operation(summary = "Generate business trip order print form")
    BusinessTripPrintFormResponse generateOrder(Authentication authentication, @PathVariable UUID id, @RequestParam(name = "templateCode", required = false) String templateCode) {
        ensureApprove(authentication);
        return businessTripService.generateOrder(id, templateCode);
    }

    @PostMapping("/{id}/report")
    @Operation(summary = "Submit return report")
    BusinessTripResponse submitReport(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody BusinessTripReportRequest request) {
        ensureWrite(authentication);
        return businessTripService.submitReport(id, request);
    }

    @GetMapping("/{id}/documents")
    @Operation(summary = "List business trip documents")
    List<BusinessTripDocumentResponse> documents(Authentication authentication, @PathVariable UUID id) {
        ensureRead(authentication);
        return businessTripService.getDocuments(id);
    }

    @PostMapping(path = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload business trip document")
    BusinessTripDocumentResponse uploadDocument(
        Authentication authentication,
        @PathVariable UUID id,
        @Valid @RequestPart("meta") BusinessTripDocumentUploadRequest request,
        @RequestPart("file") MultipartFile file
    ) {
        ensureWrite(authentication);
        return businessTripService.uploadDocument(id, request, file);
    }

    @GetMapping("/{id}/documents/{documentId}/download")
    @Operation(summary = "Download business trip document")
    ResponseEntity<Resource> download(Authentication authentication, HttpServletRequest request, @PathVariable UUID id, @PathVariable UUID documentId) {
        ensureRead(authentication);
        BusinessTripDocument document = businessTripService.getDocumentEntity(id, documentId);
        return protectedFileAccessService.serve(authentication, request, "BUSINESS_TRIP", id, documentId, document.getStorageKey(), document.getOriginalFileName(), document.getContentType(), false);
    }

    @GetMapping("/{id}/documents/{documentId}/preview")
    @Operation(summary = "Preview business trip document")
    ResponseEntity<Resource> preview(Authentication authentication, HttpServletRequest request, @PathVariable UUID id, @PathVariable UUID documentId) {
        ensureRead(authentication);
        BusinessTripDocument document = businessTripService.getDocumentEntity(id, documentId);
        return protectedFileAccessService.serve(authentication, request, "BUSINESS_TRIP", id, documentId, document.getStorageKey(), document.getOriginalFileName(), document.getContentType(), true);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close business trip after report and documents")
    BusinessTripResponse close(Authentication authentication, @PathVariable UUID id) {
        ensureWrite(authentication);
        return businessTripService.close(id);
    }

    private void ensureRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "BUSINESS_TRIP", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "BUSINESS_TRIP", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureApprove(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "BUSINESS_TRIP", "APPROVE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
}
