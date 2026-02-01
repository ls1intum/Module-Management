import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MenubarModule } from 'primeng/menubar';
import type { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-account-layout',
  standalone: true,
  imports: [RouterModule, MenubarModule],
  templateUrl: './account-layout.component.html'
})
export class AccountLayoutComponent {
  menuItems: MenuItem[] = [
    { label: 'Account Information', icon: 'pi pi-user', routerLink: ['information'] },
    { label: 'Passkeys', icon: 'pi pi-key', routerLink: ['passkeys'] }
  ];
}
