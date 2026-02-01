import { Routes } from '@angular/router';
import { ProposalCreateComponent } from './pages/proposal-create/proposal-create.component';
import { ProposalViewComponent } from './pages/proposal-view/proposal-view.component';
import { FeedbackViewComponent } from './pages/feedback-view/feedback-view.component';
import { IndexComponent } from './pages/index/index.component';
import { ProfessorHomePageComponent } from './pages/professor-home/professor-home-page.component';
import { ModuleVersionEditComponent } from './pages/module-version-edit/module-version-edit.component';
import { ApprovalStaffHomePageComponent } from './pages/approval-staff-home/approval-staff-home-page.component';
import { AuthGuard } from './core/security/auth.guard';
import { AdminGuard } from './core/security/admin.guard';
import { ModuleVersionViewComponent } from './pages/module-version-view/module-version-view.component';
import { SimilarModulesPage } from './pages/similar-modules/similar-modules.component';
import { AccountInformationComponent } from './pages/account-management/account-information/account-information.component';
import { AccountPasskeysComponent } from './pages/account-management/passkeys/account-passkeys.component';
import { AdminUsersPageComponent } from './pages/admin/users/admin-users-page.component';

export const routes: Routes = [
  { path: '', component: IndexComponent },
  {
    path: 'proposals',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: ProfessorHomePageComponent },
      { path: 'create', component: ProposalCreateComponent },
      { path: 'view/:id', component: ProposalViewComponent },
      { path: 'view/:id/version/:versionId', component: ModuleVersionViewComponent },
      { path: 'view/:id/version/:versionId/edit', component: ModuleVersionEditComponent },
      { path: 'view/:id/version/:versionId/overlap', component: SimilarModulesPage }
    ]
  },
  {
    path: 'feedbacks',
    canActivate: [AuthGuard],
    children: [
      { path: 'for-user/:id', component: ApprovalStaffHomePageComponent },
      { path: 'view/:id', component: FeedbackViewComponent }
    ]
  },
  {
    path: 'account',
    canActivate: [AuthGuard],
    children: [
      { path: 'information', component: AccountInformationComponent },
      { path: 'passkeys', component: AccountPasskeysComponent },
      { path: '', redirectTo: 'information', pathMatch: 'full' }
    ]
  },
  {
    path: 'admin',
    canActivate: [AuthGuard, AdminGuard],
    children: [
      { path: 'users', component: AdminUsersPageComponent },
      { path: '', redirectTo: 'users', pathMatch: 'full' }
    ]
  }
];
