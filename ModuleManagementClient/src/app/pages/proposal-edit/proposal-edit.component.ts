import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModuleVersionControllerService, ModuleVersionUpdateRequestDTO, ProposalControllerService } from '../../core/modules/openapi';
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

@Component({
  selector: 'app-proposal-create',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HlmButtonDirective, HlmFormFieldModule, HlmInputDirective, BrnSelectImports, HlmSelectImports, LayoutComponent, RouterModule],
  templateUrl: './proposal-edit.component.html'
})
export class ProposalsEditComponent {
  proposalForm: FormGroup;
  loading: boolean = false;
  error: string | null = null;
  moduleVersionId: number | null = null;
  moduleVersionDto: ModuleVersionUpdateRequestDTO | null = null;
  users = [
    { id: 1, name: 'Professor1' },
    { id: 2, name: 'Professor2' }
  ];

  constructor(private formBuilder: FormBuilder, private moduleVersionService: ModuleVersionControllerService, private route: ActivatedRoute, private router: Router) {
    this.proposalForm = this.formBuilder.group({
      userId: [null],
      moduleVersionId: [null],
      titleEng: [''],
      levelEng: [''],
      languageEng: ['undefined'],
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
    this.moduleVersionId = Number(route.snapshot.paramMap.get('id'));
    this.fetchModuleVersion(this.moduleVersionId);
  }

  async fetchModuleVersion(moduleVersionId: number) {
    this.loading = true;
    // TODO REMOVE AND DO PROPERLY
    const userId = 1;
    this.moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId, userId).subscribe({
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

      const proposalData = this.proposalForm.value;
      this.moduleVersionService.updateModuleVersion(this.moduleVersionId!, proposalData).subscribe({
        next: (response) => {
          this.proposalForm.reset();
          this.router.navigate(['/proposals/view', response.moduleVersionId], {
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
