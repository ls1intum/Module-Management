import { Component } from '@angular/core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { ProposalBaseComponent } from '../../components/create-edit-base/create-edit-base.component';
import { FeedbackAuthorDisplayPipe } from '../../pipes/feedbackAuthorDisplay.pipe';
import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { ToggleButtonGroupComponent } from '../../components/toggle-button-group/toggle-button-group.component';
import { ModuleEditStepperComponent } from '../../components/module-edit-stepper/module-edit-stepper.component';
import { ModuleDegreeProgramAssignmentDTO, ProposalRequestDTO } from '../../core/modules/openapi';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { FeedbackMessageComponent } from '../../components/feedback-message/feedback-message.component';

@Component({
  selector: 'app-proposal-create',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    FeedbackAuthorDisplayPipe,
    FeedbackStatusPipe,
    ToggleButtonGroupComponent,
    ModuleEditStepperComponent,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    InputNumberModule,
    MessageModule,
    SelectModule,
    ProgressSpinnerModule,
    TagModule,
    FeedbackMessageComponent
  ],
  templateUrl: '../../components/create-edit-base/create-edit-base.component.html',
  styleUrl: '../../components/create-edit-base/create-edit-base-layout.css'
})
export class ProposalCreateComponent extends ProposalBaseComponent {
  constructor() {
    super();
    this.loadDegreePrograms();
  }

  override async onSubmit(): Promise<void> {

    const rawAssignments = this.assignments().filter((a) => a.degreeProgramId != null && a.degreeProgramSpecializationId != null);
    const programIds = rawAssignments.map((a) => a.degreeProgramId!);
    if (new Set(programIds).size !== programIds.length) {
      this.showErrorAsToast('Each degree program can only be assigned once.');
      return;
    }
    const degreeProgramAssignments: ModuleDegreeProgramAssignmentDTO[] = rawAssignments.map((a) => ({
      degreeProgramId: a.degreeProgramId!,
      degreeProgramSpecializationId: a.degreeProgramSpecializationId!
    }));
    const body: ProposalRequestDTO = {
      ...this.proposalForm.value,
      titleEng: this.proposalForm.value.titleEng?.trim() ?? '',
      degreeProgramAssignments
    };
    this.loading.set(true);
    try {
      const res = await firstValueFrom(this.proposalService.createProposal(body));
      const proposalId = res.proposalId;
      const moduleVersionId = res.latestModuleVersion?.moduleVersionId;
      if (proposalId != null && moduleVersionId != null) {
        await this.router.navigate(['/proposals', proposalId, 'version', moduleVersionId, 'edit'], { queryParams: { created: true } });
      } else {
        this.showErrorAsToast('Unexpected response from server.');
      }
    } catch (err: unknown) {
      this.showErrorAsToast(err, 'Failed to create proposal.');
    } finally {
      this.loading.set(false);
    }
  }
}
