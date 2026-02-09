import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { isAdminRole, isProfessorRole, isReviewerRole } from '../../core/shared/user-role.utils';
import { SecurityStore } from '../../core/security/security-store.service';
import { SidebarService } from './sidebar.service';

@Component({
  selector: 'app-side-bar',
  standalone: true,
  imports: [RouterModule, ButtonModule],
  templateUrl: './side-bar.component.html'
})
export class SideBarComponent {
  private router = inject(Router);
  sidebarService = inject(SidebarService);
  securityStore = inject(SecurityStore);

  user = this.securityStore.user;

  isAdmin = (): boolean => isAdminRole(this.user()?.role);
  isProfessor = (): boolean => isProfessorRole(this.user()?.role);
  isReviewer = (): boolean => isReviewerRole(this.user()?.role);

  isActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }
}
