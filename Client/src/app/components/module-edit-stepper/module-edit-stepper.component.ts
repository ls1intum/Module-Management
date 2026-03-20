import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AvatarModule } from 'primeng/avatar';
import type { ModuleEditStepConfig } from './module-edit-steps.config';
import { StepperStatus } from './module-edit-steps.config';

@Component({
  selector: 'app-module-edit-stepper',
  standalone: true,
  imports: [CommonModule, AvatarModule],
  templateUrl: './module-edit-stepper.component.html',
  styleUrl: './module-edit-stepper.component.css'
})
export class ModuleEditStepperComponent {
  readonly StepperStatus = StepperStatus;

  steps = input.required<ModuleEditStepConfig[]>();
  currentIndex = input.required<number>();
  stepStatuses = input.required<StepperStatus[]>();

  stepChange = output<number>();

  goToStep(index: number) {
    this.stepChange.emit(index);
  }

  getStepAvatarStyle(index: number): Record<string, string> {
    const status = this.stepStatuses()[index];
    const base = { 'font-weight': '600' };
    if (status === StepperStatus.Completed) {
      return { ...base, 'background-color': 'var(--p-success-color, #22c55e)', color: 'white' };
    }
    if (status === StepperStatus.Pending) {
      return { ...base, 'background-color': 'var(--p-warn-color, #eab308)', color: 'var(--p-primary-contrast-color, #1a1a1a)' };
    }
    if (status === StepperStatus.ActionRequired) {
      return { ...base, 'background-color': 'var(--p-error-color, #ef4444)', color: 'white' };
    }
    return { ...base, 'background-color': 'var(--p-surface-200, #e9ecef)', color: 'var(--p-text-muted-color, #6c757d)' };
  }
}
