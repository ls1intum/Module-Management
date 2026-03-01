import { Component, inject, signal, computed, input } from '@angular/core';
import { ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule, SelectLazyLoadEvent } from 'primeng/select';
import { Subject, debounceTime, distinctUntilChanged, firstValueFrom } from 'rxjs';
import { AdminUserControllerService } from '../../core/modules/openapi';
import type { UserDTO } from '../../core/modules/openapi/model/user-dto';
import type { ResponsibleUserDTO } from '../../core/modules/openapi/model/responsible-user-dto';

const USER_PAGE_SIZE = 20;
const FILTER_DEBOUNCE_MS = 500;

@Component({
  selector: 'app-users-select',
  standalone: true,
  imports: [FormsModule, ProgressSpinnerModule, SelectModule],
  templateUrl: './users-select.component.html',
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: UsersSelectComponent, multi: true }]
})
export class UsersSelectComponent implements ControlValueAccessor {
  private readonly adminUserControllerService = inject(AdminUserControllerService);

  private readonly filter$ = new Subject<string>();

  styleClass = input<string>('');
  /** When the selected user is not yet in the loaded list, pass this so they are displayed like other options. */
  selectedUser = input<ResponsibleUserDTO | null>(null);

  constructor() {
    this.loadUsers();
    this.filter$.pipe(debounceTime(FILTER_DEBOUNCE_MS), distinctUntilChanged()).subscribe((term) => {
      this.searchTerm.set(term);
      this.loadUsers(0);
    });
  }

  value = signal<string | null>(null);
  users = signal<UserDTO[]>([]);
  totalRecords = signal(0);
  loading = signal(false);
  searchTerm = signal('');

  private onTouched = () => {};
  private onChange: (v: string | null) => void = () => {};

  formatUserLabel(u: { firstName?: string; lastName?: string; email?: string; userId?: string }): string {
    return `${u.firstName ?? ''} ${u.lastName ?? ''} (${u.email ?? u.userId ?? ''})`.trim() || String(u.userId ?? '');
  }

  userOptions = computed(() => {
    const list = this.users().map((u) => ({
      label: this.formatUserLabel(u),
      value: u.userId
    }));
    const current = this.value();
    if (current && !list.some((o) => o.value === current)) {
      const u = this.selectedUser();
      const label = u ? this.formatUserLabel(u) : current;
      return [{ label, value: current }, ...list];
    }
    return list;
  });

  writeValue(v: string | null): void {
    this.value.set(v);
  }

  registerOnChange(fn: (v: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  onValueChange(v: string | null): void {
    this.value.set(v);
    this.onChange(v);
    this.onTouched();
  }

  async loadUsers(page = 0) {
    if (this.loading()) return;
    this.loading.set(true);
    try {
      const res = await firstValueFrom(this.adminUserControllerService.getUsers(page, USER_PAGE_SIZE, this.searchTerm().trim() || undefined));
      this.totalRecords.set(res.totalElements ?? 0);
      if (page === 0) {
        this.users.set(res.content ?? []);
      } else {
        this.users.update((prev) => [...prev, ...(res.content ?? [])]);
      }
    } catch (e) {
      this.users.set([]);
      this.totalRecords.set(0);
    } finally {
      this.loading.set(false);
    }
  }

  async onLazyLoad(event: SelectLazyLoadEvent): Promise<void> {
    if (this.totalRecords() > 0 && this.users().length >= this.totalRecords()) return;
    if (event.last < this.users().length) return;

    const page = Math.floor(this.users().length / USER_PAGE_SIZE);
    await this.loadUsers(page);
  }

  onFilter(event: { filter: string }): void {
    this.filter$.next(event.filter ?? '');
  }
}
