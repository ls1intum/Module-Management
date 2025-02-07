import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="content-container p-12 sm:px-24 max-w-[1400px] mx-auto">
      <ng-content></ng-content>
    </div>
  `
})
export class LayoutComponent {}
