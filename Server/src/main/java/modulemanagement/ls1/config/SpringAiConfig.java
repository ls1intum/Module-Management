package modulemanagement.ls1.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration to provide ChatClient bean for SpringAI.
 * Uses the auto-configured ChatModel from SpringAI.
 */
@Configuration
public class SpringAiConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringAiConfig.class);

    @Bean
    @Primary
    public ChatClient chatClient(ChatModel chatModel) {
        log.info("Creating ChatClient bean with ChatModel type: {}", chatModel.getClass().getSimpleName());
        return ChatClient.builder(chatModel).build();
    }
}
