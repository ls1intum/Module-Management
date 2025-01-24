package modulemanagement.ls1.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import modulemanagement.ls1.dtos.ai.CompletionServiceResponseDTO;
import modulemanagement.ls1.dtos.ai.CompletionServiceRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AiCompletionService {
    private final WebClient aiServiceWebClient;

    public Mono<String> generateContent(CompletionServiceRequestDTO request) {
        return aiServiceWebClient.post()
                .uri("/completions/content")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CompletionServiceResponseDTO.class)
                .map(CompletionServiceResponseDTO::getResponseData);
    }

    public Mono<String> generateExaminationAchievements(CompletionServiceRequestDTO request) {
        return aiServiceWebClient.post()
                .uri("/completions/examination-achievements")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CompletionServiceResponseDTO.class)
                .map(CompletionServiceResponseDTO::getResponseData);
    }

    public Mono<String> generateLearningOutcomes(CompletionServiceRequestDTO request) {
        return aiServiceWebClient.post()
                .uri("/completions/learning-outcomes")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CompletionServiceResponseDTO.class)
                .map(CompletionServiceResponseDTO::getResponseData);
    }

    public Mono<String> generateTeachingMethods(CompletionServiceRequestDTO request) {
        return aiServiceWebClient.post()
                .uri("/completions/teaching-methods")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CompletionServiceResponseDTO.class)
                .map(CompletionServiceResponseDTO::getResponseData);
    }
}