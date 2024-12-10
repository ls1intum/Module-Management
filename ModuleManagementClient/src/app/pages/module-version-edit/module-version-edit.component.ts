import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModuleVersionControllerService, ModuleVersionUpdateRequestDTO, ModuleVersionUpdateResponseDTO, } from '../../core/modules/openapi';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmFormFieldModule } from '@spartan-ng/ui-formfield-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { BrnSelectImports } from '@spartan-ng/ui-select-brain';
import { HlmSelectImports } from '@spartan-ng/ui-select-helm';
import { LayoutComponent } from '../../components/layout.component';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AutoResizeDirective } from '../../core/shared/autoresize.directive';
import { HlmAlertDescriptionDirective, HlmAlertDirective, HlmAlertIconDirective, HlmAlertTitleDirective } from '@spartan-ng/ui-alert-helm';
import { HlmIconComponent, provideIcons } from '@spartan-ng/ui-icon-helm';
import { lucideInfo } from '@ng-icons/lucide';

@Component({
  selector: 'module-version-edit',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HlmButtonDirective, HlmFormFieldModule, HlmInputDirective, BrnSelectImports, HlmSelectImports, LayoutComponent, RouterModule, AutoResizeDirective, HlmAlertDescriptionDirective, HlmAlertDirective, HlmAlertIconDirective, HlmAlertTitleDirective, HlmIconComponent],
  providers: [provideIcons({lucideInfo})],
  templateUrl: './module-version-edit.component.html'
})
export class ModuleVersionEditComponent {
  proposalForm: FormGroup;
  loading: boolean = false;
  error: string | null = null;
  moduleVersionId: number | null = null;
  moduleVersionDto: ModuleVersionUpdateRequestDTO | null = null;
  moduleVersionService: ModuleVersionControllerService = inject(ModuleVersionControllerService);

  constructor(private formBuilder: FormBuilder, route: ActivatedRoute, private router: Router) {
    this.moduleVersionId = Number(route.snapshot.paramMap.get('id'));
    this.proposalForm = this.formBuilder.group({
      titleEng: ['', Validators.required],
      levelEng: [''],
      languageEng: [''],
      repetitionEng: [''],
      frequencyEng: [''],
      credits: [null],
      hoursTotal: [null],
      hoursSelfStudy: [null],
      hoursPresence: [null],
      examinationAchievementsEng: [''],
      recommendedPrerequisitesEng: [''],
      contentEng: [''],
      learningOutcomesEng: [''],
      teachingMethodsEng: [''],
      mediaEng: [''],
      literatureEng: [''],
      responsiblesEng: [''],
      lvSwsLecturerEng: ['']
    });
    this.fetchModuleVersion(this.moduleVersionId);
  }

  async fetchModuleVersion(moduleVersionId: number) {
    this.loading = true;
    this.moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId).subscribe({
      next: (response: ModuleVersionUpdateRequestDTO) => {
        this.proposalForm.patchValue(response);
        this.moduleVersionDto = response;
      },
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }

  async onSubmit() {
    if (this.proposalForm.valid) {
      this.loading = true;
      this.error = null;

      const proposalData: ModuleVersionUpdateRequestDTO = {
        ...this.proposalForm.value,
        moduleVersionId: this.moduleVersionId
      };
      this.moduleVersionService.updateModuleVersion(this.moduleVersionId!, proposalData).subscribe({
        next: (response: ModuleVersionUpdateResponseDTO) => {
          this.proposalForm.reset();
          this.router.navigate(['/proposals/view', response.proposalId], {
            queryParams: { created: true }
          });
        },
        error: (err: HttpErrorResponse) => {
          console.log(err);
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