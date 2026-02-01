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
      if (isAdminRole(this.user()!.role)) {
        this.router.navigateByUrl('/admin');
      } else if (isProfessorRole(this.user()!.role)) {
        this.router.navigateByUrl('/proposals');
      } else if (isReviewerRole(this.user()!.role) && this.user()!.userId) {
        this.router.navigate(['/feedbacks/for-user', this.user()!.userId]);
      }
    });
  }
}
