import { Component, inject, signal } from '@angular/core';
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
  similarModules = signal<SimilarModuleDTO[]>([]);
  isLoading = signal(false);

  constructor() {
    this.route.params.subscribe((param) => {
      const moduleVersionId = Number(param['versionId']);
      if (moduleVersionId) {
        this.checkSimilarity(moduleVersionId);
      }
    });
  }

  protected checkSimilarity(moduleVersionId: number) {
    this.isLoading.set(true);
    this.moduleVersionService.checkSimilarity(moduleVersionId).subscribe({
      next: (response: SimilarModuleDTO[]) => this.similarModules.set(response),
      error: (err: HttpErrorResponse) => console.log(err),
      complete: () => this.isLoading.set(false)
    });
  }
}
