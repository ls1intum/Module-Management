import { Component, inject, signal } from '@angular/core';
import { AdminUserControllerService } from '../../../core/modules/openapi';
import { UserDTO } from '../../../core/modules/openapi/model/user-dto';
import { FormsModule } from '@angular/forms';
import { TableModule, TablePageEvent } from 'primeng/table';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-admin-users-page',
  standalone: true,
  imports: [FormsModule, TableModule, MultiSelectModule, InputTextModule, ButtonModule, ToastModule],
  templateUrl: './admin-users-page.component.html'
})
export class AdminUsersPageComponent {
  private readonly adminUserControllerService = inject(AdminUserControllerService);
  private readonly messageService = inject(MessageService);

  users = signal<UserDTO[]>([]);
  totalRecords = signal(0);
  loading = signal(false);
  savingUserId = signal<string | null>(null);
  searchQuery = '';
  currentPageSize = signal(10);
  firstRowIndex = signal(0);
  roles = Object.entries(UserDTO.RolesEnum).map(([key, value]) => ({
    label: key.replace(/([A-Z])/g, ' $1').trim(),
    value
  }));

  constructor() {
    this.loadUsers(0, this.currentPageSize(), this.searchQuery);
  }

  async pageChange(event: TablePageEvent) {
    const first = event.first ?? 0;
    const rows = event.rows ?? this.currentPageSize();
    this.currentPageSize.set(rows);
    this.firstRowIndex.set(first);
    const page = rows > 0 ? Math.floor(first / rows) : 0;
    await this.loadUsers(page, rows, this.searchQuery);
  }

  runSearch() {
    this.firstRowIndex.set(0);
    this.loadUsers(0, this.currentPageSize(), this.searchQuery);
  }

  async loadUsers(page = 0, size = 10, search?: string) {
    this.loading.set(true);
    try {
      const res = await firstValueFrom(this.adminUserControllerService.getUsers(page, size, search?.trim() || undefined));
      this.users.set(res.content ?? []);
      this.totalRecords.set(res.totalElements ?? 0);
    } catch (e) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load users.'
      });
      this.users.set([]);
      this.totalRecords.set(0);
    } finally {
      this.loading.set(false);
    }
  }

  async onRolesChange(user: UserDTO, newRoles: UserDTO.RolesEnum[]) {
    if (!user.userId) return;
    const sortedNew = [...(newRoles ?? [])].sort();
    const sortedCurrent = [...(user.roles ?? [])].sort();
    if (sortedNew.length === sortedCurrent.length && sortedNew.every((r, i) => r === sortedCurrent[i])) return;
    this.savingUserId.set(user.userId);
    try {
      await firstValueFrom(this.adminUserControllerService.updateUserRole(user.userId, { roles: newRoles ?? [] }));
      this.users.update((list) => list.map((u) => (u.userId === user.userId ? { ...u, roles: newRoles ?? [] } : u)));
      this.messageService.add({
        severity: 'success',
        summary: 'Roles updated',
        detail: `Roles for ${user.firstName} ${user.lastName} have been updated.`
      });
    } catch (e) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to update roles.'
      });
    } finally {
      this.savingUserId.set(null);
    }
  }

  isSaving(userId: string | undefined): boolean {
    return userId !== undefined && this.savingUserId() === userId;
  }
}
