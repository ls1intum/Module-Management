import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { ProposalListTableComponent } from '../../components/proposal-list-table/proposal-list-table.component';
import { SecurityStore } from '../../core/security/security-store.service';
import { HlmAlertDescriptionDirective, HlmAlertDirective, HlmAlertIconDirective, HlmAlertTitleDirective } from '@spartan-ng/ui-alert-helm';
import { HlmIconComponent } from '@spartan-ng/ui-icon-helm';
import { provideIcons } from '@ng-icons/core';
import { lucideInfo } from '@ng-icons/lucide';

@Component({
  selector: 'professor-home-page',
  standalone: true,
  imports: [
    RouterModule,
    HlmButtonDirective,
    ProposalListTableComponent,
    HlmAlertDescriptionDirective,
    HlmAlertDirective,
    HlmAlertIconDirective,
    HlmAlertTitleDirective,
    HlmIconComponent
  ],
  providers: [provideIcons({ lucideInfo })],
  templateUrl: './professor-home-page.component.html'
})
export class ProfessorHomePageComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
}
