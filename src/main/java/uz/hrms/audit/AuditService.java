package uz.hrms.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.hrms.AuditLog;
import uz.hrms.AuditLogRepository;
import uz.hrms.security.CurrentUser;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logRequest(HttpServletRequest request, HttpServletResponse response) {
        if (request.getRequestURI().startsWith("/api/") == false) {
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CurrentUser currentUser = authentication != null && authentication.getPrincipal() instanceof CurrentUser user ? user : null;
        AuditLog auditLog = new AuditLog();
        if (currentUser != null) {
            auditLog.setActorUserId(currentUser.getUserId());
            auditLog.setActorEmployeeId(currentUser.getEmployeeId());
        }
        auditLog.setAction(request.getMethod() + " " + request.getRequestURI());
        auditLog.setDetailsJson("{\"status\":" + response.getStatus() + "}");
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setOccurredAt(Instant.now());
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditDtos.AuditLogResponse> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(item -> new AuditDtos.AuditLogResponse(
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
