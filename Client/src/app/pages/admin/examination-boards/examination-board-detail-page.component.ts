import { Component, OnDestroy, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { firstValueFrom } from 'rxjs';
import { ExaminationBoardControllerService, type ExaminationBoardDTO, type ResponsibleUserDTO } from '../../../core/modules/openapi';
import { BreadcrumbLabelsService } from '../../../components/breadcrumb/breadcrumb-labels.service';
import { UsersSelectComponent } from '../../../components/users-select/users-select.component';

@Component({
  selector: 'app-examination-board-detail-page',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, ToastModule, DialogModule, TooltipModule, UsersSelectComponent],
  templateUrl: './examination-board-detail-page.component.html'
})
export class ExaminationBoardDetailPageComponent implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly examinationBoardsService = inject(ExaminationBoardControllerService);
  private readonly messageService = inject(MessageService);
  private readonly breadcrumbLabels = inject(BreadcrumbLabelsService);

  board = signal<ExaminationBoardDTO | null>(null);
  loading = signal(true);
  saving = signal(false);

  editName = '';
  userIdToAdd: string | null = null;
  addMemberDialogVisible = signal(false);

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id != null) {
        void this.loadBoard(Number(id));
      } else {
        this.board.set(null);
      }
    });
  }

  ngOnDestroy(): void {
    this.breadcrumbLabels.examinationBoardName.set(null);
  }

  async loadBoard(id: number) {
    this.loading.set(true);
    this.breadcrumbLabels.examinationBoardName.set(null);
    try {
      const b = await firstValueFrom(this.examinationBoardsService.getExaminationBoard(id));
      this.board.set(b);
      this.editName = b.name ?? '';
      this.breadcrumbLabels.examinationBoardName.set(b.name ?? null);
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Examination board not found.' });
      this.board.set(null);
      this.breadcrumbLabels.examinationBoardName.set(null);
    } finally {
      this.loading.set(false);
    }
  }

  memberDisplayName(m: ResponsibleUserDTO): string {
    const name = [m.firstName, m.lastName].filter(Boolean).join(' ').trim();
    if (name) return name;
    return (m.email ?? m.userId ?? '—').trim();
  }

  currentMemberUserIds(): string[] {
    return (this.board()?.members ?? []).map((m) => m.userId!).filter(Boolean);
  }

  openAddMemberDialog() {
    this.userIdToAdd = null;
    this.addMemberDialogVisible.set(true);
  }

  onAddMemberDialogVisibleChange(visible: boolean) {
    this.addMemberDialogVisible.set(visible);
    if (!visible) {
      this.userIdToAdd = null;
    }
  }

  closeAddMemberDialog() {
    this.onAddMemberDialogVisibleChange(false);
  }

  async saveName() {
    const b = this.board();
    const id = b?.examinationBoardId;
    const name = this.editName.trim();
    if (!b || id == null || !name) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Name is required.' });
      return;
    }
    this.saving.set(true);
    try {
      await firstValueFrom(
        this.examinationBoardsService.updateExaminationBoard(id, {
          name,
          userIds: this.currentMemberUserIds()
        })
      );
      this.messageService.add({ severity: 'success', summary: 'Saved', detail: 'Board details updated.' });
      await this.loadBoard(id);
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to save name.' });
    } finally {
      this.saving.set(false);
    }
  }

  async removeMember(userId: string) {
    const b = this.board();
    const id = b?.examinationBoardId;
    if (!b || id == null) return;
    const next = this.currentMemberUserIds().filter((uid) => uid !== userId);
    this.saving.set(true);
    try {
      await firstValueFrom(
        this.examinationBoardsService.updateExaminationBoard(id, {
          name: b.name ?? '',
          userIds: next
        })
      );
      this.messageService.add({ severity: 'success', summary: 'Updated', detail: 'Member removed.' });
      await this.loadBoard(id);
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to remove member.' });
    } finally {
      this.saving.set(false);
    }
  }

  async addMember() {
    const b = this.board();
    const id = b?.examinationBoardId;
    const uid = this.userIdToAdd;
    if (!b || id == null || !uid) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Select a user to add.' });
      return;
    }
    if (this.currentMemberUserIds().includes(uid)) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'That user is already on this board.' });
      return;
    }
    const merged = [...new Set([...this.currentMemberUserIds(), uid])];
    this.saving.set(true);
    try {
      await firstValueFrom(
        this.examinationBoardsService.updateExaminationBoard(id, {
          name: b.name ?? '',
          userIds: merged
        })
      );
      this.messageService.add({ severity: 'success', summary: 'Updated', detail: 'Member added.' });
      this.userIdToAdd = null;
      this.closeAddMemberDialog();
      await this.loadBoard(id);
    } catch {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to add member.' });
    } finally {
      this.saving.set(false);
    }
  }
}
