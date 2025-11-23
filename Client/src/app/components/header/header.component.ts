import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SecurityStore } from '../../core/security/security-store.service';
import { HlmButtonModule } from '@spartan-ng/ui-button-helm';
import { HlmAvatarModule } from '@spartan-ng/ui-avatar-helm';
import { HlmMenuModule } from '@spartan-ng/ui-menu-helm';
import { BrnMenuTriggerDirective } from '@spartan-ng/ui-menu-brain';
import { HlmIconComponent } from '@spartan-ng/ui-icon-helm';
import { provideIcons } from '@ng-icons/core';
import { lucideLogOut, lucideSettings } from '@ng-icons/lucide';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  standalone: true,
  imports: [RouterLink, HlmButtonModule, HlmAvatarModule, HlmMenuModule, BrnMenuTriggerDirective, HlmIconComponent],
  providers: [
    provideIcons({
      lucideLogOut,
      lucideSettings
    })
  ]
})
export class HeaderComponent {
  securityStore = inject(SecurityStore);

  user = this.securityStore.user;

  signIn() {
    this.securityStore.signIn();
  }

  signOut() {
    this.securityStore.signOut();
  }
}
