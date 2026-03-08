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
  ModuleVersionViewFeedbackDTO
} from '../../core/modules/openapi';
import { DegreeProgramsControllerService } from '../../core/modules/openapi/api/degree-programs-controller.service';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MODULE_EDIT_STEPS } from '../module-edit-stepper/module-edit-steps.config';

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

  stepCompleted = computed(() => {
    this.formValueVersion();
    const form = this.proposalForm;
    const assignmentsList = this.assignments();
    return MODULE_EDIT_STEPS.map((step, index) => {
      const allFieldsFilled = step.controlNames.every((name) => this.controlHasValue(form.get(name)));
      if (step.id === 'basic') {
        const hasCompleteAssignment = assignmentsList.some(
          (a) => a.degreeProgramId != null && a.degreeProgramSpecializationId != null
        );
        return allFieldsFilled && hasCompleteAssignment;
      }
      return allFieldsFilled;
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

  currentVersionFeedbacks = computed(() => {
    const dto = this.moduleVersionDto();
    return dto && 'feedbacks' in dto ? ((dto as ModuleVersionViewDTO).feedbacks ?? []) : [];
  });

  canSubmitForFeedback = computed(() => {
    const dto = this.moduleVersionDto();
    const status = dto && 'status' in dto ? (dto as ModuleVersionViewDTO).status : null;
    return status === 'PENDING_SUBMISSION' && this.proposalForm.valid;
  });

  private _syncAssignmentsFromDto = effect(() => {
    const dto = this.moduleVersionDto();
    if (dto && 'degreeProgramAssignments' in dto && Array.isArray((dto as ModuleVersionViewDTO).degreeProgramAssignments)) {
      const list = (dto as ModuleVersionViewDTO).degreeProgramAssignments ?? [];
      this.assignments.set(list.map((a) => ({ degreeProgramId: a.degreeProgramId ?? null, degreeProgramSpecializationId: a.degreeProgramSpecializationId ?? null })));
    }
  });

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

  submitForFeedback(): void {
    const dto = this.moduleVersionDto();
    const proposalId = dto && 'proposalId' in dto ? (dto as ModuleVersionViewDTO).proposalId : null;
    if (proposalId == null) return;
    this.proposalService.submitProposal(proposalId).subscribe({
      next: () => this.router.navigate(['/proposals/view', proposalId]),
      error: (err: HttpErrorResponse) => this.error.set(err.error?.message ?? err.error ?? 'Failed to submit')
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
