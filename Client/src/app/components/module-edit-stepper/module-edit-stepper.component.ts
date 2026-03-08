import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import type { ModuleEditStepConfig } from './module-edit-steps.config';
import type { ModuleVersionViewFeedbackDTO } from '../../core/modules/openapi';
import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';

@Component({
  selector: 'app-module-edit-stepper',
  standalone: true,
  imports: [CommonModule, ButtonModule, FeedbackStatusPipe],
  templateUrl: './module-edit-stepper.component.html',
  styleUrl: './module-edit-stepper.component.css'
})
export class ModuleEditStepperComponent {
  steps = input.required<ModuleEditStepConfig[]>();
  currentIndex = input.required<number>();
  stepCompleted = input.required<boolean[]>();
  feedbacks = input<ModuleVersionViewFeedbackDTO[] | undefined>([]);
  status = input<string | undefined>();

  stepChange = output<number>();

  goToStep(index: number) {
    this.stepChange.emit(index);
  }
}
