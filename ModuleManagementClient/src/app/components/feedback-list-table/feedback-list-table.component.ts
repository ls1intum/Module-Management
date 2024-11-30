import { Component, Input, inject } from '@angular/core';
import { HlmTableComponent, HlmTrowComponent, HlmThComponent, HlmTdComponent, HlmCaptionComponent } from '@spartan-ng/ui-table-helm';
import { Feedback, FeedbackControllerService } from '../../core/modules/openapi';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmBadgeDirective } from '@spartan-ng/ui-badge-helm';
import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'feedback-list-table',
  standalone: true,
  imports: [
    HlmTableComponent,
    HlmTrowComponent,
    HlmThComponent,
    HlmTdComponent,
    HlmCaptionComponent,
    HlmButtonDirective,
    RouterModule,
    CommonModule,
    HlmBadgeDirective,
    FeedbackStatusPipe
  ],
  host: {
    class: 'w-full overflow-x-auto'
  },
  templateUrl: './feedback-list-table.component.html'
})
export class FeedbackListTableComponent {
  feedbackService = inject(FeedbackControllerService);
  securityStore = inject(SecurityStore);
  user = this.securityStore.loadedUser;
  loading: boolean = true;
  error: string | null = null;
  feedbacks: Feedback[] = [];
  feedbackStatusEnum = Feedback.StatusEnum;

  constructor() {
    if (this.securityStore.signedIn()) {
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
