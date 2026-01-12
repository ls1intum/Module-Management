package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modulemanagement.ls1.shared.MatrixUtil;
import org.ejml.data.DMatrixRMaj;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private static final int BATCH_SIZE = 32;

    private final EmbeddingModel embeddingModel;

    public DMatrixRMaj generateEmbedding(String text) {
        EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(text));
        float[] output = embeddingResponse.getResult().getOutput();
        return MatrixUtil.floatArrayToMatrix(output);
    }

    public DMatrixRMaj generateEmbeddings(List<String> texts) {
        List<float[]> allEmbeddings = new ArrayList<>();

        for (int i = 0; i < texts.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(i, end);

            try {
                EmbeddingResponse response = embeddingModel.embedForResponse(batch);
                response.getResults().forEach(e -> allEmbeddings.add(e.getOutput()));
                log.info("Generated embeddings for batch {}-{}", i + 1, end);
            } catch (Exception e) {
                log.warn("Failed to generate embeddings for batch {}-{}: {}",
                        i + 1, end, e.getMessage());
            }

        }

        return MatrixUtil.floatArraysToMatrix(allEmbeddings);
    }
}
