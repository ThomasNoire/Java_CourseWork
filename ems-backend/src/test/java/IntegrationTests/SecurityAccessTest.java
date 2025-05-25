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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = EmsBackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private Long testEmployeeId;

    @BeforeEach
    public void setup() {
        // Генеруємо токени для тестування
        adminToken = jwtTokenUtil.generateToken("admin@example.com", "ADMIN");
        userToken = jwtTokenUtil.generateToken("user@example.com", "USER");
    }

    @Test
    public void testRegisterUser() throws Exception {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "newuser@example.com");
        registerRequest.put("password", "password123");

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is("newuser@example.com")))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void testUserRolePermissions() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        EmployeeDto newEmployee = new EmployeeDto();
        newEmployee.setFirstName("Test");
        newEmployee.setLastName("User");
        newEmployee.setEmail("test.user@example.com");

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmployee)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAdminRolePermissions() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        EmployeeDto newEmployee = new EmployeeDto();
        newEmployee.setFirstName("Admin");
        newEmployee.setLastName("Test");
        newEmployee.setEmail("admin.test@example.com");

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmployee)))
                .andExpect(status().isCreated())
                .andReturn();

        EmployeeDto createdEmployee = objectMapper.readValue(
                result.getResponse().getContentAsString(), EmployeeDto.class);
        testEmployeeId = createdEmployee.getId();


        newEmployee.setFirstName("Updated");
        newEmployee.setId(testEmployeeId);

        mockMvc.perform(put("/api/employees/" + testEmployeeId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")));

        mockMvc.perform(delete("/api/employees/" + testEmployeeId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee deleted successfully!"));
    }

    @Test
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPublicEndpoints() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCrossSiteRequestForgery() throws Exception {
        EmployeeDto newEmployee = new EmployeeDto();
        newEmployee.setFirstName("CSRF");
        newEmployee.setLastName("Test");
        newEmployee.setEmail("csrf.test@example.com");

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmployee)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/employees")
                        .header("Origin", "http://example.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }
} 