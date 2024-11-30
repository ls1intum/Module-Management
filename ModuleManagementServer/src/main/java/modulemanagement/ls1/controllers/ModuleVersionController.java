package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateResponseDTO;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.AuthenticationService;
import modulemanagement.ls1.services.ModuleVersionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/module-versions")
public class ModuleVersionController {
    private final ModuleVersionService moduleVersionService;
    private final AuthenticationService authenticationService;

    public ModuleVersionController(ModuleVersionService moduleVersionService, AuthenticationService authenticationService) {
        this.moduleVersionService = moduleVersionService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/{moduleVersionId}")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> getModuleVersionUpdateDtoFromId(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ModuleVersionUpdateResponseDTO dto = moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId, user.getUserId());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{moduleVersionId}")
    public ResponseEntity<ModuleVersionUpdateResponseDTO> updateModuleVersion(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moduleVersionId, @Valid @RequestBody ModuleVersionUpdateRequestDTO moduleVersion) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ModuleVersionUpdateResponseDTO updatedModuleVersion = moduleVersionService.updateModuleVersionFromRequest(user.getUserId(), moduleVersionId, moduleVersion);
        return ResponseEntity.ok(updatedModuleVersion);
    }
}
