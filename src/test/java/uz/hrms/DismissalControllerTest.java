package uz.hrms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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

@WebMvcTest(controllers = DismissalController.class)
@AutoConfigureMockMvc(addFilters = false)
class DismissalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DismissalService dismissalService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    void listReturnsDismissalRequests() throws Exception {
        UUID dismissalId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("DISMISSAL"), eq("READ"))).thenReturn(true);
        when(accessPolicy.canReadEmployee(any(), eq(employeeId))).thenReturn(true);
        when(dismissalService.list(null, null, null)).thenReturn(List.of(
            new DismissalListItemResponse(
                dismissalId,
                employeeId,
                null,
                DismissalReasonType.RESIGNATION,
                LocalDate.of(2026, 4, 20),
                DismissalStatus.DRAFT,
                null,
                false
            )
        ));

        mockMvc.perform(get("/api/v1/dismissals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(dismissalId.toString()))
            .andExpect(jsonPath("$[0].reasonType").value("RESIGNATION"));
    }

    @Test
    void finalizeReturnsUpdatedCard() throws Exception {
        UUID dismissalId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        DismissalCardResponse response = new DismissalCardResponse(
            dismissalId,
            employeeId,
            null,
            null,
            DismissalReasonType.RESIGNATION,
            "By own will",
            LocalDate.of(2026, 4, 20),
            DismissalStatus.FINALIZED,
            "DISM-2026-00001",
            "DISMISSAL_DEFAULT",
            OffsetDateTime.now(),
            "<html></html>",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null,
            OffsetDateTime.now(),
            "PENDING",
            "done",
            ClearanceChecklistStatus.COMPLETED,
            List.of(),
            List.of()
        );
        when(accessPolicy.hasPermission(any(), eq("DISMISSAL"), eq("FINALIZE"))).thenReturn(true);
        when(accessPolicy.canReadEmployee(any(), eq(employeeId))).thenReturn(true);
        when(dismissalService.get(dismissalId)).thenReturn(response);
        when(dismissalService.finalizeDismissal(eq(dismissalId), eq("Finalize now"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/dismissals/{id}/finalize", dismissalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"commentText\": \"Finalize now\"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dismissalId.toString()))
            .andExpect(jsonPath("$.status").value("FINALIZED"));
    }
}
