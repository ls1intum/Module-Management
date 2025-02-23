import { Component, inject, OnInit } from '@angular/core';
import { FeedbackControllerService, Feedback, FeedbackListItemDto } from '../../core/modules/openapi';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmTableComponent } from '../../../spartan-components/ui-table-helm/src/lib/hlm-table.component';
import { HlmCaptionComponent } from '../../../spartan-components/ui-table-helm/src/lib/hlm-caption.component';
import { HlmTrowComponent } from '../../../spartan-components/ui-table-helm/src/lib/hlm-trow.component';
import { FeedbackStatusPipe } from '../../pipes/feedbackStatus.pipe';
import { HlmTdComponent, HlmThComponent } from '@spartan-ng/ui-table-helm';
import { HlmBadgeDirective } from '@spartan-ng/ui-badge-helm';
import { SecurityStore } from '../../core/security/security-store.service';
import { toast } from 'ngx-sonner';
import { HlmToasterComponent } from '@spartan-ng/ui-sonner-helm';

@Component({
  selector: 'approval-staff-home-page',
  standalone: true,
  imports: [FeedbackStatusPipe, HlmButtonDirective, HlmTableComponent, HlmTdComponent, HlmThComponent, HlmBadgeDirective, HlmTrowComponent, RouterModule, HlmToasterComponent],
  templateUrl: './approval-staff-home-page.component.html'
})
export class ApprovalStaffHomePageComponent implements OnInit {
  securityStore = inject(SecurityStore);
  feedbackService = inject(FeedbackControllerService);
  loading: boolean = true;
  error: string | null = null;
  feedbacks: FeedbackListItemDto[] = [];
  feedbackStatusEnum = Feedback.StatusEnum;
  user = this.securityStore.loadedUser;

  constructor(private route: ActivatedRoute) {
    this.fetchFeedbacksForUser();
  }

    ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['feedback_given'] === 'true') {
      toast.success('Feedback Submitted', {
        description: 'Your feedback has been submitted successfully'
        });
      }
      if (params['rejected'] === 'true') {
        toast.success('Module Proposal Rejected', {
          description: 'You sucessfully rejected this Module Proposal'
        });
      }
    });
  }

  private async fetchFeedbacksForUser() {
    this.loading = true;
    this.feedbackService.getFeedbacksForAuthenticatedUser().subscribe({
      next: (feedbacks: FeedbackListItemDto[]) => (this.feedbacks = feedbacks),
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }
}
