import { Component, computed, effect, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  CompletionServiceRequestDTO,
  DegreeProgramDTO,
  ModuleVersionControllerService,
  ProposalControllerService,
  ModuleVersionViewDTO,
  CompletionServiceResponseDTO,
  ModuleVersionViewFeedbackDTO,
  ProposalViewDTO
} from '../../core/modules/openapi';
import { DegreeProgramsControllerService } from '../../core/modules/openapi/api/degree-programs-controller.service';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MODULE_EDIT_STEPS, StepperStatus } from '../module-edit-stepper/module-edit-steps.config';
import { BreadcrumbLabelsService } from '../breadcrumb/breadcrumb-labels.service';

@Component({
  template: ''
})
export abstract class ProposalBaseComponent {
  protected formBuilder = inject(FormBuilder);
  protected router = inject(Router);
  protected location = inject(Location);
  protected moduleVersionService = inject(ModuleVersionControllerService);
  protected proposalService = inject(ProposalControllerService);
  protected degreeProgramsService = inject(DegreeProgramsControllerService);
  protected breadcrumbLabels = inject(BreadcrumbLabelsService);

  readonly MODULE_EDIT_STEPS = MODULE_EDIT_STEPS;

  proposalForm: FormGroup;
  loading = signal(false);
  loadingPrograms = signal(true);
  error = signal<string | null>(null);
  moduleVersionDto = signal<ModuleVersionViewDTO | null>(null);
  moduleVersionId: number | null = null;
  feedbacks = signal<ModuleVersionViewFeedbackDTO[] | undefined>([]);

  degreePrograms = signal<DegreeProgramDTO[]>([]);
  assignments = signal<{ degreeProgramId: number | null; degreeProgramSpecializationId: number | null }[]>([]);

  isCreateMode = computed(() => this.moduleVersionId == null);
  currentStepIndex = signal(0);

  /** Updated on form valueChanges so stepCompleted computed re-runs when user types. */
  private formValueVersion = signal(0);

  stepsStatuses = computed(() => {
    this.formValueVersion();
    const form = this.proposalForm;
    const assignmentsList = this.assignments();
    return MODULE_EDIT_STEPS.map((step) => {
      if (step.id === 'basic') {
        const allFieldsFilled = step.controlNames.every((name) => this.controlHasValue(form.get(name)));
        const hasCompleteAssignment = assignmentsList.some((a) => a.degreeProgramId != null && a.degreeProgramSpecializationId != null);
        if (allFieldsFilled && hasCompleteAssignment) {
          return StepperStatus.Completed;
        } else {
          return StepperStatus.Default;
        }
      }
      if (step.id === 'submit-coordinator-feedback') {
        const feedbacks = this.coordinatorFeedbacksForStep1().feedbacks;
        if (feedbacks.length === 0) return StepperStatus.Default;
        const pending = ModuleVersionViewFeedbackDTO.FeedbackStatusEnum.PendingFeedback;
        const approved = ModuleVersionViewFeedbackDTO.FeedbackStatusEnum.Approved;
        const rejected = ModuleVersionViewFeedbackDTO.FeedbackStatusEnum.Rejected;
        if (feedbacks.some((fb) => fb.feedbackStatus === rejected)) return StepperStatus.Rejected;
        if (feedbacks.some((fb) => (fb.feedbackStatus ?? pending) === pending)) return StepperStatus.Pending;
        if (feedbacks.every((fb) => fb.feedbackStatus === approved)) return StepperStatus.Completed;
        return StepperStatus.FeedbackGiven;
      }

      if (step.id === 'submit-full-feedback') {
        return StepperStatus.Default;
      }
      return step.controlNames.every((name) => this.controlHasValue(form.get(name))) ? StepperStatus.Completed : StepperStatus.Default;
    });
  });

  private controlHasValue(c: ReturnType<FormGroup['get']>): boolean {
    if (!c) return false;
    const v = c.value;
    if (v === undefined || v === null) return false;
    if (typeof v === 'string') return v.trim() !== '';
    if (typeof v === 'number') return true;
    return true;
  }

  moduleVersionStatus = computed(() => {
    const dto = this.moduleVersionDto();
    return dto && 'status' in dto ? (dto as ModuleVersionViewDTO).status : undefined;
  });

  /** First step (basic + assignments) is complete. */
  isFirstStepComplete = computed(() => this.stepsStatuses()[0] === StepperStatus.Completed);

  /** Can submit for feedback (never submitted yet). */
  canRequestCoordinatorsFeedback = computed(() => {
    const status = this.moduleVersionStatus();
    return status === 'PENDING_FIRST_SUBMISSION' && this.isFirstStepComplete();
  });

  /** Can submit for full feedback (second submission): PENDING_FULL_SUBMISSION, all steps done, all coordinator feedback accepted. */
  canRequestFullFeedback = computed(() => {
    return (
      this.moduleVersionStatus() === 'PENDING_FULL_SUBMISSION' &&
      this.stepsStatuses()
        .slice(0, 6)
        .every((s) => s === StepperStatus.Completed)
    );
  });

  /** Coordinator feedbacks for this version (for current assignments). From moduleVersionDto().feedbacks. */
  coordinatorFeedbacksForCurrentAssignmentsFromDto = computed(() => {
    const dto = this.moduleVersionDto();
    const feedbacks = (dto as ModuleVersionViewDTO)?.feedbacks ?? [];
    const coordinator = feedbacks.filter((f) => f.feedbackRole == null);
    const specIds = new Set((dto as ModuleVersionViewDTO)?.degreeProgramAssignments?.map((a) => a.degreeProgramSpecializationId).filter((id): id is number => id != null) ?? []);
    if (specIds.size === 0) return coordinator;
    return coordinator.filter((f) => f.degreeProgramSpecializationId != null && specIds.has(f.degreeProgramSpecializationId));
  });

  /** Coordinator feedbacks to show in step 1: current version if any, otherwise previous version (feedbacks() from API). */
  coordinatorFeedbacksForStep1 = computed(() => {
    const fromDto = this.coordinatorFeedbacksForCurrentAssignmentsFromDto();
    if (fromDto.length > 0) return { feedbacks: fromDto, fromPrevious: false };
    const prev = this.feedbacks() ?? [];
    const coordinator = prev.filter((f) => f.feedbackRole == null);
    const dto = this.moduleVersionDto() as ModuleVersionViewDTO | null;
    const specIds = new Set(dto?.degreeProgramAssignments?.map((a) => a.degreeProgramSpecializationId).filter((id): id is number => id != null) ?? []);
    const filtered = specIds.size === 0 ? coordinator : coordinator.filter((f) => f.degreeProgramSpecializationId != null && specIds.has(f.degreeProgramSpecializationId));
    return { feedbacks: filtered, fromPrevious: true };
  });

  showPrompt: { [key: string]: boolean } = {
    examination: false,
    content: false,
    learning: false,
    teaching: false
  };

  togglePromptField(field: string) {
    this.showPrompt[field] = !this.showPrompt[field];
  }

  goBack(): void {
    this.location.back();
  }

  private destroyRef = inject(DestroyRef);

  constructor() {
    this.proposalForm = this.formBuilder.group({
      bulletPoints: [''],
      titleEng: ['', Validators.required],
      titleDe: [''],
      levelEng: [''],
      languageEng: ['English'],
      repetitionEng: [''],
      frequencyEng: [''],
      credits: [null],
      hoursLecture: [null],
      hoursExercise: [null],
      hoursPractical: [null],
      hoursSeminar: [null],
      firstSemesterAvailable: [''],
      successorModuleName: [''],
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
    this.proposalForm.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.formValueVersion.update((n) => n + 1));
  }

  goToStep(index: number) {
    this.currentStepIndex.set(index);
  }

  loadDegreePrograms() {
    this.loadingPrograms.set(true);
    this.degreeProgramsService.getDegreeProgramsWithSpecializations().subscribe({
      next: (list) => this.degreePrograms.set(list ?? []),
      error: () => this.degreePrograms.set([]),
      complete: () => this.loadingPrograms.set(false)
    });
  }

  addAssignment() {
    this.assignments.update((a) => [...a, { degreeProgramId: null, degreeProgramSpecializationId: null }]);
  }

  removeAssignment(index: number) {
    this.assignments.update((a) => a.filter((_, i) => i !== index));
  }

  setAssignmentProgram(index: number, degreeProgramId: number | null) {
    this.assignments.update((a) => {
      const next = [...a];
      next[index] = { ...next[index], degreeProgramId, degreeProgramSpecializationId: null };
      return next;
    });
  }

  setAssignmentSpecialization(index: number, degreeProgramSpecializationId: number | null) {
    this.assignments.update((a) => {
      const next = [...a];
      next[index] = { ...next[index], degreeProgramSpecializationId };
      return next;
    });
  }

  degreeProgramsAvailableForRow(rowIndex: number): DegreeProgramDTO[] {
    const selected = this.assignments()
      .map((a) => a.degreeProgramId)
      .filter(Boolean) as number[];
    return this.degreePrograms().filter((p) => {
      const current = this.assignments()[rowIndex]?.degreeProgramId;
      return !selected.includes(p.degreeProgramId) || p.degreeProgramId === current;
    });
  }

  specializationsForProgram(degreeProgramId: number | null) {
    if (!degreeProgramId) return [];
    const program = this.degreePrograms().find((p) => p.degreeProgramId === degreeProgramId);
    return program?.degreeProgramSpecializations ?? [];
  }

  onProgramChange(rowIndex: number) {
    this.setAssignmentSpecialization(rowIndex, null);
  }

  requestCoordinatorsFeedback(): void {
    const dto = this.moduleVersionDto();
    const proposalId = dto && 'proposalId' in dto ? (dto as ModuleVersionViewDTO).proposalId : null;
    if (proposalId == null) return;
    this.loading.set(true);
    this.error.set(null);
    this.proposalService.requestCoordinatorsFeedback(proposalId).subscribe({
      next: (response: ProposalViewDTO) => {
        this.moduleVersionDto.set(response);
        // When backend created a new version (immutable versioning), switch to editing the new version
        const newId = response?.latestModuleVersion?.moduleVersionId;
        if (newId != null && newId !== this.moduleVersionId) {
          this.moduleVersionService.getPreviousModuleVersionFeedback(newId).subscribe({
            next: (feedbacks) => this.feedbacks.set([...feedbacks]),
            error: (err: HttpErrorResponse) => this.error.set(err.error)
          });

          this.moduleVersionId = newId;
          this.breadcrumbLabels.versionLabel.set(response?.latestVersion != null ? `Version ${response.latestVersion}` : null);
          this.router.navigate(['/proposals', proposalId, 'version', newId, 'edit'], { replaceUrl: true });
        }
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? err.error ?? 'Failed to submit');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }

  requestFullFeedback(): void {
    const dto = this.moduleVersionDto();
    const proposalId = dto && 'proposalId' in dto ? (dto as ModuleVersionViewDTO).proposalId : null;
    if (proposalId == null) return;
    this.loading.set(true);
    this.error.set(null);
    this.proposalService.requestFullFeedback(proposalId).subscribe({
      next: (response: ProposalViewDTO) => {
        this.moduleVersionDto.set(response);
        // When backend created a new version (immutable versioning), switch to editing the new version
        const newId = response?.latestModuleVersion?.moduleVersionId;
        if (newId != null && newId !== this.moduleVersionId) {
          // Refresh feedbacks so the next step reflects statuses from the freezed version.
          this.moduleVersionService.getPreviousModuleVersionFeedback(newId).subscribe({
            next: (feedbacks) => this.feedbacks.set([...feedbacks]),
            error: (err: HttpErrorResponse) => this.error.set(err.error)
          });

          this.moduleVersionId = newId;
          this.breadcrumbLabels.versionLabel.set(response?.latestVersion != null ? `Version ${response.latestVersion}` : null);
          this.router.navigate(['/proposals', proposalId, 'version', newId, 'edit'], { replaceUrl: true });
        }
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err.error?.message ?? err.error ?? 'Failed to submit for full feedback');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
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
    return this.feedbacks()?.some((feedback) => feedback[field]) || false;
  }

  protected async generateContent() {
    this.loading.set(true);
    const data = this.getCompletionServiceRequestDTO('contentPromptEng');
    this.moduleVersionService.generateContent(data).subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({ contentEng: response.responseData });
      },
      error: (err: HttpErrorResponse) => console.log(err.error),
      complete: () => this.loading.set(false)
    });
  }

  protected async generateExaminationAchievements() {
    this.loading.set(true);
    const data = this.getCompletionServiceRequestDTO('examinationAchievementsPromptEng');
    this.moduleVersionService.generateExaminationAchievements(data).subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({ examinationAchievementsEng: response.responseData });
      },
      error: (err: HttpErrorResponse) => console.log(err.error),
      complete: () => this.loading.set(false)
    });
  }

  protected async generateLearningOutcomes() {
    this.loading.set(true);
    const data = this.getCompletionServiceRequestDTO('learningOutcomesPromptEng');
    this.moduleVersionService.generateLearningOutcomes(data).subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({ learningOutcomesEng: response.responseData });
      },
      error: (err: HttpErrorResponse) => console.log(err.error),
      complete: () => this.loading.set(false)
    });
  }

  protected async generateTeachingMethods() {
    this.loading.set(true);
    const data = this.getCompletionServiceRequestDTO('teachingMethodsPromptEng');
    this.moduleVersionService.generateTeachingMethods(data).subscribe({
      next: (response: CompletionServiceResponseDTO) => {
        this.proposalForm.patchValue({ teachingMethodsEng: response.responseData });
      },
      error: (err: HttpErrorResponse) => console.log(err.error),
      complete: () => this.loading.set(false)
    });
  }

  abstract onSubmit(): void;
}
