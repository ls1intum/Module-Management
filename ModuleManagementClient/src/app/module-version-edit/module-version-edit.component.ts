import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ModuleVersionControllerService, ModuleVersionUpdateRequestDTO, UserIdDTO } from '../core/modules/openapi';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-module-version-edit',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './module-version-edit.component.html',
  styleUrl: './module-version-edit.component.css'
})
export class ModuleVersionEditComponent {
  moduleVersionForm: FormGroup;
  moduleVersionId: number | undefined;
  loading: boolean = false;
  error: string | null = null;

  constructor(private formBuilder: FormBuilder, private moduleVersionService: ModuleVersionControllerService,     private route: ActivatedRoute) {
    this.moduleVersionForm = this.formBuilder.group({
      userId: [null],
      moduleVersionId: [null],
      version: [null],
      moduleId: [''],
      status: [''],
      isComplete: [false],
      titleEng: [''],
      levelEng: [''],
      languageEng: [''],
      frequencyEng: [''],
      credits: [null],
      hoursTotal: [null],
      hoursSelfStudy: [null],
      hoursPresence: [null],
      examinationAchievementsEng: [''],
      repetitionEng: [''],
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
    const userIdDto: UserIdDTO = { userId: userId };
    this.moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId, userIdDto).subscribe({
      next: (response: ModuleVersionUpdateRequestDTO) => this.moduleVersionForm.patchValue(response),
      error: (err) => this.error = err,
      complete: () => this.loading = false
    })
  }

  async onSubmit() {
    this.loading = true;
    if (this.moduleVersionForm.valid && this.moduleVersionId) {
      const moduleVersionData = this.moduleVersionForm.value;
      this.moduleVersionService.updateModuleVersion(this.moduleVersionId ,moduleVersionData).subscribe({
        next: (response) => this.moduleVersionForm.patchValue(response),
        error: (err) => this.error = err,
        complete: () => this.loading = false
      })
    }
  }
}
