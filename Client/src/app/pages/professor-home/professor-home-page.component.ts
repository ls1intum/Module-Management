import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProposalListTableComponent } from '../../components/proposal-list-table/proposal-list-table.component';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'professor-home-page',
  standalone: true,
  imports: [
    RouterModule,
    ButtonModule,
    MessageModule,
    ProposalListTableComponent
  ],
  templateUrl: './professor-home-page.component.html'
})
export class ProfessorHomePageComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
}
