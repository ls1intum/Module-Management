import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  CompletionServiceRequestDTO,
  ModuleVersionControllerService,
  ProposalControllerService,
  ModuleVersionUpdateRequestDTO,
  SimilarModulesDTO,
  CompletionServiceResponseDTO,
  OverlapDetectionRequestDTO,
  ModuleVersionViewFeedbackDTO
} from '../../core/modules/openapi';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { HlmSeparatorDirective, HlmSeparatorModule } from '@spartan-ng/ui-separator-helm';
import { BrnSeparatorModule } from '@spartan-ng/ui-separator-brain';
import { BrnSelectComponent } from '@spartan-ng/ui-select-brain';

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

  protected async checkSimilarity() {
    const data: OverlapDetectionRequestDTO = {
      ...this.proposalForm.value
    };

    this.moduleVersionService.checkSimilarity(data).subscribe({
      next: (response: SimilarModulesDTO) => console.log(response.similarModules),
      error: (err: HttpErrorResponse) => console.log(err)
    });

    return;
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
