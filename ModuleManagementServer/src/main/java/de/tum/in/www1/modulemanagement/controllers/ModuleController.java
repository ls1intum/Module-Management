package de.tum.in.www1.modulemanagement.controllers;

import de.tum.in.www1.modulemanagement.models.CitModule;
import de.tum.in.www1.modulemanagement.services.ModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @PostMapping("/create")
    public ResponseEntity<CitModule> createModule(@RequestBody CitModule module) {
        CitModule createdModule = moduleService.createModule(module);
        return ResponseEntity.ok(createdModule);
    }

    @GetMapping
    public ResponseEntity<List<CitModule>> getAllModules() {
        List<CitModule> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<CitModule> getModuleById(@PathVariable Long id) {
        CitModule fetchedModule = moduleService.findById(id);
        return ResponseEntity.ok(fetchedModule);
    }

    @GetMapping("/moduleId/{id}")
    public ResponseEntity<CitModule> getModuleByModuleId(@PathVariable String id) {
        CitModule fetchedModule = moduleService.findByModuleId(id);
        return ResponseEntity.ok(fetchedModule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitModule> updateModule(@PathVariable Long id, @RequestBody CitModule module) {
        CitModule updatedModule = moduleService.updateModule(id, module);
        return ResponseEntity.ok(updatedModule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}

