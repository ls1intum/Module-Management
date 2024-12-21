package modulemanagement.ls1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiServiceConfig {

    @Bean
    public WebClient aiServiceWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:5000")
                .build();
    }
}
