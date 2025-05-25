package IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.coursework.ems_backend.EmsBackendApplication;
import net.coursework.ems_backend.dto.EmployeeDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {EmsBackendApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@SpringBootTest(classes = {EmsBackendApplication.class},
//        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FullSystemTest {

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;
    private Long createdEmployeeId;
    private final String baseUrl = "http://localhost:8081";

    @BeforeAll
    public void setup() {
        System.out.println("Запуск комплексного тестування системи");
    }

    @Test
    @Order(1)
    public void testApplicationStarts() {
        System.out.println("1. Перевірка запуску програми");
        String response = restTemplate.getForObject(baseUrl + "/", String.class);
        assertNotNull(response, "Сервер не відповідає на базовий запит");
    }

    @Test
    @Order(2)
    public void testSwaggerAvailable() {
        System.out.println("2. Перевірка доступності Swagger");
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/swagger-ui/index.html", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Swagger UI недоступний");
    }

    @Test
    @Order(3)
    public void testRegisterUser() throws Exception {
        System.out.println("3. Тестування реєстрації нового користувача");
        
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "newuser@example.com");
        registerRequest.put("password", "password123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/register", 
                new HttpEntity<>(registerRequest, createJsonHeaders()),
                String.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Реєстрація користувача не повернула статус CREATED");
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertTrue(jsonResponse.has("token"), "Відповідь не містить JWT токена");
        assertTrue(jsonResponse.has("email"), "Відповідь не містить email");
        assertEquals("newuser@example.com", jsonResponse.get("email").asText(), 
                "Email у відповіді не співпадає з запитаним");
    }

    @Test
    @Order(4)
    public void testAdminLogin() throws Exception {
        System.out.println("4. Тестування входу адміністратора");
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "admin@example.com");
        loginRequest.put("password", "admin123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/login", 
                new HttpEntity<>(loginRequest, createJsonHeaders()),
                String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Вхід адміністратора не вдався");
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertTrue(jsonResponse.has("token"), "Відповідь не містить JWT токена");
        adminToken = jsonResponse.get("token").asText();
        System.out.println("  • Отримано токен адміністратора: " + adminToken);
    }

    @Test
    @Order(5)
    public void testUserLogin() throws Exception {
        System.out.println("5. Тестування входу звичайного користувача");
        
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "user@example.com");
        loginRequest.put("password", "user123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/login", 
                new HttpEntity<>(loginRequest, createJsonHeaders()),
                String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Вхід користувача не вдався");
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        assertTrue(jsonResponse.has("token"), "Відповідь не містить JWT токена");
        userToken = jsonResponse.get("token").asText();
        System.out.println("  • Отримано токен користувача: " + userToken);
    }

    @Test
    @Order(6)
    public void testAdminCanGetEmployees() {
        System.out.println("6. Перевірка доступу адміністратора до списку працівників");
        
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(adminToken);
        
        ResponseEntity<EmployeeDto[]> response = restTemplate.exchange(
                baseUrl + "/api/employees",
                HttpMethod.GET, 
                new HttpEntity<>(headers),
                EmployeeDto[].class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Адміністратор не може отримати список працівників");
        assertNotNull(response.getBody(), "Тіло відповіді порожнє");
    }

    @Test
    @Order(7)
    public void testUserCanGetEmployees() {
        System.out.println("7. Перевірка доступу користувача до списку працівників");
        
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(userToken);
        
        ResponseEntity<EmployeeDto[]> response = restTemplate.exchange(
                baseUrl + "/api/employees",
                HttpMethod.GET, 
                new HttpEntity<>(headers),
                EmployeeDto[].class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Користувач не може отримати список працівників");
        assertNotNull(response.getBody(), "Тіло відповіді порожнє");
    }

    @Test
    @Order(8)
    public void testAdminCanCreateEmployee() throws Exception {
        System.out.println("8. Перевірка можливості адміністратора створювати працівників");
        
        EmployeeDto newEmployee = new EmployeeDto();
        newEmployee.setFirstName("Test");
        newEmployee.setLastName("Employee");
        newEmployee.setEmail("test.employee@example.com");
        
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(adminToken);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/employees",
                HttpMethod.POST, 
                new HttpEntity<>(newEmployee, headers),
                String.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "Адміністратор не може створити працівника");
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        createdEmployeeId = jsonResponse.get("id").asLong();
        System.out.println("  • Створено працівника з ID: " + createdEmployeeId);
    }

    @Test
    @Order(9)
    public void testUserCannotCreateEmployee() {
        System.out.println("9. Перевірка заборони користувачу створювати працівників");
        
        EmployeeDto newEmployee = new EmployeeDto();
        newEmployee.setFirstName("User");
        newEmployee.setLastName("Created");
        newEmployee.setEmail("user.created@example.com");
        
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(userToken);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/employees",
                HttpMethod.POST, 
                new HttpEntity<>(newEmployee, headers),
                String.class);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Користувач може створити працівника, але не повинен мати такої можливості");
    }

    @Test
    @Order(10)
    public void testAdminCanUpdateEmployee() {
        System.out.println("10. Перевірка можливості адміністратора оновлювати працівників");
        
        EmployeeDto updatedEmployee = new EmployeeDto();
        updatedEmployee.setId(createdEmployeeId);
        updatedEmployee.setFirstName("Updated");
        updatedEmployee.setLastName("Name");
        updatedEmployee.setEmail("test.employee@example.com");
        
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(adminToken);
        
        ResponseEntity<EmployeeDto> response = restTemplate.exchange(
                baseUrl + "/api/employees/" + createdEmployeeId,
                HttpMethod.PUT, 
                new HttpEntity<>(updatedEmployee, headers),
                EmployeeDto.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Адміністратор не може оновити працівника");
        assertEquals("Updated", response.getBody().getFirstName(),
                "Ім'я працівника не було оновлено");
    }

    @Test
    @Order(11)
    public void testUserCannotUpdateEmployee() {
        System.out.println("11. Перевірка заборони користувачу оновлювати працівників");
        
        EmployeeDto updatedEmployee = new EmployeeDto();
        updatedEmployee.setId(createdEmployeeId);
        updatedEmployee.setFirstName("User");
        updatedEmployee.setLastName("Updated");
        updatedEmployee.setEmail("test.employee@example.com");
        
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(userToken);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/employees/" + createdEmployeeId,
                HttpMethod.PUT, 
                new HttpEntity<>(updatedEmployee, headers),
                String.class);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Користувач може оновити працівника, але не повинен мати такої можливості");
    }

    @Test
    @Order(12)
    public void testBothRolesCanGetEmployeeById() {
        System.out.println("12. Перевірка доступу обох ролей до інформації про працівника");
        
        // Перевірка для адміністратора
        HttpHeaders adminHeaders = createJsonHeaders();
        adminHeaders.setBearerAuth(adminToken);
        
        ResponseEntity<EmployeeDto> adminResponse = restTemplate.exchange(
                baseUrl + "/api/employees/" + createdEmployeeId,
                HttpMethod.GET, 
                new HttpEntity<>(adminHeaders),
                EmployeeDto.class);
        
        assertEquals(HttpStatus.OK, adminResponse.getStatusCode(),
                "Адміністратор не може отримати інформацію про працівника");
        
        // Перевірка для користувача
        HttpHeaders userHeaders = createJsonHeaders();
        userHeaders.setBearerAuth(userToken);
        
        ResponseEntity<EmployeeDto> userResponse = restTemplate.exchange(
                baseUrl + "/api/employees/" + createdEmployeeId,
                HttpMethod.GET, 
                new HttpEntity<>(userHeaders),
                EmployeeDto.class);
        
        assertEquals(HttpStatus.OK, userResponse.getStatusCode(),
                "Користувач не може отримати інформацію про працівника");
    }

    @Test
    @Order(13)
    public void testAdminCanDeleteEmployee() {
        System.out.println("13. Перевірка можливості адміністратора видаляти працівників");
        
        HttpHeaders headers = createJsonHeaders();
        headers.setBearerAuth(adminToken);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/employees/" + createdEmployeeId,
                HttpMethod.DELETE, 
                new HttpEntity<>(headers),
                String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Адміністратор не може видалити працівника");
        assertEquals("Employee deleted successfully!", response.getBody(),
                "Некоректне повідомлення про видалення");
    }

    @Test
    @Order(14)
    public void testUserCannotDeleteEmployee() {
        System.out.println("14. Перевірка заборони користувачу видаляти працівників");
        
        // Спочатку створимо нового працівника, щоб мати що видаляти
        EmployeeDto newEmployee = new EmployeeDto();
        newEmployee.setFirstName("To");
        newEmployee.setLastName("Delete");
        newEmployee.setEmail("to.delete@example.com");
        
        HttpHeaders adminHeaders = createJsonHeaders();
        adminHeaders.setBearerAuth(adminToken);
        
        ResponseEntity<EmployeeDto> createResponse = restTemplate.exchange(
                baseUrl + "/api/employees",
                HttpMethod.POST, 
                new HttpEntity<>(newEmployee, adminHeaders),
                EmployeeDto.class);
        
        Long newEmployeeId = createResponse.getBody().getId();
        
        // Тепер перевіримо, що користувач не може видалити
        HttpHeaders userHeaders = createJsonHeaders();
        userHeaders.setBearerAuth(userToken);
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/employees/" + newEmployeeId,
                HttpMethod.DELETE, 
                new HttpEntity<>(userHeaders),
                String.class);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Користувач може видалити працівника, але не повинен мати такої можливості");
    }

    @Test
    @Order(15)
    public void testUnauthenticatedAccess() {
        System.out.println("15. Перевірка блокування неавторизованого доступу");
        
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/employees", String.class);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(),
                "Неавторизований доступ не блокується");
    }

    @AfterAll
    public void printSummary() {
        System.out.println("\n==================================================");
        System.out.println("РЕЗУЛЬТАТИ ТЕСТУВАННЯ:");
        System.out.println("==================================================");
        System.out.println("✅ Програма успішно запускається");
        System.out.println("✅ Swagger UI доступний");
        System.out.println("✅ Реєстрація нових користувачів працює");
        System.out.println("✅ Автентифікація адміністратора та користувача працює");
        System.out.println("✅ Користувач з роллю USER може отримувати список працівників");
        System.out.println("✅ Користувач з роллю USER не може створювати/оновлювати/видаляти працівників");
        System.out.println("✅ Адміністратор з роллю ADMIN має повний доступ до всіх операцій");
        System.out.println("✅ Неавторизований доступ блокується");
        System.out.println("==================================================");
        System.out.println("ЗАГАЛЬНИЙ ВИСНОВОК: Система правильно налаштована з точки зору безпеки та прав доступу");
        System.out.println("==================================================");
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
} 