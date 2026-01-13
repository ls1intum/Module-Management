import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SecurityStore } from '../../core/security/security-store.service';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  standalone: true,
  imports: [RouterLink, ButtonModule, AvatarModule, MenuModule]
})
export class HeaderComponent {
  securityStore = inject(SecurityStore);

  user = this.securityStore.user;

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
      command: () => this.signOut()
    }
  ];

  signIn() {
    this.securityStore.signIn();
  }

  signOut() {
    this.securityStore.signOut();
  }
}
