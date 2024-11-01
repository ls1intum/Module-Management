import { Routes } from '@angular/router';
import { ProposalListComponent } from './proposal-list/proposal-list.component';
import { ProposalsCreateComponent } from './proposal-create/proposal-create.component';

export const routes: Routes = [
  { path: '', component: ProposalListComponent },
  { path: 'proposals/create', component: ProposalsCreateComponent}
];
