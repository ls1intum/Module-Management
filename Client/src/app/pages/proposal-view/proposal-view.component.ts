import { Component, inject } from '@angular/core';
import { AddModuleVersionDTO, ModuleVersion, Proposal, ProposalControllerService, ProposalViewDTO } from '../../core/modules/openapi';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { StatusInfoPipeline, StatusDisplayPipe } from '../../pipes/proposalStatus.pipe';
import { FadedModuleVersionStatusPipe, ModuleVersionStatusTagPipe } from '../../pipes/moduleVersionStatus.pipe';
import { FadedFeedbackStatusPipe, FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { FeedbackDepartmentPipe } from '../../pipes/feedbackDepartment.pipe';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-proposal-view',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    TagModule,
    MessageModule,
    ToastModule,
    TooltipModule,
    FeedbackStatusPipe,
    FeedbackDepartmentPipe,
    FadedFeedbackStatusPipe,
    RouterModule,
    CommonModule,
    StatusDisplayPipe,
    StatusInfoPipeline,
    ModuleVersionStatusTagPipe,
    FadedModuleVersionStatusPipe,
    ProgressSpinnerModule
  ],
  host: {
    class: 'w-full overflow-x-auto'
  },
  templateUrl: './proposal-view.component.html'
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
