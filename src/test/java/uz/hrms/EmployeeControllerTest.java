package uz.hrms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

@WebMvcTest(controllers = EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeQueryService employeeQueryService;

    @MockBean
    private EmployeeCommandService employeeCommandService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    void listReturnsEmployees() throws Exception {
        UUID employeeId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("EMPLOYEE"), eq("READ"))).thenReturn(true);
        when(employeeQueryService.list(any(), eq(null), eq(null), eq(null))).thenReturn(List.of(
            new EmployeeListItemResponse(
                employeeId,
                null,
                "DEV-0001",
                "ACTIVE",
                "dev.employee",
                "Dev Employee",
                "dev.employee@hrms.local",
                null,
                "HR Department",
                null,
                "HR Specialist",
                null,
                null,
                LocalDate.of(2026, 4, 1),
                null,
                false
            )
        ));

        mockMvc.perform(get("/api/v1/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].employeeId").value(employeeId.toString()))
            .andExpect(jsonPath("$[0].personnelNumber").value("DEV-0001"));
    }

    @Test
    void createReturnsEmployeeCard() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("EMPLOYEE"), eq("WRITE"))).thenReturn(true);
        when(employeeCommandService.create(any(), any())).thenReturn(
            new EmployeeCardResponse(
                employeeId,
                userId,
                "DEV-0002",
                "ACTIVE",
                "dev.new",
                "New",
                "Employee",
                null,
                "dev.new@hrms.local",
                "Employee New",
                null,
                "People Operations",
                null,
                "HR Specialist",
                null,
                "ST-001",
                null,
                null,
                LocalDate.of(2026, 4, 3),
                null,
                false
            )
        );

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "personnelNumber": "DEV-0002",
                      "username": "dev.new",
                      "password": "ChangeMe123!",
                      "email": "dev.new@hrms.local",
                      "firstName": "New",
                      "lastName": "Employee",
                      "status": "ACTIVE",
                      "hireDate": "2026-04-03"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.employeeId").value(employeeId.toString()))
            .andExpect(jsonPath("$.username").value("dev.new"));
    }

    @Test
    void statusPatchReturnsUpdatedCard() throws Exception {
        UUID employeeId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("EMPLOYEE"), eq("WRITE"))).thenReturn(true);
        when(employeeCommandService.changeStatus(any(), eq(employeeId), any())).thenReturn(
            new EmployeeCardResponse(
                employeeId,
                null,
                "DEV-0003",
                "DISMISSED",
                "dev.dismissed",
                "Dismissed",
                "Employee",
                null,
                "dev.dismissed@hrms.local",
                "Employee Dismissed",
                null,
                "People Operations",
                null,
                "HR Specialist",
                null,
                "ST-003",
                null,
                null,
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2026, 4, 3),
                false
            )
        );

        mockMvc.perform(patch("/api/v1/employees/{employeeId}/status", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "DISMISSED",
                      "dismissalDate": "2026-04-03"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DISMISSED"));
    }
}
