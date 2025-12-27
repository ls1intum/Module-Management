package modulemanagement.ls1.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class SpringAiConfig {

    private static final Logger log = LoggerFactory.getLogger(SpringAiConfig.class);

    @Value("${USE_LOCAL_LLM:false}")
    private boolean useLocalLLM;

    @Primary
    @Bean
    public ChatModel primaryChatModel(List<ChatModel> chatModels) {
        if (chatModels == null || chatModels.isEmpty()) {
            log.warn("No ChatModel beans found. Primary ChatModel will not be available.");
            return null;
        }

        for (ChatModel model : chatModels) {
            log.info("Found Chat Model: {} with temperature: {}",
                    model.getDefaultOptions().getModel(),
                    model.getDefaultOptions().getTemperature());

        }

        ChatModel selectedModel;
        if (useLocalLLM) {
            selectedModel = chatModels.stream()
                    .filter(model -> !model.getClass().getSimpleName().contains("Azure"))
                    .findFirst()
                    .orElse(chatModels.getFirst());
            log.info("USE_LOCAL_LLM=true: Using OpenAI ChatModel: {} as primary",
                    selectedModel.getClass().getSimpleName());
        } else {
            selectedModel = chatModels.stream()
                    .filter(model -> model.getClass().getSimpleName().contains("Azure"))
                    .findFirst()
                    .orElse(chatModels.getFirst());
            log.info("USE_LOCAL_LLM=false: Using Azure OpenAI ChatModel: {} as primary",
                    selectedModel.getClass().getSimpleName());
        }

        return selectedModel;
    }

    @Primary
    @Bean
    public EmbeddingModel primaryEmbeddingModel(List<EmbeddingModel> embeddingModels) {
        if (embeddingModels == null || embeddingModels.isEmpty()) {
            log.warn("No EmbeddingModel beans found. Primary EmbeddingModel will not be available.");
            return null;
        }

        for (EmbeddingModel model : embeddingModels) {
            log.info("Found Embedding Model: {}", model.getClass().getSimpleName());
        }

        EmbeddingModel selectedModel;
        if (useLocalLLM) {
            selectedModel = embeddingModels.stream()
                    .filter(model -> !model.getClass().getSimpleName().contains("Azure"))
                    .findFirst()
                    .orElse(embeddingModels.getFirst());
            log.info("USE_LOCAL_LLM=true: Using OpenAI EmbeddingModel: {} as primary",
                    selectedModel.getClass().getSimpleName());
        } else {
            selectedModel = embeddingModels.stream()
                    .filter(model -> model.getClass().getSimpleName().contains("Azure"))
                    .findFirst()
                    .orElse(embeddingModels.getFirst());
            log.info("USE_LOCAL_LLM=false: Using Azure OpenAI EmbeddingModel: {} as primary",
                    selectedModel.getClass().getSimpleName());
        }

        return selectedModel;
    }
}
