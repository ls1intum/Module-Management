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
import {
  filterCoordinatorFeedbacksForAssignments,
  filterExaminationBoardMemberFeedbacks
} from '../module-edit-stepper/coordinator-feedback.util';
import { MODULE_EDIT_STEPS, StepperStatus } from '../module-edit-stepper/module-edit-steps.config';
import { coordinatorFeedbackStepStatus, examinationBoardFeedbackStepStatus } from '../module-edit-stepper/module-version-stepper-status.util';
import { BreadcrumbLabelsService } from '../breadcrumb/breadcrumb-labels.service';
import { MessageService } from 'primeng/api';

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
  private readonly messageService = inject(MessageService);

  readonly MODULE_EDIT_STEPS = MODULE_EDIT_STEPS;

  proposalForm: FormGroup;
  loading = signal(false);
  loadingPrograms = signal(true);
  moduleVersionDto = signal<ModuleVersionViewDTO | null>(null);
  moduleVersionId: number | null = null;
  feedbacks = signal<ModuleVersionViewFeedbackDTO[] | undefined>([]);

  degreePrograms = signal<DegreeProgramDTO[]>([]);
  assignments = signal<{ degreeProgramId: number | null; degreeProgramSpecializationId: number | null }[]>([]);

  isCreateMode = computed(() => this.moduleVersionId == null);
  currentStepIndex = signal(0);

  /** Updated on form valueChanges so stepCompleted computed re-runs when user types. */
  private formValueVersion = signal(0);

  /**
   * Per-step stepper state. Coordinator and examination-board steps use only
   * `moduleVersionStatus()` (server workflow enum; aligned with proposal status on the server).
   * Other steps depend on the form and/or assignments.
   */
  stepsStatuses = computed(() => {
    this.formValueVersion();
    const form = this.proposalForm;
    const assignmentsList = this.assignments();
    const mvStatus = this.moduleVersionStatus();
    return MODULE_EDIT_STEPS.map((step) => {
      if (step.id === 'submit-coordinator-feedback') {
        return coordinatorFeedbackStepStatus(mvStatus);
      }
      if (step.id === 'submit-examination-board-feedback') {
        return examinationBoardFeedbackStepStatus(mvStatus);
      }
      if (step.id === 'basic') {
        const allFieldsFilled = step.controlNames.every((name) => this.controlHasValue(form.get(name)));
        const hasCompleteAssignment = assignmentsList.some((a) => a.degreeProgramId != null && a.degreeProgramSpecializationId != null);
        if (allFieldsFilled && hasCompleteAssignment) {
          return StepperStatus.Completed;
        } else {
          return StepperStatus.Default;
        }
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
    return status === 'WAITING_FOR_COORDINATORS_SUBMISSION' && this.isFirstStepComplete();
  });

  /** Steps 0–5 complete; workflow status (server-driven) implies coordinators are done. */
  canRequestExaminationBoardFeedback = computed(() => {
    const statuses = this.stepsStatuses();
    const throughContentSteps = statuses.slice(0, 6);
    return this.moduleVersionStatus() === 'WAITING_FOR_EXAMINATION_BOARD_SUBMISSION' && throughContentSteps.every((s) => s === StepperStatus.Completed);
  });

  /**
   * Coordinator feedback for step 1: current module version DTO if any rows match; otherwise
   * {@code feedbacks()} from the previous-version API (latest draft may not list rows yet).
   */
  coordinatorFeedbacksForStep1 = computed(() => {
    const dto = this.moduleVersionDto() as ModuleVersionViewDTO | null;
    const fromDto = filterCoordinatorFeedbacksForAssignments(dto?.feedbacks ?? [], dto);
    if (fromDto.length > 0) {
      return { feedbacks: fromDto, fromPrevious: false };
    }
    const fromPrev = filterCoordinatorFeedbacksForAssignments(this.feedbacks() ?? [], dto);
    return { feedbacks: fromPrev, fromPrevious: true };
  });

  /**
   * Examination-board member feedback for step 6: current DTO if listed there; else
   * {@code feedbacks()} from previous-version API (same pattern as coordinator step 1).
   */
  examinationBoardMemberFeedbacksForStep6 = computed(() => {
    const dto = this.moduleVersionDto() as ModuleVersionViewDTO | null;
    const fromDto = filterExaminationBoardMemberFeedbacks(dto?.feedbacks ?? []);
    if (fromDto.length > 0) {
      return { feedbacks: fromDto, fromPrevious: false };
    }
    const fromPrev = filterExaminationBoardMemberFeedbacks(this.feedbacks() ?? []);
    return { feedbacks: fromPrev, fromPrevious: true };
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

  /** Shows a PrimeNG toast (global {@code p-toast} in {@code AppComponent}). */
  protected showErrorAsToast(err: unknown, fallbackMessage?: string): void {
    const detail = this.resolveErrorMessage(err, fallbackMessage);
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail,
      life: 8000
    });
  }

  private resolveErrorMessage(err: unknown, fallbackMessage?: string): string {
    if (err instanceof HttpErrorResponse) {
      const fromBody = this.messageFromHttpBody(err.error);
      if (fromBody) return fromBody;
      if (err.status === 0) {
        return fallbackMessage ?? 'Network error. Check your connection.';
      }
      return fallbackMessage ?? err.message ?? `Request failed (${err.status}).`;
    }
    const direct = this.messageFromHttpBody(err);
    if (direct) return direct;
    return fallbackMessage ?? 'Something went wrong.';
  }

  private messageFromHttpBody(body: unknown): string | null {
    if (body == null || body === '') return null;
    if (typeof body === 'string') {
      const t = body.trim();
      return t.length > 0 ? t : null;
    }
    if (typeof body === 'object' && body !== null) {
      const o = body as Record<string, unknown>;
      for (const key of ['message', 'error', 'detail', 'title']) {
        const v = o[key];
        if (typeof v === 'string' && v.trim()) return v.trim();
      }
    }
    return null;
  }

  onProgramChange(rowIndex: number) {
    this.setAssignmentSpecialization(rowIndex, null);
  }

  requestExaminationBoardFeedback(): void {
    const dto = this.moduleVersionDto();
    const proposalId = dto && 'proposalId' in dto ? (dto as ModuleVersionViewDTO).proposalId : null;
    if (proposalId == null) return;
    this.loading.set(true);
    this.proposalService.requestExaminationBoardFeedback(proposalId).subscribe({
      next: (response: ProposalViewDTO) => {
        this.moduleVersionDto.set(response);
        const newId = response?.latestModuleVersion?.moduleVersionId;
        if (newId != null && newId !== this.moduleVersionId) {
          this.moduleVersionService.getPreviousModuleVersionFeedback(newId).subscribe({
            next: (feedbacks) => this.feedbacks.set([...feedbacks]),
            error: (err: HttpErrorResponse) => this.showErrorAsToast(err)
          });
          this.moduleVersionId = newId;
          this.breadcrumbLabels.versionLabel.set(response?.latestVersion != null ? `Version ${response.latestVersion}` : null);
          this.router.navigate(['/proposals', proposalId, 'version', newId, 'edit'], { replaceUrl: true });
        }
      },
      error: (err: HttpErrorResponse) => {
        this.showErrorAsToast(err, 'Failed to submit for examination board feedback');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }

  requestCoordinatorsFeedback(): void {
    const dto = this.moduleVersionDto();
    const proposalId = dto && 'proposalId' in dto ? (dto as ModuleVersionViewDTO).proposalId : null;
    if (proposalId == null) return;
    this.loading.set(true);
    this.proposalService.requestCoordinatorsFeedback(proposalId).subscribe({
      next: (response: ProposalViewDTO) => {
        this.moduleVersionDto.set(response);
        // When backend created a new version (immutable versioning), switch to editing the new version
        const newId = response?.latestModuleVersion?.moduleVersionId;
        if (newId != null && newId !== this.moduleVersionId) {
          this.moduleVersionService.getPreviousModuleVersionFeedback(newId).subscribe({
            next: (feedbacks) => this.feedbacks.set([...feedbacks]),
            error: (err: HttpErrorResponse) => this.showErrorAsToast(err)
          });

          this.moduleVersionId = newId;
          this.breadcrumbLabels.versionLabel.set(response?.latestVersion != null ? `Version ${response.latestVersion}` : null);
          this.router.navigate(['/proposals', proposalId, 'version', newId, 'edit'], { replaceUrl: true });
        }
      },
      error: (err: HttpErrorResponse) => {
        this.showErrorAsToast(err, 'Failed to submit');
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
      error: (err: HttpErrorResponse) => this.showErrorAsToast(err, 'Failed to generate content'),
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
      error: (err: HttpErrorResponse) => this.showErrorAsToast(err, 'Failed to generate examination achievements'),
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
      error: (err: HttpErrorResponse) => this.showErrorAsToast(err, 'Failed to generate learning outcomes'),
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
      error: (err: HttpErrorResponse) => this.showErrorAsToast(err, 'Failed to generate teaching methods'),
      complete: () => this.loading.set(false)
    });
  }

  abstract onSubmit(): void;
}
