package modulemanagement.ls1.controllers;

import jakarta.validation.Valid;
import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.services.DegreeProgramSpecializationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/degree-program-specializations")
@PreAuthorize("hasRole('ADMIN')")
public class DegreeProgramSpecializationsController {

    private final DegreeProgramSpecializationService degreeProgramSpecializationService;

    public DegreeProgramSpecializationsController(DegreeProgramSpecializationService degreeProgramSpecializationService) {
        this.degreeProgramSpecializationService = degreeProgramSpecializationService;
    }

    @GetMapping
    public ResponseEntity<List<DegreeProgramSpecializationDTO>> getAllDegreeProgramSpecializations() {
        return ResponseEntity.ok(degreeProgramSpecializationService.getAllDegreeProgramSpecializations());
    }

    @PostMapping
    public ResponseEntity<DegreeProgramSpecializationDTO> createDegreeProgramSpecialization(@Valid @RequestBody CreateDegreeProgramSpecializationDTO dto) {
        return ResponseEntity.ok(degreeProgramSpecializationService.createDegreeProgramSpecialization(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DegreeProgramSpecializationDTO> updateDegreeProgramSpecialization(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDegreeProgramSpecializationDTO dto) {
        return ResponseEntity.ok(degreeProgramSpecializationService.updateDegreeProgramSpecialization(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDegreeProgramSpecialization(@PathVariable Long id) {
        degreeProgramSpecializationService.deleteDegreeProgramSpecialization(id);
        return ResponseEntity.noContent().build();
    }
}
