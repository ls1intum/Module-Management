import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { TextareaModule } from 'primeng/textarea';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { firstValueFrom } from 'rxjs';
import {
  AiReviewGuidelineControllerService,
  AiReviewGuidelineDTO,
  CreateAiReviewGuidelineDTO,
  UpdateAiReviewGuidelineDTO
} from '../../core/modules/openapi';
import {
  getProposalReviewSectionLabel,
  PROPOSAL_REVIEW_SECTION_OPTIONS
} from '../../core/shared/proposal-review-section.utils';

@Component({
  selector: 'app-ai-review-guidelines-page',
  standalone: true,
  imports: [
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    InputNumberModule,
    SelectModule,
    DialogModule,
    ToastModule,
    TooltipModule
  ],
  templateUrl: './ai-review-guidelines-page.component.html'
})
export class AiReviewGuidelinesPageComponent {
  private readonly guidelinesService = inject(AiReviewGuidelineControllerService);
  private readonly messageService = inject(MessageService);

  guidelines = signal<AiReviewGuidelineDTO[]>([]);
  loading = signal(true);
  saving = signal(false);

  sectionOptions = PROPOSAL_REVIEW_SECTION_OPTIONS;
  getSectionLabel = getProposalReviewSectionLabel;

  dialogVisible = signal(false);
  editingGuidelineId = signal<number | null>(null);

  formSection: AiReviewGuidelineDTO.SectionEnum = AiReviewGuidelineDTO.SectionEnum.General;
  formTitle = '';
  formInstruction = '';
  formSortOrder = 0;

  dialogTitle = computed(() => (this.editingGuidelineId() != null ? 'Edit guideline' : 'Create guideline'));

  constructor() {
    this.loadGuidelines();
  }

  async loadGuidelines() {
    this.loading.set(true);
    try {
      const list = await firstValueFrom(this.guidelinesService.getAllGuidelines());
      this.guidelines.set(list ?? []);
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load AI review guidelines.' });
      this.guidelines.set([]);
    } finally {
      this.loading.set(false);
    }
  }

  openCreateDialog() {
    this.editingGuidelineId.set(null);
    this.formSection = AiReviewGuidelineDTO.SectionEnum.General;
    this.formTitle = '';
    this.formInstruction = '';
    this.formSortOrder = 0;
    this.dialogVisible.set(true);
  }

  openEditDialog(guideline: AiReviewGuidelineDTO) {
    if (guideline.guidelineId == null) return;
    this.editingGuidelineId.set(guideline.guidelineId);
    this.formSection = guideline.section ?? AiReviewGuidelineDTO.SectionEnum.General;
    this.formTitle = guideline.title ?? '';
    this.formInstruction = guideline.instruction ?? '';
    this.formSortOrder = guideline.sortOrder ?? 0;
    this.dialogVisible.set(true);
  }

  onDialogVisibleChange(visible: boolean) {
    this.dialogVisible.set(visible);
    if (!visible) {
      this.editingGuidelineId.set(null);
    }
  }

  closeDialog() {
    this.onDialogVisibleChange(false);
  }

  async saveGuideline() {
    const title = this.formTitle.trim();
    const instruction = this.formInstruction.trim();
    if (!title) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Enter a title.' });
      return;
    }
    if (!instruction) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Enter guideline instructions.' });
      return;
    }

    const payload: CreateAiReviewGuidelineDTO = {
      section: this.formSection,
      title,
      instruction,
      sortOrder: this.formSortOrder
    };

    this.saving.set(true);
    try {
      const id = this.editingGuidelineId();
      if (id != null) {
        await firstValueFrom(this.guidelinesService.updateGuideline(id, payload as UpdateAiReviewGuidelineDTO));
        this.messageService.add({ severity: 'success', summary: 'Saved', detail: 'Guideline updated.' });
      } else {
        await firstValueFrom(this.guidelinesService.createGuideline(payload));
        this.messageService.add({ severity: 'success', summary: 'Created', detail: 'Guideline created.' });
      }
      this.closeDialog();
      await this.loadGuidelines();
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to save guideline.' });
    } finally {
      this.saving.set(false);
    }
  }

  async deleteGuideline(guideline: AiReviewGuidelineDTO) {
    const id = guideline.guidelineId;
    if (id == null) return;
    if (!confirm(`Delete guideline "${guideline.title}"?`)) return;
    try {
      await firstValueFrom(this.guidelinesService.deleteGuideline(id));
      this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Guideline deleted.' });
      await this.loadGuidelines();
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete guideline.' });
    }
  }
}
