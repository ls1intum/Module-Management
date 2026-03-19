import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { ModuleVersionControllerService, ModuleVersionViewDTO, ModuleVersionViewFeedbackDTO } from '../../core/modules/openapi';
import { BreadcrumbLabelsService } from '../../components/breadcrumb/breadcrumb-labels.service';
import { ModuleEditStepperComponent } from '../../components/module-edit-stepper/module-edit-stepper.component';
import { MODULE_EDIT_STEPS, StepperStatus } from '../../components/module-edit-stepper/module-edit-steps.config';
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
    ModuleEditStepperComponent,
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
  templateUrl: './module-version-view.component.html',
  styleUrl: './module-version-view.component.css'
})
export class ModuleVersionViewComponent {
  route = inject(ActivatedRoute);
  moduleVersionService = inject(ModuleVersionControllerService);
  private breadcrumbLabels = inject(BreadcrumbLabelsService);
  proposalId: string | null = null;
  moduleVersionId: number | null = null;
  loading = signal(true);
  moduleVersionDto = signal<ModuleVersionViewDTO | null>(null);
  moduleVersionStatus = ModuleVersionViewDTO.StatusEnum;
  error = signal<string | null>(null);

  readonly MODULE_EDIT_STEPS = MODULE_EDIT_STEPS;
  currentStepIndex = signal(0);

  /** Coordinator feedbacks for this version (for current assignments). From moduleVersionDto().feedbacks. */
  coordinatorFeedbacksForCurrentAssignments = computed(() => {
    const dto = this.moduleVersionDto();
    const feedbacks = dto?.feedbacks ?? [];
    const coordinator = feedbacks.filter((f) => f.feedbackRole == null);
    const specIds = new Set(dto?.degreeProgramAssignments?.map((a) => a.degreeProgramSpecializationId).filter((id): id is number => id != null) ?? []);
    if (specIds.size === 0) return coordinator;
    return coordinator.filter((f) => f.degreeProgramSpecializationId != null && specIds.has(f.degreeProgramSpecializationId));
  });

  /** Previous module version feedback (all non-invalidated for proposal). Fetched when loading; used when current version has no feedbacks yet. */
  previousVersionFeedbacks = signal<ModuleVersionViewFeedbackDTO[]>([]);

  /** Coordinator feedbacks to show in step 1: current version if any, otherwise previous version feedback (so latest MV still shows status). */
  coordinatorFeedbacksForStep1 = computed(() => {
    const fromCurrent = this.coordinatorFeedbacksForCurrentAssignments();
    if (fromCurrent.length > 0) return { feedbacks: fromCurrent, fromPrevious: false };
    const prev = this.previousVersionFeedbacks();
    const coordinator = prev.filter((f) => f.feedbackRole == null);
    const dto = this.moduleVersionDto();
    const specIds = new Set(dto?.degreeProgramAssignments?.map((a) => a.degreeProgramSpecializationId).filter((id): id is number => id != null) ?? []);
    const filtered = specIds.size === 0 ? coordinator : coordinator.filter((f) => f.degreeProgramSpecializationId != null && specIds.has(f.degreeProgramSpecializationId));
    return { feedbacks: filtered, fromPrevious: true };
  });

  moduleFields: ModuleField[] = [
    { key: 'titleEng', label: 'Title', section: 'basic', feedbackKey: 'titleFeedback' },
    { key: 'titleDe', label: 'Title (German)', section: 'basic', feedbackKey: 'titleDeFeedback' },
    { key: 'levelEng', label: 'Level', section: 'basic', feedbackKey: 'levelFeedback' },
    { key: 'languageEng', label: 'Language', section: 'basic', feedbackKey: 'languageFeedback' },
    { key: 'frequencyEng', label: 'Frequency', section: 'basic', feedbackKey: 'frequencyFeedback' },
    { key: 'credits', label: 'Credits', section: 'hours', feedbackKey: 'creditsFeedback' },
    { key: 'hoursLecture', label: 'Hours (Lecture)', section: 'basic', feedbackKey: 'hoursLectureFeedback' },
    { key: 'hoursExercise', label: 'Hours (Exercise)', section: 'basic', feedbackKey: 'hoursExerciseFeedback' },
    { key: 'hoursPractical', label: 'Hours (Practical)', section: 'basic', feedbackKey: 'hoursPracticalFeedback' },
    { key: 'hoursSeminar', label: 'Hours (Seminar)', section: 'basic', feedbackKey: 'hoursSeminarFeedback' },
    { key: 'firstSemesterAvailable', label: 'First semester available', section: 'basic', feedbackKey: 'firstSemesterAvailableFeedback' },
    { key: 'successorModuleName', label: 'Successor module', section: 'basic', feedbackKey: 'successorModuleNameFeedback' },
    { key: 'duration', label: 'Duration', section: 'basic', feedbackKey: 'durationFeedback' },
    { key: 'repetitionEng', label: 'Repetition', section: 'basic', feedbackKey: 'repetitionFeedback' },
    { key: 'hoursTotal', label: 'Total Hours', section: 'hours', feedbackKey: 'hoursTotalFeedback' },
    { key: 'hoursSelfStudy', label: 'Self-Study Hours', section: 'hours', feedbackKey: 'hoursSelfStudyFeedback' },
    { key: 'hoursPresence', label: 'Presence Hours', section: 'hours', feedbackKey: 'hoursPresenceFeedback' },
    { key: 'bulletPoints', label: 'Key Points', section: 'content', isLongText: true, feedbackKey: 'bulletPointsFeedback' },
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

  stepStatuses = computed(() => {
    const dto = this.moduleVersionDto();
    if (!dto) return MODULE_EDIT_STEPS.map(() => StepperStatus.Default);

    return MODULE_EDIT_STEPS.map((step, index) => {
      if (step.id === 'feedbacks') return StepperStatus.Default;
      if (step.id === 'submit-coordinator-feedback') {
        const feedbacks = this.coordinatorFeedbacksForStep1().feedbacks;
        if (feedbacks.length === 0) return StepperStatus.Default;
        const hasRejection = feedbacks.some((fb) => fb.feedbackStatus === ModuleVersionViewFeedbackDTO.FeedbackStatusEnum.Rejected);
        if (hasRejection) return StepperStatus.ActionRequired;
        const allApproved = feedbacks.every((fb) => fb.feedbackStatus === ModuleVersionViewFeedbackDTO.FeedbackStatusEnum.Approved);
        return allApproved ? StepperStatus.Completed : StepperStatus.Pending;
      }

      if (step.id === 'submit-full-feedback') return StepperStatus.Default;

      const keys = MODULE_EDIT_STEPS[index].controlNames as (keyof ModuleVersionViewDTO)[];
      if (!keys?.length) return StepperStatus.Default;
      const allFilled = keys.every((k) => {
        const v = dto[k];
        if (v === undefined || v === null) return false;
        if (typeof v === 'string') return v.trim() !== '';
        if (typeof v === 'number') return true;
        return true;
      });
      return allFilled ? StepperStatus.Completed : StepperStatus.Default;
    });
  });

  constructor() {
    const params = this.route.snapshot.paramMap;
    this.proposalId = params.get('id');
    const versionId = params.get('versionId');
    this.moduleVersionId = Number(versionId);
    this.fetchModuleVersionViewDto(this.moduleVersionId);
    this.fetchPreviousVersionsFeedbacks(this.moduleVersionId);
  }

  goToStep(index: number) {
    this.currentStepIndex.set(index);
  }

  getFieldsForViewStep(stepIndex: number): ModuleField[] {
    const keys = MODULE_EDIT_STEPS[stepIndex].controlNames as (keyof ModuleVersionViewDTO)[];
    if (!keys?.length) return [];
    return this.moduleFields.filter((f) => keys.includes(f.key));
  }

  private fetchModuleVersionViewDto(moduleVersionId: number) {
    this.loading.set(true);
    this.moduleVersionService.getModuleVersion(moduleVersionId).subscribe({
      next: (data: ModuleVersionViewDTO) => {
        this.moduleVersionDto.set(data);
        const version = data?.version;
        this.breadcrumbLabels.proposalTitle.set(data?.titleEng ?? null);
        this.breadcrumbLabels.versionLabel.set(version != null ? `Version ${version}` : null);
      },
      error: (err: HttpErrorResponse) => this.error.set(err.error),
      complete: () => this.loading.set(false)
    });
  }

  private fetchPreviousVersionsFeedbacks(moduleVersionId: number) {
    this.moduleVersionService.getPreviousModuleVersionFeedback(moduleVersionId).subscribe({
      next: (list) => this.previousVersionFeedbacks.set(list)
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
