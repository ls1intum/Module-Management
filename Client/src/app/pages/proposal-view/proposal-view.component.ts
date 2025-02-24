import { Component, inject } from '@angular/core';
import { HlmTableComponent, HlmTrowComponent, HlmThComponent, HlmTdComponent, HlmCaptionComponent } from '@spartan-ng/ui-table-helm';
import { AddModuleVersionDTO, ModuleVersion, Proposal, ProposalControllerService, ProposalViewDTO } from '../../core/modules/openapi';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
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
import { HlmAlertDescriptionDirective, HlmAlertDirective, HlmAlertIconDirective, HlmAlertTitleDirective } from '@spartan-ng/ui-alert-helm';
import { lucideInfo } from '@ng-icons/lucide';
import { provideIcons } from '@ng-icons/core';
import { HlmIconComponent } from '@spartan-ng/ui-icon-helm';
import { HlmToasterComponent } from "../../../spartan-components/ui-sonner-helm/src/lib/hlm-toaster.component";

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
    FadedModuleVersionStatusPipe,
    HlmAlertDescriptionDirective,
    HlmAlertDirective,
    HlmAlertIconDirective,
    HlmAlertTitleDirective,
    HlmIconComponent,
    HlmToasterComponent
],
  host: {
    class: 'w-full overflow-x-auto'
  },
  templateUrl: './proposal-view.component.html',
  providers: [provideIcons({ lucideInfo })]
})
export class ProposalViewComponent {
  router = inject(Router);
  route = inject(ActivatedRoute);
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
    this.route.params.subscribe((params) => {
      const proposalId = Number(params['id']);
      if (proposalId) {
        this.loading = true;
        this.proposalService.getProposalView(proposalId).subscribe({
          next: (data: ProposalViewDTO) => (this.proposal = data),
          error: (err: HttpErrorResponse) => (this.error = err.error),
          complete: () => (this.loading = false)
        });
      }
    });
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
        next: () => this.router.navigate(['/proposals']),
        error: (err: HttpErrorResponse) => (this.error = err.error)
      });
    }
  }

  updateProposal() {
    if (this.proposal) {
      this.router.navigate(['/proposals/edit', this.proposal.proposalId]);
    }
  }

  viewModuleVersion(versionId: number | undefined) {
    if (versionId) {
      this.router.navigate(['/module-version/view', versionId]);
    }
  }

  editLatestModuleVersion() {
    if (this.proposal?.latestVersion) {
      this.router.navigate(['/module-version/edit', this.proposal.latestModuleVersion!.moduleVersionId]);
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
