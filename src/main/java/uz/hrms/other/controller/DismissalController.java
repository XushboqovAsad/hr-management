package uz.hrms.other.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.other.*;

@RestController
@RequestMapping("/api/v1/dismissals")
@Validated
@Tag(name = "Dismissals")
class DismissalController {

    private final DismissalService dismissalService;
    private final AccessPolicy accessPolicy;

    DismissalController(DismissalService dismissalService, AccessPolicy accessPolicy) {
        this.dismissalService = dismissalService;
        this.accessPolicy = accessPolicy;
    }

    @GetMapping
    @Operation(summary = "List dismissal requests")
    List<DismissalListItemResponse> list(
        Authentication authentication,
        @RequestParam(name = "employeeId", required = false) UUID employeeId,
        @RequestParam(name = "departmentId", required = false) UUID departmentId,
        @RequestParam(name = "status", required = false) DismissalStatus status
    ) {
        ensureDismissalRead(authentication);
        return dismissalService.list(employeeId, departmentId, status)
            .stream()
            .filter(item -> accessPolicy.canReadEmployee(authentication, item.employeeId()))
            .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Dismissal card")
    DismissalCardResponse get(Authentication authentication, @PathVariable UUID id) {
        ensureDismissalRead(authentication);
        DismissalCardResponse response = dismissalService.get(id);
        ensureEmployeeScope(authentication, response.employeeId());
        return response;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create dismissal request")
    DismissalCardResponse create(Authentication authentication, @Valid @RequestBody DismissalRequestUpsertRequest request) {
        ensureDismissalWrite(authentication);
        ensureEmployeeScope(authentication, request.employeeId());
        return dismissalService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update dismissal request")
    DismissalCardResponse update(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody DismissalRequestUpsertRequest request) {
        ensureDismissalWrite(authentication);
        ensureEmployeeScope(authentication, request.employeeId());
        return dismissalService.update(id, request);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit dismissal for approval")
    DismissalCardResponse submit(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) DismissalDecisionRequest request) {
        ensureDismissalWrite(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.submit(id, request == null ? null : request.commentText());
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve dismissal")
    DismissalCardResponse approve(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) DismissalDecisionRequest request) {
        ensureDismissalApprove(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.approve(id, request == null ? null : request.commentText());
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject dismissal")
    DismissalCardResponse reject(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) DismissalDecisionRequest request) {
        ensureDismissalApprove(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.reject(id, request == null ? null : request.commentText());
    }

    @PostMapping("/{id}/order")
    @Operation(summary = "Generate dismissal order")
    DismissalPrintFormResponse order(
        Authentication authentication,
        @PathVariable UUID id,
        @RequestParam(name = "templateCode", required = false) String templateCode
    ) {
        ensureDismissalWrite(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.generateOrder(id, templateCode);
    }

    @PostMapping("/{id}/start-clearance")
    @Operation(summary = "Start clearance checklist")
    DismissalCardResponse startClearance(Authentication authentication, @PathVariable UUID id) {
        ensureDismissalWrite(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.startClearance(id);
    }

    @GetMapping("/{id}/checklist")
    @Operation(summary = "Get clearance checklist")
    List<ClearanceChecklistItemResponse> checklist(Authentication authentication, @PathVariable UUID id) {
        ensureDismissalRead(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.checklist(id);
    }

    @PostMapping("/{id}/checklist/items/{itemId}")
    @Operation(summary = "Update clearance checklist item")
    DismissalCardResponse updateChecklistItem(
        Authentication authentication,
        @PathVariable UUID id,
        @PathVariable UUID itemId,
        @Valid @RequestBody DismissalChecklistItemUpdateRequest request
    ) {
        ensureDismissalWrite(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.updateChecklistItem(id, itemId, request);
    }

    @PostMapping("/{id}/finalize")
    @Operation(summary = "Finalize dismissal")
    DismissalCardResponse finalizeDismissal(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) DismissalDecisionRequest request) {
        ensureDismissalFinalize(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.finalizeDismissal(id, request == null ? null : request.commentText());
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive dismissed employee card")
    DismissalCardResponse archive(Authentication authentication, @PathVariable UUID id, @RequestBody(required = false) DismissalDecisionRequest request) {
        ensureDismissalArchive(authentication);
        DismissalCardResponse current = dismissalService.get(id);
        ensureEmployeeScope(authentication, current.employeeId());
        return dismissalService.archive(id, request == null ? null : request.commentText());
    }

    private void ensureDismissalRead(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DISMISSAL", "READ")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureDismissalWrite(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DISMISSAL", "WRITE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureDismissalApprove(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DISMISSAL", "APPROVE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureDismissalFinalize(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DISMISSAL", "FINALIZE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureDismissalArchive(Authentication authentication) {
        if (accessPolicy.hasPermission(authentication, "DISMISSAL", "ARCHIVE")) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    private void ensureEmployeeScope(Authentication authentication, UUID employeeId) {
        if (accessPolicy.canReadEmployee(authentication, employeeId)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
}
