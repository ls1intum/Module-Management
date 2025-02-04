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
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { HlmSeparatorModule } from '@spartan-ng/ui-separator-helm';
import { BrnSeparatorModule } from '@spartan-ng/ui-separator-brain';

@Component({
  template: '',
  imports: [BrnSeparatorModule, HlmSeparatorModule]
})
export abstract class ProposalBaseComponent {
  protected formBuilder = inject(FormBuilder);
  protected router = inject(Router);
  protected moduleVersionService = inject(ModuleVersionControllerService);
  protected proposalService = inject(ProposalControllerService);

  proposalForm: FormGroup;
  loading: boolean = false;
  error: string | null = null;
  moduleVersionDto: ModuleVersionUpdateRequestDTO | null = null;
  moduleVersionId: number | null = null;
  rejectionFeedbacks: ModuleVersionViewFeedbackDTO[] = [];

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

  private getCompletionServiceRequestDTO(field: string): CompletionServiceRequestDTO {
    const promptFieldMapping: { [key: string]: string } = {
      examination: 'examinationAchievementsPromptEng',
      content: 'contentPromptEng',
      learning: 'learningOutcomesPromptEng',
      teaching: 'teachingMethodsPromptEng'
    };

    const data: CompletionServiceRequestDTO = {
      bulletPoints: this.proposalForm.get('bulletPoints')?.value || 'New Module',
      contextPrompt: this.proposalForm.get(promptFieldMapping[field])?.value || '',
      ...this.proposalForm.value
    };
    return data;
  }

  protected async generateContent() {
    this.loading = true;
    const data = this.getCompletionServiceRequestDTO('content');
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
    const data = this.getCompletionServiceRequestDTO('examinationAchievementsEng');
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
    const data = this.getCompletionServiceRequestDTO('learningOutcomesEng');
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
    const data = this.getCompletionServiceRequestDTO('teachingMethodsEng');
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
