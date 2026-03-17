import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import type { ModuleEditStepConfig } from './module-edit-steps.config';
import { StepperStatus } from './module-edit-steps.config';

@Component({
  selector: 'app-module-edit-stepper',
  standalone: true,
  imports: [CommonModule, ButtonModule],
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
}
