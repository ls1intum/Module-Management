import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { SecurityStore } from '../../core/security/security-store.service';
import { MessageService } from 'primeng/api';
import { FeedbackListTableComponent } from '../../components/feedback-list-table/feedback-list-table.component';

@Component({
  selector: 'approval-staff-home-page',
  standalone: true,
  imports: [ButtonModule, TableModule, TagModule, RouterModule, ToastModule, FeedbackListTableComponent],
  providers: [MessageService],
  templateUrl: './approval-staff-home-page.component.html'
})
export class ApprovalStaffHomePageComponent implements OnInit {
  securityStore = inject(SecurityStore);
  messageService = inject(MessageService);
  user = this.securityStore.user;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      if (params['feedback_given'] === 'true') {
        this.messageService.add({ severity: 'success', summary: 'Feedback Submitted', detail: 'Your feedback has been submitted successfully' });
      }
      if (params['rejected'] === 'true') {
        this.messageService.add({ severity: 'success', summary: 'Module Proposal Rejected', detail: 'You successfully rejected this Module Proposal' });
      }
    });
  }
}
