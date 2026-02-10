import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProposalBaseComponent } from '../../components/create-edit-base/create-edit-base.component';
import { FeedbackDepartmentPipe } from '../../pipes/feedbackDepartment.pipe';
import { ModuleVersionUpdateRequestDTO, ModuleVersionUpdateResponseDTO, ModuleVersionViewFeedbackDTO } from '../../core/modules/openapi';
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
  moduleLoading = false;
  feedbackLoading = false;

  constructor(route: ActivatedRoute) {
    super();
    this.moduleVersionId = Number(route.snapshot.paramMap.get('versionId'));
    this.fetchModuleVersion(this.moduleVersionId);
    this.fetchPreviousModuleVersionFeedback(this.moduleVersionId);
  }

  fetchModuleVersion(moduleVersionId: number) {
    this.moduleLoading = true;
    this.moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId).subscribe({
      next: (response: ModuleVersionUpdateRequestDTO) => {
        this.proposalForm.patchValue(response);
        this.moduleVersionDto.set(response);
      },
      error: (err: HttpErrorResponse) => this.error.set(err.error),
      complete: () => {
        this.moduleLoading = false;
        this.loading.set(this.moduleLoading && this.feedbackLoading);
      }
    });
  }

  fetchPreviousModuleVersionFeedback(previousModuleVersionId: number) {
    this.feedbackLoading = true;
    this.moduleVersionService.getPreviousModuleVersionFeedback(previousModuleVersionId).subscribe({
      next: (response: Array<ModuleVersionViewFeedbackDTO>) => this.feedbacks.set([...response]),
      error: (err: HttpErrorResponse) => this.error.set(err.error),
      complete: () => {
        this.feedbackLoading = false;
        this.loading.set(this.moduleLoading && this.feedbackLoading);
      }
    });
  }

  override async onSubmit() {
    if (this.proposalForm.valid && this.moduleVersionId) {
      this.loading.set(true);
      this.error.set(null);

      const proposalData: ModuleVersionUpdateRequestDTO = {
        ...this.proposalForm.value,
        moduleVersionId: this.moduleVersionId
      };

      this.moduleVersionService.updateModuleVersion(this.moduleVersionId, proposalData).subscribe({
        next: (response: ModuleVersionUpdateResponseDTO) => {
          this.proposalForm.reset();
          this.router.navigate(['/proposals/view', response.proposalId], {
            queryParams: { created: true }
          });
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          this.error.set(err.error);
          this.loading.set(false);
        },
        complete: () => this.loading.set(false)
      });
    }
  }
}
