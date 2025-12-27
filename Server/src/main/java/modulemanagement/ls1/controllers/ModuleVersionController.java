package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateResponseDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.dtos.CompletionServiceResponseDTO;
import modulemanagement.ls1.dtos.CompletionServiceRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewFeedbackDTO;
import modulemanagement.ls1.dtos.SimilarModuleDTO;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.LLMGenerationService;
import modulemanagement.ls1.services.PromptBuilderService;
import modulemanagement.ls1.services.ModuleVersionService;
import jakarta.validation.Valid;
import modulemanagement.ls1.shared.CurrentUser;
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
    private final PromptBuilderService promptBuilderService;
    private final LLMGenerationService llmGenerationService;

    public ModuleVersionController(ModuleVersionService moduleVersionService,
            PromptBuilderService promptBuilderService,
            LLMGenerationService llmGenerationService) {
        this.moduleVersionService = moduleVersionService;
        this.promptBuilderService = promptBuilderService;
        this.llmGenerationService = llmGenerationService;
    }

    @GetMapping("/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> getModuleVersionUpdateDtoFromId(
            @CurrentUser User user, @PathVariable Long moduleVersionId) {
        ModuleVersionUpdateResponseDTO dto = moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId,
                user.getUserId());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> updateModuleVersion(@CurrentUser User user,
            @PathVariable Long moduleVersionId, @Valid @RequestBody ModuleVersionUpdateRequestDTO moduleVersion) {
        ModuleVersionUpdateResponseDTO updatedModuleVersion = moduleVersionService
                .updateModuleVersionFromRequest(user.getUserId(), moduleVersionId, moduleVersion);
        return ResponseEntity.ok(updatedModuleVersion);
    }

    @GetMapping("/view/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ModuleVersionViewDTO> getModuleVersionViewDto(@CurrentUser User user,
            @PathVariable Long moduleVersionId) {
        ModuleVersionViewDTO dto = moduleVersionService.getModuleVersionViewDto(moduleVersionId, user.getUserId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/previous-module-version-feedback")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<List<ModuleVersionViewFeedbackDTO>> getPreviousModuleVersionFeedback(
            @CurrentUser User user, @PathVariable Long id) {
        List<ModuleVersionViewFeedbackDTO> lastRejectionReasons = moduleVersionService
                .getPreviousModuleVersionFeedback(user.getUserId(), id);
        return ResponseEntity.ok(lastRejectionReasons);
    }

    @PostMapping("/generate/content")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<CompletionServiceResponseDTO> generateContent(
            @Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        log.info("generateContent invoked with {}", moduleInfoRequestDTO);
        String prompt = promptBuilderService.buildPrompt("content", moduleInfoRequestDTO);
        String response = llmGenerationService.generate(prompt, "content");
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/examination-achievements")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<CompletionServiceResponseDTO> generateExaminationAchievements(
            @Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        log.info("generateExaminationAchievements invoked with {} ", moduleInfoRequestDTO);
        String prompt = promptBuilderService.buildPrompt("examination-achievements", moduleInfoRequestDTO);
        String response = llmGenerationService.generate(prompt, "examination-achievements");
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/learning-outcomes")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<CompletionServiceResponseDTO> generateLearningOutcomes(
            @Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        log.info("generateLearningOutcomes invoked with {}", moduleInfoRequestDTO);
        String prompt = promptBuilderService.buildPrompt("learning-outcomes", moduleInfoRequestDTO);
        String response = llmGenerationService.generate(prompt, "learning-outcomes");
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/teaching-methods")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<CompletionServiceResponseDTO> generateTeachingMethods(
            @Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        log.info("generateTeachingMethods invoked with {}", moduleInfoRequestDTO);
        String prompt = promptBuilderService.buildPrompt("teaching-methods", moduleInfoRequestDTO);
        String response = llmGenerationService.generate(prompt, "teaching-methods");
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/overlap-detection/check-similarity/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'QUALITY_MANAGEMENT', 'ACADEMIC_PROGRAM_ADVISOR', 'EXAMINATION_BOARD')")
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
