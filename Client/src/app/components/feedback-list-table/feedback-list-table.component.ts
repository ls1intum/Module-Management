import { Component, inject } from '@angular/core';
import { Feedback, FeedbackControllerService } from '../../core/modules/openapi';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FeedbackStatusTagPipe } from '../../pipes/feedbackStatus.pipe';
import { SecurityStore } from '../../core/security/security-store.service';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'feedback-list-table',
  standalone: true,
  imports: [RouterModule, CommonModule, TableModule, FeedbackStatusTagPipe, TagModule, ButtonModule],
  host: {
    class: 'w-full overflow-x-auto'
  },
  templateUrl: './feedback-list-table.component.html'
})
export class FeedbackListTableComponent {
  feedbackService = inject(FeedbackControllerService);
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
  loading: boolean = true;
  error: string | null = null;
  feedbacks: Feedback[] = [];
  feedbackStatusEnum = Feedback.StatusEnum;

  constructor() {
    if (this.user !== undefined) {
      this.fetchFeedbacksForUser();
    } else {
      this.securityStore.signIn();
    }
  }

  private async fetchFeedbacksForUser() {
    this.loading = true;
    this.feedbackService.getFeedbacksForAuthenticatedUser().subscribe({
      next: (feedbacks) => (this.feedbacks = feedbacks),
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }
}
