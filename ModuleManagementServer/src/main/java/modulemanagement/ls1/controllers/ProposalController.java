package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.AuthenticationService;
import modulemanagement.ls1.services.ProposalService;
import jakarta.validation.Valid;
import modulemanagement.ls1.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalService proposalService;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public ProposalController(ProposalService proposalService, UserService userService, AuthenticationService authenticationService) {
        this.proposalService = proposalService;
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('admin', 'proposal-submitter', 'proposal-reviewer')")
    public ResponseEntity<User> getUser(@AuthenticationPrincipal Jwt jwt) {
        User authenticatedUser = authenticationService.getAuthenticatedUser(jwt);
        return ResponseEntity.ok(authenticatedUser);
    }

    @PostMapping(value = "/submit/{proposalId}")
    public ResponseEntity<ProposalViewDTO> submitProposal(@PathVariable Long proposalId, @RequestBody UserIdDTO request) {
        var proposalDto = proposalService.submitProposal(proposalId, request.getUserId());
        return ResponseEntity.ok(proposalDto);
    }

    @PostMapping
    public ResponseEntity<Proposal> createProposal(@Valid @RequestBody ProposalRequestDTO request) {
        Proposal proposal = proposalService.createProposalFromRequest(request);
        return ResponseEntity.ok(proposal);
    }

    @PostMapping("/add-module-version")
    public ResponseEntity<ProposalViewDTO> addModuleVersion(@Valid @RequestBody AddModuleVersionDTO request) {
        ProposalViewDTO proposal = proposalService.addModuleVersion(request);
        return ResponseEntity.ok(proposal);
    }

    @GetMapping
    public ResponseEntity<List<Proposal>> getAllProposals() {
        List<Proposal> proposals = proposalService.getAllProposals();
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/compact")
    public ResponseEntity<List<ProposalsCompactDTO>> getAllProposalsCompact() {
        List<ProposalsCompactDTO> proposals = proposalService.getAllProposalsCompact();
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proposal> getProposalById(@PathVariable long id) {
        Proposal proposal = proposalService.getProposalById(id);
        return proposal != null ? new ResponseEntity<>(proposal, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<ProposalViewDTO> getProposalView (@PathVariable Long id) {
        ProposalViewDTO p = proposalService.getProposalViewDtoById(id);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/from-user/{id}")
    public ResponseEntity<List<Proposal>> getProposalsByUserId(@PathVariable UUID id) {
        List<Proposal> proposals = proposalService.getProposalsOfUser(id);
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/compact/from-user/{id}")
    public ResponseEntity<List<ProposalsCompactDTO>> getProposalsByUserIdFromCompact(@PathVariable UUID id) {
        List<ProposalsCompactDTO> proposals = proposalService.getCompactProposalsOfUser(id);
        return ResponseEntity.ok(proposals);
    }

    @DeleteMapping(value = "/{proposalId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deleteProposal(@PathVariable Long proposalId, @Valid @RequestBody UserIdDTO request) {
        try{
            proposalService.deleteProposalById(proposalId, request.getUserId());
            return ResponseEntity.ok("Proposal deleted successfully.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.badRequest().body(e.getReason());
        }
    }
}

