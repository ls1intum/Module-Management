package modulemanagement.ls1.services;

import org.ejml.data.DMatrixRMaj;

import java.util.List;

/**
 * On-disk catalog embedding cache: matrix rows are aligned with {@code moduleIds}.
 */
public record EmbeddingCachePayload(
        DMatrixRMaj embeddings,
        List<String> moduleIds,
        String modelId,
        int dimension
) {
}
