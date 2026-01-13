import { Component, inject } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { Proposal, ProposalControllerService, ProposalsCompactDTO } from '../../core/modules/openapi';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { StatusDisplayTagPipe } from '../../pipes/proposalStatus.pipe';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'proposal-list-table',
  standalone: true,
  imports: [TableModule, ButtonModule, TagModule, CommonModule, RouterModule, StatusDisplayTagPipe],
  host: {
    class: 'w-full overflow-x-auto'
  },
  templateUrl: './proposal-list-table.component.html'
})
export class ProposalListTableComponent {
  router = inject(Router);
  proposalService = inject(ProposalControllerService);
  securityStore = inject(SecurityStore);
  loading: boolean = true;
  error: string | null = null;
  proposals: ProposalsCompactDTO[] = [];
  proposalEnum = Proposal.StatusEnum;
  user = this.securityStore.user;

  constructor() {
    if (this.user !== undefined) {
      this.fetchProposalsForUser();
    } else {
      this.securityStore.signIn();
    }
  }

  private async fetchProposalsForUser() {
    this.loading = true;

    this.proposalService.getCompactProposalsFromUser().subscribe({
      next: (proposals: ProposalsCompactDTO[]) => (this.proposals = proposals),
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }

  deleteProposal(proposalId: number) {
    this.proposalService.deleteProposal(proposalId).subscribe({
      next: () => this.router.navigate(['/proposals']),
      error: (err: HttpErrorResponse) => (this.error = err.error)
    });
  }
}
