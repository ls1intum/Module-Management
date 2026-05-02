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
import { AccountLayoutComponent } from './pages/account-management/account-layout/account-layout.component';
import { AccountInformationComponent } from './pages/account-management/account-information/account-information.component';
import { AccountPasskeysComponent } from './pages/account-management/passkeys/account-passkeys.component';
import { UsersPageComponent } from './pages/admin/users/users-page.component';
import { AllDegreeProgramsPageComponent } from './pages/admin/degree-programs/all-degree-programs-page.component';
import { DegreeProgramDetailsPageComponent } from './pages/admin/degree-programs/degree-program-details-page.component';
import { AllSpecializationsPageComponent } from './pages/admin/degree-program-specializations/all-specializations-page.component';
import { ExaminationBoardDetailPageComponent } from './pages/admin/examination-boards/examination-board-detail-page.component';
import { ExaminationBoardsPageComponent } from './pages/admin/examination-boards/examination-boards-page.component';
export const routes: Routes = [
  { path: '', component: IndexComponent },
  {
    path: 'proposals',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: ProfessorHomePageComponent },
      { path: 'create', component: ProposalCreateComponent },
      { path: ':id', component: ProposalViewComponent },
      { path: ':id/version/:versionId', component: ModuleVersionViewComponent },
      { path: ':id/version/:versionId/edit', component: ModuleVersionEditComponent },
      { path: ':id/version/:versionId/overlap', component: SimilarModulesPage }
    ]
  },
  {
    path: 'feedbacks',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: ApprovalStaffHomePageComponent },
      { path: 'view/:id', component: FeedbackViewComponent },
      { path: 'view/:id/overlap/:versionId', component: SimilarModulesPage }
    ]
  },
  {
    path: 'account',
    component: AccountLayoutComponent,
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
      { path: 'users', component: UsersPageComponent },
      { path: 'examination-boards/:id', component: ExaminationBoardDetailPageComponent },
      { path: 'examination-boards', component: ExaminationBoardsPageComponent },
      { path: 'degree-programs/specializations', component: AllSpecializationsPageComponent },
      { path: 'degree-programs/:id', component: DegreeProgramDetailsPageComponent },
      { path: 'degree-programs', component: AllDegreeProgramsPageComponent },
      { path: '', redirectTo: 'users', pathMatch: 'full' }
    ]
  }
];
