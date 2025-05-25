package IntegrationTests;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class RestTemplateConfig {

    @Bean
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate(new RestTemplateBuilder());
    }
}
