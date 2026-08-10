package modulemanagement.ls1.controllers;

import jakarta.validation.Valid;
import modulemanagement.ls1.dtos.AiReviewGuidelineDTO;
import modulemanagement.ls1.dtos.CreateAiReviewGuidelineDTO;
import modulemanagement.ls1.dtos.UpdateAiReviewGuidelineDTO;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.AiReviewGuidelineService;
import modulemanagement.ls1.shared.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-review-guidelines")
@PreAuthorize("hasRole('AI_REVIEW_GUIDELINE_MANAGER')")
public class AiReviewGuidelineController {

    private final AiReviewGuidelineService aiReviewGuidelineService;

    public AiReviewGuidelineController(AiReviewGuidelineService aiReviewGuidelineService) {
        this.aiReviewGuidelineService = aiReviewGuidelineService;
    }

    @GetMapping
    public ResponseEntity<List<AiReviewGuidelineDTO>> getAllGuidelines() {
        return ResponseEntity.ok(aiReviewGuidelineService.getAllGuidelines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiReviewGuidelineDTO> getGuideline(@PathVariable Long id) {
        return ResponseEntity.ok(aiReviewGuidelineService.getGuideline(id));
    }

    @PostMapping
    public ResponseEntity<AiReviewGuidelineDTO> createGuideline(
            @CurrentUser User user,
            @Valid @RequestBody CreateAiReviewGuidelineDTO dto) {
        return ResponseEntity.ok(aiReviewGuidelineService.createGuideline(user, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AiReviewGuidelineDTO> updateGuideline(
            @CurrentUser User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAiReviewGuidelineDTO dto) {
        return ResponseEntity.ok(aiReviewGuidelineService.updateGuideline(id, user, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuideline(@PathVariable Long id) {
        aiReviewGuidelineService.deleteGuideline(id);
        return ResponseEntity.noContent().build();
    }
}
