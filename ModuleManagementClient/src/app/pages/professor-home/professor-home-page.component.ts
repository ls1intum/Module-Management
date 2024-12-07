import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { ProposalListTableComponent } from '../../components/proposal-list-table/proposal-list-table.component';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'professor-home-page',
  standalone: true,
  imports: [RouterModule, HlmButtonDirective, ProposalListTableComponent],
  template: `
    <div class="flex justify-between items-center my-4">
      <h3 class="text-xl">Welcome, {{ user()?.name }}!</h3>
      <button hlmBtn variant="outline" [routerLink]="['/proposals/create']">Create a new Module Proposal</button>
    </div>
    <div class="w-2/3">
      <p>
        To offer a new module, please create a new Module Proposal.
        <br />
        After submitting the proposal the necessary entities will be notified and evaluate your proposal.
      </p>
    </div>
    <div>
      <h3 class="text-xl mt-8">Here are your module proposals:</h3>
    </div>
    <proposal-list-table class="p-12"></proposal-list-table>
  `
})
export class ProfessorHomePageComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.loadedUser;
}
