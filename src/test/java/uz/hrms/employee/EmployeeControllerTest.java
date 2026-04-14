package uz.hrms.employee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.hrms.security.AccessPolicy;

@WebMvcTest(controllers = EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeQueryService employeeQueryService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    @WithMockUser(username = "manager")
    void getProfileReturnsEmployeeCard() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID managerEmployeeId = UUID.randomUUID();
        when(accessPolicy.canReadEmployee(any(), eq(employeeId))).thenReturn(true);
        when(employeeQueryService.getProfile(any(), any(), eq(employeeId))).thenReturn(
            new EmployeeDtos.EmployeeProfileResponse(
                employeeId,
                userId,
                "DEV-0001",
                "ACTIVE",
                "dev.employee",
                "Dev Employee",
                departmentId,
                "HR Department",
                managerEmployeeId,
                true
            )
        );

        mockMvc.perform(get("/api/v1/employees/{employeeId}/profile", employeeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.employeeId").value(employeeId.toString()))
            .andExpect(jsonPath("$.personnelNumber").value("DEV-0001"))
            .andExpect(jsonPath("$.departmentName").value("HR Department"))
            .andExpect(jsonPath("$.sensitiveFieldsMasked").value(true));
    }
}
