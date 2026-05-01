import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from './components/header/header.component';
import { SideBarComponent } from './components/side-bar/side-bar.component';
import { BreadcrumbComponent } from './components/breadcrumb/breadcrumb.component';
import { SidebarService } from './components/side-bar/sidebar.service';
import { SecurityStore } from './core/security/security-store.service';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterModule, FormsModule, HeaderComponent, SideBarComponent, BreadcrumbComponent, DialogModule, ButtonModule, CheckboxModule],
  templateUrl: './layout.component.html'
})
export class LayoutComponent {
  sidebarService = inject(SidebarService);
  securityStore = inject(SecurityStore);
  private readonly router = inject(Router);
  user = this.securityStore.user;

  dontShowPasskeyDialogAgain = false;

  onPasskeyDialogVisibleChange(visible: boolean): void {
    if (visible) {
      this.dontShowPasskeyDialogAgain = false;
      return;
    }
    this.securityStore.closePasskeyDialog(this.dontShowPasskeyDialogAgain);
  }

  async registerPasskeyFromDialog(): Promise<void> {
    await this.securityStore.registerPasskey();
    this.securityStore.closePasskeyDialog(this.dontShowPasskeyDialogAgain);
    await this.router.navigate(['/account/passkeys']);
  }

  dismissPasskeyDialog(): void {
    this.securityStore.closePasskeyDialog(this.dontShowPasskeyDialogAgain);
  }
}
