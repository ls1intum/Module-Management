import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BrnSelectModule } from '@spartan-ng/ui-select-brain';
import { BrnSeparatorModule } from '@spartan-ng/ui-separator-brain';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FeedbackControllerService, ModuleVersionUpdateRequestDTO, FeedbackDTO, GiveFeedbackDTO, ModuleVersionControllerService } from '../../core/modules/openapi';
import { FormBuilder, FormGroup, FormsModule } from '@angular/forms';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { HlmSelectModule } from '@spartan-ng/ui-select-helm';
import { toast } from 'ngx-sonner';
import { BrnAlertDialogContentDirective, BrnAlertDialogTriggerDirective } from '@spartan-ng/ui-alertdialog-brain';
import { HlmAlertDialogComponent, HlmAlertDialogContentComponent, HlmAlertDialogDescriptionDirective, HlmAlertDialogFooterComponent, HlmAlertDialogHeaderComponent, HlmAlertDialogTitleDirective } from '@spartan-ng/ui-alertdialog-helm';
import { HttpErrorResponse } from '@angular/common/http';
import { lucideCheck, lucideX } from '@ng-icons/lucide';
import { provideIcons } from '@ng-icons/core';
import { HlmIconComponent } from "../../../spartan-components/ui-icon-helm/src/lib/hlm-icon.component";
import { BrnDialogCloseDirective } from '@spartan-ng/ui-dialog-brain';
import { HlmToasterComponent } from '@spartan-ng/ui-sonner-helm';

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
    BrnDialogCloseDirective,
    HlmAlertDialogComponent,
    HlmAlertDialogHeaderComponent,
    HlmAlertDialogFooterComponent,
    HlmAlertDialogTitleDirective,
    HlmAlertDialogDescriptionDirective,
    HlmAlertDialogContentComponent,
    HlmIconComponent,
    HlmToasterComponent
],
  providers: [provideIcons({ lucideCheck, lucideX })],
  templateUrl: './feedback-view.component.html'
})
export class FeedbackViewComponent {
  router = inject(Router);
  feedbackService = inject(FeedbackControllerService);
  moduleVersionService = inject(ModuleVersionControllerService)
  feedbackForm: FormGroup;
  feedbackId: number | null = null;
  moduleVersion: ModuleVersionUpdateRequestDTO | null = null;
  loading: boolean = true;
  error: string | null = null;
  rejectionReason: string = '';

  // utility

  fieldStates: Record<string, { accepted: boolean | null }> = {};
  fieldFeedback: Record<string, string> = {};

  getModuleVersionProperty(key: keyof ModuleVersionUpdateRequestDTO): string | undefined {
    return this.moduleVersion ? this.moduleVersion[key]?.toString() : undefined;
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
    this.moduleFields.forEach(field => {
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
      lvSwsLecturerFeedback: [''],
    });
    this.feedbackId = Number(route.snapshot.paramMap.get('id'));
    this.fetchModuleVersion(this.feedbackId);
  }

  // get data

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
    .filter(key => key.endsWith('Accepted'))
    .every(acceptedKey => {
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

  cancel() {
    this.router.navigate(['']);
  }

  checkOverlaps() {
    
  }

  pdfExport() {
    const mvid = this.moduleVersion?.moduleVersionId;
    if (!mvid) {
      toast('Exporting PDF', {
        description: 'Failed to create PDF...',
        duration: 3000
      })
      return;
    }
    
    this.feedbackService.exportModuleVersionPdf(mvid).subscribe({
      next: (response: Blob) => {
        {
          const fileName = `f${this.feedbackId}_mv${mvid}_${this.moduleVersion?.titleEng}`
          const blob = new Blob([response], { type: 'application/pdf' });
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = fileName;
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          URL.revokeObjectURL(link.href);
        }
      },
    error: () => {
      toast('Exporting PDF', {
        description: 'Failed to create PDF...',
        duration: 3000
      });
    }
    })

    toast('Exporting PDF', {
      description: 'Creating a PDF file for you to download...',
      duration: 3000
    })

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

  giveFeedback() {
    if (this.feedbackId) {
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
}
