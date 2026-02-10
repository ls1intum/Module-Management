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

  formattedRoles(): string {
    const roles = this.user()?.roles;
    if (!roles?.length) return '';
    return roles
      .map((r) =>
        r
          .replace(/_/g, ' ')
          .toLowerCase()
          .replace(/\b\w/g, (c) => c.toUpperCase())
      )
      .join(', ');
  }
}
