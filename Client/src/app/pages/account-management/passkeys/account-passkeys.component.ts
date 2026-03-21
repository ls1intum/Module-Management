import { Component, inject, signal } from '@angular/core';
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
  passkeyError = signal<string | null>(null);

  async addPasskey() {
    this.passkeyError.set(null);
    try {
      await this.securityStore.registerPasskey(window.location.pathname);
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : 'Passkey registration failed';
      this.passkeyError.set(message);
    }
  }

  async deletePasskey(passkeyId: string) {
    await this.securityStore.deletePasskey(passkeyId);
  }
}
