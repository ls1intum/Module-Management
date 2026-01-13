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

    public DMatrixRMaj loadCache(String dataHash) {
        try {
            Path cachePath = Paths.get(cacheDir);
            if (!cachePath.isAbsolute()) {
                cachePath = cachePath.toAbsolutePath();
            }
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

            // Read metadata
            Map<String, Object> metadata = objectMapper.readValue(
                    metadataFile.toFile(),
                    new TypeReference<Map<String, Object>>() {
                    });

            String cachedHash = (String) metadata.get("data_hash");
            if (!dataHash.equals(cachedHash)) {
                log.info("Cache invalidated: data hash changed");
                return null;
            }

            // Load embeddings from JSON file (2D double array)
            log.info("Loading embeddings from cache file");
            double[][] embeddingsArray = objectMapper.readValue(
                    embeddingsFile.toFile(),
                    double[][].class);

            if (embeddingsArray == null || embeddingsArray.length == 0) {
                log.warn("Cache file is empty");
                return null;
            }

            DMatrixRMaj matrix = new DMatrixRMaj(embeddingsArray);

            log.info("Successfully loaded {} x {} embeddings from cache",
                    matrix.numRows, matrix.numCols);
            return matrix;
        } catch (Exception e) {
            log.error("Error loading cache", e);
            return null;
        }
    }

    public void saveCache(String dataHash, int numModules, DMatrixRMaj embeddings) {
        try {
            Path cachePath = Paths.get(cacheDir);
            if (!cachePath.isAbsolute()) {
                cachePath = cachePath.toAbsolutePath();
            }
            Files.createDirectories(cachePath);

            // Save metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("data_hash", dataHash);
            metadata.put("num_modules", numModules);
            metadata.put("timestamp", new Date().toString());

            Path metadataFile = cachePath.resolve("metadata.json");
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(metadataFile.toFile(), metadata);

            if (embeddings != null && embeddings.numRows > 0) {
                double[][] embeddingsArray = embeddings.get2DData();

                Path embeddingsFile = cachePath.resolve("embeddings.json");
                objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValue(embeddingsFile.toFile(), embeddingsArray);

                log.info("Saved {} x {} embeddings to cache", embeddings.numRows, embeddings.numCols);
            } else {
                log.warn("No embeddings to save");
            }
        } catch (Exception e) {
            log.error("Error saving cache", e);
        }
    }
}
