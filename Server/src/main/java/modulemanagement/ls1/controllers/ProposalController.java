package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.services.ProposalService;
import modulemanagement.ls1.shared.CurrentUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ProposalViewDTO> createProposal(@CurrentUser User user,
            @Valid @RequestBody ProposalRequestDTO request) {
        log.info("createProposal invoked");
        ProposalViewDTO proposalView = proposalService.createProposalFromRequest(user, request);
        return ResponseEntity.ok(proposalView);
    }

    @PostMapping(value = "/request-coordinators-feedback/{proposalId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ProposalViewDTO> requestCoordinatorsFeedback(@CurrentUser User user,
            @PathVariable Long proposalId) {
        var proposalDto = proposalService.requestCoordinatorsFeedback(proposalId, user.getUserId());
        return ResponseEntity.ok(proposalDto);
    }

    @PostMapping(value = "/request-full-feedback/{proposalId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<ProposalViewDTO> requestFullFeedback(@CurrentUser User user,
            @PathVariable Long proposalId) {
        var proposalDto = proposalService.requestFullFeedback(proposalId, user.getUserId());
        return ResponseEntity.ok(proposalDto);
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

    @DeleteMapping(value = "/{proposalId}")
    @PreAuthorize("hasAnyRole('PROFESSOR')")
    public ResponseEntity<String> deleteProposal(@CurrentUser User user, @PathVariable Long proposalId) {
        proposalService.deleteProposalById(proposalId, user.getUserId());
        return ResponseEntity.ok("Proposal deleted successfully.");
    }
}
