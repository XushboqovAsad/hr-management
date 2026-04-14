package uz.hrms.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Security audit")
@SecurityRequirement(name = "bearerAuth")
public class SecurityAuditController {

    private final SecurityAuditService securityAuditService;

    public SecurityAuditController(SecurityAuditService securityAuditService) {
        this.securityAuditService = securityAuditService;
    }

    @GetMapping("/logins")
    @PreAuthorize("@accessPolicy.canReadAudit(authentication)")
    @Operation(summary = "Read login audit journal")
    public ResponseEntity<Page<SecurityAuditDtos.LoginAuditLogResponse>> getLogins(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(securityAuditService.getLoginAuditLogs(pageable));
    }

    @GetMapping("/personal-data-access")
    @PreAuthorize("@accessPolicy.canReadAudit(authentication)")
    @Operation(summary = "Read personal data access journal")
    public ResponseEntity<Page<SecurityAuditDtos.PersonalDataAccessLogResponse>> getPersonalDataAccess(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(securityAuditService.getPersonalDataAccessLogs(pageable));
    }

    @GetMapping("/document-access")
    @PreAuthorize("@accessPolicy.canReadAudit(authentication)")
    @Operation(summary = "Read document download and preview journal")
    public ResponseEntity<Page<SecurityAuditDtos.DocumentAccessLogResponse>> getDocumentAccess(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(securityAuditService.getDocumentAccessLogs(pageable));
    }

    @GetMapping("/admin-actions")
    @PreAuthorize("@accessPolicy.canReadAudit(authentication)")
    @Operation(summary = "Read administrator actions journal")
    public ResponseEntity<Page<SecurityAuditDtos.AdminActionLogResponse>> getAdminActions(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(securityAuditService.getAdminActionLogs(pageable));
    }

    @GetMapping("/hr-decisions")
    @PreAuthorize("@accessPolicy.canReadAudit(authentication)")
    @Operation(summary = "Read HR decisions and approvals journal")
    public ResponseEntity<Page<SecurityAuditDtos.HrDecisionLogResponse>> getHrDecisions(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(securityAuditService.getHrDecisionLogs(pageable));
    }
}
