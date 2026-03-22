package modulemanagement.ls1.controllers;

import jakarta.validation.Valid;
import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.services.ExaminationBoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/examination-boards")
@PreAuthorize("hasRole('ADMIN')")
public class ExaminationBoardController {

    private final ExaminationBoardService examinationBoardService;

    public ExaminationBoardController(ExaminationBoardService examinationBoardService) {
        this.examinationBoardService = examinationBoardService;
    }

    @GetMapping
    public ResponseEntity<List<ExaminationBoardDTO>> getAllExaminationBoards() {
        return ResponseEntity.ok(examinationBoardService.getAllExaminationBoards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExaminationBoardDTO> getExaminationBoard(@PathVariable Long id) {
        return ResponseEntity.ok(examinationBoardService.getExaminationBoard(id));
    }

    @PostMapping
    public ResponseEntity<ExaminationBoardDTO> createExaminationBoard(
            @Valid @RequestBody CreateExaminationBoardDTO dto) {
        return ResponseEntity.ok(examinationBoardService.createExaminationBoard(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExaminationBoardDTO> updateExaminationBoard(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExaminationBoardDTO dto) {
        return ResponseEntity.ok(examinationBoardService.updateExaminationBoard(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExaminationBoard(@PathVariable Long id) {
        examinationBoardService.deleteExaminationBoard(id);
        return ResponseEntity.noContent().build();
    }
}
