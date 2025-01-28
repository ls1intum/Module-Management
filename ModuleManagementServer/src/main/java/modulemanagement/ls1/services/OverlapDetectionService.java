package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import modulemanagement.ls1.dtos.ai.CompletionServiceRequestDTO;
import modulemanagement.ls1.dtos.ai.SimilarModulesDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OverlapDetectionService {

    private final WebClient aiServiceWebClient;

    public Mono<List<SimilarModulesDTO.SimilarModule>> checkModuleOverlap(CompletionServiceRequestDTO request) {
        return aiServiceWebClient.post()
                .uri("/overlap-detection/check-similarity")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SimilarModulesDTO.class)
                .map(SimilarModulesDTO::getSimilarModules);
    }
}
