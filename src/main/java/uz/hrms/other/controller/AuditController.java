package uz.hrms.other.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.hrms.other.AuditLogResponse;
import uz.hrms.other.AuditService;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditService auditService;

    AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    @Operation(summary = "Read audit logs")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.canReadAudit(authentication)")
    ResponseEntity<Page<AuditLogResponse>> getLogs(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(auditService.getLogs(pageable));
    }
}
