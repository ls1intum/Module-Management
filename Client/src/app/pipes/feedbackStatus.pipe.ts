import { Pipe, PipeTransform } from '@angular/core';
import { Feedback, ModuleVersionViewFeedbackDTO } from '../core/modules/openapi';
import { Tag } from 'primeng/tag';

@Pipe({ name: 'feedbackStatus', standalone: true })
export class FeedbackStatusPipe implements PipeTransform {
  transform(status: Feedback.StatusEnum | ModuleVersionViewFeedbackDTO.FeedbackStatusEnum): {
    text: string;
    color: string;
    severity: Tag['severity'];
  } {
    switch (status) {
      case 'PENDING_FEEDBACK':
        return {
          text: 'Pending Feedback',
          color: 'bg-yellow-500 text-white',
          severity: 'warn'
        };
      case 'APPROVED':
        return {
          text: 'Approved',
          color: 'bg-green-500 text-white',
          severity: 'success'
        };
      case 'FEEDBACK_GIVEN':
        return {
          text: 'Feedback given',
          color: 'bg-blue-500 text-white',
          severity: 'info'
        };
      case 'REJECTED':
        return {
          text: 'Rejected',
          color: 'bg-red-500 text-white',
          severity: 'danger'
        };

      default:
        return {
          text: status,
          color: 'bg-gray-400 text-white',
          severity: 'secondary'
        };
    }
  }
}
