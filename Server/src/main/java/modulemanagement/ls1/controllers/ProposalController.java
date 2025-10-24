package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.AuthenticationService;
import modulemanagement.ls1.services.ProposalService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private static final Logger log = LoggerFactory.getLogger(ProposalController.class);

    private final ProposalService proposalService;
    private final AuthenticationService authenticationService;

    public ProposalController(ProposalService proposalService, AuthenticationService authenticationService) {
        this.proposalService = proposalService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    public ResponseEntity<Proposal> createProposal(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProposalRequestDTO request) {
        log.info("createProposal invoked");
        User user = authenticationService.getAuthenticatedUser(jwt);
        Proposal proposal = proposalService.createProposalFromRequest(user, request);
        return ResponseEntity.ok(proposal);
    }

    @PostMapping(value = "/submit/{proposalId}")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<ProposalViewDTO> submitProposal(@AuthenticationPrincipal Jwt jwt, @PathVariable Long proposalId) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        var proposalDto = proposalService.submitProposal(proposalId, user.getUserId());
        return ResponseEntity.ok(proposalDto);
    }

    @PostMapping(value = "/cancel-submission/{proposalId}")
    public ResponseEntity<ProposalViewDTO> cancelSubmission(@AuthenticationPrincipal Jwt jwt, @PathVariable Long proposalId) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        var proposalDto = proposalService.cancelSubmission(proposalId, user.getUserId());
        return ResponseEntity.ok(proposalDto);
    }

    @PostMapping("/add-module-version")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<ProposalViewDTO> addModuleVersion(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddModuleVersionDTO request) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        ProposalViewDTO proposal = proposalService.addModuleVersion(user.getUserId(), request);
        return ResponseEntity.ok(proposal);
    }

    @GetMapping("/{id}/view")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<ProposalViewDTO> getProposalView (@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        User requester = authenticationService.getAuthenticatedUser(jwt);
        ProposalViewDTO p = proposalService.getProposalViewDtoById(requester.getUserId(), id);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/compact/from-authenticated-user")
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<List<ProposalsCompactDTO>> getCompactProposalsFromUser(@AuthenticationPrincipal Jwt jwt) {
        User user = authenticationService.getAuthenticatedUser(jwt);
        List<ProposalsCompactDTO> proposals = proposalService.getCompactProposalsOfUser(user.getUserId());
        return ResponseEntity.ok(proposals);
    }

    @DeleteMapping(value = "/{proposalId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('admin', 'module-submitter')")
    public ResponseEntity<String> deleteProposal(@AuthenticationPrincipal Jwt jwt, @PathVariable Long proposalId) {
        try{
            User user = authenticationService.getAuthenticatedUser(jwt);
            proposalService.deleteProposalById(proposalId, user.getUserId());
            return ResponseEntity.ok("Proposal deleted successfully.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.badRequest().body(e.getReason());
        }
    }
}

