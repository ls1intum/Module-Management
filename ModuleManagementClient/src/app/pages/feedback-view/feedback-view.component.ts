import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FeedbackControllerService, ModuleVersionUpdateRequestDTO, RejectFeedbackDTO, UserIdDTO } from '../../core/modules/openapi';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HlmScrollAreaComponent } from '@spartan-ng/ui-scrollarea-helm';
// Spartan/UI Imports
import { BrnSelectModule } from '@spartan-ng/ui-select-brain';
import { HlmSelectModule } from '@spartan-ng/ui-select-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmAlertDescriptionDirective, HlmAlertTitleDirective } from '@spartan-ng/ui-alert-helm';
import { BrnSeparatorModule } from '@spartan-ng/ui-separator-brain';
import { HlmSeparatorDirective } from '@spartan-ng/ui-separator-helm';
import { LayoutComponent } from '../../components/layout.component';
import { HlmFormFieldComponent } from '@spartan-ng/ui-formfield-helm';

@Component({
  selector: 'app-feedback-view',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    HlmScrollAreaComponent,
    BrnSelectModule,
    HlmSelectModule,
    HlmInputDirective,
    HlmButtonDirective,
    BrnSeparatorModule,
    HlmSeparatorDirective,
    LayoutComponent
  ],
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
  ];
  selectedUserId: string = 'TODO ';

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

  constructor(private route: ActivatedRoute, private router: Router) {
    this.feedbackId = Number(route.snapshot.paramMap.get('id'));
    this.fetchModuleVersion(this.feedbackId);
  }

  public async onUserChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    this.selectedUserId = selectElement.value;
    console.log(this.selectedUserId);
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
      const userIdDto: UserIdDTO = { userId: this.selectedUserId };
      this.feedbackService.approveFeedback(this.feedbackId).subscribe({
        next: () => {
          alert('Feedback approved successfully');
          this.router.navigate(['/feedbacks/for-user'], { queryParams: { created: true } });
        },
        error: (err) => (this.error = err)
      });
    }
  }

  rejectFeedback() {
    if (!this.reason) {
      this.reasonRequired = true;
      return;
    }
    if (this.feedbackId) {
      const rejectDto: RejectFeedbackDTO = { comment: this.reason };
      this.feedbackService.rejectFeedback(this.feedbackId, rejectDto).subscribe({
        next: () => {
          alert('Feedback rejected with reason: ' + this.reason);
          this.router.navigate(['/feedbacks/for-user'], { queryParams: { created: true } });
        },
        error: (err) => (this.error = err)
      });
    }
  }
}
