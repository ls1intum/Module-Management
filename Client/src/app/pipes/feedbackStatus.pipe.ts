import { Pipe, PipeTransform } from '@angular/core';
import { Feedback } from '../core/modules/openapi';
import { Tag } from 'primeng/tag';

@Pipe({ name: 'feedbackStatus', standalone: true })
export class FeedbackStatusPipe implements PipeTransform {
  transform(status: Feedback.StatusEnum): { text: string; colorClass: string } {
    switch (status) {
      case 'PENDING_SUBMISSION':
        return { text: 'Pending Submission', colorClass: 'bg-gray-500 text-white' };
      case 'PENDING_FEEDBACK':
        return { text: 'Pending Feedback', colorClass: 'bg-yellow-500 text-white' };
      case 'APPROVED':
        return { text: 'Approved', colorClass: 'bg-green-500 text-white' };
      case 'FEEDBACK_GIVEN':
        return { text: 'Feedback given', colorClass: 'bg-blue-500 text-white' };
      case 'REJECTED':
        return { text: 'Rejected', colorClass: 'bg-red-500 text-white' };
      case 'OBSOLETE':
        return { text: 'Obsolete', colorClass: 'bg-gray-300 text-gray-600' };
      case 'CANCELLED':
        return { text: 'Cancelled', colorClass: 'bg-gray-400 text-white' };
      default:
        return { text: status, colorClass: 'bg-gray-400 text-white' };
    }
  }
}

@Pipe({ name: 'fadedFeedbackStatus', standalone: true })
export class FadedFeedbackStatusPipe implements PipeTransform {
  transform(status: Feedback.StatusEnum): { text: string; colorClass: string } {
    switch (status) {
      case 'PENDING_SUBMISSION':
        return { text: 'Pending Submission', colorClass: 'bg-gray-300 text-white' };
      case 'PENDING_FEEDBACK':
        return { text: 'Pending Feedback', colorClass: 'bg-yellow-300 text-white' };
      case 'APPROVED':
        return { text: 'Approved', colorClass: 'bg-green-300 text-white' };
      case 'FEEDBACK_GIVEN':
        return { text: 'Feedback given', colorClass: 'bg-blue-300 text-white' };
      case 'REJECTED':
        return { text: 'Rejected', colorClass: 'bg-red-300 text-white' };
      case 'OBSOLETE':
        return { text: 'Obsolete', colorClass: 'bg-gray-200 text-gray-600' };
      case 'CANCELLED':
        return { text: 'Cancelled', colorClass: 'bg-gray-300 text-white' };
      default:
        return { text: status, colorClass: 'bg-gray-300 text-white' };
    }
  }
}

@Pipe({ name: 'feedbackStatusTag', standalone: true })
export class FeedbackStatusTagPipe implements PipeTransform {
  transform(status: Feedback.StatusEnum): { text: string; severity: Tag['severity'] } {
    switch (status) {
      case 'PENDING_SUBMISSION':
        return { text: 'Pending Submission', severity: 'secondary' };
      case 'PENDING_FEEDBACK':
        return { text: 'Pending Feedback', severity: 'warn' };
      case 'APPROVED':
        return { text: 'Approved', severity: 'success' };
      case 'FEEDBACK_GIVEN':
        return { text: 'Feedback given', severity: 'info' };
      case 'REJECTED':
        return { text: 'Rejected', severity: 'danger' };
      case 'OBSOLETE':
        return { text: 'Obsolete', severity: 'secondary' };
      case 'CANCELLED':
        return { text: 'Cancelled', severity: 'secondary' };
      default:
        return { text: status, severity: 'secondary' };
    }
  }
}
