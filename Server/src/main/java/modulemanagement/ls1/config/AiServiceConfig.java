package modulemanagement.ls1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiServiceConfig {

    @Value("${ai-service.url:http://localhost:5000}")
    private String aiServiceUrl;

    @Bean
    public WebClient aiServiceWebClient() {
        return WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }
}