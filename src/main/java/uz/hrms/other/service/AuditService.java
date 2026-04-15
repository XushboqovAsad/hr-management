package uz.hrms.other.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hrms.other.entity.AuditLog;
import uz.hrms.other.repository.AuditLogRepository;

import java.time.Instant;

@Service
public class AuditService {

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
