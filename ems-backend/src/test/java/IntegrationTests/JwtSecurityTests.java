package IntegrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coursework.ems_backend.EmsBackendApplication;
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
public class JwtSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private String expiredToken;
    private String tampered_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTUxNjIzOTAyMn0.hacked_signature";

    @BeforeEach
    public void setup() {
        adminToken = jwtTokenUtil.generateToken("admin@example.com", "ADMIN");
        userToken = jwtTokenUtil.generateToken("user@example.com", "USER");

        expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    @Test
    public void testValidAdminToken_shouldAllowAccess() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testValidUserToken_shouldAllowAccess() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testExpiredToken_shouldDenyAccess() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testTamperedToken_shouldDenyAccess() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + tampered_token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testTokenWithWrongSignature_shouldDenyAccess() throws Exception {
        String wrongSignatureToken = adminToken.substring(0, adminToken.lastIndexOf('.') + 1) + "wrongsignature";
        
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + wrongSignatureToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testTokenWithAdminRoleForAdminEndpoint_shouldAllow() throws Exception {

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testTokenWithUserRoleForAdminEndpoint_shouldDeny() throws Exception {
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + userToken)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testTokenWithWrongPrefix_shouldDenyAccess() throws Exception {
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Token " + adminToken))  // "Token" замість "Bearer"
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSuccessfulLogin_shouldReturnToken() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "admin@example.com");
        loginRequest.put("password", "admin123");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    public void testSuccessfulRegistration_shouldReturnToken() throws Exception {
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "newuser" + System.currentTimeMillis() + "@example.com");
        registerRequest.put("password", "password123");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void testSessionlessArchitecture() throws Exception {
        
        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/employees")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
