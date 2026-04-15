package uz.hrms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import uz.hrms.other.enums.AbsenceStatus;
import uz.hrms.other.enums.PayrollSyncStatus;
import uz.hrms.other.service.ProtectedFileAccessService;

@WebMvcTest(controllers = AbsenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AbsenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AbsenceService absenceService;

    @MockBean
    private ProtectedFileAccessService localFileStorageService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    void listReturnsAbsences() throws Exception {
        UUID employeeId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("ABSENCE"), eq("READ"))).thenReturn(true);
        when(absenceService.list(null)).thenReturn(List.of(
            new AbsenceListItemResponse(UUID.randomUUID(), employeeId, AbsenceType.SICK_LEAVE, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3), AbsenceStatus.APPROVED, true, false)
        ));

        mockMvc.perform(get("/api/v1/absences"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].employeeId").value(employeeId.toString()))
            .andExpect(jsonPath("$[0].absenceType").value("SICK_LEAVE"));
    }

    @Test
    void approveEndpointDelegatesToService() throws Exception {
        UUID absenceId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("ABSENCE"), eq("APPROVE"))).thenReturn(true);
        when(absenceService.approve(eq(absenceId), eq("ok"))).thenReturn(
            new AbsenceResponse(
                absenceId,
                employeeId,
                null,
                AbsenceType.SICK_LEAVE,
                "doctor",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 2),
                true,
                "ok",
                AbsenceStatus.APPROVED,
                PayrollSyncStatus.PENDING,
                null,
                null,
                List.of(),
                List.of(),
                List.of()
            )
        );

        mockMvc.perform(post("/api/v1/absences/" + absenceId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hrComment\":\"ok\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.hrComment").value("ok"));
    }
}
