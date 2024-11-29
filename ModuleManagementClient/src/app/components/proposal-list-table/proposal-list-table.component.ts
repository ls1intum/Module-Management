import { Component, inject, Input } from '@angular/core';
import { HlmCaptionComponent, HlmTableComponent, HlmTdComponent, HlmThComponent, HlmTrowComponent } from '@spartan-ng/ui-table-helm';
import { Proposal, ProposalControllerService, ProposalsCompactDTO, UserIdDTO } from '../../core/modules/openapi';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmBadgeDirective } from '@spartan-ng/ui-badge-helm';
import { StatusDisplayPipe } from '../../pipes/proposalStatus.pipe';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'proposal-list-table',
  standalone: true,
  imports: [
    HlmTableComponent,
    HlmTrowComponent,
    HlmThComponent,
    HlmTdComponent,
    HlmCaptionComponent,
    HlmButtonDirective,
    RouterModule,
    CommonModule,
    HlmBadgeDirective,
    StatusDisplayPipe
  ],
  host: {
    class: 'w-full overflow-x-auto'
  },
  templateUrl: './proposal-list-table.component.html'
})
export class ProposalListTableComponent {
  proposalService = inject(ProposalControllerService);
  securityStore = inject(SecurityStore);
  loading: boolean = true;
  error: string | null = null;
  proposals: ProposalsCompactDTO[] = [];
  proposalEnum = Proposal.StatusEnum;
  selectedUserId: string = '546b69f2-918b-4969-9c6e-16f2744abc4a';

  constructor() {
    if (this.securityStore.signedIn()) {
      this.fetchProposalsForUser();
    } else {
      this.securityStore.signIn();
    }
  }

  private async fetchProposalsForUser() {
    this.loading = true;

    this.proposalService.getAllProposalsCompact().subscribe({
      next: (proposals) => (this.proposals = proposals),
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }

  public async submitProposal(proposalId: number | undefined) {
    if (proposalId !== undefined) {
      const userIdDto: UserIdDTO = { userId: this.selectedUserId };
      this.proposalService.submitProposal(proposalId, userIdDto).subscribe({
        next: (response) => {
          this.proposals.map((proposal) => (proposal.status = proposal.proposalId === proposalId ? this.proposalEnum.PendingFeedback : proposal.status));
        },
        error: (err: HttpErrorResponse) => (this.error = err.error)
      });
    }
  }

  public async deleteProposal(proposalId: number | undefined) {
    if (proposalId !== undefined) {
      const userIdDto: UserIdDTO = { userId: this.selectedUserId };
      this.proposalService.deleteProposal(proposalId, userIdDto).subscribe({
        next: () => {
          const props = this.proposals.filter((proposal) => proposalId !== proposal.proposalId);
          console.log(props);
          this.proposals = props;
        },
        error: (err: HttpErrorResponse) => (this.error = err.error)
      });
    }
  }
}
