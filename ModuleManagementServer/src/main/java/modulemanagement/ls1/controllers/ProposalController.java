package modulemanagement.ls1.modulemanagementserver.controllers;

import modulemanagement.dtos.*;
import modulemanagement.models.Proposal;
import modulemanagement.services.ProposalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
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
    public ResponseEntity<ProposalViewDTO> getProposalView (@PathVariable long id) {
        ProposalViewDTO p = proposalService.getProposalViewDtoById(id);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/from-user/{id}")
    public ResponseEntity<List<Proposal>> getProposalsByUserId(@PathVariable Long id) {
        List<Proposal> proposals = proposalService.getProposalsOfUser(id);
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/compact/from-user/{id}")
    public ResponseEntity<List<ProposalsCompactDTO>> getProposalsByUserIdFromCompact(@PathVariable Long id) {
        List<ProposalsCompactDTO> proposals = proposalService.getCompactProposalsOfUser(id);
        return ResponseEntity.ok(proposals);
    }

    @DeleteMapping(value = "/{proposalId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deleteProposal(@PathVariable long proposalId, @Valid @RequestBody UserIdDTO request) {
        try{
            proposalService.deleteProposalById(proposalId, request.getUserId());
            return ResponseEntity.ok("Proposal deleted successfully.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.badRequest().body(e.getReason());
        }
    }
}

