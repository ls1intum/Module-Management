import { Component } from '@angular/core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProposalBaseComponent } from '../../components/create-edit-base/create-edit-base.component';
import { FeedbackDepartmentPipe } from '../../pipes/feedbackDepartment.pipe';
import { ModuleEditStepperComponent } from '../../components/module-edit-stepper/module-edit-stepper.component';
import { ModuleDegreeProgramAssignmentDTO, ModuleVersionUpdateRequestDTO, ModuleVersionViewDTO, ModuleVersionViewFeedbackDTO } from '../../core/modules/openapi';
import { ToggleButtonGroupComponent } from '../../components/toggle-button-group/toggle-button-group.component';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';

@Component({
  selector: 'app-module-version-edit',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    CommonModule,
    RouterModule,
    FeedbackDepartmentPipe,
    ToggleButtonGroupComponent,
    ModuleEditStepperComponent,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    InputNumberModule,
    MessageModule,
    SelectModule,
    ProgressSpinnerModule,
    TagModule
  ],
  templateUrl: '../../components/create-edit-base/create-edit-base.component.html',
  styleUrl: '../../components/create-edit-base/create-edit-base-layout.css'
})
export class ModuleVersionEditComponent extends ProposalBaseComponent {
  override moduleVersionId: number;
  moduleLoading = false;
  feedbackLoading = false;

  constructor(route: ActivatedRoute) {
    super();
    this.moduleVersionId = Number(route.snapshot.paramMap.get('versionId'));
    this.loadDegreePrograms();
    this.fetchModuleVersion(this.moduleVersionId);
    this.fetchPreviousModuleVersionFeedback(this.moduleVersionId);
  }

  fetchModuleVersion(moduleVersionId: number) {
    this.moduleLoading = true;
    this.moduleVersionService.getModuleVersion(moduleVersionId).subscribe({
      next: (response: ModuleVersionViewDTO) => {
        this.proposalForm.patchValue(response);
        this.moduleVersionDto.set(response);
        const list = (response?.degreeProgramAssignments ?? []).map((a) => ({
          degreeProgramId: a.degreeProgramId ?? null,
          degreeProgramSpecializationId: a.degreeProgramSpecializationId ?? null
        }));
        this.assignments.set(list.length > 0 ? list : [{ degreeProgramId: null, degreeProgramSpecializationId: null }]);
        const version = response?.version;
        this.breadcrumbLabels.proposalTitle.set(response?.titleEng ?? null);
        this.breadcrumbLabels.versionLabel.set(version != null ? `Version ${version}` : null);
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

  override onSubmit(): void {
    if (this.moduleVersionId == null) return;
    this.loading.set(true);
    this.error.set(null);
    const rawAssignments = this.assignments().filter((a) => a.degreeProgramId != null && a.degreeProgramSpecializationId != null);
    const degreeProgramAssignments: ModuleDegreeProgramAssignmentDTO[] = rawAssignments.map((a) => ({
      degreeProgramId: a.degreeProgramId!,
      degreeProgramSpecializationId: a.degreeProgramSpecializationId!
    }));
    const payload: ModuleVersionUpdateRequestDTO = {
      ...this.proposalForm.value,
      moduleVersionId: this.moduleVersionId,
      degreeProgramAssignments
    };
    this.moduleVersionService.updateModuleVersion(this.moduleVersionId, payload).subscribe({
      next: (response: ModuleVersionViewDTO) => this.moduleVersionDto.set(response),
      error: (err: HttpErrorResponse) => this.error.set(err.error),
      complete: () => this.loading.set(false)
    });
  }
}
