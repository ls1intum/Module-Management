import { Pipe, PipeTransform } from '@angular/core';
import { Feedback, ModuleVersionViewFeedbackDTO } from '../core/modules/openapi';
import { Tag } from 'primeng/tag';

@Pipe({ name: 'feedbackStatus', standalone: true })
export class FeedbackStatusPipe implements PipeTransform {
  transform(status: Feedback.StatusEnum | ModuleVersionViewFeedbackDTO.FeedbackStatusEnum): {
    text: string;
    normalColor: string;
    fadedColor: string;
    severity: Tag['severity'];
  } {
    switch (status) {
      case 'PENDING_SUBMISSION':
        return {
          text: 'Pending Submission',
          normalColor: 'bg-gray-500 text-white',
          fadedColor: 'bg-gray-300 text-white',
          severity: 'secondary'
        };
      case 'PENDING_FEEDBACK':
        return {
          text: 'Pending Feedback',
          normalColor: 'bg-yellow-500 text-white',
          fadedColor: 'bg-yellow-300 text-white',
          severity: 'warn'
        };
      case 'APPROVED':
        return {
          text: 'Approved',
          normalColor: 'bg-green-500 text-white',
          fadedColor: 'bg-green-300 text-white',
          severity: 'success'
        };
      case 'FEEDBACK_GIVEN':
        return {
          text: 'Feedback given',
          normalColor: 'bg-blue-500 text-white',
          fadedColor: 'bg-blue-300 text-white',
          severity: 'info'
        };
      case 'REJECTED':
        return {
          text: 'Rejected',
          normalColor: 'bg-red-500 text-white',
          fadedColor: 'bg-red-300 text-white',
          severity: 'danger'
        };
      case 'OBSOLETE':
        return {
          text: 'Obsolete',
          normalColor: 'bg-gray-300 text-gray-600',
          fadedColor: 'bg-gray-200 text-gray-600',
          severity: 'secondary'
        };
      case 'CANCELLED':
        return {
          text: 'Cancelled',
          normalColor: 'bg-gray-400 text-white',
          fadedColor: 'bg-gray-300 text-white',
          severity: 'secondary'
        };
      default:
        return {
          text: status,
          normalColor: 'bg-gray-400 text-white',
          fadedColor: 'bg-gray-300 text-white',
          severity: 'secondary'
        };
    }
  }
}
