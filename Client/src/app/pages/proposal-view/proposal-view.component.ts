import { Component, inject, signal } from '@angular/core';
import { AddModuleVersionDTO, ModuleVersion, Proposal, ProposalControllerService, ProposalViewDTO } from '../../core/modules/openapi';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { StatusInfoPipeline, StatusDisplayPipe } from '../../pipes/proposalStatus.pipe';
import { ModuleVersionStatusPipe } from '../../pipes/moduleVersionStatus.pipe';
import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
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
    RouterModule,
    CommonModule,
    StatusDisplayPipe,
    StatusInfoPipeline,
    ModuleVersionStatusPipe,
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
  loading = signal(true);
  error = signal<string | null>(null);
  proposal = signal<ProposalViewDTO | null>(null);
  proposalStatusEnum = Proposal.StatusEnum;
  moduleStatusEnum = ModuleVersion.StatusEnum;

  constructor() {
    this.fetchProposal();
  }

  private fetchProposal() {
    this.route.params.subscribe((params) => {
      const proposalId = Number(params['id']);
      if (proposalId) {
        this.loading.set(true);
        this.proposalService.getProposalView(proposalId).subscribe({
          next: (data: ProposalViewDTO) => this.proposal.set(data),
          error: (err: HttpErrorResponse) => this.error.set(err.error),
          complete: () => this.loading.set(false)
        });
      }
    });
  }

  submitProposal() {
    if (this.proposal()) {
      this.proposalService.submitProposal(this.proposal()!.proposalId!).subscribe({
        next: (response: ProposalViewDTO) => this.proposal.set(response),
        error: (err: HttpErrorResponse) => this.error.set(err.error)
      });
    }
  }

  cancelProposal() {
    if (this.proposal()) {
      this.proposalService.cancelSubmission(this.proposal()!.proposalId!).subscribe({
        next: (response: ProposalViewDTO) => this.proposal.set(response),
        error: (err: HttpErrorResponse) => this.error.set(err.error)
      });
    }
  }

  deleteProposal() {
    if (this.proposal()) {
      this.proposalService.deleteProposal(this.proposal()!.proposalId!).subscribe({
        next: () => this.router.navigate(['/proposals']),
        error: (err: HttpErrorResponse) => this.error.set(err.error)
      });
    }
  }

  addNewModuleVersion() {
    if (this.proposal()) {
      const addModuleVersionDto: AddModuleVersionDTO = { proposalId: this.proposal()!.proposalId! };
      this.proposalService.addModuleVersion(addModuleVersionDto).subscribe({
        next: (response: ProposalViewDTO) => this.proposal.set(response),
        error: (err: HttpErrorResponse) => this.error.set(err.error)
      });
    }
  }
}
