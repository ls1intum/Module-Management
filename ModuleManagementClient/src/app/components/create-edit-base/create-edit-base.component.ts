import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  CompletionServiceRequestDTO,
  ModuleVersionControllerService,
  ProposalControllerService,
  ModuleVersionUpdateRequestDTO,
  SimilarModulesDTO,
  CompletionServiceResponseDTO,
  OverlapDetectionRequestDTO
} from '../../core/modules/openapi';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({ template: '' })
export abstract class ProposalBaseComponent {
  protected formBuilder = inject(FormBuilder);
  protected router = inject(Router);
  protected moduleVersionService = inject(ModuleVersionControllerService);
  protected proposalService = inject(ProposalControllerService);

  proposalForm: FormGroup;
  loading: boolean = false;
  error: string | null = null;
  moduleVersionDto: ModuleVersionUpdateRequestDTO | null = null;

  showPrompt: { [key: string]: boolean } = {
    examination: false,
    content: false,
    learning: false,
    teaching: false,
    media: false
  };

  promptFieldMapping: { [key: string]: string } = {
    examination: 'examinationAchievementsPromptEng',
    content: 'contentPromptEng',
    learning: 'learningOutcomesPromptEng',
    teaching: 'teachingMethodsPromptEng',
    media: 'mediaPromptEng'
  };

  fieldMapping: { [key: string]: string } = {
    content: 'contentEng',
    examination: 'examinationAchievementsEng',
    learning: 'learningOutcomesEng',
    teaching: 'teachingMethodsEng',
    media: 'mediaEng'
  };

  togglePromptField(field: string) {
    this.showPrompt[field] = !this.showPrompt[field];
  }

  constructor() {
    this.proposalForm = this.formBuilder.group({
      bulletPoints: [''],
      examinationPrompt: [''],
      contentPrompt: [''],
      learningPrompt: [''],
      teachingPrompt: [''],
      mediaPrompt: [''],
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
      recommendedPrerequisitesPromptEng: [''],
      contentEng: [''],
      contentPromptEng: [''],
      learningOutcomesEng: [''],
      learningOutcomesPrompotEng: [''],
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

  protected async generateField(field: string) {
    this.loading = true;

    const proposalData: CompletionServiceRequestDTO = {
      bulletPoints: this.proposalForm.get('bulletPoints')?.value || 'New Module',
      contextPrompt: this.proposalForm.get(this.promptFieldMapping[field])?.value || '',
      ...this.proposalForm.value
    };

    let response;
    switch (field) {
      case 'content':
        response = this.moduleVersionService.generateContent(proposalData);
        break;
      case 'examination':
        response = this.moduleVersionService.generateExaminationAchievements(proposalData);
        break;
      case 'learning':
        response = this.moduleVersionService.generateLearningOutcomes(proposalData);
        break;
      case 'teaching':
        response = this.moduleVersionService.generateTeachingMethods(proposalData);
        break;
    }

    response!.subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({
          [this.fieldMapping[field]]: response!.responseData
        });
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
