import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { firstValueFrom } from 'rxjs';
import {
  DegreeProgramSpecializationsControllerService,
  type CreateDegreeProgramSpecializationDTO,
  type DegreeProgramSpecializationDTO,
  type UpdateDegreeProgramSpecializationDTO
} from '../../../core/modules/openapi';
import { UsersSelectComponent } from '../../../components/users-select/users-select.component';

@Component({
  selector: 'app-all-specializations-page',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, DialogModule, ToastModule, UsersSelectComponent],
  templateUrl: './all-specializations-page.component.html'
})
export class AllSpecializationsPageComponent {
  private readonly specializationService = inject(DegreeProgramSpecializationsControllerService);
  private readonly messageService = inject(MessageService);

  specializations = signal<DegreeProgramSpecializationDTO[]>([]);
  loading = signal(false);
  dialogVisible = signal(false);
  selectedSpecialization = signal<DegreeProgramSpecializationDTO | null>(null);
  editMode = signal(false);

  name = signal('');
  responsibleUserId = signal<string | null>(null);

  constructor() {
    this.loadSpecializations();
  }

  async loadSpecializations() {
    this.loading.set(true);
    try {
      const list = await firstValueFrom(this.specializationService.getAllDegreeProgramSpecializations());
      this.specializations.set(list ?? []);
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load degree program specializations.' });
      this.specializations.set([]);
    } finally {
      this.loading.set(false);
    }
  }

  userLabel(spec: DegreeProgramSpecializationDTO): string {
    const u = spec?.responsibleUser;
    return ([u.firstName, u.lastName].filter(Boolean).join(' ').trim() || u.email) ?? '—';
  }

  openCreate() {
    this.editMode.set(false);
    this.selectedSpecialization.set(null);
    this.name.set('');
    this.responsibleUserId.set(null);
    this.dialogVisible.set(true);
  }

  openEdit(spec: DegreeProgramSpecializationDTO) {
    this.editMode.set(true);
    this.selectedSpecialization.set(spec);
    this.name.set(spec.name);
    this.responsibleUserId.set(spec.responsibleUser?.userId ?? null);
    this.dialogVisible.set(true);
  }

  async saveSpecialization() {
    const id = this.selectedSpecialization()?.degreeProgramSpecializationId;
    const nameVal = this.name().trim();
    const userIdVal = this.responsibleUserId();
    if (!nameVal || !userIdVal) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Name and responsible user are required.' });
      return;
    }
    const dto: CreateDegreeProgramSpecializationDTO & UpdateDegreeProgramSpecializationDTO = { name: nameVal, responsibleUserId: userIdVal };
    try {
      if (this.editMode() && id != null) {
        await firstValueFrom(this.specializationService.updateDegreeProgramSpecialization(id, dto));
        this.messageService.add({ severity: 'success', summary: 'Updated', detail: 'Degree program specialization updated.' });
      } else {
        await firstValueFrom(this.specializationService.createDegreeProgramSpecialization(dto));
        this.messageService.add({ severity: 'success', summary: 'Created', detail: 'Degree program specialization created.' });
      }
      this.dialogVisible.set(false);
      await this.loadSpecializations();
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to save degree program specialization.' });
    }
  }

  async deleteSpecialization(spec: DegreeProgramSpecializationDTO) {
    if (!confirm(`Delete "${spec.name}"? It will be removed from all degree programs that use it.`)) return;
    try {
      await firstValueFrom(this.specializationService.deleteDegreeProgramSpecialization(spec.degreeProgramSpecializationId));
      this.messageService.add({ severity: 'success', summary: 'Deleted', detail: 'Degree program specialization deleted.' });
      await this.loadSpecializations();
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete degree program specialization.' });
    }
  }
}
