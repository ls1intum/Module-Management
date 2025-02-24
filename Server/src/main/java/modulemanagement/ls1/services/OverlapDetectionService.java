package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import modulemanagement.ls1.dtos.OverlapDetection.OverlapDetectionRequestDTO;
import modulemanagement.ls1.dtos.OverlapDetection.SimilarModuleDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OverlapDetectionService {

    private final WebClient aiServiceWebClient;

    public Mono<List<SimilarModuleDTO>> checkModuleOverlap(OverlapDetectionRequestDTO request) {
        return aiServiceWebClient.post()
                .uri("/overlap-detection/check-similarity")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() { });
    }
}
