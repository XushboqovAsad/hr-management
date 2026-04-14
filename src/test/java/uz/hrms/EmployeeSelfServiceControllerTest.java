package uz.hrms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import uz.hrms.other.*;

@WebMvcTest(controllers = EmployeeSelfServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeSelfServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeSelfService employeeSelfService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    void dashboardReturnsSelfData() throws Exception {
        CurrentUser currentUser = currentUser();
        when(accessPolicy.hasPermission(any(), eq("ESS"), eq("READ"))).thenReturn(true);
        when(employeeSelfService.dashboard(any())).thenReturn(new EmployeeSelfDashboardResponse(
            new EmployeeSelfProfileResponse(currentUser.employeeId(), currentUser.userId(), "EMP-1", "ACTIVE", "employee", "Ali", "Valiyev", "", "Valiyev Ali", "a@b.c", null, null, null, null),
            new AttendanceDashboardResponse(0, 0, 0, 0, 0, 0, List.of()),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            0,
            new EmployeeSelfFeatureAvailabilityResponse(false, false, false, false, false, false, false, false, true, true, true, true, true, true, true)
        ));

        mockMvc.perform(get("/api/v1/ess/me/dashboard").principal(new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile.employeeId").value(currentUser.employeeId().toString()))
            .andExpect(jsonPath("$.features.directoryAvailable").value(true));
    }

    @Test
    void updateProfileReturnsUpdatedProfile() throws Exception {
        CurrentUser currentUser = currentUser();
        when(accessPolicy.hasPermission(any(), eq("ESS"), eq("WRITE"))).thenReturn(true);
        when(employeeSelfService.updateProfile(any(), any())).thenReturn(
            new EmployeeSelfProfileResponse(currentUser.employeeId(), currentUser.userId(), "EMP-1", "ACTIVE", "employee", "Ali", "Valiyev", "Karimovich", "Valiyev Ali Karimovich", "new@email.uz", null, null, null, null)
        );

        mockMvc.perform(put("/api/v1/ess/me/profile")
                .principal(new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"email\": \"new@email.uz\",
                      \"middleName\": \"Karimovich\"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("new@email.uz"))
            .andExpect(jsonPath("$.middleName").value("Karimovich"));
    }

    @Test
    void markNotificationReadReturnsUpdatedNotification() throws Exception {
        CurrentUser currentUser = currentUser();
        UUID notificationId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("ESS"), eq("WRITE"))).thenReturn(true);
        when(employeeSelfService.markNotificationRead(any(), eq(notificationId))).thenReturn(
            new EmployeeSelfNotificationResponse(notificationId, "INFO", "Title", "Body", null, null, NotificationStatus.READ, OffsetDateTime.now(), OffsetDateTime.now())
        );

        mockMvc.perform(post("/api/v1/ess/me/notifications/{id}/read", notificationId)
                .principal(new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(notificationId.toString()))
            .andExpect(jsonPath("$.status").value("READ"));
    }

    private CurrentUser currentUser() {
        return new CurrentUser(UUID.randomUUID(), UUID.randomUUID(), "employee", "hash", true, Set.of("EMPLOYEE"), Set.of("ESS:READ", "ESS:WRITE"));
    }
}
