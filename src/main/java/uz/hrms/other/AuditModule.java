package uz.hrms.other;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

record AuditLogResponse(
        UUID id,
        UUID actorUserId,
        UUID actorEmployeeId,
        String action,
        String entitySchema,
        String entityTable,
        UUID entityId,
        String detailsJson,
        OffsetDateTime occurredAt
) {
}

@Service
class AuditService {

    private final AuditLogRepository auditLogRepository;

    AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    void logRequest(HttpServletRequest request, HttpServletResponse response) {
        if (request.getRequestURI().startsWith("/api/") == false) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CurrentUser currentUser = authentication == null ? null : (authentication.getPrincipal() instanceof CurrentUser user ? user : null);
        AuditLog log = new AuditLog();
        if (currentUser != null) {
            log.setActorUserId(currentUser.userId());
            log.setActorEmployeeId(currentUser.employeeId());
        }
        log.setAction(request.getMethod() + " " + request.getRequestURI());
        log.setDetailsJson("{\"status\":" + response.getStatus() + "}");
        log.setOccurredAt(Instant.now());
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    Page<AuditLogResponse> getLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(item -> new AuditLogResponse(
                        item.getId(),
                        item.getActorUserId(),
                        item.getActorEmployeeId(),
                        item.getAction(),
                        item.getEntitySchema(),
                        item.getEntityTable(),
                        item.getEntityId(),
                        item.getDetailsJson(),
                        item.getOccurredAt()
                ));
    }
}

@Component
class RequestAuditFilter extends OncePerRequestFilter {

    private final AuditService auditService;

    RequestAuditFilter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.startsWith("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            auditService.logRequest(request, response);
        }
    }
}

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit")
@SecurityRequirement(name = "bearerAuth")
class AuditController {

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
