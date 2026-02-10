import { ActivatedRoute, Router, RouterModule } from '@angular/router';

import { Component, inject, signal } from '@angular/core';
import { FeedbackControllerService, ModuleVersionUpdateRequestDTO, FeedbackDTO, GiveFeedbackDTO, ModuleVersionControllerService } from '../../core/modules/openapi';
import { FormBuilder, FormGroup, FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { TextareaModule } from 'primeng/textarea';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TooltipModule } from 'primeng/tooltip';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-feedback-view',
  standalone: true,
  imports: [FormsModule, RouterModule, ButtonModule, TextareaModule, DialogModule, ToastModule, ProgressSpinnerModule, TooltipModule, MessageModule],
  templateUrl: './feedback-view.component.html'
})
export class FeedbackViewComponent {
  router = inject(Router);
  feedbackService = inject(FeedbackControllerService);
  messageService = inject(MessageService);
  moduleVersionService = inject(ModuleVersionControllerService);
  feedbackForm: FormGroup;
  feedbackId: number | null = null;
  moduleVersion = signal<ModuleVersionUpdateRequestDTO | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  rejectionReason: string = '';
  showRejectDialog: boolean = false;

  // utility

  fieldStates: Record<string, { accepted: boolean | null }> = {};
  fieldFeedback: Record<string, string> = {};

  getModuleVersionProperty(key: keyof ModuleVersionUpdateRequestDTO): string | undefined {
    const mv = this.moduleVersion();
    return mv ? mv[key]?.toString() : undefined;
  }

  // prepare form and data

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

  constructor(formBulider: FormBuilder, route: ActivatedRoute) {
    this.moduleFields.forEach((field) => {
      this.fieldStates[field.key] = { accepted: null };
      this.fieldFeedback[field.key] = '';
    });
    this.feedbackForm = formBulider.group({
      titleAccepted: [null],
      titleFeedback: [''],
      levelAccepted: [null],
      levelFeedback: [''],
      languageAccepted: [null],
      languageFeedback: [''],
      frequencyAccepted: [null],
      frequencyFeedback: [''],
      creditsAccepted: [null],
      creditsFeedback: [''],
      durationAccepted: [null],
      durationFeedback: [''],
      hoursTotalAccepted: [null],
      hoursTotalFeedback: [''],
      hoursSelfStudyAccepted: [null],
      hoursSelfStudyFeedback: [''],
      hoursPresenceAccepted: [null],
      hoursPresenceFeedback: [''],
      examinationAchievementsAccepted: [null],
      examinationAchievementsFeedback: [''],
      repetitionAccepted: [null],
      repetitionFeedback: [''],
      recommendedPrerequisitesAccepted: [null],
      recommendedPrerequisitesFeedback: [''],
      contentAccepted: [null],
      contentFeedback: [''],
      learningOutcomesAccepted: [null],
      learningOutcomesFeedback: [''],
      teachingMethodsAccepted: [null],
      teachingMethodsFeedback: [''],
      mediaAccepted: [null],
      mediaFeedback: [''],
      literatureAccepted: [null],
      literatureFeedback: [''],
      responsiblesAccepted: [null],
      responsiblesFeedback: [''],
      lvSwsLecturerAccepted: [null],
      lvSwsLecturerFeedback: ['']
    });
    this.feedbackId = Number(route.snapshot.paramMap.get('id'));
    this.fetchModuleVersion(this.feedbackId);
  }

  // get data

  private fetchModuleVersion(feedbackId: number | null) {
    this.loading.set(true);
    if (feedbackId) {
      this.feedbackService.getModuleVersionOfFeedback(feedbackId).subscribe({
        next: (response: ModuleVersionUpdateRequestDTO) => this.moduleVersion.set(response),
        error: (err: HttpErrorResponse) => this.error.set(err.error),
        complete: () => this.loading.set(false)
      });
    }
  }

  // field methods

  handleApprove(key: string) {
    const formField = key.replace('Eng', '');
    this.fieldStates[key] = { accepted: true };
    this.fieldFeedback[key] = '';
    const acceptedField = `${formField}Accepted`;
    const feedbackField = `${formField}Feedback`;
    this.feedbackForm.get(acceptedField)?.setValue(true);
    this.feedbackForm.get(feedbackField)?.setValue('');
  }

  handleReject(key: string) {
    const formField = key.replace('Eng', '');
    this.fieldStates[key] = { accepted: false };
    const acceptedField = `${formField}Accepted`;
    this.feedbackForm.get(acceptedField)?.setValue(false);
  }

  updateFeedback(key: string, value: string) {
    const formField = key.replace('Eng', '');
    this.fieldFeedback[key] = value;
    this.feedbackForm.patchValue({
      [`${formField}Feedback`]: value
    });
  }

  // form methods

  isEveryFieldFilled(): boolean {
    const formValues = this.feedbackForm.value;
    return Object.keys(formValues)
      .filter((key) => key.endsWith('Accepted'))
      .every((acceptedKey) => {
        const isAccepted = formValues[acceptedKey];
        if (isAccepted === true) {
          return true;
        }
        if (isAccepted === false) {
          const feedbackKey = acceptedKey.replace('Accepted', 'Feedback');
          const feedback = formValues[feedbackKey];
          return feedback && feedback.trim().length > 0;
        }
        return false;
      });
  }

  checkOverlaps() {}

  pdfExport() {
    const mvid = this.moduleVersion()?.moduleVersionId;
    if (!mvid) {
      this.messageService.add({ severity: 'error', summary: 'Exporting PDF', detail: 'Failed to create PDF...' });
      return;
    }

    this.messageService.add({ severity: 'info', summary: 'Exporting PDF', detail: 'Creating a PDF file for you to download...' });

    this.feedbackService.exportModuleVersionPdf(mvid).subscribe({
      next: (response: Blob) => {
        {
          const fileName = `f${this.feedbackId}_mv${mvid}_${this.moduleVersion()?.titleEng}`;
          const blob = new Blob([response], { type: 'application/pdf' });
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = fileName;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(link.href);
          this.messageService.add({ severity: 'success', summary: 'PDF Exported', detail: 'PDF file downloaded successfully' });
        }
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Exporting PDF', detail: 'Failed to create PDF...' });
      }
    });
  }

  reject() {
    if (this.feedbackId && this.rejectionReason.trim().length > 0) {
      const giveFeedbackDTO: GiveFeedbackDTO = { comment: this.rejectionReason };
      this.feedbackService.rejectFeedback(this.feedbackId, giveFeedbackDTO).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Module Proposal Rejected', detail: 'You successfully rejected this Module Proposal' });
          this.showRejectDialog = false;
          this.router.navigate(['']);
        },
        error: (err: HttpErrorResponse) => {
          this.messageService.add({ severity: 'error', summary: 'Rejection failed', detail: err.error || 'Unable to reject module' });
          this.error.set(err.error);
        }
      });
    }
  }

  giveFeedback() {
    if (this.feedbackId) {
      const feedbackDTO: FeedbackDTO = {
        ...this.feedbackForm.value
      };
      this.feedbackService.giveFeedback(this.feedbackId, feedbackDTO).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Feedback Submitted', detail: 'Your feedback has been submitted successfully' });
          this.router.navigate(['']);
        },
        error: (err: HttpErrorResponse) => {
          this.messageService.add({ severity: 'error', summary: 'Sending feedback failed', detail: err.error || 'Unable to send feedback' });
          this.error.set(err.error);
        }
      });
    }
  }
}
