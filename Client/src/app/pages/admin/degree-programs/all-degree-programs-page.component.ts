import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { firstValueFrom } from 'rxjs';
import { DegreeProgramsControllerService, type DegreeProgramDTO } from '../../../core/modules/openapi';
import { UsersSelectComponent } from '../../../components/users-select/users-select.component';

@Component({
  selector: 'app-all-degree-programs-page',
  standalone: true,
  imports: [RouterLink, FormsModule, TableModule, ButtonModule, InputTextModule, DialogModule, TooltipModule, ToastModule, UsersSelectComponent],
  templateUrl: './all-degree-programs-page.component.html'
})
export class AllDegreeProgramsPageComponent {
  private readonly degreeProgramService = inject(DegreeProgramsControllerService);
  private readonly messageService = inject(MessageService);

  programs = signal<DegreeProgramDTO[]>([]);
  loading = signal(false);
  dialogVisible = signal(false);

  name = signal('');
  responsibleUserId = signal<string | null>(null);

  constructor() {
    this.loadPrograms();
  }

  async loadPrograms() {
    this.loading.set(true);
    try {
      const list = await firstValueFrom(this.degreeProgramService.getAllDegreePrograms());
      this.programs.set(list ?? []);
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load degree programs.' });
      this.programs.set([]);
    } finally {
      this.loading.set(false);
    }
  }

  userLabel(program: { responsibleUserId?: string | null; responsibleUser?: { firstName?: string; lastName?: string; email?: string } }): string {
    const u = program?.responsibleUser;
    if (!u) return program?.responsibleUserId ?? '—';
    return ([u.firstName, u.lastName].filter(Boolean).join(' ').trim() || u.email) ?? program?.responsibleUserId ?? '—';
  }

  openCreate() {
    this.name.set('');
    this.responsibleUserId.set(null);
    this.dialogVisible.set(true);
  }

  async saveProgram() {
    const nameVal = this.name().trim();
    const userIdVal = this.responsibleUserId();
    if (!nameVal || !userIdVal) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Name and responsible user are required.' });
      return;
    }
    try {
      await firstValueFrom(this.degreeProgramService.createDegreeProgram({ name: nameVal, responsibleUserId: userIdVal }));
      this.messageService.add({ severity: 'success', summary: 'Created', detail: 'Degree program created.' });
      this.dialogVisible.set(false);
      await this.loadPrograms();
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to save degree program.' });
    }
  }

  async deleteProgram(program: DegreeProgramDTO) {
    if (!confirm(`Delete degree program "${program.name}"?`)) return;
    try {
      await firstValueFrom(this.degreeProgramService.deleteDegreeProgram(program.degreeProgramId));
      this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Degree program deleted.' });
      await this.loadPrograms();
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete degree program.' });
    }
  }
}
