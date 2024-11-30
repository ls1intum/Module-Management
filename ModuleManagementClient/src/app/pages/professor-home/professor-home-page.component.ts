import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { ProposalListTableComponent } from '../../components/proposal-list-table/proposal-list-table.component';

@Component({
  selector: 'professor-home-page',
  standalone: true,
  imports: [RouterModule, HlmButtonDirective, ProposalListTableComponent],
  template: `
    <div class="flex justify-between items-center my-4">
      <h3 class="text-xl">Welcome, Professor! Here are your module proposals.</h3>
      <button hlmBtn variant="outline" [routerLink]="['/proposals/create']">Create a new Module Proposal</button>
    </div>
    <proposal-list-table class="p-12"></proposal-list-table>
  `
})
export class ProfessorHomePageComponent {}
