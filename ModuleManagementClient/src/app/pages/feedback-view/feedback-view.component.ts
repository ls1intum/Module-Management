import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BrnSelectModule } from '@spartan-ng/ui-select-brain';
import { BrnSeparatorModule } from '@spartan-ng/ui-separator-brain';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FeedbackControllerService, ModuleVersionUpdateRequestDTO, RejectFeedbackDTO } from '../../core/modules/openapi';
import { FormsModule } from '@angular/forms';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { HlmScrollAreaComponent } from '@spartan-ng/ui-scrollarea-helm';
import { HlmSelectModule } from '@spartan-ng/ui-select-helm';
import { HlmSeparatorDirective } from '@spartan-ng/ui-separator-helm';
import { HlmToasterComponent } from '@spartan-ng/ui-sonner-helm';
import { toast } from 'ngx-sonner';

@Component({
  selector: 'app-feedback-view',
  standalone: true,
  imports: [
    BrnSelectModule,
    BrnSeparatorModule,
    CommonModule,
    FormsModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmScrollAreaComponent,
    HlmSelectModule,
    HlmSeparatorDirective,
    HlmToasterComponent,
    RouterModule
  ],
  templateUrl: './feedback-view.component.html'
})
export class FeedbackViewComponent {
  feedbackService = inject(FeedbackControllerService);
  feedbackId: number | null = null;
  moduleVersion: ModuleVersionUpdateRequestDTO | null = null;
  loading: boolean = true;
  error: string | null = null;
  reason: string = '';
  reasonRequired: boolean = false;

  getModuleVersionProperty(key: keyof ModuleVersionUpdateRequestDTO): string | undefined {
    return this.moduleVersion ? this.moduleVersion[key]?.toString() : undefined;
  }

  moduleFields = [
    { key: 'titleEng', label: 'Title (EN)' },
    { key: 'levelEng', label: 'Level (EN)' },
    { key: 'languageEng', label: 'Language (EN)' },
    { key: 'frequencyEng', label: 'Frequency (EN)' },
    { key: 'credits', label: 'Credits' },
    { key: 'hoursTotal', label: 'Total Hours' },
    { key: 'hoursSelfStudy', label: 'Self-Study Hours' },
    { key: 'hoursPresence', label: 'Presence Hours' },
    { key: 'examinationAchievementsEng', label: 'Examination Achievements (EN)' },
    { key: 'repetitionEng', label: 'Repetition (EN)' },
    { key: 'recommendedPrerequisitesEng', label: 'Recommended Prerequisites (EN)' },
    { key: 'contentEng', label: 'Content (EN)' },
    { key: 'learningOutcomesEng', label: 'Learning Outcomes (EN)' },
    { key: 'teachingMethodsEng', label: 'Teaching Methods (EN)' },
    { key: 'mediaEng', label: 'Media (EN)' },
    { key: 'literatureEng', label: 'Literature (EN)' },
    { key: 'responsiblesEng', label: 'Responsibles (EN)' },
    { key: 'lvSwsLecturerEng', label: 'Lecturer SWs (EN)' }
  ] as const;

  constructor(route: ActivatedRoute, private router: Router) {
    this.feedbackId = Number(route.snapshot.paramMap.get('id'));
    this.fetchModuleVersion(this.feedbackId);
  }

  private async fetchModuleVersion(feedbackId: number | null) {
    this.loading = true;
    if (feedbackId) {
      this.feedbackService.getModuleVersionOfFeedback(feedbackId).subscribe({
        next: (response) => (this.moduleVersion = response),
        error: (err) => (this.error = err),
        complete: () => (this.loading = false)
      });
    } else {
      this.error = "Couldn't find feedbackId";
      this.loading = false;
    }
  }

  approveFeedback() {
    if (this.feedbackId) {
      this.feedbackService.approveFeedback(this.feedbackId).subscribe({
        next: () => {
          toast('Feedback Approved', {
            description: 'The feedback has been successfully approved.',
            duration: 3000,
            action: {
              label: '',
              onClick: () => {}
            }
          });
          this.router.navigate([''], { queryParams: { created: true } });
        },
        error: (err) => {
          toast('Approval Failed', {
            description: err.message || 'Unable to approve feedback',
            duration: 3000
          });
          this.error = err;
        }
      });
    }
  }

  rejectFeedback() {
    if (!this.reason) {
      this.reasonRequired = true;
      toast('Reason Required', {
        description: 'Please provide a reason for rejecting the feedback.',
        duration: 3000
      });
      return;
    }

    if (this.feedbackId) {
      const rejectDto: RejectFeedbackDTO = { comment: this.reason };
      this.feedbackService.rejectFeedback(this.feedbackId, rejectDto).subscribe({
        next: () => {
          toast('Rejected', {
            description: 'The feedback has been successfully approved.',
            duration: 3000
          });
          this.router.navigate(['/feedbacks/for-user'], { queryParams: { created: true } });
        },
        error: (err) => {
          toast('Rejection Failed', {
            description: err.message || 'Unable to reject feedback',
            duration: 3000
          });
          this.error = err;
        }
      });
    }
  }
}
