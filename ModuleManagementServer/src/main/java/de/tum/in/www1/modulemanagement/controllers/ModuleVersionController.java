package de.tum.in.www1.modulemanagement.controllers;

import de.tum.in.www1.modulemanagement.dtos.ModuleVersionUpdateRequestDTO;
import de.tum.in.www1.modulemanagement.models.ModuleVersion;
import de.tum.in.www1.modulemanagement.services.ModuleVersionService;
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

    @PutMapping("/{moduleVersionId}")
    public ResponseEntity<ModuleVersion> updateModuleVersion(@PathVariable Long moduleVersionId, @Valid @RequestBody ModuleVersionUpdateRequestDTO moduleVersion) {
        ModuleVersion updatedModuleVersion = moduleVersionService.updateModuleVersionFromRequest(moduleVersionId, moduleVersion);
        return ResponseEntity.ok(updatedModuleVersion);
    }
}
