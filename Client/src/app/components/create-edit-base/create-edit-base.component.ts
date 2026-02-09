import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  CompletionServiceRequestDTO,
  ModuleVersionControllerService,
  ProposalControllerService,
  ModuleVersionUpdateRequestDTO,
  CompletionServiceResponseDTO,
  ModuleVersionViewFeedbackDTO
} from '../../core/modules/openapi';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  template: ''
})
export abstract class ProposalBaseComponent {
  protected formBuilder = inject(FormBuilder);
  protected router = inject(Router);
  protected location = inject(Location);
  protected moduleVersionService = inject(ModuleVersionControllerService);
  protected proposalService = inject(ProposalControllerService);

  proposalForm: FormGroup;
  loading: boolean = false;
  error: string | null = null;
  moduleVersionDto: ModuleVersionUpdateRequestDTO | null = null;
  moduleVersionId: number | null = null;
  feedbacks: ModuleVersionViewFeedbackDTO[] | undefined = [];

  showPrompt: { [key: string]: boolean } = {
    examination: false,
    content: false,
    learning: false,
    teaching: false
  };

  fieldMapping: { [key: string]: string } = {
    content: 'contentEng',
    examination: 'examinationAchievementsEng',
    learning: 'learningOutcomesEng',
    teaching: 'teachingMethodsEng'
  };

  togglePromptField(field: string) {
    this.showPrompt[field] = !this.showPrompt[field];
  }

  goBack(): void {
    this.location.back();
  }

  constructor() {
    this.proposalForm = this.formBuilder.group({
      bulletPoints: [''],
      titleEng: ['', Validators.required],
      levelEng: [''],
      languageEng: ['English'],
      repetitionEng: [''],
      frequencyEng: [''],
      credits: [null],
      duration: [''],
      hoursTotal: [null],
      hoursSelfStudy: [null],
      hoursPresence: [null],
      examinationAchievementsEng: [''],
      examinationAchievementsPromptEng: [''],
      recommendedPrerequisitesEng: [''],
      contentEng: [''],
      contentPromptEng: [''],
      learningOutcomesEng: [''],
      learningOutcomesPromptEng: [''],
      teachingMethodsEng: [''],
      teachingMethodsPromptEng: [''],
      mediaEng: [''],
      literatureEng: [''],
      responsiblesEng: [''],
      lvSwsLecturerEng: ['']
    });
  }

  private getCompletionServiceRequestDTO(promptFieldName: string): CompletionServiceRequestDTO {
    const data: CompletionServiceRequestDTO = {
      bulletPoints: this.proposalForm.get('bulletPoints')?.value || 'New Module',
      contextPrompt: this.proposalForm.get(promptFieldName)?.value || '',
      ...this.proposalForm.value
    };
    return data;
  }

  hasFeedback(field: keyof ModuleVersionViewFeedbackDTO): boolean {
    return this.feedbacks?.some((feedback) => feedback[field]) || false;
  }

  protected async generateContent() {
    this.loading = true;
    const data = this.getCompletionServiceRequestDTO('contentPromptEng');
    this.moduleVersionService.generateContent(data).subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({ contentEng: response.responseData });
      },
      error: (err: HttpErrorResponse) => {
        console.log(err.error);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  protected async generateExaminationAchievements() {
    this.loading = true;
    const data = this.getCompletionServiceRequestDTO('examinationAchievementsPromptEng');
    this.moduleVersionService.generateExaminationAchievements(data).subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({ examinationAchievementsEng: response.responseData });
      },
      error: (err: HttpErrorResponse) => {
        console.log(err.error);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  protected async generateLearningOutcomes() {
    this.loading = true;
    const data = this.getCompletionServiceRequestDTO('learningOutcomesPromptEng');
    this.moduleVersionService.generateLearningOutcomes(data).subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({ learningOutcomesEng: response.responseData });
      },
      error: (err: HttpErrorResponse) => {
        console.log(err.error);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  protected async generateTeachingMethods() {
    this.loading = true;
    const data = this.getCompletionServiceRequestDTO('teachingMethodsPromptEng');
    this.moduleVersionService.generateTeachingMethods(data).subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({ teachingMethodsEng: response.responseData });
      },
      error: (err: HttpErrorResponse) => {
        console.log(err.error);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  abstract onSubmit(): void;
}
