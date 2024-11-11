import { Component, inject } from '@angular/core';
import { HlmTableComponent, HlmTrowComponent, HlmThComponent, HlmTdComponent, HlmCaptionComponent } from '@spartan-ng/ui-table-helm';
import { ModuleVersion, Proposal, ProposalControllerService, ProposalViewDTO, UserIdDTO } from '../../core/modules/openapi';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmBadgeDirective } from '@spartan-ng/ui-badge-helm';
import { HttpErrorResponse } from '@angular/common/http';
import { LayoutComponent } from '../../components/layout.component';
import { StatusDisplayPipe } from '../../pipes/proposalStatus.pipe';

@Component({
  selector: 'app-proposal-view',
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
    LayoutComponent,
    StatusDisplayPipe
  ],
  host: {
    class: 'w-full overflow-x-auto'
  },
  templateUrl: './proposal-view.component.html'
})
export class ProposalViewComponent {
  proposalService = inject(ProposalControllerService);
  loading: boolean = true;
  error: string | null = null;
  proposal: ProposalViewDTO | null = null;
  proposalStatusEnum = Proposal.StatusEnum;
  moduleStatusEnum = ModuleVersion.StatusEnum;
  selectedUserId: number = 1; // You might want to inject this or get it from a service

  constructor() {
    this.fetchProposal();
  }

  private async fetchProposal() {
    const proposalId = Number(window.location.pathname.split('/').pop());
    if (proposalId) {
      this.loading = true;
      this.proposalService.getProposalView(proposalId).subscribe({
        next: (data: ProposalViewDTO) => (this.proposal = data),
        error: (err: HttpErrorResponse) => (this.error = err.error),
        complete: () => (this.loading = false)
      });
    }
  }

  submitProposal() {
    if (this.proposal) {
      const userIdDto: UserIdDTO = { userId: this.selectedUserId };
      this.proposalService.submitProposal(this.proposal.proposalId!, userIdDto).subscribe({
        next: (response: ProposalViewDTO) => {
          this.proposal = response;
        },
        error: (err: HttpErrorResponse) => (this.error = err.error)
      });
    }
  }

  deleteProposal() {
    if (this.proposal) {
      const userIdDto: UserIdDTO = { userId: this.selectedUserId };
      this.proposalService.deleteProposal(this.proposal.proposalId!, userIdDto).subscribe({
        next: () => {
          window.location.href = '/proposals';
        },
        error: (err: HttpErrorResponse) => (this.error = err.error)
      });
    }
  }

  updateProposal() {
    if (this.proposal) {
      window.location.href = `/proposals/edit/${this.proposal.proposalId}`;
    }
  }

  viewModuleVersion(versionId: number | undefined) {
    if (versionId) {
      window.location.href = `/module-version/view/${versionId}`;
    }
  }

  editLatestModuleVersion() {
    if (this.proposal?.latestVersion) {
      window.location.href = `/module-version/edit/${this.proposal.latestModuleVersion!.moduleVersionId}`;
    }
  }

  addNewModuleVersion() {
    if (this.proposal) {
      window.location.href = `/module-version/add?proposalId=${this.proposal.proposalId}`;
    }
  }
}
