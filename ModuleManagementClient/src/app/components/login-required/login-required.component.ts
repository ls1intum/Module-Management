import { Component, inject } from '@angular/core';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'login-required',
  standalone: true,
  imports: [],
  template: `
    <p class="text-center text-xl mt-8">You need to log in to use the website.</p>
    <div class="flex justify-center items-center mt-8 space-x-4">
      <a href="https://confluence.ase.in.tum.de/spaces/CITMMAI/pages/236925137/Application+Test+Users" target="_blank">Application Test Users can be found here</a>
    </div>
  `
})
export class LoginRequiredComponent {
  securityStore = inject(SecurityStore);

  login() {
    this.securityStore.keycloakService.login();
  }
}
