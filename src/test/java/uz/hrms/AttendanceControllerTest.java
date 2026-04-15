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
import uz.hrms.other.*;
import uz.hrms.other.dto.attendanceDtos.AttendanceDashboardResponse;

@WebMvcTest(controllers = AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    void dashboardReturnsAggregatedAttendance() throws Exception {
        UUID summaryId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("ATTENDANCE"), eq("READ"))).thenReturn(true);
        when(attendanceService.dashboard(any(), any(), any(), any(), any())).thenReturn(
            new AttendanceDashboardResponse(
                12,
                2,
                1,
                1,
                0,
                3,
                List.of(new AttendanceDashboardItemResponse(
                    summaryId,
                    employeeId,
                    departmentId,
                    LocalDate.of(2026, 4, 2),
                    AttendanceStatus.PRESENT,
                    10,
                    0,
                    30,
                    0,
                    1,
                    true,
                    false
                ))
            )
        );

        mockMvc.perform(get("/api/v1/attendance/dashboard")
                .param("from", "2026-04-01")
                .param("to", "2026-04-30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalDays").value(12))
            .andExpect(jsonPath("$.items[0].summaryId").value(summaryId.toString()))
            .andExpect(jsonPath("$.items[0].attendanceStatus").value("PRESENT"));
    }

    @Test
    void processReturnsProcessedAttendanceSummaries() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UUID summaryId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("ATTENDANCE"), eq("WRITE"))).thenReturn(true);
        when(attendanceService.processWorkDate(eq(LocalDate.of(2026, 4, 2)), eq(employeeId))).thenReturn(
            List.of(new AttendanceSummaryResponse(
                summaryId,
                employeeId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 4, 2),
                AttendanceStatus.PRESENT,
                0,
                0,
                15,
                0,
                0,
                false,
                false,
                null,
                OffsetDateTime.parse("2026-04-02T19:00:00+05:00"),
                null,
                List.of(),
                List.of()
            ))
        );

        mockMvc.perform(post("/api/v1/attendance/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"workDate\": \"2026-04-02\",
                      \"employeeId\": \"%s\"
                    }
                    """.formatted(employeeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(summaryId.toString()))
            .andExpect(jsonPath("$[0].attendanceStatus").value("PRESENT"));
    }
}
