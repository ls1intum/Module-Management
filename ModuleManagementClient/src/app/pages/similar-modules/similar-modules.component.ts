import { Component, inject } from '@angular/core';
import { ModuleVersionControllerService, SimilarModuleDTO } from '../../core/modules/openapi';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { provideIcons } from '@ng-icons/core';
import { lucideInfo } from '@ng-icons/lucide';
import { HlmIconModule } from '@spartan-ng/ui-icon-helm';
import { HlmButtonModule } from '@spartan-ng/ui-button-helm';
import { HlmBadgeModule } from '@spartan-ng/ui-badge-helm';
import { HlmAlertDescriptionDirective, HlmAlertIconDirective, HlmAlertModule, HlmAlertTitleDirective } from '@spartan-ng/ui-alert-helm';

@Component({
  selector: 'similar-modules-page',
  standalone: true,
  imports: [HlmIconModule, RouterModule, HlmButtonModule, HlmBadgeModule, HlmAlertDescriptionDirective, HlmAlertTitleDirective, HlmAlertIconDirective, HlmAlertModule],
  providers: [provideIcons({ lucideInfo })],
  templateUrl: './similar-modules.component.html'
})
export class SimilarModulesPage {
  router = inject(Router);
  route = inject(ActivatedRoute);
  moduleVersionService = inject(ModuleVersionControllerService);
  similarModules: SimilarModuleDTO[] = [];
  isLoading = false;

  constructor() {
    this.route.params.subscribe((param) => {
      const moduleVersionId = Number(param['id']);
      if (moduleVersionId) {
        this.checkSimilarity(moduleVersionId);
      }
    });
  }

  protected async checkSimilarity(moduleVersionId: number) {
    this.isLoading = true;

    this.moduleVersionService.checkSimilarity(moduleVersionId).subscribe({
      next: (response: SimilarModuleDTO[]) => {
        this.similarModules = response;
      },
      error: (err: HttpErrorResponse) => console.log(err),
      complete: () => (this.isLoading = false)
    });

    return;
  }

  goBack() {
    this.router.navigate(['../']);
  }
}
