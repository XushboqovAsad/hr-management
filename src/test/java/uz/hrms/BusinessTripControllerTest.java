package uz.hrms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import uz.hrms.other.enums.BusinessTripStatus;
import uz.hrms.other.enums.PayrollSyncStatus;

@WebMvcTest(controllers = BusinessTripController.class)
@AutoConfigureMockMvc(addFilters = false)
class BusinessTripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusinessTripService businessTripService;

    @MockBean
    private ProtectedFileAccessService protectedFileAccessService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    void listReturnsBusinessTrips() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("BUSINESS_TRIP"), eq("READ"))).thenReturn(true);
        when(businessTripService.list(employeeId)).thenReturn(List.of(
            new BusinessTripListItemResponse(
                tripId,
                employeeId,
                "Tashkent",
                "Customer meeting",
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 4, 12),
                uz.hrms.other.enums.BusinessTripStatus.ON_APPROVAL,
                false
            )
        ));

        mockMvc.perform(get("/api/v1/business-trips").param("employeeId", employeeId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(tripId.toString()))
            .andExpect(jsonPath("$[0].status").value("ON_APPROVAL"))
            .andExpect(jsonPath("$[0].destinationCity").value("Tashkent"));
    }

    @Test
    void createReturnsCreatedBusinessTrip() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("BUSINESS_TRIP"), eq("WRITE"))).thenReturn(true);
        when(businessTripService.create(any())).thenReturn(response(tripId, employeeId, departmentId, uz.hrms.other.enums.BusinessTripStatus.DRAFT));

        mockMvc.perform(post("/api/v1/business-trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"employeeId\": \"%s\",
                      \"approverDepartmentId\": \"%s\",
                      \"destinationCountry\": \"Uzbekistan\",
                      \"destinationCity\": \"Tashkent\",
                      \"destinationAddress\": \"Amir Temur street, 1\",
                      \"purpose\": \"Customer meeting\",
                      \"startDate\": \"2026-04-10\",
                      \"endDate\": \"2026-04-12\",
                      \"transportType\": \"TRAIN\",
                      \"accommodationDetails\": \"Hotel\",
                      \"dailyAllowance\": 350000.00,
                      \"fundingSource\": \"OPEX\",
                      \"commentText\": \"Business trip for negotiations\"
                    }
                    """.formatted(employeeId, departmentId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(tripId.toString()))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.purpose").value("Customer meeting"));
    }

    @Test
    void submitReportReturnsUpdatedBusinessTrip() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("BUSINESS_TRIP"), eq("WRITE"))).thenReturn(true);
        when(businessTripService.submitReport(eq(tripId), any())).thenReturn(
            response(tripId, employeeId, departmentId, uz.hrms.other.enums.BusinessTripStatus.REPORT_SUBMITTED)
        );

        mockMvc.perform(post("/api/v1/business-trips/{id}/report", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"reportText\": \"Meetings completed and report attached.\"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(tripId.toString()))
            .andExpect(jsonPath("$.status").value("REPORT_SUBMITTED"));
    }

    @Test
    void updateDeniedWithoutWritePermission() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("BUSINESS_TRIP"), eq("WRITE"))).thenReturn(false);

        mockMvc.perform(put("/api/v1/business-trips/{id}", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"employeeId\": \"%s\",
                      \"approverDepartmentId\": \"%s\",
                      \"destinationCountry\": \"Uzbekistan\",
                      \"destinationCity\": \"Tashkent\",
                      \"purpose\": \"Updated purpose\",
                      \"startDate\": \"2026-04-10\",
                      \"endDate\": \"2026-04-12\",
                      \"dailyAllowance\": 350000.00
                    }
                    """.formatted(employeeId, departmentId)))
            .andExpect(status().isForbidden());
    }

    private BusinessTripResponse response(UUID tripId, UUID employeeId, UUID departmentId, uz.hrms.other.enums.BusinessTripStatus status) {
        return new BusinessTripResponse(
            tripId,
            employeeId,
            null,
            departmentId,
            "Uzbekistan",
            "Tashkent",
            "Amir Temur street, 1",
            "Customer meeting",
            LocalDate.of(2026, 4, 10),
            LocalDate.of(2026, 4, 12),
            "TRAIN",
            "Hotel",
            new BigDecimal("350000.00"),
            "OPEX",
            "Business trip for negotiations",
            status,
            "BT-2026-001",
            OffsetDateTime.parse("2026-04-02T10:00:00+05:00"),
            status == BusinessTripStatus.REPORT_SUBMITTED ? OffsetDateTime.parse("2026-04-13T17:00:00+05:00") : null,
            null,
            PayrollSyncStatus.PENDING,
            List.of(),
            List.of(),
            List.of(),
            "<html>print form</html>"
        );
    }
}
