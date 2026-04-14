package uz.hrms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uz.hrms.other.*;

@WebMvcTest(controllers = ExplanationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExplanationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExplanationService explanationService;

    @MockBean
    private ProtectedFileAccessService localFileStorageService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    void inboxReturnsItems() throws Exception {
        UUID incidentId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("EXPLANATION"), eq("READ"))).thenReturn(true);
        when(explanationService.inbox(null, null, null)).thenReturn(List.of(
            new ExplanationInboxItemResponse(
                incidentId,
                employeeId,
                null,
                ExplanationIncidentSource.SCUD,
                "LATENESS",
                "Нужно предоставить объяснительную",
                OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(1),
                ExplanationIncidentStatus.PENDING_EXPLANATION,
                ExplanationStatus.DRAFT,
                false,
                false
            )
        ));

        mockMvc.perform(get("/api/v1/explanations/inbox"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].incidentId").value(incidentId.toString()))
            .andExpect(jsonPath("$[0].incidentSource").value("SCUD"));
    }

    @Test
    void createRewardReturnsCreated() throws Exception {
        UUID rewardId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("REWARD"), eq("WRITE"))).thenReturn(true);
        when(explanationService.createReward(any())).thenReturn(
            new RewardActionResponse(
                rewardId,
                employeeId,
                null,
                RewardType.BONUS,
                java.time.LocalDate.of(2026, 4, 2),
                java.math.BigDecimal.valueOf(500000),
                "UZS",
                "Quarterly bonus",
                RewardStatus.APPROVED,
                null,
                null
            )
        );

        mockMvc.perform(post("/api/v1/explanations/rewards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"employeeId\": \"%s\",
                      \"rewardType\": \"BONUS\",
                      \"rewardDate\": \"2026-04-02\",
                      \"amount\": 500000,
                      \"currencyCode\": \"UZS\",
                      \"reasonText\": \"Quarterly bonus\"
                    }
                    """.formatted(employeeId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(rewardId.toString()))
            .andExpect(jsonPath("$.rewardType").value("BONUS"));
    }
}
