import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-module-detail',
  standalone: true,
  templateUrl: './module-detail.component.html',
  styleUrls: ['./module-detail.component.css'],
})
export class ModuleDetailComponent {
  route = inject(ActivatedRoute);
  http = inject(HttpClient);

  moduleId = this.route.snapshot.paramMap.get('id');
  moduleDetails = signal<any>(null);

  constructor() {
    if (this.moduleId) {
      this.fetchModuleDetails(this.moduleId);
    }
  }

  fetchModuleDetails(id: string) {
    this.http.get(`http://localhost:8080/api/modules/id/${id}`).subscribe({
      next: (data) => {
        this.moduleDetails.set(data);
      },
      error: (err) => {
        console.error('Failed to fetch module details', err);
      },
    });
  }
}
