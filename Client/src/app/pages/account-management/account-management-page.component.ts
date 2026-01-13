import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { PanelModule } from 'primeng/panel';
import { ButtonModule } from 'primeng/button';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'account-management-page',
  standalone: true,
  imports: [PanelModule, ButtonModule, RouterModule],
  templateUrl: './account-management-page.component.html'
})
export class AccountManagementPageComponent {
  private router = inject(Router);

  isActive(path: string): boolean {
    return this.router.url === path;
  }
}
