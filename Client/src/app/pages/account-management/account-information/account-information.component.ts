import { Component, inject } from '@angular/core';
import { SecurityStore } from '../../../core/security/security-store.service';
import { PanelModule } from 'primeng/panel';

@Component({
  selector: 'account-information',
  standalone: true,
  imports: [PanelModule],
  templateUrl: './account-information.component.html'
})
export class AccountInformationComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
}
