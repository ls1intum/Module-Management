import { Component, inject } from '@angular/core';
import { SecurityStore } from '../../../core/security/security-store.service';
import { HlmCardDirective, HlmCardContentDirective, HlmCardDescriptionDirective, HlmCardHeaderDirective, HlmCardTitleDirective } from '@spartan-ng/ui-card-helm';

@Component({
  selector: 'account-information',
  standalone: true,
  imports: [HlmCardDirective, HlmCardHeaderDirective, HlmCardTitleDirective, HlmCardDescriptionDirective, HlmCardContentDirective],
  templateUrl: './account-information.component.html'
})
export class AccountInformationComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
}
