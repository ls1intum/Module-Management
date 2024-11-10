import { Routes } from '@angular/router';
import { ProposalsCreateComponent } from './pages/proposal-create/proposal-create.component';
import { ProposalViewComponent } from './proposal-view/proposal-view.component';
import { FeedbackListComponent } from './feedback-list/feedback-list.component';
import { FeedbackViewComponent } from './feedback-view/feedback-view.component';
import { IndexComponent } from './pages/index/index.component';
import { ProfessorHomePageComponent } from './pages/professor-home/professor-home-page.component';
import { ProposalsEditComponent } from './pages/proposal-edit/proposal-edit.component';

export const routes: Routes = [
  // { path: '', component: ProposalListComponent },
  { path: '', component: IndexComponent },
  { path: 'proposals', component: ProfessorHomePageComponent },
  { path: 'proposals/create', component: ProposalsCreateComponent },
  { path: 'proposals/view/:id', component: ProposalViewComponent },
  { path: 'module-version/edit/:id', component: ProposalsEditComponent },
  { path: 'feedbacks/for-user', component: FeedbackListComponent },
  { path: 'feedbacks/view/:id', component: FeedbackViewComponent }
];
