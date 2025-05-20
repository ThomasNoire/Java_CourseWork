package IntegrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coursework.ems_backend.EmsBackendApplication;
import net.coursework.ems_backend.dto.EmployeeDto;
import net.coursework.ems_backend.entity.Employee;
import net.coursework.ems_backend.repository.EmployeeRepository;
import net.coursework.ems_backend.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = EmsBackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // Token for admin and user roles
    private String adminToken;
    private String userToken;

    private Long employeeId;

    @BeforeEach
    public void setup() {
        // Clear database before each test
        employeeRepository.deleteAll();

        // Generate tokens for testing
        adminToken = jwtTokenUtil.generateToken("admin@example.com", "ADMIN");
        userToken = jwtTokenUtil.generateToken("user@example.com", "USER");

        // Create test employee to be used in tests
        createEmployeeForTesting();
    }

    private void createEmployeeForTesting() {
        try {
            String employeeJson = """
                {
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "email": "jane.smith@example.com"
                }
                """;

            MvcResult result = mockMvc.perform(post("/api/employees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + adminToken)
                            .content(employeeJson))
                    .andReturn();

            String responseContent = result.getResponse().getContentAsString();
            EmployeeDto createdEmployee = objectMapper.readValue(responseContent, EmployeeDto.class);
            employeeId = createdEmployee.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Test for creating an employee with admin role
    @Test
    public void testCreateEmployee() throws Exception {
        String employeeJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@example.com"
            }
            """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(employeeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    public void testGetEmployeeById_withAuth_shouldReturnEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")))
                .andExpect(jsonPath("$.email", is("jane.smith@example.com")));
    }

    @Test
    public void testUpdateEmployee_withAuth_shouldReturnUpdatedEmployee() throws Exception {
        String updateJson = """
                {
                    "firstName": "Janet",
                    "lastName": "Smith",
                    "email": "janet.smith@example.com"
                }
                """;

        mockMvc.perform(put("/api/employees/" + employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Janet")))
                .andExpect(jsonPath("$.email", is("janet.smith@example.com")));
    }

    @Test
    public void testDeleteEmployee_withAuth_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/employees/" + employeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee deleted successfully!"));
    }

    @Test
    public void testAuthenticationWithToken() throws Exception {
        // Try to get all employees - requires ADMIN or USER role
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUserRoleAccess() throws Exception {
        // User with USER role should have access to GET requests
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // But should not have access to POST requests (creating an employee)
        String employeeJson = """
            {
                "firstName": "Test",
                "lastName": "User",
                "email": "test.user@example.com"
            }
            """;

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isForbidden());  // Expect 403 Forbidden
    }

    @Test
    public void testAuthenticationWithoutToken() throws Exception {
        // Try to get all employees without a token
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());  // Expect 401 Unauthorized
    }
}