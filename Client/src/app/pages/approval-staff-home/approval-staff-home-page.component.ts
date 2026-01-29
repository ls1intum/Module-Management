import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { SecurityStore } from '../../core/security/security-store.service';
import { FeedbackListTableComponent } from '../../components/feedback-list-table/feedback-list-table.component';

@Component({
  selector: 'approval-staff-home-page',
  standalone: true,
  imports: [ButtonModule, TableModule, TagModule, RouterModule, FeedbackListTableComponent],
  templateUrl: './approval-staff-home-page.component.html'
})
export class ApprovalStaffHomePageComponent {
  securityStore = inject(SecurityStore);
  user = this.securityStore.user;
}
