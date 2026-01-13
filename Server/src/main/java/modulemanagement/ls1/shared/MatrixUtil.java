package modulemanagement.ls1.shared;

import java.util.List;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.NormOps_DDRM;

public class MatrixUtil {

    public static DMatrixRMaj floatArrayToMatrix(float[] floatArray) {
        DMatrixRMaj matrix = new DMatrixRMaj(1, floatArray.length);
        for (int i = 0; i < floatArray.length; i++) {
            matrix.data[i] = floatArray[i];
        }
        return matrix;
    }

    public static DMatrixRMaj floatArraysToMatrix(List<float[]> floatArrays) {
        if (floatArrays.isEmpty()) {
            return new DMatrixRMaj(0, 0);
        }

        int numRows = floatArrays.size();
        int numCols = floatArrays.get(0).length;

        DMatrixRMaj matrix = new DMatrixRMaj(numRows, numCols);
        int idx = 0;
        for (float[] embedding : floatArrays) {
            if (embedding.length != numCols) {
                throw new IllegalArgumentException(
                        String.format("Inconsistent array lengths: expected %d, got %d", numCols, embedding.length));
            }
            for (float value : embedding) {
                matrix.data[idx++] = value;
            }
        }
        return matrix;
    }

    public static void normalizeRows(DMatrixRMaj matrix) {
        int numRows = matrix.numRows;
        int numCols = matrix.numCols;

        double[] data = matrix.data;

        for (int i = 0; i < numRows; i++) {
            double norm = 0.0;
            int rowStart = i * numCols;

            // Compute row norm
            for (int j = 0; j < numCols; j++) {
                double val = data[rowStart + j];
                norm += val * val;
            }
            norm = Math.sqrt(norm);

            // Scale row if norm > 0
            if (norm > 0.0) {
                double scale = 1.0 / norm;
                for (int j = 0; j < numCols; j++) {
                    data[rowStart + j] *= scale;
                }
            }
        }
    }

    public static double[] calculateCosineSimilarities(DMatrixRMaj queryVector, DMatrixRMaj normalizedVectors) {

        if (queryVector.numCols != normalizedVectors.numCols) {
            throw new IllegalStateException(
                    String.format("Embedding dimension mismatch: query=%d, stored=%d",
                            queryVector.numCols, normalizedVectors.numCols));
        }

        // Normalize query vector in-place
        double vectorNorm = NormOps_DDRM.normF(queryVector);
        if (vectorNorm > 0) {
            CommonOps_DDRM.scale(1.0 / vectorNorm, queryVector);
        }

        // Compute cosine similarity using optimized matrix multiplication
        DMatrixRMaj similarities = new DMatrixRMaj(1, normalizedVectors.numRows);
        CommonOps_DDRM.multTransB(queryVector, normalizedVectors, similarities);

        return similarities.data;
    }
}
