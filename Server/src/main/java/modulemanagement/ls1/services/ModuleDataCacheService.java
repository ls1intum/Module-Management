package modulemanagement.ls1.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import org.ejml.data.DMatrixRMaj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModuleDataCacheService {

    static final int CACHE_FORMAT_VERSION = 2;

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${overlap-detection.module-info-dir:classpath:data/module-info/modules.json}")
    private String moduleInfoFile;

    @Value("${overlap-detection.cache-dir:data/cache}")
    private String cacheDir;

    public List<Map<String, Object>> loadModulesFromJson() throws IOException {
        List<Map<String, Object>> allModules = new ArrayList<>();

        try {
            Resource resource = resourceLoader.getResource(moduleInfoFile);

            if (resource.exists() && resource.isReadable()) {
                List<Map<String, Object>> fileModules = objectMapper.readValue(
                        resource.getInputStream(),
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                allModules.addAll(fileModules);
                log.info("Loaded {} modules from {}", fileModules.size(), resource.getFilename());
                return allModules;

            } else {
                log.warn("Resource does not exist or is not readable: {}", moduleInfoFile);
            }

        } catch (Exception e) {
            log.error("Error loading modules from file: {}", moduleInfoFile, e);
        }

        return allModules;
    }

    public String calculateDataHash(List<Map<String, Object>> modules) {
        try {
            List<Map<String, Object>> sorted = new ArrayList<>(modules);
            sorted.sort(Comparator.comparing(m -> m.getOrDefault("module_id", "").toString()));

            String json = objectMapper.writeValueAsString(sorted);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(json.getBytes());
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("Error calculating data hash", e);
            return UUID.randomUUID().toString();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Loads a cache only when catalog hash, model id, and ordered module ids all match.
     * Returns {@code null} when the cache is missing or incompatible.
     */
    public EmbeddingCachePayload loadCache(
            String dataHash,
            List<String> expectedModuleIds,
            String expectedModelId) {
        try {
            Path cachePath = resolveCachePath();
            log.info("Loading cache from: {}", cachePath);

            if (!Files.exists(cachePath)) {
                log.warn("Cache directory does not exist: {}, creating it", cachePath);
                Files.createDirectories(cachePath);
                return null;
            }

            Path embeddingsFile = cachePath.resolve("embeddings.json");
            Path metadataFile = cachePath.resolve("metadata.json");

            if (!Files.exists(embeddingsFile) || !Files.exists(metadataFile)) {
                log.warn("Cache files not found. Embeddings exists: {}, Metadata exists: {}",
                        Files.exists(embeddingsFile), Files.exists(metadataFile));
                return null;
            }

            Map<String, Object> metadata = objectMapper.readValue(
                    metadataFile.toFile(),
                    new TypeReference<Map<String, Object>>() {
                    });

            Integer formatVersion = asInteger(metadata.get("cache_format_version"));
            if (formatVersion == null || formatVersion != CACHE_FORMAT_VERSION) {
                log.info("Cache invalidated: unsupported or missing format version ({})", formatVersion);
                return null;
            }

            String cachedHash = Objects.toString(metadata.get("data_hash"), null);
            if (!dataHash.equals(cachedHash)) {
                log.info("Cache invalidated: data hash changed");
                return null;
            }

            String cachedModelId = Objects.toString(metadata.get("model_id"), null);
            if (expectedModelId == null || !expectedModelId.equals(cachedModelId)) {
                log.info("Cache invalidated: embedding model changed (cached={}, expected={})",
                        cachedModelId, expectedModelId);
                return null;
            }

            List<String> cachedModuleIds = asStringList(metadata.get("module_ids"));
            if (cachedModuleIds == null || !cachedModuleIds.equals(expectedModuleIds)) {
                log.info("Cache invalidated: module id list does not match current catalog order");
                return null;
            }

            log.info("Loading embeddings from cache file");
            double[][] embeddingsArray = objectMapper.readValue(
                    embeddingsFile.toFile(),
                    double[][].class);

            if (embeddingsArray == null || embeddingsArray.length == 0) {
                log.warn("Cache file is empty");
                return null;
            }

            DMatrixRMaj matrix = new DMatrixRMaj(embeddingsArray);

            if (matrix.numRows != expectedModuleIds.size()) {
                log.warn("Cache invalidated: matrix rows ({}) != module ids ({})",
                        matrix.numRows, expectedModuleIds.size());
                return null;
            }

            Integer cachedDimension = asInteger(metadata.get("embedding_dimension"));
            if (cachedDimension != null && cachedDimension != matrix.numCols) {
                log.warn("Cache invalidated: metadata dimension ({}) != matrix columns ({})",
                        cachedDimension, matrix.numCols);
                return null;
            }

            log.info("Successfully loaded {} x {} embeddings from cache",
                    matrix.numRows, matrix.numCols);
            return new EmbeddingCachePayload(
                    matrix,
                    List.copyOf(cachedModuleIds),
                    cachedModelId,
                    matrix.numCols);
        } catch (Exception e) {
            log.error("Error loading cache", e);
            return null;
        }
    }

    public void saveCache(
            String dataHash,
            List<String> moduleIds,
            String modelId,
            DMatrixRMaj embeddings) {
        try {
            if (embeddings == null || embeddings.numRows == 0) {
                log.warn("No embeddings to save");
                return;
            }
            if (moduleIds == null || moduleIds.size() != embeddings.numRows) {
                throw new IllegalArgumentException(String.format(
                        "Cannot save cache: module id count (%s) != embedding rows (%d)",
                        moduleIds == null ? "null" : moduleIds.size(),
                        embeddings.numRows));
            }
            if (modelId == null || modelId.isBlank()) {
                throw new IllegalArgumentException("Cannot save cache without an embedding model id");
            }

            Path cachePath = resolveCachePath();
            Files.createDirectories(cachePath);

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("cache_format_version", CACHE_FORMAT_VERSION);
            metadata.put("data_hash", dataHash);
            metadata.put("model_id", modelId);
            metadata.put("embedding_dimension", embeddings.numCols);
            metadata.put("num_modules", moduleIds.size());
            metadata.put("module_ids", moduleIds);
            metadata.put("timestamp", new Date().toString());

            Path metadataFile = cachePath.resolve("metadata.json");
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(metadataFile.toFile(), metadata);

            double[][] embeddingsArray = embeddings.get2DData();
            Path embeddingsFile = cachePath.resolve("embeddings.json");
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(embeddingsFile.toFile(), embeddingsArray);

            log.info("Saved {} x {} embeddings to cache (model={})",
                    embeddings.numRows, embeddings.numCols, modelId);
        } catch (Exception e) {
            log.error("Error saving cache", e);
            throw new IllegalStateException("Failed to save embedding cache", e);
        }
    }

    private Path resolveCachePath() {
        Path cachePath = Paths.get(cacheDir);
        if (!cachePath.isAbsolute()) {
            cachePath = cachePath.toAbsolutePath();
        }
        return cachePath;
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return null;
        }
        List<String> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item == null) {
                return null;
            }
            result.add(item.toString());
        }
        return result;
    }
}
