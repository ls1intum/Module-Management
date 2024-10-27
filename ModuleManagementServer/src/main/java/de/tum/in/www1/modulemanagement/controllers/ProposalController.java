package de.tum.in.www1.modulemanagement.controllers;

import de.tum.in.www1.modulemanagement.dtos.ProposalRequestDTO;
import de.tum.in.www1.modulemanagement.dtos.SimpleSubmitDTO;
import de.tum.in.www1.modulemanagement.models.Proposal;
import de.tum.in.www1.modulemanagement.services.ProposalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    @Autowired
    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @PostMapping("/submit/{proposalId}")
    public ResponseEntity<String> submitProposal(@PathVariable Long proposalId, @RequestBody SimpleSubmitDTO request) {
        proposalService.submitProposal(proposalId, request.getUserId());
        return ResponseEntity.ok("Proposal submitted successfully.");
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

    @GetMapping("/{id}")
    public ResponseEntity<Proposal> getProposalById(@PathVariable long id) {
        Proposal proposal = proposalService.getProposalById(id);
        return proposal != null ? new ResponseEntity<>(proposal, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
