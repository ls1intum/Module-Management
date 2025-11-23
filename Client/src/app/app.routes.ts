import { Routes } from '@angular/router';
import { ProposalCreateComponent } from './pages/proposal-create/proposal-create.component';
import { ProposalViewComponent } from './pages/proposal-view/proposal-view.component';
import { FeedbackViewComponent } from './pages/feedback-view/feedback-view.component';
import { IndexComponent } from './pages/index/index.component';
import { ProfessorHomePageComponent } from './pages/professor-home/professor-home-page.component';
import { ModuleVersionEditComponent } from './pages/module-version-edit/module-version-edit.component';
import { ApprovalStaffHomePageComponent } from './pages/approval-staff-home/approval-staff-home-page.component';
import { AuthGuard } from './core/security/auth.guard';
import { ModuleVersionViewComponent } from './pages/module-version-view/module-version-view.component';
import { SimilarModulesPage } from './pages/similar-modules/similar-modules.component';
import { AccountManagementPageComponent } from './pages/account-management/account-management-page.component';

export const routes: Routes = [
  { path: '', component: IndexComponent },
  { path: 'proposals', component: ProfessorHomePageComponent, canActivate: [AuthGuard] },
  { path: 'proposals/create', component: ProposalCreateComponent, canActivate: [AuthGuard] },
  { path: 'proposals/view/:id', component: ProposalViewComponent, canActivate: [AuthGuard] },
  { path: 'module-version/edit/:id', component: ModuleVersionEditComponent, canActivate: [AuthGuard] },
  { path: 'module-version/view/:id', component: ModuleVersionViewComponent, canActivate: [AuthGuard] },
  { path: 'overlap/:id', component: SimilarModulesPage },
  { path: 'feedbacks/for-user/:id', component: ApprovalStaffHomePageComponent },
  { path: 'feedbacks/view/:id', component: FeedbackViewComponent },
  { path: 'account', component: AccountManagementPageComponent, canActivate: [AuthGuard] }
];
