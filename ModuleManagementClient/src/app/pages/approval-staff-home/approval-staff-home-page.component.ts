import { Component, inject } from '@angular/core';
import { FeedbackControllerService, Feedback, FeedbackListItemDto } from '../../core/modules/openapi';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { LayoutComponent } from '../../components/layout.component';
import { HlmTableComponent } from '../../../spartan-components/ui-table-helm/src/lib/hlm-table.component';
import { HlmCaptionComponent } from '../../../spartan-components/ui-table-helm/src/lib/hlm-caption.component';
import { HlmTrowComponent } from '../../../spartan-components/ui-table-helm/src/lib/hlm-trow.component';
import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { HlmTdComponent, HlmThComponent } from '@spartan-ng/ui-table-helm';
import { HlmBadgeDirective } from '@spartan-ng/ui-badge-helm';

@Component({
  selector: 'approval-staff-home-page',
  standalone: true,
  imports: [
    FeedbackStatusPipe,
    HlmButtonDirective,
    HlmCaptionComponent,
    HlmTableComponent,
    HlmTdComponent,
    HlmThComponent,
    HlmBadgeDirective,
    HlmTrowComponent,
    LayoutComponent,
    RouterModule
  ],
  templateUrl: './approval-staff-home-page.component.html'
})
export class ApprovalStaffHomePageComponent {
  feedbackService = inject(FeedbackControllerService);
  loading: boolean = true;
  error: string | null = null;
  feedbacks: FeedbackListItemDto[] = [];
  feedbackStatusEnum = Feedback.StatusEnum;
  userId: string | null = '565cf992-cfe8-4f28-9f2c-d74eca826403';

  constructor(private route: ActivatedRoute) {
    this.userId = route.snapshot.paramMap.get('id')!;
    this.fetchFeedbacksForUser(this.userId);
  }

  public async onUserChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    const userId = selectElement.value;
    await this.fetchFeedbacksForUser(userId);
  }

  private async fetchFeedbacksForUser(userId: string) {
    this.loading = true;
    this.feedbackService.getFeedbacksForUser(userId).subscribe({
      next: (feedbacks: FeedbackListItemDto[]) => (this.feedbacks = feedbacks),
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }
}
