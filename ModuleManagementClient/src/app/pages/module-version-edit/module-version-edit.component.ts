import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
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
import { LayoutComponent } from '../../components/layout.component';
import { ProposalBaseComponent } from '../../components/create-edit-base/create-edit-base.component';
import { ModuleVersionUpdateRequestDTO, ModuleVersionUpdateResponseDTO } from '../../core/modules/openapi';

@Component({
  selector: 'app-module-version-edit',
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
    LayoutComponent,
    RouterModule,
    AutoResizeDirective,
    HlmAlertDescriptionDirective,
    HlmAlertDirective,
    HlmAlertIconDirective,
    HlmAlertTitleDirective,
    HlmIconComponent
  ],
  providers: [provideIcons({ lucideInfo })],
  templateUrl: '../../components/create-edit-base/create-edit-base.component.html'
})
export class ModuleVersionEditComponent extends ProposalBaseComponent {
  moduleVersionId: number | null = null;
  override moduleVersionDto: ModuleVersionUpdateRequestDTO | null = null;

  constructor(route: ActivatedRoute) {
    super();
    this.moduleVersionId = Number(route.snapshot.paramMap.get('id'));
    this.fetchModuleVersion(this.moduleVersionId);
  }

  async fetchModuleVersion(moduleVersionId: number) {
    this.loading = true;
    this.moduleVersionService.getModuleVersionUpdateDtoFromId(moduleVersionId).subscribe({
      next: (response: ModuleVersionUpdateRequestDTO) => {
        this.proposalForm.patchValue(response);
        this.moduleVersionDto = response;
      },
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }

  override async onSubmit() {
    if (this.proposalForm.valid && this.moduleVersionId) {
      this.loading = true;
      this.error = null;

      const proposalData: ModuleVersionUpdateRequestDTO = {
        ...this.proposalForm.value,
        moduleVersionId: this.moduleVersionId
      };
      
      this.moduleVersionService.updateModuleVersion(this.moduleVersionId, proposalData).subscribe({
        next: (response: ModuleVersionUpdateResponseDTO) => {
          this.proposalForm.reset();
          this.router.navigate(['/proposals/view', response.proposalId], {
            queryParams: { created: true }
          });
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
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