import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { firstValueFrom } from 'rxjs';
import { ExaminationBoardControllerService, type ExaminationBoardSummaryDTO } from '../../../core/modules/openapi';

@Component({
  selector: 'app-examination-boards-page',
  standalone: true,
  imports: [RouterLink, FormsModule, TableModule, ButtonModule, InputTextModule, DialogModule, ToastModule, TooltipModule],
  templateUrl: './examination-boards-page.component.html'
})
export class ExaminationBoardsPageComponent {
  private readonly examinationBoardsService = inject(ExaminationBoardControllerService);
  private readonly messageService = inject(MessageService);

  boards = signal<ExaminationBoardSummaryDTO[]>([]);
  loading = signal(true);
  saving = signal(false);

  createDialogVisible = signal(false);
  createBoardName = '';

  constructor() {
    this.loadBoards();
  }

  async loadBoards() {
    this.loading.set(true);
    try {
      const list = await firstValueFrom(this.examinationBoardsService.getAllExaminationBoards());
      this.boards.set(list ?? []);
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load examination boards.' });
      this.boards.set([]);
    } finally {
      this.loading.set(false);
    }
  }

  openCreateDialog() {
    this.createBoardName = '';
    this.createDialogVisible.set(true);
  }

  onCreateDialogVisibleChange(visible: boolean) {
    this.createDialogVisible.set(visible);
    if (!visible) {
      this.createBoardName = '';
    }
  }

  closeCreateDialog() {
    this.onCreateDialogVisibleChange(false);
  }

  async createBoard() {
    const name = this.createBoardName.trim();
    if (!name) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Enter a name.' });
      return;
    }
    this.saving.set(true);
    try {
      await firstValueFrom(this.examinationBoardsService.createExaminationBoard({ name }));
      this.messageService.add({ severity: 'success', summary: 'Created', detail: 'Examination board created.' });
      this.closeCreateDialog();
      await this.loadBoards();
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to create examination board.' });
    } finally {
      this.saving.set(false);
    }
  }

  async deleteBoard(board: ExaminationBoardSummaryDTO) {
    const id = board.examinationBoardId;
    if (id == null) return;
    if (!confirm(`Delete examination board "${board.name}"?`)) return;
    try {
      await firstValueFrom(this.examinationBoardsService.deleteExaminationBoard(id));
      this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Examination board deleted.' });
      await this.loadBoards();
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete examination board.' });
    }
  }
}
