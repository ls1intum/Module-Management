import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModuleVersion, ModuleVersionControllerService, ModuleVersionViewDTO } from '../../core/modules/openapi';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { HlmBadgeDirective } from '@spartan-ng/ui-badge-helm';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { ModuleVersionStatusPipe } from '../../pipes/moduleVersionStatus.pipe';
import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { FeedbackDepartmentPipe } from '../../pipes/feedbackDepartment.pipe';

interface ModuleField {
  key: keyof ModuleVersionViewDTO;
  label: string;
  section: 'basic' | 'hours' | 'content';
  isLongText?: boolean;
  hasPrompt?: keyof ModuleVersionViewDTO;
}

@Component({
  selector: 'module-version-view',
  standalone: true,
  imports: [CommonModule, RouterModule, HlmBadgeDirective, HlmButtonDirective, ModuleVersionStatusPipe, FeedbackStatusPipe, FeedbackDepartmentPipe],
  templateUrl: './module-version-view.component.html'
})
export class ModuleVersionViewComponent {
  route = inject(ActivatedRoute);
  moduleVerisionService = inject(ModuleVersionControllerService);
  loading = true;
  moduleVersionDto: ModuleVersionViewDTO | null = null;
  moduleVersionStatus = ModuleVersion.StatusEnum;
  error: string | null = null;

  moduleFields: ModuleField[] = [
    { key: 'titleEng', label: 'Title (EN)', section: 'basic' },
    { key: 'levelEng', label: 'Level', section: 'basic' },
    { key: 'languageEng', label: 'Language', section: 'basic' },
    { key: 'frequencyEng', label: 'Frequency', section: 'basic' },
    { key: 'duration', label: 'Duration', section: 'basic' },
    { key: 'repetitionEng', label: 'Repetition', section: 'basic' },
    { key: 'credits', label: 'Credits', section: 'hours' },
    { key: 'hoursTotal', label: 'Total Hours', section: 'hours' },
    { key: 'hoursSelfStudy', label: 'Self-Study Hours', section: 'hours' },
    { key: 'hoursPresence', label: 'Presence Hours', section: 'hours' },
    { key: 'bulletPoints', label: 'Key Points', section: 'content', isLongText: true },
    {
      key: 'examinationAchievementsEng',
      label: 'Examination Achievements',
      section: 'content',
      isLongText: true,
      hasPrompt: 'examinationAchievementsPromptEng'
    },
    { key: 'recommendedPrerequisitesEng', label: 'Recommended Prerequisites', section: 'content', isLongText: true },
    {
      key: 'contentEng',
      label: 'Module Content',
      section: 'content',
      isLongText: true,
      hasPrompt: 'contentPromptEng'
    },
    {
      key: 'learningOutcomesEng',
      label: 'Learning Outcomes',
      section: 'content',
      isLongText: true,
      hasPrompt: 'learningOutcomesPromptEng'
    },
    {
      key: 'teachingMethodsEng',
      label: 'Teaching Methods',
      section: 'content',
      isLongText: true,
      hasPrompt: 'teachingMethodsPromptEng'
    },
    {
      key: 'mediaEng',
      label: 'Media',
      section: 'content',
      isLongText: true
    },
    { key: 'literatureEng', label: 'Literature', section: 'content', isLongText: true },
    { key: 'responsiblesEng', label: 'Responsibles', section: 'content', isLongText: true },
    { key: 'lvSwsLecturerEng', label: 'Lecturer', section: 'content', isLongText: true }
  ];

  constructor() {
    const moduleVersionId = Number(this.route.snapshot.paramMap.get('id'));
    this.fetchModuleVersionViewDto(moduleVersionId);
  }

  private fetchModuleVersionViewDto(moduleVersionId: number) {
    this.loading = true;
    this.moduleVerisionService.getModuleVersionViewDto(moduleVersionId).subscribe({
      next: (data: ModuleVersionViewDTO) => (this.moduleVersionDto = data),
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }

  getModuleVersionProperty(key: keyof ModuleVersionViewDTO): string {
    const value = this.moduleVersionDto?.[key];
    return value == null ? '' : String(value);
  }

  getFieldsBySection(section: 'basic' | 'hours' | 'content') {
    return this.moduleFields.filter((field) => field.section === section);
  }

  isLatestVersion(): boolean {
    return this.moduleVersionDto?.version === this.moduleVersionDto?.latestVersion;
  }
}
