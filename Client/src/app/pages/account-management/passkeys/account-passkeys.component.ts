import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SecurityStore } from '../../../core/security/security-store.service';
import { HlmCardDirective, HlmCardContentDirective, HlmCardDescriptionDirective, HlmCardHeaderDirective, HlmCardTitleDirective } from '@spartan-ng/ui-card-helm';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmIconComponent } from '@spartan-ng/ui-icon-helm';
import { provideIcons } from '@ng-icons/core';
import { lucidePlus, lucideTrash2, lucideKey } from '@ng-icons/lucide';

@Component({
  selector: 'account-passkeys',
  standalone: true,
  imports: [
    CommonModule,
    HlmCardDirective,
    HlmCardHeaderDirective,
    HlmCardTitleDirective,
    HlmCardDescriptionDirective,
    HlmCardContentDirective,
    HlmButtonDirective,
    HlmIconComponent
  ],
  providers: [
    provideIcons({
      lucidePlus,
      lucideTrash2,
      lucideKey
    })
  ],
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
