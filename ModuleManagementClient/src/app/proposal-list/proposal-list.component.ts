import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ProposalsCompactDTO, UserIdDTO } from '../core/modules/openapi';
import { ProposalControllerService } from '../core/modules/openapi';
import { Proposal } from '../core/modules/openapi'

@Component({
  selector: 'app-proposal-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './proposal-list.component.html',
  styleUrl: './proposal-list.component.css'
})
export class ProposalListComponent {
  proposalService = inject(ProposalControllerService);
  proposals: ProposalsCompactDTO[] = [];
  proposalEnum = Proposal.StatusEnum;
  users = [
    { id: 1, name: 'Professor1' },
    { id: 2, name: 'Professor2' },
    { id: 3, name: 'QM User' },
    { id: 4, name: 'ASA User' },
    { id: 5, name: 'EB User' },
    { id: 6, name: 'Admin'}
  ];
  loading: boolean = true;
  error: string | null = null;
  selectedUserId: number | null = null;

  constructor() {
    this.fetchProposals();
  }

  public async onUserChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    const userId = parseInt(selectElement.value, 10);
    this.loading = true;
    const selectedUser = this.users.find(user => user.id === userId);
    this.selectedUserId = userId;
    const isProfessor = selectedUser?.name.includes('Professor');
    
    try {
      if (isProfessor) {
        await this.fetchProposalsForUser(userId);
      } else {
        await this.fetchProposals();
      }
    } catch (err) {
      this.error = 'Failed to load proposals';
      console.error(err);
    } finally {
      this.loading = false;
    }
  }

  private async fetchProposals() {
    this.loading = true;
    this.proposalService.getAllProposalsCompact().subscribe({
      next: (proposals) => this.proposals = proposals,
      error: (err) => this.error = err,
      complete: () => this.loading = false
    })
  }

  private async fetchProposalsForUser(userId: number) {
    this.loading = true;
    this.proposalService.getProposalsByUserIdFromCompact(userId).subscribe({
      next: (proposals) => this.proposals = proposals,
      error: (err) => this.error = err,
      complete: () => this.loading = false
    })
  }

  public async submitProposal(proposalId: number | undefined ) {
    if (proposalId !== undefined && this.selectedUserId) {
      const userIdDto: UserIdDTO = { userId: this.selectedUserId };
      this.proposalService.submitProposal(proposalId, userIdDto).subscribe({
        next: (response) => {
          console.log(response);
        }
      })
    }
  }

  public async deleteProposal(proposalId: number | undefined) {
    
    if (proposalId !== undefined && this.selectedUserId) {
      const userIdDto: UserIdDTO = { userId: this.selectedUserId };
      this.proposalService.deleteProposal(proposalId, userIdDto).subscribe({
        next: () => this.proposals = this.proposals.filter(proposal => proposalId !== proposal.proposalId),
        error: (err) => this.error = err,
      })
    }
  }
}