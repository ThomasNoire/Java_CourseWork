package IntegrationTests;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleServerTest {

    private final String baseUrl = "http://localhost:8081";
    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testServerRunning() {
        System.out.println("Checking if server is running at " + baseUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/", String.class);
        System.out.println("Server response status: " + response.getStatusCode());
        System.out.println("Server response body: " + response.getBody());
        
        assertEquals(200, response.getStatusCodeValue(), "Server should be running and return 200 status code");
    }
}
