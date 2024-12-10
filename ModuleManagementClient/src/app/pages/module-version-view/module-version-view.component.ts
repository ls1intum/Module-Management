import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModuleVersionControllerService, ModuleVersionViewDTO } from '../../core/modules/openapi';
import { RouterModule } from '@angular/router';
import { HlmScrollAreaComponent } from '@spartan-ng/ui-scrollarea-helm';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'module-version-view',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HlmScrollAreaComponent
  ],
  templateUrl: './module-version-view.component.html'
})
export class ModuleVersionViewComponent {
  moduleVerisionService = inject(ModuleVersionControllerService);
  loading = true;
  moduleVersionDto: ModuleVersionViewDTO | null = null;
  error: string | null = null;

moduleFields = [
  { key: 'titleEng', label: 'Title (EN)' }
] as { key: keyof ModuleVersionViewDTO; label: string }[];


  constructor() {
    const moduleVersionId = Number(window.location.pathname.split('/').pop());
    this.fetchModuleVersionViewDto(moduleVersionId);
  }

  private fetchModuleVersionViewDto(moduleVersionId: number) {
    this.loading = true;
    this.moduleVerisionService.getModuleVersionViewDto(moduleVersionId).subscribe({
      next: (data: ModuleVersionViewDTO) => (this.moduleVersionDto = data),
      error: (err: HttpErrorResponse) => (this.error = err.error),
      complete: () => (this.loading = false)
    });
  }

getModuleVersionProperty(key: keyof ModuleVersionViewDTO): string {
  const value = this.moduleVersionDto?.[key];
  return value == null ? '' : String(value);
}
}
