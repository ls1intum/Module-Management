import { Component, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CitModule } from '../core/modules/openapi';
import { lastValueFrom } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

interface CitModuleExtended extends CitModule {
  englishTranslation?: { title?: string };
  germanTranslation?: { title?: string };
}

@Component({
  selector: 'app-module-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './module-list.component.html',
  styleUrls: ['./module-list.component.css'],
})

export class ModuleListComponent {
  moduleService = inject(HttpClient);
  citModules: CitModuleExtended[] = [];
  loading: boolean = true;
  error: string | null = null;

  constructor() {
    this.fetchModules();
  }

  private async fetchModules() {
    try {
      const modules = await lastValueFrom(this.moduleService.get<CitModule[]>('http://localhost:8080/api/modules'));
      this.citModules = modules.map(module => ({
        ...module,
        englishTranslation: module.translations?.find(t => t.language === 'en'),
        germanTranslation: module.translations?.find(t => t.language === 'de')
      }));
    } catch (err) {
      this.error = 'Failed to load modules';
      console.error(err);
    } finally {
      this.loading = false;
    }
  }

  async deleteModule(id: number) {
    const confirmDelete = confirm("Are you sure you want to delete this module?");
    if (confirmDelete) {
      try {
        await lastValueFrom(this.moduleService.delete(`http://localhost:8080/api/modules/${id}`));
        this.citModules = this.citModules.filter(module => module.id !== id);
      } catch (err) {
        console.error('Failed to delete module', err);
        this.error = 'Failed to delete module';
      }
    }
  }
}
