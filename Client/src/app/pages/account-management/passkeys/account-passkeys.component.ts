import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SecurityStore } from '../../../core/security/security-store.service';
import { PanelModule } from 'primeng/panel';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'account-passkeys',
  standalone: true,
  imports: [CommonModule, PanelModule, ButtonModule],
  templateUrl: './account-passkeys.component.html'
})
export class AccountPasskeysComponent {
  securityStore = inject(SecurityStore);

  passkeys = this.securityStore.passkeys;

  async addPasskey() {
    await this.securityStore.registerPasskey(window.location.pathname);
  }

  async deletePasskey(passkeyId: string) {
    await this.securityStore.deletePasskey(passkeyId);
  }
}
