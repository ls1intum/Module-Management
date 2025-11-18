package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.ProposalService;
import modulemanagement.ls1.shared.CurrentUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private static final Logger log = LoggerFactory.getLogger(ProposalController.class);

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<Proposal> createProposal(@CurrentUser User user,
            @Valid @RequestBody ProposalRequestDTO request) {
        log.info("createProposal invoked");
        Proposal proposal = proposalService.createProposalFromRequest(user, request);
        return ResponseEntity.ok(proposal);
    }

    @PostMapping(value = "/submit/{proposalId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ProposalViewDTO> submitProposal(@CurrentUser User user,
            @PathVariable Long proposalId) {
        var proposalDto = proposalService.submitProposal(proposalId, user.getUserId());
        return ResponseEntity.ok(proposalDto);
    }

    @PostMapping(value = "/cancel-submission/{proposalId}")
    public ResponseEntity<ProposalViewDTO> cancelSubmission(@CurrentUser User user,
            @PathVariable Long proposalId) {
        var proposalDto = proposalService.cancelSubmission(proposalId, user.getUserId());
        return ResponseEntity.ok(proposalDto);
    }

    @PostMapping("/add-module-version")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ProposalViewDTO> addModuleVersion(@CurrentUser User user,
            @Valid @RequestBody AddModuleVersionDTO request) {
        ProposalViewDTO proposal = proposalService.addModuleVersion(user.getUserId(), request);
        return ResponseEntity.ok(proposal);
    }

    @GetMapping("/{id}/view")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ProposalViewDTO> getProposalView(@CurrentUser User user, @PathVariable Long id) {
        ProposalViewDTO p = proposalService.getProposalViewDtoById(user.getUserId(), id);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/compact/from-authenticated-user")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<List<ProposalsCompactDTO>> getCompactProposalsFromUser(@CurrentUser User user) {
        List<ProposalsCompactDTO> proposals = proposalService.getCompactProposalsOfUser(user.getUserId());
        return ResponseEntity.ok(proposals);
    }

    @DeleteMapping(value = "/{proposalId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<String> deleteProposal(@CurrentUser User user, @PathVariable Long proposalId) {
        try {
            proposalService.deleteProposalById(proposalId, user.getUserId());
            return ResponseEntity.ok("Proposal deleted successfully.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.badRequest().body(e.getReason());
        }
    }
}
