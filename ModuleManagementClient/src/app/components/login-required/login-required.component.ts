import { Component, inject } from '@angular/core';
import { SecurityStore } from '../../core/security/security-store.service';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';

@Component({
  selector: 'login-required',
  standalone: true,
  imports: [HlmButtonDirective],
  template: `
    <p class="text-center text-xl mt-8">You need to log in to use the website.</p>
    <div class="flex justify-center mt-8">
      <button hlmBtn variant="default" (click)="login()">Login/Sign up</button>
    </div>
  `
})
export class LoginRequiredComponent {
  securityStore = inject(SecurityStore);

  login() {
    this.securityStore.keycloakService.login();
  }
}
