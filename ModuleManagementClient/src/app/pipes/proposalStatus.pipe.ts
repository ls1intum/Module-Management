import { Pipe, PipeTransform } from '@angular/core';
import { Proposal } from '../core/modules/openapi';

@Pipe({ name: 'statusDisplay', standalone: true })
export class StatusDisplayPipe implements PipeTransform {
  transform(status: Proposal.StatusEnum): { text: string; colorClass: string } {
    switch (status) {
      case 'PENDING_SUBMISSION':
        return { text: 'Pending Submission', colorClass: 'bg-gray-500' };
      case 'PENDING_FEEDBACK':
        return { text: 'Pending Feedback', colorClass: 'bg-yellow-500' };
      case 'ACCEPTED':
        return { text: 'Accepted', colorClass: 'bg-green-500' };
      case 'REQUIRES_REVIEW':
        return { text: 'Requires Review', colorClass: 'bg-red-500' };
      case 'CANCELED':
        return { text: 'Canceled', colorClass: 'bg-gray-300' };
      default:
        return { text: status, colorClass: 'bg-gray-400' };
    }
  }
}
