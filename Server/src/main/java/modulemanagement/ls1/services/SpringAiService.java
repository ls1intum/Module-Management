package modulemanagement.ls1.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAiService {

    private final ChatClient chatClient;

    public SpringAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateResponse(String userPrompt) {

        var chatResponse = chatClient.prompt()
                .user(userPrompt)
                .call();

        return chatResponse.content();
    }
}
