import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProposalControllerService, ProposalRequestDTO } from '../../core/modules/openapi';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
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
  templateUrl: './proposal-create.component.html'
})
export class ProposalsCreateComponent {
  proposalService = inject(ProposalControllerService);
  proposalForm: FormGroup;
  loading: boolean = false;
  error: string | null = null;

  constructor(private formBuilder: FormBuilder, private router: Router) {
    this.proposalForm = this.formBuilder.group({
      titleEng: ['', Validators.required],
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
  }

  async onSubmit() {
    if (this.proposalForm.valid) {
      this.loading = true;
      this.error = null;

      const proposalData = this.proposalForm.value;
      this.proposalService.createProposal(proposalData).subscribe({
        next: (response) => {
          this.proposalForm.reset();
          this.router.navigate(['/proposals/view', response.proposalId], {
            queryParams: { created: true }
          });
        },
        error: (err: HttpErrorResponse) => {
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
