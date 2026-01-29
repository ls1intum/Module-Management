import { Pipe, PipeTransform } from '@angular/core';
import { Proposal } from '../core/modules/openapi';
import { Tag } from 'primeng/tag';
@Pipe({ name: 'statusDisplay', standalone: true })
export class StatusDisplayPipe implements PipeTransform {
  transform(status: Proposal.StatusEnum): { text: string; severity: Tag['severity'] } {
    switch (status) {
      case 'PENDING_SUBMISSION':
        return { text: 'Pending Submission', severity: 'secondary' };
      case 'PENDING_FEEDBACK':
        return { text: 'Pending Feedback', severity: 'warn' };
      case 'ACCEPTED':
        return { text: 'Accepted', severity: 'success' };
      case 'REQUIRES_REVIEW':
        return { text: 'Requires Review', severity: 'info' };
      case 'REJECTED':
        return { text: 'Rejected', severity: 'danger' };
      default:
        return { text: status, severity: 'secondary' };
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
