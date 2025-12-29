package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modulemanagement.ls1.dtos.SimilarModuleDTO;
import modulemanagement.ls1.models.ModuleVersion;

import org.ejml.data.DMatrixRMaj;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverlapDetectionService {

    private final ModuleDataService moduleDataService;
    private final EmbeddingSimilarityService embeddingSimilarityService;

    private DMatrixRMaj storedEmbeddings;
    private List<Map<String, Object>> modules;
    private String currentDataHash;

    @Value("${overlap-detection.threshold:0.6}")
    private double similarityThreshold;

    @PostConstruct
    public void initialize() {
        log.info("Initializing OverlapDetectionService");
        try {
            loadModules();
        } catch (Exception e) {
            log.error("Failed to initialize OverlapDetectionService", e);
        }
    }

    private void loadModules() throws IOException {

        // Load modules from JSON files
        List<Map<String, Object>> loadedModules = moduleDataService.loadModulesFromJson();
        this.modules = loadedModules;

        log.info("Loaded {} modules from JSON files", modules.size());

        if (modules.isEmpty()) {
            log.warn("No modules found to load");
            this.storedEmbeddings = new DMatrixRMaj(0, 0);
            return;
        }

        // Calculate data hash
        String dataHash = moduleDataService.calculateDataHash(modules);
        this.currentDataHash = dataHash;
        log.info("Calculated data hash: {}", dataHash);

        // Try to load from cache
        DMatrixRMaj cachedEmbeddings = moduleDataService.loadCache(dataHash);
        if (cachedEmbeddings != null) {
            log.info("Loaded embeddings from cache: {} x {} matrix",
                    cachedEmbeddings.numRows, cachedEmbeddings.numCols);
            this.storedEmbeddings = cachedEmbeddings;
            return;
        }

        // Generate embeddings
        log.info("Computing new embeddings for {} modules", modules.size());
        List<String> texts = modules.stream()
                .map(moduleDataService::createModuleText)
                .collect(Collectors.toList());
        log.info("Generated module texts for embedding {}", texts.size());

        this.storedEmbeddings = embeddingSimilarityService.generateEmbeddings(texts);

        log.info("Generated embeddings matrix: {} x {}",
                storedEmbeddings.numRows, storedEmbeddings.numCols);

        moduleDataService.saveCache(currentDataHash, modules.size(), storedEmbeddings);
    }

    public List<SimilarModuleDTO> checkModuleOverlap(ModuleVersion request) {
        if (modules == null || modules.isEmpty() || storedEmbeddings == null || storedEmbeddings.numRows == 0) {
            log.warn("No modules loaded for similarity check");
            return Collections.emptyList();
        }

        try {
            // Create module text and generate embedding
            String moduleText = moduleDataService.createModuleText(request);
            log.info("Generated module text (length: {}): {}", moduleText.length(),
                    moduleText.length() > 100 ? moduleText.substring(0, 100) + "..." : moduleText);

            DMatrixRMaj queryVector = embeddingSimilarityService.generateEmbedding(moduleText);
            log.info("Query embedding dimension: {}", queryVector.numCols);
            log.info("Stored embeddings matrix: {} x {}", storedEmbeddings.numRows, storedEmbeddings.numCols);

            // Check for dimension mismatch
            if (queryVector.numCols != storedEmbeddings.numCols) {
                log.error("DIMENSION MISMATCH: Query embedding dimension ({}) != Stored embeddings dimension ({})",
                        queryVector.numCols, storedEmbeddings.numCols);
                throw new IllegalStateException(
                        String.format("Embedding dimension mismatch: query=%d, stored=%d",
                                queryVector.numCols, storedEmbeddings.numCols));
            }

            // Calculate cosine similarities
            double[] similarityScores = embeddingSimilarityService.calculateCosineSimilarities(
                    queryVector, storedEmbeddings);

            log.info("Calculated {} similarity scores. Range: [{}, {}]",
                    similarityScores.length,
                    Arrays.stream(similarityScores).min().orElse(0.0),
                    Arrays.stream(similarityScores).max().orElse(0.0));

            // Filter by threshold and create results
            List<SimilarModuleDTO> results = new ArrayList<>();

            for (int i = 0; i < similarityScores.length; i++) {
                if (similarityScores[i] >= similarityThreshold) {
                    Map<String, Object> module = modules.get(i);
                    SimilarModuleDTO similarModule = SimilarModuleDTO.fromModuleMap(module, similarityScores[i]);
                    results.add(similarModule);
                }
            }

            // Sort by similarity (descending) and return top 5
            List<SimilarModuleDTO> sortedResults = results.stream()
                    .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                    .limit(5)
                    .collect(Collectors.toList());

            log.info("Found {} similar modules (threshold: {}). Top 5: {}",
                    results.size(), similarityThreshold,
                    sortedResults.stream()
                            .map(m -> String.format("%s (%.4f)", m.getTitleEng(), m.getSimilarity()))
                            .collect(Collectors.joining(", ")));

            return sortedResults;

        } catch (Exception e) {
            log.error("Error checking module overlap", e);
            throw new RuntimeException("Failed to check module overlap", e);
        }
    }

}
