package uz.hrms.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class SecurityAuditService {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final List<String> HR_DECISION_MARKERS = List.of("approve", "reject", "review", "close", "submit", "finalize", "archive", "order", "sign");

    private final LoginAuditLogRepository loginAuditLogRepository;
    private final PersonalDataAccessLogRepository personalDataAccessLogRepository;
    private final DocumentAccessLogRepository documentAccessLogRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final HrDecisionLogRepository hrDecisionLogRepository;

    public SecurityAuditService(LoginAuditLogRepository loginAuditLogRepository,
                                PersonalDataAccessLogRepository personalDataAccessLogRepository,
                                DocumentAccessLogRepository documentAccessLogRepository,
                                AdminActionLogRepository adminActionLogRepository,
                                HrDecisionLogRepository hrDecisionLogRepository) {
        this.loginAuditLogRepository = loginAuditLogRepository;
        this.personalDataAccessLogRepository = personalDataAccessLogRepository;
        this.documentAccessLogRepository = documentAccessLogRepository;
        this.adminActionLogRepository = adminActionLogRepository;
        this.hrDecisionLogRepository = hrDecisionLogRepository;
    }

    public void recordLoginEvent(UUID actorUserId,
                                 String username,
                                 String eventType,
                                 String result,
                                 String failureReason,
                                 HttpServletRequest request) {
        LoginAuditLog log = new LoginAuditLog();
        log.setActorUserId(actorUserId);
        log.setUsername(username);
        log.setEventType(eventType);
        log.setResult(result);
        log.setFailureReason(failureReason);
        log.setIpAddress(request == null ? null : request.getRemoteAddr());
        log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
        log.setOccurredAt(Instant.now());
        loginAuditLogRepository.save(log);
    }

    public void recordPersonalDataAccess(Authentication authentication,
                                         UUID targetEmployeeId,
                                         String requestUri,
                                         String accessType,
                                         Collection<String> fieldsAccessed,
                                         Collection<String> maskedFields,
                                         boolean allowed) {
        Actor actor = actor(authentication);
        PersonalDataAccessLog log = new PersonalDataAccessLog();
        log.setActorUserId(actor.userId());
        log.setActorEmployeeId(actor.employeeId());
        log.setTargetEmployeeId(targetEmployeeId);
        log.setRequestUri(requestUri);
        log.setAccessType(accessType);
        log.setFieldsAccessed(join(fieldsAccessed));
        log.setMaskedFields(join(maskedFields));
        log.setAccessAllowed(allowed);
        log.setOccurredAt(Instant.now());
        personalDataAccessLogRepository.save(log);
    }

    public void recordDocumentAccess(Authentication authentication,
                                     String documentModule,
                                     UUID entityId,
                                     UUID documentId,
                                     String storageKey,
                                     String accessMode,
                                     String requestUri,
                                     boolean allowed) {
        Actor actor = actor(authentication);
        DocumentAccessLog log = new DocumentAccessLog();
        log.setActorUserId(actor.userId());
        log.setActorEmployeeId(actor.employeeId());
        log.setDocumentModule(documentModule);
        log.setEntityId(entityId);
        log.setDocumentId(documentId);
        log.setStorageKey(storageKey);
        log.setAccessMode(accessMode);
        log.setRequestUri(requestUri);
        log.setAccessAllowed(allowed);
        log.setOccurredAt(Instant.now());
        documentAccessLogRepository.save(log);
    }

    public void captureDerivedEvents(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String uri = request.getRequestURI();
        if (shouldWriteAdminAction(authentication, request)) {
            Actor actor = actor(authentication);
            AdminActionLog log = new AdminActionLog();
            log.setActorUserId(actor.userId());
            log.setActorEmployeeId(actor.employeeId());
            log.setRequestMethod(request.getMethod());
            log.setRequestUri(uri);
            log.setStatusCode(response.getStatus());
            log.setDetailsJson("{\"status\":" + response.getStatus() + "}");
            log.setOccurredAt(Instant.now());
            adminActionLogRepository.save(log);
        }
        if (shouldWriteHrDecision(request)) {
            Actor actor = actor(authentication);
            HrDecisionLog log = new HrDecisionLog();
            log.setActorUserId(actor.userId());
            log.setActorEmployeeId(actor.employeeId());
            log.setModuleCode(resolveModuleCode(uri));
            log.setDecisionAction(resolveDecisionAction(uri, request.getMethod()));
            log.setEntityType(resolveEntityType(uri));
            log.setEntityId(extractUuid(uri));
            log.setRequestUri(uri);
            log.setStatusCode(response.getStatus());
            log.setDetailsJson("{\"method\":\"" + request.getMethod() + "\",\"status\":" + response.getStatus() + "}");
            log.setOccurredAt(Instant.now());
            hrDecisionLogRepository.save(log);
        }
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditDtos.LoginAuditLogResponse> getLoginAuditLogs(Pageable pageable) {
        return loginAuditLogRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(item -> new SecurityAuditDtos.LoginAuditLogResponse(
                        item.getId(), item.getActorUserId(), item.getUsername(), item.getEventType(), item.getResult(),
                        item.getFailureReason(), item.getIpAddress(), item.getUserAgent(), item.getOccurredAt()
                ));
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditDtos.PersonalDataAccessLogResponse> getPersonalDataAccessLogs(Pageable pageable) {
        return personalDataAccessLogRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(item -> new SecurityAuditDtos.PersonalDataAccessLogResponse(
                        item.getId(), item.getActorUserId(), item.getActorEmployeeId(), item.getTargetEmployeeId(),
                        item.getRequestUri(), item.getAccessType(), item.getFieldsAccessed(), item.getMaskedFields(),
                        item.isAccessAllowed(), item.getOccurredAt()
                ));
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditDtos.DocumentAccessLogResponse> getDocumentAccessLogs(Pageable pageable) {
        return documentAccessLogRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(item -> new SecurityAuditDtos.DocumentAccessLogResponse(
                        item.getId(), item.getActorUserId(), item.getActorEmployeeId(), item.getDocumentModule(),
                        item.getEntityId(), item.getDocumentId(), item.getStorageKey(), item.getAccessMode(),
                        item.getRequestUri(), item.isAccessAllowed(), item.getOccurredAt()
                ));
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditDtos.AdminActionLogResponse> getAdminActionLogs(Pageable pageable) {
        return adminActionLogRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(item -> new SecurityAuditDtos.AdminActionLogResponse(
                        item.getId(), item.getActorUserId(), item.getActorEmployeeId(), item.getRequestMethod(),
                        item.getRequestUri(), item.getStatusCode(), item.getDetailsJson(), item.getOccurredAt()
                ));
    }

    @Transactional(readOnly = true)
    public Page<SecurityAuditDtos.HrDecisionLogResponse> getHrDecisionLogs(Pageable pageable) {
        return hrDecisionLogRepository.findAllByOrderByOccurredAtDesc(pageable)
                .map(item -> new SecurityAuditDtos.HrDecisionLogResponse(
                        item.getId(), item.getActorUserId(), item.getActorEmployeeId(), item.getModuleCode(),
                        item.getDecisionAction(), item.getEntityType(), item.getEntityId(), item.getRequestUri(),
                        item.getStatusCode(), item.getDetailsJson(), item.getOccurredAt()
                ));
    }

    private boolean shouldWriteAdminAction(Authentication authentication, HttpServletRequest request) {
        if (isSafeMethod(request.getMethod())) {
            return false;
        }
        if (request.getRequestURI().startsWith("/api/v1/admin")) {
            return true;
        }
        return actor(authentication).hasAnyRole("SUPER_ADMIN", "HR_ADMIN");
    }

    private boolean shouldWriteHrDecision(HttpServletRequest request) {
        if (isSafeMethod(request.getMethod())) {
            return false;
        }
        String uri = request.getRequestURI().toLowerCase(Locale.ROOT);
        return HR_DECISION_MARKERS.stream().anyMatch(uri::contains);
    }

    private boolean isSafeMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method);
    }

    private String resolveModuleCode(String uri) {
        String[] parts = uri.split("/");
        if (parts.length < 4) {
            return "API";
        }
        return parts[3].replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private String resolveEntityType(String uri) {
        String[] parts = uri.split("/");
        if (parts.length < 4) {
            return null;
        }
        return parts[3];
    }

    private String resolveDecisionAction(String uri, String method) {
        String normalized = uri.toLowerCase(Locale.ROOT);
        for (String marker : HR_DECISION_MARKERS) {
            if (normalized.contains(marker)) {
                return marker.toUpperCase(Locale.ROOT);
            }
        }
        return method.toUpperCase(Locale.ROOT);
    }

    private UUID extractUuid(String uri) {
        Matcher matcher = UUID_PATTERN.matcher(uri);
        if (matcher.find() == false) {
            return null;
        }
        return UUID.fromString(matcher.group());
    }

    private String join(Collection<String> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return String.join(",", items);
    }

    private Actor actor(Authentication authentication) {
        Object principal = authentication == null ? null : authentication.getPrincipal();
        UUID userId = readUuid(principal, "getUserId", "userId");
        UUID employeeId = readUuid(principal, "getEmployeeId", "employeeId");
        List<String> roles = readStrings(principal, "getRoles", "roles");
        return new Actor(userId, employeeId, roles);
    }

    @SuppressWarnings("unchecked")
    private List<String> readStrings(Object principal, String getterName, String accessorName) {
        Object value = invoke(principal, getterName, accessorName);
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private UUID readUuid(Object principal, String getterName, String accessorName) {
        Object value = invoke(principal, getterName, accessorName);
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof String text && text.isBlank() == false) {
            return UUID.fromString(text);
        }
        return null;
    }

    private Object invoke(Object principal, String getterName, String accessorName) {
        if (principal == null) {
            return null;
        }
        for (String name : List.of(getterName, accessorName)) {
            try {
                Method method = principal.getClass().getMethod(name);
                return method.invoke(principal);
            } catch (ReflectiveOperationException ignored) {
                // try next accessor
            }
        }
        return null;
    }

    private record Actor(UUID userId, UUID employeeId, List<String> roles) {
        boolean hasAnyRole(String... expected) {
            for (String value : expected) {
                if (roles.contains(value)) {
                    return true;
                }
            }
            return false;
        }
    }
}
