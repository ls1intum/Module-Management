import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProposalBaseComponent } from '../../components/create-edit-base/create-edit-base.component';
import { FeedbackDepartmentPipe } from '../../pipes/feedbackDepartment.pipe';
import { ModuleVersionUpdateRequestDTO, ModuleVersionUpdateResponseDTO, ModuleVersionViewDTO, ModuleVersionViewFeedbackDTO } from '../../core/modules/openapi';
import { ToggleButtonGroupComponent } from '../../components/toggle-button-group/toggle-button-group.component';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-module-version-edit',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    FeedbackDepartmentPipe,
    ToggleButtonGroupComponent,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    InputNumberModule,
    MessageModule
  ],
  templateUrl: '../../components/create-edit-base/create-edit-base.component.html'
})
export class ModuleVersionEditComponent extends ProposalBaseComponent {
  override moduleVersionId: number;
  override moduleVersionDto: ModuleVersionUpdateRequestDTO | null = null;
  moduleLoading: boolean = false;
  override feedbacks: Array<ModuleVersionViewFeedbackDTO> | undefined = [];
  feedbackLoading: boolean = false;

  constructor(route: ActivatedRoute) {
    super();
    this.moduleVersionId = Number(route.snapshot.paramMap.get('versionId'));
    this.fetchModuleVersion(this.moduleVersionId);
    this.fetchPreviousModuleVersionFeedback(this.moduleVersionId);
  }

  async fetchModuleVersion(moduleVersionId: number) {
    this.moduleLoading = true;
    this.moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId).subscribe({
      next: (response: ModuleVersionUpdateRequestDTO) => {
        this.proposalForm.patchValue(response);
        this.moduleVersionDto = response;
      },
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => {
        this.moduleLoading = false;
        this.loading = this.moduleLoading && this.feedbackLoading;
      }
    });
  }

  async fetchPreviousModuleVersionFeedback(previousModuleVersionId: number) {
    this.feedbackLoading = true;
    this.moduleVersionService.getPreviousModuleVersionFeedback(previousModuleVersionId).subscribe({
      next: (response: Array<ModuleVersionViewFeedbackDTO>) => {
        this.feedbacks = [...response];
      },
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => {
        this.moduleLoading = false;
        this.loading = this.moduleLoading && this.feedbackLoading;
      }
    });
  }

  override async onSubmit() {
    if (this.proposalForm.valid && this.moduleVersionId) {
      this.loading = true;
      this.error = null;

      const proposalData: ModuleVersionUpdateRequestDTO = {
        ...this.proposalForm.value,
        moduleVersionId: this.moduleVersionId
      };

      console.log(proposalData);
      this.moduleVersionService.updateModuleVersion(this.moduleVersionId, proposalData).subscribe({
        next: (response: ModuleVersionUpdateResponseDTO) => {
          this.proposalForm.reset();
          this.router.navigate(['/proposals/view', response.proposalId], {
            queryParams: { created: true }
          });
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          this.error = err.error;
          this.loading = false;
        },
        complete: () => {
          this.loading = false;
        }
      });
    }
  }
}
