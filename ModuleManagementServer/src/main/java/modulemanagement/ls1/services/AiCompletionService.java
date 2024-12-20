package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import modulemanagement.ls1.dtos.ai.CompletionServiceResponseDTO;
import modulemanagement.ls1.dtos.ai.ModuleInfoRequestDTO;
import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AiCompletionService {

//    private final WebClient aiServiceWebClient;

//   public Mono<String> generateContent(ModuleInfoRequestDTO request) {
//       return null;
//        return aiServiceWebClient.post()
//                .uri("/completions/content")
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(CompletionServiceResponseDTO.class)
//                .map(CompletionServiceResponseDTO::getResponseData);
//    }

//    public Mono<String> generateExaminationAchievements(ModuleInfoRequestDTO request) {
//        return null;
//        return aiServiceWebClient.post()
//                .uri("/completions/examination-achievements")
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(CompletionServiceResponseDTO.class)
//                .map(CompletionServiceResponseDTO::getResponseData);
//    }
}