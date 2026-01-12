package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modulemanagement.ls1.shared.LLMUsageUtil;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LLMGenerationService {
    private final ChatModel chatModel;

    public String generate(String prompt, String field) {
        try {
            long startTime = System.currentTimeMillis();

            Prompt chatPrompt = new Prompt(new UserMessage(prompt));
            ChatResponse chatResponse = chatModel.call(chatPrompt);

            String response = chatResponse.getResult().getOutput().getText();
            long duration = System.currentTimeMillis() - startTime;

            LLMUsageUtil.extractAndLogUsage(chatResponse, duration);

            return response;
        } catch (Exception e) {
            log.error("Error generating {}: {}", field, e.getMessage(), e);
            throw new RuntimeException("Failed to generate " + field + ": " + e.getMessage(), e);
        }
    }
}
