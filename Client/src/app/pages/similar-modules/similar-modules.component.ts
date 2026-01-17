import { Component, inject } from '@angular/core';
import { ModuleVersionControllerService, SimilarModuleDTO } from '../../core/modules/openapi';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PanelModule } from 'primeng/panel';
import { TextareaModule } from 'primeng/textarea';

@Component({
  selector: 'similar-modules-page',
  standalone: true,
  imports: [RouterModule, ButtonModule, TagModule, MessageModule, ProgressSpinnerModule, PanelModule, TextareaModule],
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
}
