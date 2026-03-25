import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SecurityStore } from '../../core/security/security-store.service';
import { ThemeService } from '../../core/theme/theme.service';
import { SidebarService } from '../side-bar/sidebar.service';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { SignInComponent } from '../sign-in/sign-in.component';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  standalone: true,
  imports: [RouterLink, ButtonModule, TooltipModule, SignInComponent]
})
export class HeaderComponent {
  securityStore = inject(SecurityStore);
  themeService = inject(ThemeService);
  sidebarService = inject(SidebarService);

  user = this.securityStore.user;
  isDarkMode = this.themeService.isDarkMode;

  toggleTheme() {
    this.themeService.toggleTheme();
  }
}
