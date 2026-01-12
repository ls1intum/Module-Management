package modulemanagement.ls1.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modulemanagement.ls1.dtos.SimilarModuleDTO;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.shared.MatrixUtil;

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

    private final ModuleDataCacheService moduleDataCacheService;
    private final EmbeddingService embeddingService;

    private DMatrixRMaj storedEmbeddings;
    private List<Map<String, Object>> storedModules;
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

        this.storedModules = moduleDataCacheService.loadModulesFromJson();
        log.info("Loaded {} modules from JSON files", storedModules.size());
        if (storedModules.isEmpty()) {
            this.storedEmbeddings = new DMatrixRMaj(0, 0);
            return;
        }

        String dataHash = moduleDataCacheService.calculateDataHash(storedModules);
        this.currentDataHash = dataHash;
        log.info("Calculated data hash: {}", dataHash);

        DMatrixRMaj cachedEmbeddings = moduleDataCacheService.loadCache(dataHash);
        if (cachedEmbeddings != null) {
            log.info("Loaded embeddings from cache: {} x {} matrix",
                    cachedEmbeddings.numRows, cachedEmbeddings.numCols);
            this.storedEmbeddings = cachedEmbeddings;
            return;
        }

        log.info("Computing new embeddings for stored modules");
        List<String> texts = new ArrayList<>();
        for (Map<String, Object> module : storedModules) {
            texts.add(createModuleText(module));
        }

        this.storedEmbeddings = embeddingService.generateEmbeddings(texts);
        log.info("Generated embeddings matrix: {} x {}",
                storedEmbeddings.numRows, storedEmbeddings.numCols);

        MatrixUtil.normalizeRows(this.storedEmbeddings);
        log.info("Normalized embeddings for similarity calculation");

        moduleDataCacheService.saveCache(currentDataHash, storedModules.size(), storedEmbeddings);
    }

    public List<SimilarModuleDTO> checkModuleOverlap(ModuleVersion request) {
        if (storedModules == null || storedModules.isEmpty() || storedEmbeddings == null
                || storedEmbeddings.numRows == 0) {
            log.warn("No modules loaded for similarity check");
            return new ArrayList<>();
        }

        try {
            String moduleText = createModuleText(request);
            log.info("Generated module text (length: {}): {}", moduleText);

            DMatrixRMaj queryVector = embeddingService.generateEmbedding(moduleText);
            log.info("Generated query embedding vector of dimension: {}", queryVector.numCols);

            double[] similarityScores = MatrixUtil.calculateCosineSimilarities(
                    queryVector, storedEmbeddings);

            log.info("Calculated {} similarity scores. Range: [{}, {}]",
                    similarityScores.length,
                    Arrays.stream(similarityScores).min().orElse(0.0),
                    Arrays.stream(similarityScores).max().orElse(0.0));

            // Filter by threshold and create results
            List<SimilarModuleDTO> results = new ArrayList<>();

            for (int i = 0; i < similarityScores.length; i++) {
                if (similarityScores[i] >= similarityThreshold) {
                    Map<String, Object> module = storedModules.get(i);
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

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private void addIfNotEmpty(List<String> list, String value) {
        if (value != null && !value.isEmpty()) {
            list.add(value);
        }
    }

    private String createModuleText(Map<String, Object> module) {
        List<String> fields = new ArrayList<>();

        String title = getString(module, "title");
        if (title != null && !title.isEmpty()) {
            fields.add(title + title);
        }

        addIfNotEmpty(fields, getString(module, "content"));
        addIfNotEmpty(fields, getString(module, "learning_outcomes"));
        addIfNotEmpty(fields, getString(module, "examination_achievements"));
        addIfNotEmpty(fields, getString(module, "teaching_methods"));
        addIfNotEmpty(fields, getString(module, "literature"));

        return String.join(" ", fields);
    }

    private String createModuleText(ModuleVersion module) {
        List<String> fields = new ArrayList<>();

        if (module.getTitleEng() != null && !module.getTitleEng().isEmpty()) {
            fields.add(module.getTitleEng() + module.getTitleEng());
        }

        addIfNotEmpty(fields, module.getContentEng());
        addIfNotEmpty(fields, module.getLearningOutcomesEng());
        addIfNotEmpty(fields, module.getExaminationAchievementsEng());
        addIfNotEmpty(fields, module.getTeachingMethodsEng());
        addIfNotEmpty(fields, module.getLiteratureEng());

        return String.join(" ", fields);
    }

}
