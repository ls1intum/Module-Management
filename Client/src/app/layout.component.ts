import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from './components/header/header.component';
import { SideBarComponent } from './components/side-bar/side-bar.component';
import { SidebarService } from './components/side-bar/sidebar.service';
import { SecurityStore } from './core/security/security-store.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterModule, HeaderComponent, SideBarComponent],
  templateUrl: './layout.component.html'
})
export class LayoutComponent {
  sidebarService = inject(SidebarService);
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
}
