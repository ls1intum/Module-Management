import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProposalControllerService } from '../core/modules/openapi';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-proposal-create',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './proposal-create.component.html',
  styleUrl: './proposal-create.component.css'
})
export class ProposalsCreateComponent {
  proposalForm: FormGroup;
  loading: boolean = false;
  error: string | null = null;
  users = [
    { id: 1, name: 'Professor1' },
    { id: 2, name: 'Professor2' }
  ];

  constructor(private formBuilder: FormBuilder, private proposalService: ProposalControllerService, private router: Router) {
    this.proposalForm = this.formBuilder.group({
      userId: [null, Validators.required],
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
  }

  async onSubmit() {
    this.loading = true;
    if (this.proposalForm.valid) {
      const proposalData = this.proposalForm.value;
      this.proposalService.createProposal(proposalData).subscribe({
        next: (response) => {
          this.proposalForm.reset();
          this.router.navigate(['/proposal/view', response.proposalId], { queryParams: { created: true } });
        },
        error: (err) => this.error = err,
        complete: () => this.loading = false,
      });
    }
  }
}
