import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { TooltipModule } from 'primeng/tooltip';
import { MenuItem } from 'primeng/api';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'app-sign-in',
  standalone: true,
  imports: [RouterModule, ButtonModule, MenuModule, TooltipModule],
  templateUrl: './sign-in.component.html'
})
export class SignInComponent {
  readonly securityStore = inject(SecurityStore);
  readonly user = this.securityStore.user;

  menuItems: MenuItem[] = [
    {
      label: 'Settings',
      icon: 'pi pi-cog',
      routerLink: '/account'
    },
    {
      separator: true
    },
    {
      label: 'Sign Out',
      icon: 'pi pi-sign-out',
      command: () => this.securityStore.signOut()
    }
  ];
}
