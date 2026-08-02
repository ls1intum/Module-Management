package modulemanagement.ls1.services;

import org.ejml.data.DMatrixRMaj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModuleDataCacheServiceTest {

    @TempDir
    Path tempDir;

    private ModuleDataCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new ModuleDataCacheService(new DefaultResourceLoader());
        ReflectionTestUtils.setField(cacheService, "cacheDir", tempDir.toString());
    }

    @Test
    void saveAndLoadRoundTripRequiresMatchingModelAndModuleIds() {
        List<String> moduleIds = List.of("A", "B");
        DMatrixRMaj matrix = new DMatrixRMaj(new double[][] {
                {1.0, 0.0},
                {0.0, 1.0}
        });

        cacheService.saveCache("hash-1", moduleIds, "model-x", matrix);

        EmbeddingCachePayload loaded = cacheService.loadCache("hash-1", moduleIds, "model-x");
        assertNotNull(loaded);
        assertEquals(2, loaded.embeddings().numRows);
        assertEquals(2, loaded.embeddings().numCols);
        assertEquals(moduleIds, loaded.moduleIds());
        assertEquals("model-x", loaded.modelId());
        assertEquals(1.0, loaded.embeddings().get(0, 0), 1e-9);
        assertEquals(1.0, loaded.embeddings().get(1, 1), 1e-9);
    }

    @Test
    void loadCacheInvalidatesOnModelChange() {
        List<String> moduleIds = List.of("A");
        DMatrixRMaj matrix = new DMatrixRMaj(new double[][] {{0.5, 0.5}});
        cacheService.saveCache("hash-1", moduleIds, "model-old", matrix);

        assertNull(cacheService.loadCache("hash-1", moduleIds, "model-new"));
    }

    @Test
    void loadCacheInvalidatesOnModuleIdOrderChange() {
        List<String> moduleIds = List.of("A", "B");
        DMatrixRMaj matrix = new DMatrixRMaj(new double[][] {
                {1.0, 0.0},
                {0.0, 1.0}
        });
        cacheService.saveCache("hash-1", moduleIds, "model-x", matrix);

        assertNull(cacheService.loadCache("hash-1", List.of("B", "A"), "model-x"));
    }

    @Test
    void loadCacheInvalidatesOnDataHashChange() {
        List<String> moduleIds = List.of("A");
        DMatrixRMaj matrix = new DMatrixRMaj(new double[][] {{1.0, 0.0}});
        cacheService.saveCache("hash-1", moduleIds, "model-x", matrix);

        assertNull(cacheService.loadCache("hash-2", moduleIds, "model-x"));
    }

    @Test
    void saveCacheRejectsRowCountMismatch() {
        DMatrixRMaj matrix = new DMatrixRMaj(new double[][] {{1.0, 0.0}});
        assertThrows(IllegalStateException.class,
                () -> cacheService.saveCache("hash-1", List.of("A", "B"), "model-x", matrix));
    }

    @Test
    void calculateDataHashIsOrderIndependentForSameModules() {
        List<Map<String, Object>> aThenB = List.of(
                Map.of("module_id", "A", "title", "one"),
                Map.of("module_id", "B", "title", "two"));
        List<Map<String, Object>> bThenA = List.of(
                Map.of("module_id", "B", "title", "two"),
                Map.of("module_id", "A", "title", "one"));

        assertEquals(cacheService.calculateDataHash(aThenB), cacheService.calculateDataHash(bThenA));
    }
}
