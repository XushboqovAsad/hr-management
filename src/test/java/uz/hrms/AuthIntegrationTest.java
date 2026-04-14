package uz.hrms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import uz.hrms.other.*;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired UserRoleAssignmentRepository userRoleAssignmentRepository;
    @Autowired EmployeeRepository employeeRepository;
    @Autowired EmployeeAssignmentRepository employeeAssignmentRepository;
    @Autowired DepartmentRepository departmentRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private UserAccount hrAdminUser;
    private UserAccount managerUser;
    private UserAccount employeeUser;
    private Employee managerEmployee;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeAssignmentRepository.deleteAll();
        employeeRepository.deleteAll();
        userRoleAssignmentRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        departmentRepository.deleteAll();
        userAccountRepository.deleteAll();

        Department department = new Department();
        department.setCode("IT");
        department.setName("IT Department");
        departmentRepository.save(department);

        hrAdminUser = createUser("hradmin", "HR", "Admin");
        managerUser = createUser("manager", "Dept", "Manager");
        employeeUser = createUser("employee", "Base", "Employee");

        assignRole(hrAdminUser, RoleCode.HR_ADMIN, AccessScopeType.GLOBAL, null);
        assignRole(managerUser, RoleCode.MANAGER, AccessScopeType.DEPARTMENT, department.getId());
        assignRole(employeeUser, RoleCode.EMPLOYEE, AccessScopeType.SELF, null);

        managerEmployee = createEmployee(managerUser, "0001");
        employee = createEmployee(employeeUser, "0002");

        createAssignment(managerEmployee, department, null);
        createAssignment(employee, department, managerEmployee);
    }

    @Test
    void shouldLoginAndReadOwnContext() throws Exception {
        String token = login("hradmin", "Password123");

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("hradmin"));
    }

    @Test
    void employeeShouldReadOnlyOwnProfileAndManagerShouldReadSubordinate() throws Exception {
        String employeeToken = login("employee", "Password123");
        String managerToken = login("manager", "Password123");

        mockMvc.perform(get("/api/v1/employees/{id}/profile", employee.getId()).header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personnelNumber").value("0002"));

        mockMvc.perform(get("/api/v1/employees/{id}/profile", managerEmployee.getId()).header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/employees/{id}/profile", employee.getId()).header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personnelNumber").value("0002"));
    }

    private UserAccount createUser(String username, String firstName, String lastName) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("Password123"));
        user.setEmail(username + "@test.local");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(true);
        return userAccountRepository.save(user);
    }

    private void assignRole(UserAccount user, RoleCode roleCode, AccessScopeType scopeType, java.util.UUID departmentId) {
        UserRoleAssignment assignment = new UserRoleAssignment();
        assignment.setUser(user);
        assignment.setRole(roleRepository.findByCodeAndDeletedFalse(roleCode).orElseThrow());
        assignment.setScopeType(scopeType);
        assignment.setScopeDepartmentId(departmentId);
        assignment.setActive(true);
        assignment.setValidFrom(LocalDate.now().minusDays(1));
        userRoleAssignmentRepository.save(assignment);
    }

    private Employee createEmployee(UserAccount user, String personnelNumber) {
        Employee employeeEntity = new Employee();
        employeeEntity.setUser(user);
        employeeEntity.setPersonnelNumber(personnelNumber);
        employeeEntity.setStatus("ACTIVE");
        return employeeRepository.save(employeeEntity);
    }

    private void createAssignment(Employee target, Department department, Employee manager) {
        EmployeeAssignment assignment = new EmployeeAssignment();
        assignment.setEmployee(target);
        assignment.setDepartment(department);
        assignment.setManagerEmployee(manager);
        assignment.setPrimaryAssignment(true);
        assignment.setEffectiveFrom(LocalDate.now().minusDays(1));
        employeeAssignmentRepository.save(assignment);
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String marker = "\"accessToken\":\"";
        int start = response.indexOf(marker) + marker.length();
        int end = response.indexOf('"', start);
        return response.substring(start, end);
    }
}
