import { Component, computed, effect, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { ProfessorHomePageComponent } from '../professor-home/professor-home-page.component';
import { ApprovalStaffHomePageComponent } from '../approval-staff-home/approval-staff-home-page.component';
import { SecurityStore } from '../../core/security/security-store.service';
import { LoginRequiredComponent } from '../../components/login-required/login-required.component';
import { User } from '../../core/modules/openapi';

@Component({
  selector: 'index-component',
  standalone: true,
  imports: [RouterModule, ProfessorHomePageComponent, ApprovalStaffHomePageComponent, LoginRequiredComponent],
  templateUrl: './index.component.html'
})
export class IndexComponent {
  private router = inject(Router);
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;

  constructor() {
    effect(() => {
      const u = this.user();
      if (u?.role === 'ADMIN') {
        this.router.navigateByUrl('/admin');
      }
    });
  }

  isProposalSubmitter = computed(() => {
    const user = this.user();
    return user && user.role === User.RoleEnum.Professor;
  });

  isProposalReviewer = computed(() => {
    const user = this.user();
    return user && user.role && [User.RoleEnum.QualityManagement, User.RoleEnum.AcademicProgramAdvisor, User.RoleEnum.ExaminationBoard].includes(user.role);
  });
}
