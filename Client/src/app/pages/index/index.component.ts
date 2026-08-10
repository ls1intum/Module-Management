import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DividerModule } from 'primeng/divider';
import { PanelModule } from 'primeng/panel';
import { SecurityStore } from '../../core/security/security-store.service';
import { SignInComponent } from '../../components/sign-in/sign-in.component';
import { isAdminRole, isAiReviewGuidelineManagerRole, isProfessorRole, isReviewerRole } from '../../core/shared/user-role.utils';

@Component({
  selector: 'index-component',
  standalone: true,
  imports: [RouterModule, ButtonModule, DividerModule, PanelModule, SignInComponent],
  templateUrl: './index.component.html'
})
export class IndexComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;

  isAdmin = (): boolean => isAdminRole(this.user()?.roles);
  isProfessor = (): boolean => isProfessorRole(this.user()?.roles);
  isReviewer = (): boolean => isReviewerRole(this.user()?.roles);
  isAiReviewGuidelineManager = (): boolean => isAiReviewGuidelineManagerRole(this.user()?.roles);
}
