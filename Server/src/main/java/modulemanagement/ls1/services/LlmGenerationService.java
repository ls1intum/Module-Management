package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LlmGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LlmGenerationService.class);
    private final ChatModel chatModel;
    private final UsageTrackingService usageTrackingService;

    public String generate(String prompt, String field) {
        try {
            long startTime = System.currentTimeMillis();

            Prompt chatPrompt = new Prompt(new UserMessage(prompt));
            ChatResponse chatResponse = chatModel.call(chatPrompt);

            String response = chatResponse.getResult().getOutput().getText();
            long duration = System.currentTimeMillis() - startTime;

            usageTrackingService.extractAndLogUsage(chatResponse, duration);

            return response;
        } catch (Exception e) {
            log.error("Error generating {}: {}", field, e.getMessage(), e);
            throw new RuntimeException("Failed to generate " + field + ": " + e.getMessage(), e);
        }
    }
}

