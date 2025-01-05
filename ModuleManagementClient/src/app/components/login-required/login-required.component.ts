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
    <p class="text-center text-lg mt-8">Test users currently available in the system</p>
    <div class="flex justify-center">
    <hlm-table class="w-1/2 mt-8">
     <hlm-trow>
       <hlm-th class="w-52">Username</hlm-th>
       <hlm-th class="w-40">Password</hlm-th>
       <hlm-th class="flex-1">Roles</hlm-th>
     </hlm-trow>
     <hlm-trow>
       <hlm-td class="font-medium w-52">professor1</hlm-td>
       <hlm-td class="w-40">test</hlm-td>
       <hlm-td class="flex-1">Proposal Submitter</hlm-td>
     </hlm-trow>
     <hlm-trow>
       <hlm-td class="font-medium w-52">professor2</hlm-td>
       <hlm-td class="w-40">test</hlm-td>
       <hlm-td class="flex-1">Proposal Submitter</hlm-td>
     </hlm-trow>
     <hlm-trow>
       <hlm-td class="font-medium w-52">apa1</hlm-td>
       <hlm-td class="w-40">test</hlm-td>
       <hlm-td class="flex-1">Proposal Reviewer</hlm-td>
     </hlm-trow>
     <hlm-trow>
       <hlm-td class="font-medium w-52">qm1</hlm-td>
       <hlm-td class="w-40">test</hlm-td>
       <hlm-td class="flex-1">Proposal Reviewer</hlm-td>
     </hlm-trow>
     <hlm-trow>
       <hlm-td class="font-medium w-52">eb1</hlm-td>
       <hlm-td class="w-40">test</hlm-td>
       <hlm-td class="flex-1">Proposal Reviewer</hlm-td>
     </hlm-trow>
   </hlm-table>
    </div>
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
