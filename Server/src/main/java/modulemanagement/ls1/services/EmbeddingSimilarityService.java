package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingSimilarityService {

    private final EmbeddingModel embeddingModel;

    public DMatrixRMaj generateEmbedding(String text) {
        EmbeddingResponse embeddingResponse = embeddingModel.embedForResponse(List.of(text));
        float[] output = embeddingResponse.getResult().getOutput();

        double[] data = new double[output.length];
        for (int i = 0; i < output.length; i++) {
            data[i] = output[i];
        }
        return new DMatrixRMaj(1, output.length, true, data);

    }

    public DMatrixRMaj generateEmbeddings(List<String> texts) {
        List<float[]> allEmbeddings = new ArrayList<>();

        EmbeddingResponse response = embeddingModel.embedForResponse(texts);

        response.getResults().forEach(e -> allEmbeddings.add(e.getOutput()));

        if (allEmbeddings.isEmpty()) {
            return new DMatrixRMaj(0, 0);
        }

        double[] data = new double[allEmbeddings.size() * allEmbeddings.get(0).length];
        int idx = 0;
        for (float[] embedding : allEmbeddings) {
            for (float value : embedding) {
                data[idx++] = value;
            }
        }
        return new DMatrixRMaj(allEmbeddings.size(), allEmbeddings.get(0).length, true, data);
    }

    public double[] calculateCosineSimilarities(DMatrixRMaj queryVector, DMatrixRMaj storedEmbeddings) {
        // Normalize query vector
        double queryNorm = NormOps_DDRM.normF(queryVector);
        DMatrixRMaj normalizedQuery = new DMatrixRMaj(queryVector);
        if (queryNorm > 0) {
            CommonOps_DDRM.scale(1.0 / queryNorm, normalizedQuery);
        }

        // Normalize stored embeddings (each row) - work directly on matrix data
        DMatrixRMaj normalizedStored = new DMatrixRMaj(storedEmbeddings);
        int cols = storedEmbeddings.numCols;
        for (int i = 0; i < storedEmbeddings.numRows; i++) {
            // Compute row norm directly from matrix data
            double rowNorm = 0.0;
            int rowStart = i * cols;
            for (int j = 0; j < cols; j++) {
                double val = normalizedStored.data[rowStart + j];
                rowNorm += val * val;
            }
            rowNorm = Math.sqrt(rowNorm);

            // Scale row in-place
            if (rowNorm > 0) {
                double invNorm = 1.0 / rowNorm;
                for (int j = 0; j < cols; j++) {
                    normalizedStored.data[rowStart + j] *= invNorm;
                }
            }
        }

        // Compute cosine similarity: normalizedQuery * normalizedStored^T
        DMatrixRMaj similarities = new DMatrixRMaj(1, storedEmbeddings.numRows);
        CommonOps_DDRM.multTransB(normalizedQuery, normalizedStored, similarities);

        return similarities.data;
    }
}
