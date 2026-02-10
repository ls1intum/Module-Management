import { Component, effect, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { SecurityStore } from '../../core/security/security-store.service';
import { isAdminRole, isProfessorRole, isReviewerRole } from '../../core/shared/user-role.utils';

@Component({
  selector: 'index-component',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './index.component.html'
})
export class IndexComponent {
  private router = inject(Router);
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;

  constructor() {
    effect(() => {
      if (!this.user()) return;
      const roles = this.user()!.roles;
      if (isAdminRole(roles)) {
        this.router.navigateByUrl('/admin');
      } else if (isProfessorRole(roles)) {
        this.router.navigateByUrl('/proposals');
      } else if (isReviewerRole(roles)) {
        this.router.navigateByUrl('/feedbacks');
      }
    });
  }
}
