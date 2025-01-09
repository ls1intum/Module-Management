import { Component, inject } from '@angular/core';
import { SecurityStore } from '../../core/security/security-store.service';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmTableComponent } from "../../../spartan-components/ui-table-helm/src/lib/hlm-table.component";
import { HlmCaptionComponent } from "../../../spartan-components/ui-table-helm/src/lib/hlm-caption.component";
import { HlmTrowComponent } from "../../../spartan-components/ui-table-helm/src/lib/hlm-trow.component";
import { HlmThComponent } from "../../../spartan-components/ui-table-helm/src/lib/hlm-th.component";
import { HlmTdComponent } from "../../../spartan-components/ui-table-helm/src/lib/hlm-td.component";

@Component({
  selector: 'login-required',
  standalone: true,
  imports: [HlmButtonDirective, HlmTableComponent, HlmTrowComponent, HlmThComponent, HlmTdComponent],
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
