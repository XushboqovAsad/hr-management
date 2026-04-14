package uz.hrms;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import uz.hrms.audit.SecurityAuditService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProtectedFileAccessServiceTest {

    @Test
    void shouldServeAttachmentAndLogDocumentAccess() {
        LocalFileStorageService storageService = mock(LocalFileStorageService.class);
        SecurityAuditService securityAuditService = mock(SecurityAuditService.class);
        ProtectedFileAccessService service = new ProtectedFileAccessService(storageService, securityAuditService);
        Authentication authentication = mock(Authentication.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        UUID entityId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        when(request.getRequestURI()).thenReturn("/api/v1/absences/1/documents/2/download");
        when(storageService.load("absences/file.pdf")).thenReturn(new ByteArrayResource("ok".getBytes()));

        ResponseEntity<org.springframework.core.io.Resource> response = service.serve(
                authentication,
                request,
                "ABSENCE",
                entityId,
                documentId,
                "absences/file.pdf",
                "file.pdf",
                "application/pdf",
                false
        );

        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("attachment");
        verify(securityAuditService).recordDocumentAccess(eq(authentication), eq("ABSENCE"), eq(entityId), eq(documentId), eq("absences/file.pdf"), eq("DOWNLOAD"), eq("/api/v1/absences/1/documents/2/download"), eq(true));
    }
}
