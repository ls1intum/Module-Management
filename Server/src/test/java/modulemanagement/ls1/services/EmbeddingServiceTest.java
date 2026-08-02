package modulemanagement.ls1.services;

import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Test
    void generateEmbeddingsFailsClosedWhenABatchThrows() {
        EmbeddingService service = new EmbeddingService(embeddingModel);

        float[][] firstBatch = java.util.stream.IntStream.range(0, 32)
                .mapToObj(i -> vector(i, 1f))
                .toArray(float[][]::new);

        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(responseOf(firstBatch))
                .thenThrow(new RuntimeException("provider unavailable"));

        List<String> texts = java.util.stream.IntStream.range(0, 33)
                .mapToObj(i -> "text-" + i)
                .toList();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.generateEmbeddings(texts));

        assertTrue(ex.getMessage().contains("aborting cache build"));
        assertNotNull(ex.getCause());
        verify(embeddingModel, times(2)).embedForResponse(anyList());
    }

    @Test
    void generateEmbeddingsRequiresOneVectorPerInput() {
        EmbeddingService service = new EmbeddingService(embeddingModel);
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(responseOf(vector(0.1f, 0.2f))); // one vector for two texts

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.generateEmbeddings(List.of("a", "b")));

        assertTrue(ex.getMessage().contains("returned 1 vectors for 2 texts"));
    }

    @Test
    void generateEmbeddingsPreservesInputOrder() {
        EmbeddingService service = new EmbeddingService(embeddingModel);
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(responseOf(vector(1f, 0f), vector(0f, 1f)));

        DMatrixRMaj matrix = service.generateEmbeddings(List.of("first", "second"));

        assertEquals(2, matrix.numRows);
        assertEquals(2, matrix.numCols);
        assertEquals(1.0, matrix.get(0, 0), 1e-9);
        assertEquals(0.0, matrix.get(0, 1), 1e-9);
        assertEquals(0.0, matrix.get(1, 0), 1e-9);
        assertEquals(1.0, matrix.get(1, 1), 1e-9);
    }

    private static float[] vector(float... values) {
        return values;
    }

    private static EmbeddingResponse responseOf(float[]... vectors) {
        List<Embedding> embeddings = java.util.stream.IntStream.range(0, vectors.length)
                .mapToObj(i -> new Embedding(vectors[i], i))
                .toList();
        return new EmbeddingResponse(embeddings);
    }
}
