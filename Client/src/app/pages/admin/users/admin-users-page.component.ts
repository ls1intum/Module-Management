import { Component, inject, signal } from '@angular/core';
import { AdminUserControllerService } from '../../../core/modules/openapi';
import { UserDTO } from '../../../core/modules/openapi/model/user-dto';
import { FormsModule } from '@angular/forms';
import { TableModule, TablePageEvent } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-admin-users-page',
  standalone: true,
  imports: [FormsModule, TableModule, SelectModule, ToastModule],
  templateUrl: './admin-users-page.component.html'
})
export class AdminUsersPageComponent {
  private readonly adminUserControllerService = inject(AdminUserControllerService);
  private readonly messageService = inject(MessageService);

  users = signal<UserDTO[]>([]);
  totalRecords = signal(0);
  loading = signal(false);
  savingUserId = signal<string | null>(null);
  search = signal<string | undefined>(undefined);
  roles = Object.entries(UserDTO.RoleEnum).map(([key, value]) => ({
    label: key,
    value
  }));

  constructor() {
    this.loadUsers(0, 10, this.search());
  }

  async pageChange(event: TablePageEvent) {
    const first = event.first ?? 0;
    const rows = event.rows ?? 10;
    const page = rows > 0 ? Math.floor(first / rows) : 0;
    await this.loadUsers(page, rows, this.search());
  }

  async loadUsers(page = 0, size = 10, search?: string) {
    this.loading.set(true);
    try {
      const res = await firstValueFrom(this.adminUserControllerService.getUsers(page, size, search));
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

  async onRoleChange(user: UserDTO, newRole: UserDTO.RoleEnum) {
    if (!user.userId || user.role === newRole) return;
    this.savingUserId.set(user.userId);
    try {
      await firstValueFrom(this.adminUserControllerService.updateUserRole(user.userId, { role: newRole }));
      this.users.update((list) => list.map((u) => (u.userId === user.userId ? { ...u, role: newRole } : u)));
      this.messageService.add({
        severity: 'success',
        summary: 'Role updated',
        detail: `Role for ${user.firstName} ${user.lastName} has been updated.`
      });
    } catch (e) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to update role.'
      });
    } finally {
      this.savingUserId.set(null);
    }
  }

  isSaving(userId: string | undefined): boolean {
    return userId !== undefined && this.savingUserId() === userId;
  }
}
