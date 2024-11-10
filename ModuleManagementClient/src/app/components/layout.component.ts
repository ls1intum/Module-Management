import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterModule, HlmButtonDirective],
  template: `
    <div class="navbar bg-blue-100 p-4 flex justify-between items-center">
      <a hlmBtn variant="link" class="text-xl font-bold" [routerLink]="['/']">CIT Module Management</a>
      <ng-content select="[slot=navbar]"></ng-content>
    </div>
    <div class="content-container p-8 sm:px-24">
      <ng-content></ng-content>
    </div>
  `
})
export class LayoutComponent {}
