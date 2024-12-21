import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CompletionServiceRequestDTO, ModuleVersionControllerService, ProposalControllerService, ModuleVersionUpdateRequestDTO } from '../../core/modules/openapi';
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
  moduleVersionDto: ModuleVersionUpdateRequestDTO | null = null;  // Added this line
  
  constructor() {
    this.proposalForm = this.formBuilder.group({
      bulletPoints: [''],
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

  protected async generateField(field: string) {
    const proposalData: CompletionServiceRequestDTO = {
      bulletPoints: this.proposalForm.get('bulletPoints')?.value || 'New Module',
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