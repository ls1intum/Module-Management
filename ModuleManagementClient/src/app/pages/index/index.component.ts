import { Component, computed, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ProfessorHomePageComponent } from '../professor-home/professor-home-page.component';
import { ApprovalStaffHomePageComponent } from '../approval-staff-home/approval-staff-home-page.component';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'index-component',
  standalone: true,
  imports: [RouterModule, ProfessorHomePageComponent, ApprovalStaffHomePageComponent],
  template: `
    @if (userLoaded()) { @if (isProposalSubmitter()) {
    <ng-container>
      <professor-home-page />
    </ng-container>
    } @else if (isProposalReviewer()) {
    <ng-container>
      <approval-staff-home-page />
    </ng-container>
    } @else {
    <p class="text-center mt-8">You do not have access to this application.</p>
    } } @else {loading}
  `
})
export class IndexComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
  userLoaded = this.securityStore.loaded;

  isProposalSubmitter = computed(() => {
    const user = this.user();
    return user && user.roles.includes('proposal-submitter');
  });

  isProposalReviewer = computed(() => {
    const user = this.user();
    return user && user.roles.includes('proposal-reviewer');
  });
}
