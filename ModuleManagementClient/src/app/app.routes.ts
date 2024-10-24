import { Routes } from '@angular/router';
import { ModuleListComponent } from './module-list/module-list.component';
import { ModuleCreateComponent } from './module-create/module-create.component';
import { ModuleDetailComponent } from './module-detail/module-detail.component';
import { ModuleEditComponent } from './module-edit/module-edit.component';

export const routes: Routes = [
  { path: '', component: ModuleListComponent },
  { path: 'modules', component: ModuleListComponent },
  { path: 'modules/create', component: ModuleCreateComponent },
  { path: 'modules/edit/:id', component: ModuleEditComponent },
  { path: 'modules/id/:id', component: ModuleDetailComponent },
];
