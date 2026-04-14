package uz.hrms.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestAuditFilter extends OncePerRequestFilter {

    private final AuditService auditService;
    private final SecurityAuditService securityAuditService;

    public RequestAuditFilter(AuditService auditService, SecurityAuditService securityAuditService) {
        this.auditService = auditService;
        this.securityAuditService = securityAuditService;
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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            auditService.logRequest(request, response);
            securityAuditService.captureDerivedEvents(request, response, authentication);
        }
    }
}
