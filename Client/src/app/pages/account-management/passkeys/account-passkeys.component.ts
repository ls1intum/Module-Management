import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SecurityStore } from '../../../core/security/security-store.service';
import { PanelModule } from 'primeng/panel';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'account-passkeys',
  standalone: true,
  imports: [CommonModule, PanelModule, ButtonModule],
  templateUrl: './account-passkeys.component.html'
})
export class AccountPasskeysComponent {
  securityStore = inject(SecurityStore);
  private readonly messageService = inject(MessageService);

  passkeys = this.securityStore.passkeys;

  async addPasskey() {
    try {
      await this.securityStore.registerPasskey(window.location.pathname);
    } catch (e: unknown) {
      const detail = e instanceof Error ? e.message : 'Passkey registration failed';
      this.messageService.add({
        severity: 'error',
        summary: 'Passkey registration',
        detail
      });
    }
  }

  async deletePasskey(passkeyId: string) {
    await this.securityStore.deletePasskey(passkeyId);
  }
}
