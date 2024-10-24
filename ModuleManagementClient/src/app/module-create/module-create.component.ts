import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-module-create',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './module-create.component.html',
  styleUrls: ['./module-create.component.css']
})
export class ModuleCreateComponent {
  moduleId: string = '';
  creationDate: string = this.formatCurrentDate();
  translations: {
    language: string;
    title: string;
    recommendedPrerequisites: string;
    assessmentMethod: string;
    learningOutcomes: string;
    mediaUsed: string;
    literature: string;
  }[] = [
    {
      language: 'en',
      title: '',
      recommendedPrerequisites: '',
      assessmentMethod: '',
      learningOutcomes: '',
      mediaUsed: '',
      literature: ''
    },
    {
      language: 'de',
      title: '',
      recommendedPrerequisites: '',
      assessmentMethod: '',
      learningOutcomes: '',
      mediaUsed: '',
      literature: ''
    }
  ];

  constructor(private http: HttpClient, private router: Router) {}

  private formatCurrentDate(): string {
    const now = new Date();
    return now.toISOString().split('.')[0]; // This will format it as "YYYY-MM-DDTHH:MM:SS"
  }

  createModule() {
    const newModule = {
      moduleId: this.moduleId,
      creationDate: this.creationDate,
      translations: this.translations.map(t => ({
        language: t.language,
        title: t.title,
        recommendedPrerequisites: t.recommendedPrerequisites,
        assessmentMethod: t.assessmentMethod,
        learningOutcomes: t.learningOutcomes,
        mediaUsed: t.mediaUsed,
        literature: t.literature
      }))
    };

    this.http.post('http://localhost:8080/api/modules/create', newModule)
      .subscribe({
        next: () => {
          this.router.navigate(['/modules']); // Adjust route as necessary
        },
        error: (err) => {
          console.error('Failed to create module', err);
          // Handle error accordingly
        }
      });
  }
}
