import { Component, inject } from '@angular/core';
import { HlmTableComponent, HlmTrowComponent, HlmThComponent, HlmTdComponent, HlmCaptionComponent } from '@spartan-ng/ui-table-helm';
import { AddModuleVersionDTO, ModuleVersion, Proposal, ProposalControllerService, ProposalViewDTO } from '../../core/modules/openapi';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmBadgeDirective } from '@spartan-ng/ui-badge-helm';
import { BrnHoverCardComponent, BrnHoverCardContentDirective, BrnHoverCardTriggerDirective } from '@spartan-ng/ui-hovercard-brain';
import { HttpErrorResponse } from '@angular/common/http';
import { StatusDisplayPipe, StatusInfoPipeline } from '../../pipes/proposalStatus.pipe';
import { FadedModuleVersionStatusPipe, ModuleVersionStatusPipe } from '../../pipes/moduleVersionStatus.pipe';
import { HlmHoverCardModule } from '@spartan-ng/ui-hovercard-helm';
import { FadedFeedbackStatusPipe, FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { FeedbackDepartmentPipe } from '../../pipes/feedbackDepartment.pipe';

@Component({
  selector: 'app-proposal-view',
  standalone: true,
  imports: [
    BrnHoverCardComponent,
    BrnHoverCardContentDirective,
    BrnHoverCardTriggerDirective,
    HlmTableComponent,
    HlmTrowComponent,
    HlmHoverCardModule,
    HlmThComponent,
    HlmTdComponent,
    HlmButtonDirective,
    FeedbackStatusPipe,
    FeedbackDepartmentPipe,
    FadedFeedbackStatusPipe,
    RouterModule,
    CommonModule,
    HlmBadgeDirective,
    StatusDisplayPipe,
    StatusInfoPipeline,
    ModuleVersionStatusPipe,
    FadedModuleVersionStatusPipe
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
      this.proposalService.submitProposal(this.proposal.proposalId!).subscribe({
        next: (response: ProposalViewDTO) => (this.proposal = response),
        error: (err: HttpErrorResponse) => (this.error = err.error)
      });
    }
  }

  cancelProposal() {
    if (this.proposal) {
      this.proposalService.cancelSubmission(this.proposal.proposalId!).subscribe({
        next: (response: ProposalViewDTO) => (this.proposal = response),
        error: (err: HttpErrorResponse) => (this.error = err.error)
      });
    }
  }

  deleteProposal() {
    if (this.proposal) {
      this.proposalService.deleteProposal(this.proposal.proposalId!).subscribe({
        next: () => (window.location.href = '/proposals'),
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
      const addModuleVersionDto: AddModuleVersionDTO = { proposalId: this.proposal.proposalId! };
      this.proposalService.addModuleVersion(addModuleVersionDto).subscribe({
        next: (response: ProposalViewDTO) => (this.proposal = response),
        error: (err: HttpErrorResponse) => (this.error = err.error)
      });
    }
  }
}
