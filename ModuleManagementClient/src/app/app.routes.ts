import { Routes } from '@angular/router';
import { ProposalListComponent } from './proposal-list/proposal-list.component';
import { ProposalsCreateComponent } from './proposal-create/proposal-create.component';
import { ProposalViewComponent } from './proposal-view/proposal-view.component';
import { ModuleVersionEditComponent } from './module-version-edit/module-version-edit.component';

export const routes: Routes = [
  { path: '', component: ProposalListComponent },
  { path: 'proposals/create', component: ProposalsCreateComponent},
  { path: 'proposals/view/:id', component: ProposalViewComponent},
  { path: 'module-version/edit/:id', component: ModuleVersionEditComponent}
];
