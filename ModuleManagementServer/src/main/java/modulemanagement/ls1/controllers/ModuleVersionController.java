package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateResponseDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.dtos.ai.CompletionServiceResponseDTO;
import modulemanagement.ls1.dtos.ai.CompletionServiceRequestDTO;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.AiCompletionService;
import modulemanagement.ls1.services.AuthenticationService;
import modulemanagement.ls1.services.ModuleVersionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/module-versions")
public class ModuleVersionController {
    private final ModuleVersionService moduleVersionService;
    private final AuthenticationService authenticationService;
    private final AiCompletionService aiCompletionService;

    public ModuleVersionController(ModuleVersionService moduleVersionService, AuthenticationService authenticationService, AiCompletionService aiCompletionService) {
        this.moduleVersionService = moduleVersionService;
        this.authenticationService = authenticationService;
        this.aiCompletionService = aiCompletionService;
    }

    @GetMapping("/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter')")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> getModuleVersionUpdateDtoFromId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ModuleVersionUpdateResponseDTO dto = moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId, user.getUserId());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter')")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> updateModuleVersion(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId, @Valid @RequestBody ModuleVersionUpdateRequestDTO moduleVersion) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ModuleVersionUpdateResponseDTO updatedModuleVersion = moduleVersionService.updateModuleVersionFromRequest(user.getUserId(), moduleVersionId, moduleVersion);
        return ResponseEntity.ok(updatedModuleVersion);
    }

    @GetMapping("/view/{moduleVersionId}")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter')")
    public ResponseEntity<ModuleVersionViewDTO> getModuleVersionViewDto(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ModuleVersionViewDTO dto = moduleVersionService.getModuleVersionViewDto(moduleVersionId, user.getUserId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/generate/content")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateContent(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        return ResponseEntity.ok(new CompletionServiceResponseDTO(aiCompletionService.generateContent(moduleInfoRequestDTO).block()));
    }

    @PostMapping("/generate/examination-achievements")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateExaminationAchievements(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        return ResponseEntity.ok(new CompletionServiceResponseDTO(aiCompletionService.generateExaminationAchievements(moduleInfoRequestDTO).block()));
    }

    @PostMapping("/generate/learning-outcomes")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateLearningOutcomes(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        return ResponseEntity.ok(new CompletionServiceResponseDTO(aiCompletionService.generateLearningOutcomes(moduleInfoRequestDTO).block()));
    }

    @PostMapping("/generate/teaching-methods")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateTeachingMethods(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        return ResponseEntity.ok(new CompletionServiceResponseDTO(aiCompletionService.generateTeachingMethods(moduleInfoRequestDTO).block()));
    }

    @PostMapping("/generate/media")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter')")
    public ResponseEntity<CompletionServiceResponseDTO> generateMedia(@Valid @RequestBody CompletionServiceRequestDTO moduleInfoRequestDTO) {
        return ResponseEntity.ok(new CompletionServiceResponseDTO(aiCompletionService.generateMedia(moduleInfoRequestDTO).block()));
    }
}
