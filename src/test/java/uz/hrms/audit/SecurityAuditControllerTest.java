package uz.hrms.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.hrms.security.AccessPolicy;

@WebMvcTest(controllers = SecurityAuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class SecurityAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityAuditService securityAuditService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    @WithMockUser(username = "auditor")
    void getLoginsReturnsPagedLogins() throws Exception {
        UUID logId = UUID.randomUUID();
        when(accessPolicy.canReadAudit(any())).thenReturn(true);
        when(securityAuditService.getLoginAuditLogs(any())).thenReturn(new PageImpl<>(java.util.List.of(
            new SecurityAuditDtos.LoginAuditLogResponse(
                logId,
                UUID.randomUUID(),
                "employee",
                "LOGIN",
                "SUCCESS",
                null,
                "127.0.0.1",
                "JUnit",
                Instant.parse("2026-04-02T05:00:00Z")
            )
        )));

        mockMvc.perform(get("/api/v1/audit/logins"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(logId.toString()))
            .andExpect(jsonPath("$.content[0].username").value("employee"))
            .andExpect(jsonPath("$.content[0].result").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "auditor")
    void getDocumentAccessReturnsPagedDocumentAudit() throws Exception {
        UUID documentId = UUID.randomUUID();
        when(accessPolicy.canReadAudit(any())).thenReturn(true);
        when(securityAuditService.getDocumentAccessLogs(any())).thenReturn(new PageImpl<>(java.util.List.of(
            new SecurityAuditDtos.DocumentAccessLogResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "BUSINESS_TRIP",
                UUID.randomUUID(),
                documentId,
                "files/trip/test.pdf",
                "DOWNLOAD",
                "/api/v1/business-trips/1/documents/2/download",
                true,
                Instant.parse("2026-04-02T05:10:00Z")
            )
        )));

        mockMvc.perform(get("/api/v1/audit/document-access"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].documentModule").value("BUSINESS_TRIP"))
            .andExpect(jsonPath("$.content[0].documentId").value(documentId.toString()))
            .andExpect(jsonPath("$.content[0].accessMode").value("DOWNLOAD"));
    }
}
