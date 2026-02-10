import { Component, inject, signal } from '@angular/core';
import { Feedback, FeedbackControllerService } from '../../core/modules/openapi';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';

import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { SecurityStore } from '../../core/security/security-store.service';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'feedback-list-table',
  standalone: true,
  imports: [RouterModule, TableModule, FeedbackStatusPipe, TagModule, ButtonModule],
  host: {
    class: 'w-full overflow-x-auto'
  },
  templateUrl: './feedback-list-table.component.html'
})
export class FeedbackListTableComponent {
  feedbackService = inject(FeedbackControllerService);
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
  loading = signal(true);
  error = signal<string | null>(null);
  feedbacks = signal<Feedback[]>([]);
  feedbackStatusEnum = Feedback.StatusEnum;

  constructor() {
    if (this.user() !== undefined) {
      this.fetchFeedbacksForUser();
    } else {
      this.securityStore.signIn();
    }
  }

  private fetchFeedbacksForUser() {
    this.loading.set(true);
    this.feedbackService.getFeedbacksForAuthenticatedUser().subscribe({
      next: (feedbacks) => this.feedbacks.set(feedbacks),
      error: (err: HttpErrorResponse) => this.error.set(err.error),
      complete: () => this.loading.set(false)
    });
  }
}
