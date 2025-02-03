import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmFormFieldModule } from '@spartan-ng/ui-formfield-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { BrnSelectImports } from '@spartan-ng/ui-select-brain';
import { HlmSelectImports } from '@spartan-ng/ui-select-helm';
import { HlmLabelDirective } from '@spartan-ng/ui-label-helm';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AutoResizeDirective } from '../../core/shared/autoresize.directive';
import { HlmAlertDescriptionDirective, HlmAlertDirective, HlmAlertIconDirective, HlmAlertTitleDirective } from '@spartan-ng/ui-alert-helm';
import { HlmIconComponent } from '@spartan-ng/ui-icon-helm';
import { provideIcons } from '@ng-icons/core';
import { lucideInfo } from '@ng-icons/lucide';
import { ProposalBaseComponent } from '../../components/create-edit-base/create-edit-base.component';
import { FeedbackDepartmentPipe } from '../../pipes/feedbackDepartment.pipe';
import { HlmSeparatorModule } from '@spartan-ng/ui-separator-helm';
import { BrnSeparatorComponent } from '@spartan-ng/ui-separator-brain';

@Component({
  selector: 'app-proposal-create',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    HlmButtonDirective,
    HlmFormFieldModule,
    HlmInputDirective,
    BrnSelectImports,
    HlmSelectImports,
    HlmLabelDirective,
    RouterModule,
    AutoResizeDirective,
    HlmAlertDescriptionDirective,
    HlmAlertDirective,
    HlmAlertIconDirective,
    HlmAlertTitleDirective,
    HlmIconComponent,
    FeedbackDepartmentPipe,
    HlmSeparatorModule,
    BrnSeparatorComponent
  ],
  providers: [provideIcons({ lucideInfo })],
  templateUrl: '../../components/create-edit-base/create-edit-base.component.html'
})
export class ProposalCreateComponent extends ProposalBaseComponent {
  override async onSubmit() {
    if (this.proposalForm.valid) {
      this.loading = true;
      this.error = null;

      const proposalData = this.proposalForm.value;
      this.proposalService.createProposal(proposalData).subscribe({
        next: (response) => {
          this.proposalForm.reset();
          this.router.navigate(['/proposals/view', response.proposalId], {
            queryParams: { created: true }
          });
        },
        error: (err: HttpErrorResponse) => {
          this.error = err.error;
          this.loading = false;
        },
        complete: () => {
          this.loading = false;
        }
      });
    }
  }
}
