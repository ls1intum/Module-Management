package modulemanagement.ls1.services;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final ObjectMapper objectMapper;

    public Mono<String> generateContent(CompletionServiceRequestDTO request) {
        try {
            System.out.println("Request body: " + objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
}