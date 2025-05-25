package IntegrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coursework.ems_backend.EmsBackendApplication;
import net.coursework.ems_backend.dto.EmployeeDto;
import net.coursework.ems_backend.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = EmsBackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class AdditionalIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private String invalidToken = "invalid.token.value";
    private Long employeeId;

    @BeforeEach
    public void setup() {

        adminToken = jwtTokenUtil.generateToken("admin@example.com", "ADMIN");
        userToken = jwtTokenUtil.generateToken("user@example.com", "USER");

        createTestEmployee();
    }

    private void createTestEmployee() {
        try {
            EmployeeDto employee = new EmployeeDto();
            employee.setFirstName("Test");
            employee.setLastName("Employee");
            employee.setEmail("test.employee@example.com");

            MvcResult result = mockMvc.perform(post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content(objectMapper.writeValueAsString(employee)))
                    .andReturn();

            EmployeeDto createdEmployee = objectMapper.readValue(
                    result.getResponse().getContentAsString(), EmployeeDto.class);
            employeeId = createdEmployee.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInvalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testMalformedToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "NotBearer " + adminToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testMissingAuthorizationHeader_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidRegistrationData_shouldReturn400() throws Exception {
        Map<String, String> invalidUser = new HashMap<>();
        invalidUser.put("email", "");
        invalidUser.put("password", "123");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDuplicateEmail_shouldReturnError() throws Exception {
        Map<String, String> existingUser = new HashMap<>();
        existingUser.put("email", "admin@example.com");
        existingUser.put("password", "password123");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidLoginCredentials_shouldReturn401() throws Exception {
        Map<String, String> invalidLogin = new HashMap<>();
        invalidLogin.put("email", "admin@example.com");
        invalidLogin.put("password", "wrongpassword");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateEmployeeWithInvalidData_shouldReturn400() throws Exception {
        EmployeeDto invalidEmployee = new EmployeeDto();
        invalidEmployee.setFirstName("");
        invalidEmployee.setLastName("");
        invalidEmployee.setEmail("invalid-email");

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateNonExistentEmployee_shouldReturn404() throws Exception {
        EmployeeDto employee = new EmployeeDto();
        employee.setFirstName("Updated");
        employee.setLastName("Employee");
        employee.setEmail("updated.employee@example.com");

        mockMvc.perform(put("/api/employees/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteNonExistentEmployee_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/employees/99999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetNonExistentEmployee_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/employees/99999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllEmployees_shouldReturnPaginatedResult() throws Exception {
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllEmployeesWithSorting_shouldReturnSortedResult() throws Exception {
        mockMvc.perform(get("/api/employees")
                .param("sort", "firstName,asc")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserCanGetEmployeeById_shouldReturnEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Test")))
                .andExpect(jsonPath("$.lastName", is("Employee")));
    }

    @Test
    public void testUserCannotUpdateEmployee_shouldReturn403() throws Exception {
        EmployeeDto employee = new EmployeeDto();
        employee.setFirstName("Updated");
        employee.setLastName("ByUser");
        employee.setEmail("updated.byuser@example.com");

        mockMvc.perform(put("/api/employees/" + employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUserCannotDeleteEmployee_shouldReturn403() throws Exception {
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}
