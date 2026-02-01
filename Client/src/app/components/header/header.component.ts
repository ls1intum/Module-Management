import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SecurityStore } from '../../core/security/security-store.service';
import { ThemeService } from '../../core/theme/theme.service';
import { SidebarService } from '../side-bar/sidebar.service';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { TooltipModule } from 'primeng/tooltip';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  standalone: true,
  imports: [RouterLink, ButtonModule, AvatarModule, MenuModule, TooltipModule]
})
export class HeaderComponent {
  securityStore = inject(SecurityStore);
  themeService = inject(ThemeService);
  sidebarService = inject(SidebarService);

  user = this.securityStore.user;
  isDarkMode = this.themeService.isDarkMode;

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

  toggleTheme() {
    this.themeService.toggleTheme();
  }

  signIn() {
    this.securityStore.signIn();
  }

  signOut() {
    this.securityStore.signOut();
  }
}
