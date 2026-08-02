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

    /**
     * Embeds every text in order. Any batch failure aborts the whole call so
     * callers never receive a matrix whose rows no longer align with the input.
     */
    public DMatrixRMaj generateEmbeddings(List<String> texts) {
        List<float[]> allEmbeddings = new ArrayList<>(texts.size());

        for (int i = 0; i < texts.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(i, end);

            final EmbeddingResponse response;
            try {
                response = embeddingModel.embedForResponse(batch);
            } catch (RuntimeException e) {
                throw new IllegalStateException(String.format(
                        "Failed to generate embeddings for batch %d-%d; aborting cache build",
                        i + 1, end), e);
            }

            List<float[]> batchEmbeddings = response.getResults().stream()
                    .map(e -> e.getOutput())
                    .toList();

            if (batchEmbeddings.size() != batch.size()) {
                throw new IllegalStateException(String.format(
                        "Embedding batch %d-%d returned %d vectors for %d texts",
                        i + 1, end, batchEmbeddings.size(), batch.size()));
            }

            allEmbeddings.addAll(batchEmbeddings);
            log.info("Generated embeddings for batch {}-{}", i + 1, end);
        }

        if (allEmbeddings.size() != texts.size()) {
            throw new IllegalStateException(String.format(
                    "Expected %d embeddings but produced %d", texts.size(), allEmbeddings.size()));
        }

        return MatrixUtil.floatArraysToMatrix(allEmbeddings);
    }
}
