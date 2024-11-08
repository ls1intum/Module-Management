import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FeedbackControllerService, ModuleVersionUpdateRequestDTO, RejectFeedbackDTO, UserIdDTO } from '../core/modules/openapi';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-feedback-view',
  standalone: true,
  imports: [ CommonModule, RouterModule, FormsModule],
  templateUrl: './feedback-view.component.html',
  styleUrl: './feedback-view.component.css'
})
export class FeedbackViewComponent {
  feedbackService = inject(FeedbackControllerService);
  feedbackId: number | null = null;
  moduleVersion: ModuleVersionUpdateRequestDTO | null = null;
  loading: boolean = true;
  error: string | null = null;
  reason: string = '';
  reasonRequired: boolean = false;
  users = [
    { id: 3, name: 'QM User' },
    { id: 4, name: 'ASA User' },
    { id: 5, name: 'EB User' }
  ]
  selectedUserId: number = 3;

  constructor(private route: ActivatedRoute, private router: Router) {
    this.feedbackId = Number(route.snapshot.paramMap.get('id'));
    this.fetchModuleVersion(this.feedbackId);
  }

  public async onUserChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    this.selectedUserId = parseInt(selectElement.value, 10);
  }
  private async fetchModuleVersion(feedbackId: number | null){
    this.loading = true;
    if (feedbackId) {
      this.feedbackService.getModuleVersionOfFeedback(feedbackId).subscribe({
        next: (response) => this.moduleVersion = response,
        error: (err) => this.error = err,
        complete: () => this.loading = false
      });
    } else {
      this.error = "Couldn't find feedbackId";
      this.loading = false;
    }
  }

  approveFeedback() {
    if (this.feedbackId) {
      const userIdDto: UserIdDTO = { userId: this.selectedUserId };
      this.feedbackService.approveFeedback(this.feedbackId, userIdDto).subscribe({
        next: () => {
          alert('Feedback approved successfully');
          this.router.navigate(['/feedbacks/for-user'], { queryParams: { created: true } });
        },
        error: (err) => this.error = err
      });
    }
  }

  rejectFeedback() {
    if (!this.reason) {
      this.reasonRequired = true;
      return;
    }
    if (this.feedbackId) {
      const rejectDto: RejectFeedbackDTO = { userId: 3, comment: this.reason};
      this.feedbackService.rejectFeedback(this.feedbackId, rejectDto).subscribe({
        next: () => {
          alert('Feedback rejected with reason: ' + this.reason);
          this.router.navigate(['/feedbacks/for-user'], { queryParams: { created: true } });
        },
        error: (err) => this.error = err
      });
    }
  }
}
