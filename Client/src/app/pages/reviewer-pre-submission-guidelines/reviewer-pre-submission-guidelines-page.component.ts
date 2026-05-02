import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ReviewerPreSubmissionGuidelineControllerService, ReviewerPreSubmissionGuidelineDto, ReviewerPreSubmissionGuidelineWriteDto, User } from '../../core/modules/openapi';
import { MODULE_VERSION_GUIDELINE_RELATED_FIELDS, labelForModuleVersionFieldKey } from '../../core/reviewer-pre-submission-guidelines/module-version-field-options';
import { REVIEWER_ROLES } from '../../core/shared/user-role.utils';
import { SecurityStore } from '../../core/security/security-store.service';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MessageService } from 'primeng/api';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { ToastModule } from 'primeng/toast';

type ReviewerGuidelineRole = (typeof REVIEWER_ROLES)[number];

const ROLE_LABELS: Partial<Record<User.RolesEnum, string>> = {
  [User.RolesEnum.QualityManagement]: 'Quality management',
  [User.RolesEnum.AcademicProgramAdvisor]: 'Academic program advisor',
  [User.RolesEnum.ExaminationBoard]: 'Examination board',
  [User.RolesEnum.ProgramCoordinator]: 'Program coordinator',
  [User.RolesEnum.SpecializationAreaCoordinator]: 'Specialization area coordinator'
};

@Component({
  selector: 'app-reviewer-pre-submission-guidelines-page',
  standalone: true,
  imports: [
    FormsModule,
    DatePipe,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    TextareaModule,
    SelectModule,
    InputNumberModule,
    CheckboxModule,
    TagModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './reviewer-pre-submission-guidelines-page.component.html'
})
export class ReviewerPreSubmissionGuidelinesPageComponent {
  private readonly guidelineApi = inject(ReviewerPreSubmissionGuidelineControllerService);
  private readonly messageService = inject(MessageService);
  readonly securityStore = inject(SecurityStore);

  guidelines = signal<ReviewerPreSubmissionGuidelineDto[]>([]);
  loading = signal(false);
  saving = signal(false);

  dialogVisible = signal(false);
  editingId = signal<number | null>(null);

  formReviewerRole = signal<ReviewerGuidelineRole | null>(null);
  formTitle = signal('');
  formContent = signal('');
  formGoodExample = signal('');
  formBadExample = signal('');
  formRelatedFieldKey = signal('');
  formSortOrder = signal(0);
  formActive = signal(true);

  readonly relatedFieldOptions = [{ value: '', label: '— None —' }, ...MODULE_VERSION_GUIDELINE_RELATED_FIELDS];

  roleSelectOptions = computed(() => {
    const roles = this.securityStore.user()?.roles ?? [];
    return REVIEWER_ROLES.filter((r) => roles.includes(r)).map((value) => ({
      value,
      label: ROLE_LABELS[value]
    }));
  });

  constructor() {
    void this.refresh();
  }

  labelForField = labelForModuleVersionFieldKey;

  roleLabel(role: ReviewerPreSubmissionGuidelineDto.ReviewerRoleEnum | undefined): string {
    if (!role) return '—';
    return ROLE_LABELS[role as User.RolesEnum] ?? String(role);
  }

  async refresh() {
    this.loading.set(true);
    try {
      const list = await firstValueFrom(this.guidelineApi.list());
      this.guidelines.set(list ?? []);
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Could not load guidelines.' });
      this.guidelines.set([]);
    } finally {
      this.loading.set(false);
    }
  }

  openCreate() {
    const opts = this.roleSelectOptions();
    if (opts.length === 0) {
      this.messageService.add({
        severity: 'warn',
        summary: 'No reviewer role',
        detail: 'Your account has no reviewer role that can own guidelines.'
      });
      return;
    }
    this.editingId.set(null);
    this.formReviewerRole.set(opts[0]!.value);
    this.formTitle.set('');
    this.formContent.set('');
    this.formGoodExample.set('');
    this.formBadExample.set('');
    this.formRelatedFieldKey.set('');
    this.formSortOrder.set(0);
    this.formActive.set(true);
    this.dialogVisible.set(true);
  }

  openEdit(row: ReviewerPreSubmissionGuidelineDto) {
    this.editingId.set(row.guidelineId ?? null);
    this.formReviewerRole.set((row.reviewerRole as ReviewerGuidelineRole | undefined) ?? null);
    this.formTitle.set(row.title ?? '');
    this.formContent.set(row.content ?? '');
    this.formGoodExample.set(row.goodExample ?? '');
    this.formBadExample.set(row.badExample ?? '');
    this.formRelatedFieldKey.set(row.relatedModuleFieldKey ?? '');
    this.formSortOrder.set(row.sortOrder ?? 0);
    this.formActive.set(row.active ?? true);
    this.dialogVisible.set(true);
  }

  closeDialog() {
    this.dialogVisible.set(false);
  }

  private buildWriteDto(): ReviewerPreSubmissionGuidelineWriteDto {
    const role = this.formReviewerRole();
    if (!role) {
      throw new Error('Reviewer role required');
    }
    const related = this.formRelatedFieldKey().trim();
    return {
      reviewerRole: role as ReviewerPreSubmissionGuidelineWriteDto.ReviewerRoleEnum,
      title: this.formTitle().trim(),
      content: this.formContent(),
      goodExample: this.formGoodExample().trim() || undefined,
      badExample: this.formBadExample().trim() || undefined,
      relatedModuleFieldKey: related.length > 0 ? related : undefined,
      sortOrder: this.formSortOrder(),
      active: this.formActive()
    };
  }

  async save() {
    const title = this.formTitle().trim();
    if (!title) {
      this.messageService.add({ severity: 'warn', summary: 'Title required', detail: 'Please enter a title.' });
      return;
    }
    if (!this.formReviewerRole()) {
      this.messageService.add({ severity: 'warn', summary: 'Role required', detail: 'Please choose a reviewer role.' });
      return;
    }
    this.saving.set(true);
    try {
      const body = this.buildWriteDto();
      const id = this.editingId();
      if (id == null) {
        await firstValueFrom(this.guidelineApi.create(body));
        this.messageService.add({ severity: 'success', summary: 'Saved', detail: 'Guideline created.' });
      } else {
        await firstValueFrom(this.guidelineApi.update(id, body));
        this.messageService.add({ severity: 'success', summary: 'Saved', detail: 'Guideline updated.' });
      }
      this.closeDialog();
      await this.refresh();
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Save failed', detail: 'Could not save guideline.' });
    } finally {
      this.saving.set(false);
    }
  }

  async remove(row: ReviewerPreSubmissionGuidelineDto) {
    const id = row.guidelineId;
    if (id == null) return;
    if (!confirm(`Delete guideline “${row.title ?? ''}”?`)) return;
    try {
      await firstValueFrom(this.guidelineApi._delete(id));
      this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Guideline removed.' });
      await this.refresh();
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Delete failed', detail: 'Could not delete guideline.' });
    }
  }
}
