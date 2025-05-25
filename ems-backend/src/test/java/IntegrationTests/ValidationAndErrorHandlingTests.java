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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = EmsBackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class ValidationAndErrorHandlingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    public void setup() {
        adminToken = jwtTokenUtil.generateToken("admin@example.com", "ADMIN");
    }

    @Test
    public void testCreateEmployeeWithEmptyFirstName_shouldReturnValidationError() throws Exception {
        // Тест валідації: порожнє ім'я працівника
        EmployeeDto invalidEmployee = new EmployeeDto();
        invalidEmployee.setFirstName("");  // Порожнє ім'я
        invalidEmployee.setLastName("Smith");
        invalidEmployee.setEmail("john.smith@example.com");

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("firstName")));
    }

    @Test
    public void testCreateEmployeeWithEmptyLastName_shouldReturnValidationError() throws Exception {

        EmployeeDto invalidEmployee = new EmployeeDto();
        invalidEmployee.setFirstName("John");
        invalidEmployee.setLastName("");  // Порожнє прізвище
        invalidEmployee.setEmail("john.smith@example.com");

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("lastName")));
    }

    @Test
    public void testCreateEmployeeWithInvalidEmail_shouldReturnValidationError() throws Exception {
        EmployeeDto invalidEmployee = new EmployeeDto();
        invalidEmployee.setFirstName("John");
        invalidEmployee.setLastName("Smith");
        invalidEmployee.setEmail("invalid-email");  // Неправильний формат email

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("email")));
    }

    @Test
    public void testCreateEmployeeWithDuplicateEmail_shouldReturnError() throws Exception {

        EmployeeDto employee1 = new EmployeeDto();
        employee1.setFirstName("John");
        employee1.setLastName("Doe");
        employee1.setEmail("duplicate@example.com");

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(employee1)))
                .andExpect(status().isCreated());

        EmployeeDto employee2 = new EmployeeDto();
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("duplicate@example.com");

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content(objectMapper.writeValueAsString(employee2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("email")));
    }

    @Test
    public void testRegisterUserWithEmptyEmail_shouldReturnValidationError() throws Exception {
        Map<String, String> invalidUser = new HashMap<>();
        invalidUser.put("email", "");  // Порожній email
        invalidUser.put("password", "password123");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("email")));
    }

    @Test
    public void testRegisterUserWithShortPassword_shouldReturnValidationError() throws Exception {
        Map<String, String> invalidUser = new HashMap<>();
        invalidUser.put("email", "valid@example.com");
        invalidUser.put("password", "123");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("password")));
    }

    @Test
    public void testLoginWithInvalidCredentials_shouldReturnAuthError() throws Exception {
        Map<String, String> invalidLogin = new HashMap<>();
        invalidLogin.put("email", "admin@example.com");
        invalidLogin.put("password", "wrongpassword");  // Неправильний пароль

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", containsString("Authentication failed")));
    }

    @Test
    public void testMethodNotAllowed_shouldReturnError() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "admin@example.com");
        loginData.put("password", "admin123");

        mockMvc.perform(put("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testInvalidContentType_shouldReturnError() throws Exception {

        String loginData = "email=admin@example.com&password=admin123";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(loginData))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidJsonFormat_shouldReturnError() throws Exception {
        String invalidJson = "{\"email\":\"admin@example.com\", \"password\":\"admin123\"";

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
