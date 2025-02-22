import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BrnSelectModule } from '@spartan-ng/ui-select-brain';
import { BrnSeparatorModule } from '@spartan-ng/ui-separator-brain';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FeedbackControllerService, ModuleVersionUpdateRequestDTO, FeedbackDTO, GiveFeedbackDTO } from '../../core/modules/openapi';
import { FormBuilder, FormGroup, FormsModule } from '@angular/forms';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { HlmSelectModule } from '@spartan-ng/ui-select-helm';
import { toast } from 'ngx-sonner';

import { BrnAlertDialogContentDirective, BrnAlertDialogTriggerDirective } from '@spartan-ng/ui-alertdialog-brain';
import {
  HlmAlertDialogActionButtonDirective,
  HlmAlertDialogCancelButtonDirective,
  HlmAlertDialogComponent,
  HlmAlertDialogContentComponent,
  HlmAlertDialogDescriptionDirective,
  HlmAlertDialogFooterComponent,
  HlmAlertDialogHeaderComponent,
  HlmAlertDialogTitleDirective
} from '@spartan-ng/ui-alertdialog-helm';
import { HttpErrorResponse } from '@angular/common/http';
import { lucideCheck, lucideX } from '@ng-icons/lucide';
import { provideIcons } from '@ng-icons/core';
import { HlmIconComponent } from "../../../spartan-components/ui-icon-helm/src/lib/hlm-icon.component";

@Component({
  selector: 'app-feedback-view',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    BrnSelectModule,
    BrnSeparatorModule,
    HlmButtonDirective,
    HlmInputDirective,
    HlmSelectModule,
    BrnAlertDialogTriggerDirective,
    BrnAlertDialogContentDirective,
    HlmAlertDialogComponent,
    HlmAlertDialogHeaderComponent,
    HlmAlertDialogFooterComponent,
    HlmAlertDialogTitleDirective,
    HlmAlertDialogDescriptionDirective,
    HlmAlertDialogCancelButtonDirective,
    HlmAlertDialogActionButtonDirective,
    HlmAlertDialogContentComponent,
    HlmIconComponent
],
  providers: [provideIcons({ lucideCheck, lucideX })],
  templateUrl: './feedback-view.component.html'
})
export class FeedbackViewComponent {
  router = inject(Router);
  feedbackService = inject(FeedbackControllerService);
  protected formBuilder = inject(FormBuilder);
  feedbackForm: FormGroup;
  feedbackId: number | null = null;
  moduleVersion: ModuleVersionUpdateRequestDTO | null = null;
  loading: boolean = true;
  error: string | null = null;
  reason: string = '';
  reasonRequired: boolean = false;

  showFeedbackForm: boolean = false;
  showRejectForm: boolean = false;
  feedbackReason: string = '';
  rejectionReason: string = '';

  fieldStates: Record<string, { accepted: boolean | null }> = {};
  fieldFeedback: Record<string, string> = {};

  handleApprove(key: string) {
    this.fieldStates[key] = { accepted: true };
    this.fieldFeedback[key] = '';
    this.feedbackForm.patchValue({
      [`${key}Accepted`]: true,
      [`${key}Feedback`]: ''
    });
  }

  handleReject(key: string) {
    this.fieldStates[key] = { accepted: false };
    this.feedbackForm.patchValue({
      [`${key}Accepted`]: false
    });
  }

  updateFeedback(key: string, value: string) {
    this.fieldFeedback[key] = value;
    this.feedbackForm.patchValue({
      [`${key}Feedback`]: value
    });
  }

  getModuleVersionProperty(key: keyof ModuleVersionUpdateRequestDTO): string | undefined {
    return this.moduleVersion ? this.moduleVersion[key]?.toString() : undefined;
  }

  moduleFields = [
    { key: 'titleEng', label: 'Title' },
    { key: 'levelEng', label: 'Level' },
    { key: 'languageEng', label: 'Language' },
    { key: 'frequencyEng', label: 'Frequency' },
    { key: 'credits', label: 'Credits' },
    { key: 'duration', label: 'Duration' },
    { key: 'hoursTotal', label: 'Total Hours' },
    { key: 'hoursSelfStudy', label: 'Self-Study Hours' },
    { key: 'hoursPresence', label: 'Presence Hours' },
    { key: 'examinationAchievementsEng', label: 'Examination Achievements' },
    { key: 'repetitionEng', label: 'Repetition' },
    { key: 'recommendedPrerequisitesEng', label: 'Recommended Prerequisites' },
    { key: 'contentEng', label: 'Content' },
    { key: 'learningOutcomesEng', label: 'Learning Outcomes' },
    { key: 'teachingMethodsEng', label: 'Teaching Methods' },
    { key: 'mediaEng', label: 'Media' },
    { key: 'literatureEng', label: 'Literature' },
    { key: 'responsiblesEng', label: 'Responsibles' },
    { key: 'lvSwsLecturerEng', label: 'Lecturer SWs' }
  ] as const;

  constructor(route: ActivatedRoute) {
    this.feedbackForm = this.formBuilder.group({
      titleAccepted: [],
      titleFeedback: [''],
      levelAccepted: [],
      levelFeedback: [''],
      languageAccepted: [],
      language_feedback: [''],
      frequencyAccepted: [],
      frequencyFeedback: [''],
      creditsAccepted: [],
      creditsFeedback: [''],
      durationAccepted: [],
      durationFeedback: [''],
      hoursTotalAccepted: [],
      hoursTotalFeedback: [''],
      hoursSelfStudyAccepted: [],
      hoursSelfStudyFeedback: [''],
      hoursPresenceAccepted: [],
      hoursPresenceFeedback: [''],
      examinationAchievementsAccepted: [],
      examinationAchievementsFeedback: [''],
      repetitionAccepted: [],
      repetitionFeedback: [''],
      contentAccepted: [],
      contentFeedback: [''],
      learningOutcomesAccepted: [],
      learningOutcomesFeedback: [''],
      teachingMethodsAccepted: [],
      teachingMethodsFeedback: [''],
      mediaAccepted: [],
      mediaFeedback: [''],
      literatureAccepted: [],
      literatureFeedback: [''],
      responsiblesAccepted: [],
      responsiblesFeedback: [''],
      lvSwsLecturerAccepted: [],
      lvSwsLecturerFeedback: [''],
    });
    this.feedbackId = Number(route.snapshot.paramMap.get('id'));
    this.fetchModuleVersion(this.feedbackId);
  }

  private async fetchModuleVersion(feedbackId: number | null) {
    this.loading = true;
    if (feedbackId) {
      this.feedbackService.getModuleVersionOfFeedback(feedbackId).subscribe({
        next: (response: ModuleVersionUpdateRequestDTO) => (this.moduleVersion = response),
        error: (err: HttpErrorResponse) => (this.error = err.error),
        complete: () => (this.loading = false)
      });
    }
  }

  // Form control methods
  openFeedbackForm() {
    this.showFeedbackForm = true;
    this.showRejectForm = false;
    this.rejectionReason = '';
  }

  openRejectForm() {
    this.showRejectForm = true;
    this.showFeedbackForm = false;
    this.feedbackReason = '';
  }

  cancelFeedback() {
    this.showFeedbackForm = false;
    this.feedbackReason = '';
  }

  cancelReject() {
    this.showRejectForm = false;
    this.rejectionReason = '';
  }

  cancel() {
    this.router.navigate(['']);
  }

  submit() {

  }

  hasRejectedFields() {
    return false;
  }

  hasApprovedFields() {
    return true;
  }

  giveFeedback() {
    if (this.feedbackId && this.feedbackReason.trim().length > 0) {
      const feedbackDTO: FeedbackDTO = {
        ...this.feedbackForm.value,
      }
      this.feedbackService.giveFeedback(this.feedbackId, feedbackDTO).subscribe({
        next: () => {
          this.router.navigate([''], { queryParams: { feedback_given: true } });
        },
        error: (err: HttpErrorResponse) => {
          toast('Sending feedback failed.', {
            description: err.error || 'Unable to send feedback',
            duration: 3000
          });
          this.error = err.error;
        }
      });
    }
  }

  reject() {
    if (this.feedbackId && this.rejectionReason.trim().length > 0) {
      const giveFeedbackDTO: GiveFeedbackDTO = { comment: this.rejectionReason };
      this.feedbackService.rejectFeedback(this.feedbackId, giveFeedbackDTO).subscribe({
        next: () => {
          this.router.navigate([''], { queryParams: { rejected: true } });
        },
        error: (err: HttpErrorResponse) => {
          toast('Rejection failed.', {
            description: err.error || 'Unable to reject module',
            duration: 3000
          });
          this.error = err.error;
        }
      });
    }
  }
}
