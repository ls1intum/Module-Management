import { Component, computed, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ProfessorHomePageComponent } from '../professor-home/professor-home-page.component';
import { ApprovalStaffHomePageComponent } from '../approval-staff-home/approval-staff-home-page.component';
import { SecurityStore } from '../../core/security/security-store.service';
import { LoginRequiredComponent } from '../../components/login-required/login-required.component';

@Component({
  selector: 'index-component',
  standalone: true,
  imports: [RouterModule, ProfessorHomePageComponent, ApprovalStaffHomePageComponent, LoginRequiredComponent],
  templateUrl: './index.component.html'
})
export class IndexComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
  userLoaded = this.securityStore.loaded;

  isProposalSubmitter = computed(() => {
    const user = this.user();
    return user && user.roles.includes('module-submitter');
  });

  isProposalReviewer = computed(() => {
    const user = this.user();
    return user && user.roles.includes('module-reviewer');
  });
}
