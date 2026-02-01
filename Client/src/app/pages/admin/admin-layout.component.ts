import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { PanelModule } from 'primeng/panel';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [PanelModule, ButtonModule, RouterModule],
  templateUrl: './admin-layout.component.html'
})
export class AdminLayoutComponent {
  private router = inject(Router);

  isActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }
}
