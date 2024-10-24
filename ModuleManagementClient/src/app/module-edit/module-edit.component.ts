import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-module-edit',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './module-edit.component.html',
  styleUrls: ['./module-edit.component.css'],
})
export class ModuleEditComponent {
  moduleId: string | null = null;
  module: any = { translations: [] }; // Initialize with an empty translations array
  loading: boolean = true;
  error: string | null = null;

  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  constructor() {
    this.moduleId = this.route.snapshot.paramMap.get('id');
    if (this.moduleId) {
      this.fetchModule(this.moduleId);
    }
  }

  private async fetchModule(id: string) {
    try {
      this.module = await lastValueFrom(
        this.http.get(`http://localhost:8080/api/modules/id/${id}`)
      );
      this.module.translations.sort((a: any, b: any) => {
        if (a.language === 'en') return -1;
        if (b.language === 'en') return 1;
        return 0;
      });
    } catch (err) {
      this.error = 'Failed to load module';
      console.error(err);
    } finally {
      this.loading = false;
    }
  }

  async updateModule() {
    try {
      await lastValueFrom(
        this.http.put(`http://localhost:8080/api/modules/${this.module.id}`, this.module)
      );
      this.router.navigate(['/modules']); // Navigate back to the module list after updating
    } catch (err) {
      console.error('Failed to update module', err);
      this.error = 'Failed to update module';
    }
  }
}
