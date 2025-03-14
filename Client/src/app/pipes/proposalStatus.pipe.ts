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
        return { text: 'Requires Review', colorClass: 'bg-blue-500' };
      case 'REJECTED':
        return { text: 'Rejected', colorClass: 'bg-red-500' };
      default:
        return { text: status, colorClass: 'bg-gray-400' };
    }
  }
}

@Pipe({ name: 'statusInfo', standalone: true })
export class StatusInfoPipeline implements PipeTransform {
  transform(status: Proposal.StatusEnum): string {
    switch (status) {
      case 'PENDING_SUBMISSION':
        return 'This module proposal is pending submission. Please fill all module information fields and submit the module proposal. After submission the necessary staff will be notified to review your proposal.';
      case 'PENDING_FEEDBACK':
        return 'This module proposal is submitted and waiting for review by the necessary staff. Please wait for their feedback. If you made a mistake you can cancel the submission. If a staff member already gave feedback, you will need to create a new module version for consistency. New module versions copy the content of the previously submitted module version.';
      case 'ACCEPTED':
        return 'This module is approved.';
      case 'REQUIRES_REVIEW':
        return 'This module proposal requires your review. It was either rejected by a staff member, or you canceled it after a staff member already gave feedback. Please create a new module version and update your proposal by the rejection feedback.';
      case 'REJECTED':
        return 'This proposal was rejected. You cannot modify this module proposal anymore.';
      default:
        return status;
    }
  }
}
