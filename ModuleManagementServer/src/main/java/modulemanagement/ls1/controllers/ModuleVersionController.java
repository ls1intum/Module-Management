package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateResponseDTO;
import modulemanagement.ls1.services.ModuleVersionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/module-versions")
public class ModuleVersionController {
    private final ModuleVersionService moduleVersionService;

    public ModuleVersionController(ModuleVersionService moduleVersionService) {
        this.moduleVersionService = moduleVersionService;
    }

    @GetMapping("/{moduleVersionId}")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> getModuleVersionUpdateDtoFromId(@PathVariable Long moduleVersionId, @Valid @RequestParam Long userId) {
        ModuleVersionUpdateResponseDTO dto = moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId, userId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{moduleVersionId}")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> updateModuleVersion(@PathVariable Long moduleVersionId, @Valid @RequestBody ModuleVersionUpdateRequestDTO moduleVersion) {
        ModuleVersionUpdateResponseDTO updatedModuleVersion = moduleVersionService.updateModuleVersionFromRequest(moduleVersionId, moduleVersion);
        return ResponseEntity.ok(updatedModuleVersion);
    }
}
