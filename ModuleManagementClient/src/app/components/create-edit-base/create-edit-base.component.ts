import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CompletionServiceRequestDTO, ModuleVersionControllerService, ProposalControllerService, ModuleVersionUpdateRequestDTO } from '../../core/modules/openapi';
import { Router } from '@angular/router';

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
      mediaPromptEng: [''],
      literatureEng: [''],
      responsiblesEng: [''],
      lvSwsLecturerEng: ['']
    });
  }

  protected async generateField(field: string) {
    const promptFieldMapping: { [key: string]: string } = {
      'examination': 'examinationAchievementsPromptEng',
      'content': 'contentPromptEng',
      'learning': 'learningOutcomesPromptEng',
      'teaching': 'teachingMethodsPromptEng',
      'media': 'mediaPromptEng'
    };

    const proposalData: CompletionServiceRequestDTO = {
      bulletPoints: this.proposalForm.get('bulletPoints')?.value || 'New Module',
      contextPrompt: this.proposalForm.get(promptFieldMapping[field])?.value || '',
      ...this.proposalForm.value
    };
    this.loading = true;
    try {
      let response;
      switch(field) {
        case 'content':
          response = await this.moduleVersionService.generateContent(proposalData).toPromise();
          break;
        case 'examination':
          response = await this.moduleVersionService.generateExaminationAchievements(proposalData).toPromise();
          break;
        case 'learning':
          response = await this.moduleVersionService.generateLearningOutcomes(proposalData).toPromise();
          break;
        case 'teaching':
          response = await this.moduleVersionService.generateTeachingMethods(proposalData).toPromise();
          break;
        case 'media':
          response = await this.moduleVersionService.generateMedia(proposalData).toPromise();
          break;
      }
      
      if (response?.responseData) {
        const fieldMapping: { [key: string]: string } = {
          'content': 'contentEng',
          'examination': 'examinationAchievementsEng',
          'learning': 'learningOutcomesEng',
          'teaching': 'teachingMethodsEng',
          'media': 'mediaEng'
        };
        
        this.proposalForm.patchValue({
          [fieldMapping[field]]: response.responseData
        });
      }
    } catch (err: any) {
      this.error = err.error;
    } finally {
      this.loading = false;
    }
  }

  abstract onSubmit(): void;
}