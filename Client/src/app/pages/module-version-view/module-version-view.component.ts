import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { ModuleVersionControllerService, ModuleVersionViewDTO, ModuleVersion, ModuleVersionViewFeedbackDTO } from '../../core/modules/openapi';
import { FeedbackDepartmentPipe } from '../../pipes/feedbackDepartment.pipe';
import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { ModuleVersionStatusPipe } from '../../pipes/moduleVersionStatus.pipe';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MessageModule } from 'primeng/message';
import { CardModule } from 'primeng/card';
import { PanelModule } from 'primeng/panel';

export interface ModuleField {
  key: keyof ModuleVersionViewDTO;
  label: string;
  section: 'basic' | 'hours' | 'content';
  isLongText?: boolean;
  hasPrompt?: keyof ModuleVersionViewDTO;
  feedbackKey?: string;
}

@Component({
  selector: 'module-version-view',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ModuleVersionStatusPipe,
    FeedbackStatusPipe,
    FeedbackDepartmentPipe,
    ButtonModule,
    TagModule,
    ProgressSpinnerModule,
    MessageModule,
    CardModule,
    PanelModule
  ],
  templateUrl: './module-version-view.component.html'
})
export class ModuleVersionViewComponent {
  route = inject(ActivatedRoute);
  moduleVersionService = inject(ModuleVersionControllerService);
  proposalId: string | null = null;
  moduleVersionId: number | null = null;
  loading = signal(true);
  moduleVersionDto = signal<ModuleVersionViewDTO | null>(null);
  moduleVersionStatus = ModuleVersion.StatusEnum;
  error = signal<string | null>(null);

  moduleFields: ModuleField[] = [
    { key: 'titleEng', label: 'Title', section: 'basic', feedbackKey: 'titleFeedback' },
    { key: 'levelEng', label: 'Level', section: 'basic', feedbackKey: 'levelFeedback' },
    { key: 'languageEng', label: 'Language', section: 'basic', feedbackKey: 'languageFeedback' },
    { key: 'frequencyEng', label: 'Frequency', section: 'basic', feedbackKey: 'frequencyFeedback' },
    { key: 'duration', label: 'Duration', section: 'basic', feedbackKey: 'durationFeedback' },
    { key: 'repetitionEng', label: 'Repetition', section: 'basic', feedbackKey: 'repetitionFeedback' },
    { key: 'credits', label: 'Credits', section: 'hours', feedbackKey: 'creditsFeedback' },
    { key: 'hoursTotal', label: 'Total Hours', section: 'hours', feedbackKey: 'hoursTotalFeedback' },
    { key: 'hoursSelfStudy', label: 'Self-Study Hours', section: 'hours', feedbackKey: 'hoursSelfStudyFeedback' },
    { key: 'hoursPresence', label: 'Presence Hours', section: 'hours', feedbackKey: 'hoursPresenceFeedback' },
    { key: 'bulletPoints', label: 'Key Points', section: 'content', isLongText: true },
    {
      key: 'examinationAchievementsEng',
      label: 'Examination Achievements',
      section: 'content',
      isLongText: true,
      hasPrompt: 'examinationAchievementsPromptEng',
      feedbackKey: 'examinationAchievementsFeedback'
    },
    { key: 'recommendedPrerequisitesEng', label: 'Recommended Prerequisites', section: 'content', isLongText: true, feedbackKey: 'recommendedPrerequisitesFeedback' },
    { key: 'contentEng', label: 'Module Content', section: 'content', isLongText: true, hasPrompt: 'contentPromptEng', feedbackKey: 'contentFeedback' },
    {
      key: 'learningOutcomesEng',
      label: 'Learning Outcomes',
      section: 'content',
      isLongText: true,
      hasPrompt: 'learningOutcomesPromptEng',
      feedbackKey: 'learningOutcomesFeedback'
    },
    { key: 'teachingMethodsEng', label: 'Teaching Methods', section: 'content', isLongText: true, hasPrompt: 'teachingMethodsPromptEng', feedbackKey: 'teachingMethodsFeedback' },
    { key: 'mediaEng', label: 'Media', section: 'content', isLongText: true, feedbackKey: 'mediaFeedback' },
    { key: 'literatureEng', label: 'Literature', section: 'content', isLongText: true, feedbackKey: 'literatureFeedback' },
    { key: 'responsiblesEng', label: 'Responsibles', section: 'content', isLongText: true, feedbackKey: 'responsiblesFeedback' },
    { key: 'lvSwsLecturerEng', label: 'Lecturer', section: 'content', isLongText: true, feedbackKey: 'lvSwsLecturerFeedback' }
  ];

  constructor() {
    const params = this.route.snapshot.paramMap;
    this.proposalId = params.get('id');
    const versionId = params.get('versionId');
    this.moduleVersionId = Number(versionId);
    this.fetchModuleVersionViewDto(this.moduleVersionId);
  }

  private fetchModuleVersionViewDto(moduleVersionId: number) {
    this.loading.set(true);
    this.moduleVersionService.getModuleVersionViewDto(moduleVersionId).subscribe({
      next: (data: ModuleVersionViewDTO) => this.moduleVersionDto.set(data),
      error: (err: HttpErrorResponse) => this.error.set(err.error),
      complete: () => this.loading.set(false)
    });
  }

  pdfExport() {
    const mvid = this.moduleVersionId;
    if (!mvid) {
      return;
    }

    this.moduleVersionService.exportProfessorModuleVersionPdf(mvid).subscribe({
      next: (response: Blob) => {
        {
          const fileName = `mv${mvid}_${this.moduleVersionDto()!.titleEng}`;
          const blob = new Blob([response], { type: 'application/pdf' });
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = fileName;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(link.href);
        }
      },
      error: (err: HttpErrorResponse) => this.error.set(err.error)
    });
  }

  getModuleVersionProperty(key: keyof ModuleVersionViewDTO): string {
    const value = this.moduleVersionDto()?.[key];
    return value == null ? '' : String(value);
  }

  getFieldsBySection(section: 'basic' | 'hours' | 'content') {
    return this.moduleFields.filter((field) => field.section === section);
  }

  isLatestVersion(): boolean {
    return this.moduleVersionDto()?.version === this.moduleVersionDto()?.latestVersion;
  }

  getFeedbackFields(feedback: ModuleVersionViewFeedbackDTO): { key: string; label: string; value: string }[] {
    const feedbackFields: { key: string; label: string; value: string }[] = [];

    for (const field of this.moduleFields) {
      if (!field.feedbackKey) continue;

      const feedbackValue = feedback[field.feedbackKey as keyof ModuleVersionViewFeedbackDTO];
      if (feedbackValue) {
        feedbackFields.push({
          key: field.feedbackKey,
          label: field.label,
          value: String(feedbackValue)
        });
      }
    }

    return feedbackFields;
  }

  getFieldFeedbacks(fieldKey: keyof ModuleVersionViewDTO): ModuleVersionViewFeedbackDTO[] {
    if (!this.moduleVersionDto()?.feedbacks) return [];

    const field = this.moduleFields.find((f) => f.key === fieldKey);
    if (!field?.feedbackKey) return [];

    return this.moduleVersionDto()!.feedbacks!.filter((feedback) => {
      const feedbackValue = feedback[field.feedbackKey as keyof ModuleVersionViewFeedbackDTO];
      return feedbackValue !== null && feedbackValue !== undefined && feedbackValue !== '';
    });
  }

  getFeedbackContent(feedback: ModuleVersionViewFeedbackDTO, fieldKey: keyof ModuleVersionViewDTO): string {
    const field = this.moduleFields.find((f) => f.key === fieldKey);
    if (!field?.feedbackKey) return '';

    return String(feedback[field.feedbackKey as keyof ModuleVersionViewFeedbackDTO] || '');
  }
}
