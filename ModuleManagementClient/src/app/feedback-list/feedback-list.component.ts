import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Feedback, FeedbackControllerService } from '../core/modules/openapi';

@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './feedback-list.component.html',
  styleUrl: './feedback-list.component.css'
})
export class FeedbackListComponent {
  feedbackService = inject(FeedbackControllerService);
  feedbacks: Feedback[] = [];
  feedbackStatusEnum = Feedback.StatusEnum;
  users = [
    { id: 3, name: 'QM User' },
    { id: 4, name: 'ASA User' },
    { id: 5, name: 'EB User' },
  ]
  loading: boolean = true;
  error: string | null = null;
  selectedUserId: number = 3;

  constructor() {
    this.fetchFeedbacksForUser(this.selectedUserId);
  }

  public async onUserChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    const userId = parseInt(selectElement.value, 10);
    await this.fetchFeedbacksForUser(userId);
  }

  public async fetchFeedbacksForUser(userId: number) {
    this.loading = true;
    this.feedbackService.getFeedbacksForUser(userId).subscribe({
      next: (feedbacks) => this.feedbacks = feedbacks,
      error: (err) => this.error = err,
      complete: () => this.loading = false
    })
  }
}
