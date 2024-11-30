import { Routes } from '@angular/router';
import { ProposalsCreateComponent } from './pages/proposal-create/proposal-create.component';
import { ProposalViewComponent } from './pages/proposal-view/proposal-view.component';
import { FeedbackViewComponent } from './pages/feedback-view/feedback-view.component';
import { IndexComponent } from './pages/index/index.component';
import { ProfessorHomePageComponent } from './pages/professor-home/professor-home-page.component';
import { ProposalsEditComponent } from './pages/proposal-edit/proposal-edit.component';
import { ApprovalStaffHomePageComponent } from './pages/approval-staff-home/approval-staff-home-page.component';
import { AuthGuard } from './core/security/auth.guard';

export const routes: Routes = [
  { path: '', component: IndexComponent },
  { path: 'proposals', component: ProfessorHomePageComponent, canActivate: [AuthGuard] },
  { path: 'proposals/create', component: ProposalsCreateComponent },
  { path: 'proposals/view/:id', component: ProposalViewComponent },
  { path: 'module-version/edit/:id', component: ProposalsEditComponent },
  { path: 'feedbacks/for-user/:id', component: ApprovalStaffHomePageComponent },
  { path: 'feedbacks/view/:id', component: FeedbackViewComponent }
];
