import { Component } from '@angular/core';
import { HlmTabsComponent, HlmTabsListComponent, HlmTabsTriggerDirective, HlmTabsContentDirective } from '@spartan-ng/ui-tabs-helm';
import { AccountInformationComponent } from './account-information/account-information.component';
import { AccountPasskeysComponent } from './passkeys/account-passkeys.component';

@Component({
  selector: 'account-management-page',
  standalone: true,
  imports: [
    HlmTabsComponent,
    HlmTabsListComponent,
    HlmTabsTriggerDirective,
    HlmTabsContentDirective,
    AccountInformationComponent,
    AccountPasskeysComponent
  ],
  templateUrl: './account-management-page.component.html'
})
export class AccountManagementPageComponent {
}

