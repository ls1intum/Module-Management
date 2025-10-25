package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateResponseDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.dtos.CompletionServiceResponseDTO;
import modulemanagement.ls1.dtos.CompletionServiceRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewFeedbackDTO;
import modulemanagement.ls1.dtos.SimilarModuleDTO;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.AiCompletionService;
import modulemanagement.ls1.services.AuthenticationService;
import modulemanagement.ls1.services.ModuleVersionService;
import jakarta.validation.Valid;
import modulemanagement.ls1.shared.TimeLogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/module-versions")
public class ModuleVersionController {

    private static final Logger log = LoggerFactory.getLogger(ModuleVersionController.class);

    private final ModuleVersionService moduleVersionService;
    private final AuthenticationService authenticationService;
    private final AiCompletionService aiCompletionService;

    public ModuleVersionController(ModuleVersionService moduleVersionService, AuthenticationService authenticationService, AiCompletionService aiCompletionService) {
        this.moduleVersionService = moduleVersionService;
        this.authenticationService = authenticationService;
        this.aiCompletionService = aiCompletionService;
    }

    @GetMapping("/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> getModuleVersionUpdateDtoFromId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ModuleVersionUpdateResponseDTO dto = moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId, user.getUserId());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> updateModuleVersion(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId, @Valid @RequestBody ModuleVersionUpdateRequestDTO moduleVersion) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ModuleVersionUpdateResponseDTO updatedModuleVersion = moduleVersionService.updateModuleVersionFromRequest(user.getUserId(), moduleVersionId, moduleVersion);
        return ResponseEntity.ok(updatedModuleVersion);
    }

    @GetMapping("/view/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<ModuleVersionViewDTO> getModuleVersionViewDto(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ModuleVersionViewDTO dto = moduleVersionService.getModuleVersionViewDto(moduleVersionId, user.getUserId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/previous-module-version-feedback")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<List<ModuleVersionViewFeedbackDTO>> getPreviousModuleVersionFeedback(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        List<ModuleVersionViewFeedbackDTO> lastRejectionReasons = moduleVersionService.getPreviousModuleVersionFeedback(user.getUserId(), id);
        return ResponseEntity.ok(lastRejectionReasons);
    }

    @PostMapping("/generate/content")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateContent(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        long start = System.nanoTime();
        log.info("generateContent invoked with {}", moduleInfoRequestDTO);
        var response = aiCompletionService.generateContent(moduleInfoRequestDTO).block();
        log.info("generateContent took {}", TimeLogUtil.formatDurationFrom(start));
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/examination-achievements")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateExaminationAchievements(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        long start = System.nanoTime();
        log.info("generateExaminationAchievements invoked with {} ", moduleInfoRequestDTO);
        var response = aiCompletionService.generateExaminationAchievements(moduleInfoRequestDTO).block();
        log.info("generateExaminationAchievements took {}", TimeLogUtil.formatDurationFrom(start));
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/learning-outcomes")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateLearningOutcomes(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        long start = System.nanoTime();
        log.info("generateLearningOutcomes invoked with {}", moduleInfoRequestDTO);
        var response = aiCompletionService.generateLearningOutcomes(moduleInfoRequestDTO).block();
        log.info("generateLearningOutcomes took {}", TimeLogUtil.formatDurationFrom(start));
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/generate/teaching-methods")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateTeachingMethods(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        long start = System.nanoTime();
        log.info("generateTeachingMethods invoked with {}", moduleInfoRequestDTO);
        var response = aiCompletionService.generateTeachingMethods(moduleInfoRequestDTO).block();
        log.info("generateTeachingMethods took {}", TimeLogUtil.formatDurationFrom(start));
        return ResponseEntity.ok(new CompletionServiceResponseDTO(response));
    }

    @PostMapping("/overlap-detection/check-similarity/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter', 'module-reviewer')")
    public ResponseEntity<List<SimilarModuleDTO>> checkSimilarity(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId) {
        long start = System.nanoTime();
        log.info("checkSimilarity invoked for module {}", moduleVersionId);
        User user = authenticationService.getAuthenticatedUser(jwt);
        var similarModules = this.moduleVersionService.getSimilarModules(moduleVersionId, user);
        log.info("checkSimilarity took {}", TimeLogUtil.formatDurationFrom(start));
        return ResponseEntity.ok(similarModules);
    }

    @GetMapping(value = "/{moduleVersionId}/export-professor-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<Resource> exportProfessorModuleVersionPdf(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId) {
        User user = authenticationService.getAuthenticatedUser(jwt);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=module_version_%s.pdf", moduleVersionId))
                .body(moduleVersionService.generateProfessorModuleVersionPdf(moduleVersionId, user.getUserId()));
    }
}
