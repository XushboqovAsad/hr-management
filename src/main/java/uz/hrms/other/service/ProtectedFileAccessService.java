package uz.hrms.other;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import uz.hrms.audit.SecurityAuditService;

import java.util.UUID;

@Service
public class ProtectedFileAccessService {

    private final LocalFileStorageService localFileStorageService;
    private final SecurityAuditService securityAuditService;

    public ProtectedFileAccessService(LocalFileStorageService localFileStorageService,
                                      SecurityAuditService securityAuditService) {
        this.localFileStorageService = localFileStorageService;
        this.securityAuditService = securityAuditService;
    }

    public ResponseEntity<Resource> serve(Authentication authentication,
                                          HttpServletRequest request,
                                          String documentModule,
                                          UUID entityId,
                                          UUID documentId,
                                          String storageKey,
                                          String originalFileName,
                                          String contentType,
                                          boolean inline) {
        String safeStorageKey = validateStorageKey(storageKey);
        String accessMode = inline ? "PREVIEW" : "DOWNLOAD";
        try {
            Resource resource = localFileStorageService.load(safeStorageKey);
            securityAuditService.recordDocumentAccess(authentication, documentModule, entityId, documentId, safeStorageKey, accessMode, request.getRequestURI(), true);
            ContentDisposition disposition = inline
                    ? ContentDisposition.inline().filename(originalFileName).build()
                    : ContentDisposition.attachment().filename(originalFileName).build();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                    .body(resource);
        } catch (RuntimeException exception) {
            securityAuditService.recordDocumentAccess(authentication, documentModule, entityId, documentId, safeStorageKey, accessMode, request.getRequestURI(), false);
            throw exception;
        }
    }

    private String validateStorageKey(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("Storage key is required");
        }
        if (storageKey.contains("..")) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return storageKey;
    }
}
