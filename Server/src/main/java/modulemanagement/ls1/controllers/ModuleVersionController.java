package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.dtos.CompletionServiceResponseDTO;
import modulemanagement.ls1.dtos.CompletionServiceRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewFeedbackDTO;
import modulemanagement.ls1.dtos.SimilarModuleDTO;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.LLMGenerationService;
import modulemanagement.ls1.shared.LLMPromptUtil;
import modulemanagement.ls1.services.ModuleVersionService;
import jakarta.validation.Valid;
import modulemanagement.ls1.shared.CurrentUser;
import modulemanagement.ls1.shared.ReviewerRoles;
import modulemanagement.ls1.shared.TimeLogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/module-versions")
public class ModuleVersionController {

    private static final Logger log = LoggerFactory.getLogger(ModuleVersionController.class);

    private final ModuleVersionService moduleVersionService;
    private final LLMGenerationService llmGenerationService;

    public ModuleVersionController(ModuleVersionService moduleVersionService,
            LLMGenerationService llmGenerationService) {
        this.moduleVersionService = moduleVersionService;
        this.llmGenerationService = llmGenerationService;
    }

    @PutMapping("/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ModuleVersionViewDTO> updateModuleVersion(@CurrentUser User user,
            @PathVariable Long moduleVersionId, @Valid @RequestBody ModuleVersionUpdateRequestDTO moduleVersion) {
        ModuleVersionViewDTO updatedModuleVersion = moduleVersionService
                .updateModuleVersionFromRequest(user.getUserId(), moduleVersionId, moduleVersion);
        return ResponseEntity.ok(updatedModuleVersion);
    }

    @GetMapping("/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ModuleVersionViewDTO> getModuleVersion(@CurrentUser User user,
            @PathVariable Long moduleVersionId) {
        ModuleVersionViewDTO dto = moduleVersionService.getModuleVersion(moduleVersionId, user.getUserId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/previous-module-versions-feedback")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<List<ModuleVersionViewFeedbackDTO>> getPreviousModuleVersionFeedback(
            @CurrentUser User user, @PathVariable Long id) {
        List<ModuleVersionViewFeedbackDTO> previousFeedbacks = moduleVersionService
                .getPreviousModuleVersionFeedback(user.getUserId(), id);
        return ResponseEntity.ok(previousFeedbacks);
    }

    @PostMapping("/generate/content")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<CompletionServiceResponseDTO> generateContent(
            @Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        log.info("generateContent invoked with {}", moduleInfoRequestDTO);
        String prompt = LLMPromptUtil.buildPrompt("content", moduleInfoRequestDTO);
        String response = llmGenerationService.generate(prompt, "content");
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/examination-achievements")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<CompletionServiceResponseDTO> generateExaminationAchievements(
            @Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        log.info("generateExaminationAchievements invoked with {} ", moduleInfoRequestDTO);
        String prompt = LLMPromptUtil.buildPrompt("examination-achievements", moduleInfoRequestDTO);
        String response = llmGenerationService.generate(prompt, "examination-achievements");
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/learning-outcomes")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<CompletionServiceResponseDTO> generateLearningOutcomes(
            @Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        log.info("generateLearningOutcomes invoked with {}", moduleInfoRequestDTO);
        String prompt = LLMPromptUtil.buildPrompt("learning-outcomes", moduleInfoRequestDTO);
        String response = llmGenerationService.generate(prompt, "learning-outcomes");
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/teaching-methods")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<CompletionServiceResponseDTO> generateTeachingMethods(
            @Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        log.info("generateTeachingMethods invoked with {}", moduleInfoRequestDTO);
        String prompt = LLMPromptUtil.buildPrompt("teaching-methods", moduleInfoRequestDTO);
        String response = llmGenerationService.generate(prompt, "teaching-methods");
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/overlap-detection/check-similarity/{moduleVersionId}")
    @PreAuthorize(ReviewerRoles.HAS_PROFESSOR_OR_ANY_REVIEWER_ROLE)
    public ResponseEntity<List<SimilarModuleDTO>> checkSimilarity(@CurrentUser User user,
            @PathVariable Long moduleVersionId) {
        long start = System.nanoTime();
        log.info("checkSimilarity invoked for module {}", moduleVersionId);
        var similarModules = this.moduleVersionService.getSimilarModules(moduleVersionId, user);
        log.info("checkSimilarity took {}", TimeLogUtil.formatDurationFrom(start));
        return ResponseEntity.ok(similarModules);
    }

    @GetMapping(value = "/{moduleVersionId}/export-professor-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<Resource> exportProfessorModuleVersionPdf(@CurrentUser User user,
            @PathVariable Long moduleVersionId) {

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        String.format("inline; filename=module_version_%s.pdf", moduleVersionId))
                .body(moduleVersionService.generateProfessorModuleVersionPdf(moduleVersionId, user.getUserId()));
    }
}
