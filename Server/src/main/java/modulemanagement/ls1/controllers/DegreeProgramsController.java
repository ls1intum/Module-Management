package modulemanagement.ls1.controllers;

import jakarta.validation.Valid;
import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.services.DegreeProgramService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/degree-programs")
@PreAuthorize("hasRole('ADMIN')")
public class DegreeProgramsController {

    private final DegreeProgramService degreeProgramService;

    public DegreeProgramsController(DegreeProgramService degreeProgramService) {
        this.degreeProgramService = degreeProgramService;
    }

    @GetMapping
    public ResponseEntity<List<DegreeProgramDTO>> getAllDegreePrograms() {
        return ResponseEntity.ok(degreeProgramService.getAllDegreePrograms());
    }

    @GetMapping("/with-specializations")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResponseEntity<List<DegreeProgramDTO>> getDegreeProgramsWithSpecializations() {
        return ResponseEntity.ok(degreeProgramService.getAllDegreeProgramsWithSpecializations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DegreeProgramDTO> getDegreeProgram(@PathVariable Long id) {
        return ResponseEntity.ok(degreeProgramService.getDegreeProgram(id));
    }

    @PostMapping
    public ResponseEntity<DegreeProgramDTO> createDegreeProgram(@Valid @RequestBody CreateDegreeProgramDTO dto) {
        return ResponseEntity.ok(degreeProgramService.createDegreeProgram(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DegreeProgramDTO> updateDegreeProgram(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDegreeProgramDTO dto) {
        return ResponseEntity.ok(degreeProgramService.updateDegreeProgram(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDegreeProgram(@PathVariable Long id) {
        degreeProgramService.deleteDegreeProgram(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{degreeProgramId}/degree-program-specializations/batch")
    public ResponseEntity<DegreeProgramDTO> addSpecializationsToDegreeProgram(
            @PathVariable Long degreeProgramId,
            @Valid @RequestBody AddSpecializationsToDegreeProgramDTO dto) {
        return ResponseEntity.ok(degreeProgramService.addSpecializationsToDegreeProgram(degreeProgramId, dto.getDegreeProgramSpecializationIds()));
    }

    @DeleteMapping("/{degreeProgramId}/degree-program-specializations/{degreeProgramSpecializationId}")
    public ResponseEntity<DegreeProgramDTO> removeSpecializationFromDegreeProgram(
            @PathVariable Long degreeProgramId,
            @PathVariable Long degreeProgramSpecializationId) {
        return ResponseEntity.ok(degreeProgramService.removeSpecializationFromDegreeProgram(degreeProgramId, degreeProgramSpecializationId));
    }
}
