package modulemanagement.ls1.modulemanagementserver.controllers;

import modulemanagement.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.services.ModuleVersionService;
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
    public ResponseEntity<ModuleVersionUpdateRequestDTO> getModuleVersionUpdateDtoFromId(@PathVariable Long moduleVersionId, @Valid @RequestParam Long userId) {
        ModuleVersionUpdateRequestDTO dto = moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId, userId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{moduleVersionId}")
    public ResponseEntity<ModuleVersionUpdateRequestDTO> updateModuleVersion(@PathVariable Long moduleVersionId, @Valid @RequestBody ModuleVersionUpdateRequestDTO moduleVersion) {
        ModuleVersionUpdateRequestDTO updatedModuleVersion = moduleVersionService.updateModuleVersionFromRequest(moduleVersionId, moduleVersion);
        return ResponseEntity.ok(updatedModuleVersion);
    }
}
