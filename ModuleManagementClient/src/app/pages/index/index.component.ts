import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';

@Component({
  selector: 'index-component',
  standalone: true,
  imports: [RouterModule, HlmButtonDirective],
  template: `
    <div class="flex flex-col items-center justify-center min-h-screen text-center">
      <h1 class="text-3xl font-bold mb-4">CIT Module Management</h1>
      <p class="mb-6 text-lg">This is a placeholder page, please select a role:</p>
      <div class="flex gap-6">
        <button hlmBtn [routerLink]="['/proposals']">Professor View</button>
        <button hlmBtn [routerLink]="['/feedbacks/for-user/3']">QM Approval Staff View</button>
        <button hlmBtn [routerLink]="['/feedbacks/for-user/4']">ASA Approval Staff View</button>
        <button hlmBtn [routerLink]="['/feedbacks/for-user/5']">EB Approval Staff View</button>
      </div>
    </div>
  `
})
export class IndexComponent {}
