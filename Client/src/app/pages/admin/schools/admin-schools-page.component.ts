import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { TableModule, TablePageEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { firstValueFrom } from 'rxjs';
import { AdminControllerService, PageResponseDTOSchool, School } from '../../../core/modules/openapi';

@Component({
  selector: 'app-admin-schools-page',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, MessageModule, ToastModule, ConfirmDialogModule, DialogModule],
  templateUrl: './admin-schools-page.component.html'
})
export class AdminSchoolsPageComponent {
  private readonly adminControllerService = inject(AdminControllerService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  schools = signal<School[]>([]);
  totalRecords = signal(0);
  loading = signal(false);
  saving = signal(false);
  newSchoolName = '';
  showCreateDialog = signal(false);
  searchQuery = '';
  currentPageSize = signal(10);
  firstRowIndex = signal(0);

  constructor() {
    this.loadSchools(0, this.currentPageSize(), this.searchQuery);
  }

  async pageChange(event: TablePageEvent) {
    const first = event.first ?? 0;
    const rows = event.rows ?? this.currentPageSize();
    this.currentPageSize.set(rows);
    this.firstRowIndex.set(first);
    const page = rows > 0 ? Math.floor(first / rows) : 0;
    await this.loadSchools(page, rows, this.searchQuery);
  }

  runSearch() {
    this.firstRowIndex.set(0);
    this.loadSchools(0, this.currentPageSize(), this.searchQuery);
  }

  async loadSchools(page = 0, size = 10, search?: string) {
    this.loading.set(true);
    try {
      const res = (await firstValueFrom(this.adminControllerService.getSchools(page, size, search?.trim() || undefined))) as PageResponseDTOSchool;
      this.schools.set(res.content ?? []);
      this.totalRecords.set(res.totalElements ?? 0);
    } catch {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load schools.'
      });
      this.schools.set([]);
      this.totalRecords.set(0);
    } finally {
      this.loading.set(false);
    }
  }

  confirmDeleteSchool(school: School) {
    const name = school.name ?? 'This school';
    this.confirmationService.confirm({
      message: `Are you sure you want to delete "${name}"? This action cannot be undone.`,
      header: 'Delete school',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deleteSchool(school)
    });
  }

  async deleteSchool(school: School) {
    const schoolId = school.schoolId;
    if (schoolId == null) return;
    this.loading.set(true);
    try {
      await firstValueFrom(this.adminControllerService.deleteSchool(schoolId));
      await this.loadSchools(0, this.currentPageSize(), this.searchQuery);
      this.messageService.add({
        severity: 'success',
        summary: 'School deleted',
        detail: `"${school.name}" has been deleted.`
      });
    } catch {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to delete school.'
      });
    } finally {
      this.loading.set(false);
    }
  }

  openCreateDialog() {
    this.newSchoolName = '';
    this.showCreateDialog.set(true);
  }

  async addSchool() {
    const name = this.newSchoolName?.trim();
    if (!name) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Validation',
        detail: 'Please enter a school name.'
      });
      return;
    }
    this.saving.set(true);
    try {
      await firstValueFrom(this.adminControllerService.createSchool({ name }));
      this.newSchoolName = '';
      this.showCreateDialog.set(false);
      await this.loadSchools(0, this.currentPageSize(), this.searchQuery);
      this.messageService.add({
        severity: 'success',
        summary: 'School added',
        detail: `"${name}" has been added.`
      });
    } catch {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to add school.'
      });
    } finally {
      this.saving.set(false);
    }
  }
}
