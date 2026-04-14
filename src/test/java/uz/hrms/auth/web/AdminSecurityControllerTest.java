package uz.hrms.auth.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uz.hrms.auth.service.AuthorizationQueryService;
import uz.hrms.security.AccessPolicy;

@WebMvcTest(controllers = AdminSecurityController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorizationQueryService authorizationQueryService;

    @MockBean
    private AccessPolicy accessPolicy;

    @Test
    @WithMockUser(username = "hr.admin")
    void getRolesReturnsRoles() throws Exception {
        UUID roleId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("ROLE"), eq("READ"))).thenReturn(true);
        when(authorizationQueryService.getRoles()).thenReturn(List.of(
            new AdminSecurityDtos.RoleResponse(roleId, "HR_ADMIN", "HR Admin", "HR administrator", List.of("EMPLOYEE:READ", "ROLE:READ"))
        ));

        mockMvc.perform(get("/api/v1/admin/roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(roleId.toString()))
            .andExpect(jsonPath("$[0].code").value("HR_ADMIN"))
            .andExpect(jsonPath("$[0].permissions[0]").value("EMPLOYEE:READ"));
    }

    @Test
    @WithMockUser(username = "auditor")
    void getPermissionsReturnsPermissions() throws Exception {
        UUID permissionId = UUID.randomUUID();
        when(accessPolicy.hasPermission(any(), eq("ROLE"), eq("READ"))).thenReturn(true);
        when(authorizationQueryService.getPermissions()).thenReturn(List.of(
            new AdminSecurityDtos.PermissionResponse(permissionId, "EMPLOYEE", "READ", "Read employees", "Allows viewing employee cards")
        ));

        mockMvc.perform(get("/api/v1/admin/permissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(permissionId.toString()))
            .andExpect(jsonPath("$[0].moduleCode").value("EMPLOYEE"))
            .andExpect(jsonPath("$[0].actionCode").value("READ"));
    }
}
