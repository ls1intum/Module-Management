import { Pipe, PipeTransform } from '@angular/core';
import { ProposalViewDTO } from '../core/modules/openapi';
import { Tag } from 'primeng/tag';
@Pipe({ name: 'statusDisplay', standalone: true })
export class StatusDisplayPipe implements PipeTransform {
  transform(status: ProposalViewDTO.StatusEnum): { text: string; severity: Tag['severity'] } {
    switch (status) {
      case 'PENDING_FIRST_SUBMISSION':
        return { text: 'Pending first submission', severity: 'secondary' };
      case 'PENDING_COORDINATOR_FEEDBACK':
        return { text: 'Pending coordinator feedback', severity: 'warn' };
      case 'PENDING_FULL_SUBMISSION':
        return { text: 'Pending full submission', severity: 'secondary' };
      case 'PENDING_FULL_FEEDBACK':
        return { text: 'Pending full feedback', severity: 'warn' };
      case 'ACCEPTED':
        return { text: 'Accepted', severity: 'success' };
      case 'REQUIRES_REVIEW':
        return { text: 'Requires Review', severity: 'info' };
      case 'REJECTED':
        return { text: 'Rejected', severity: 'danger' };
      default:
        return { text: status ?? '', severity: 'secondary' };
    }
  }
}

@Pipe({ name: 'statusInfo', standalone: true })
export class StatusInfoPipeline implements PipeTransform {
  transform(status: ProposalViewDTO.StatusEnum): string {
    switch (status) {
      case 'PENDING_FIRST_SUBMISSION':
        return 'This module proposal is pending submission. Please complete step 1 (basic information and degree program assignments) and submit for coordinator feedback.';
      case 'PENDING_COORDINATOR_FEEDBACK':
        return 'Submitted for coordinator feedback. Program and area coordinators are reviewing. You can cancel and resubmit if needed.';
      case 'PENDING_FULL_SUBMISSION':
        return 'Coordinator feedback was accepted. Complete all steps and submit for full feedback (quality management, program advisor, examination board) when ready.';
      case 'PENDING_FULL_FEEDBACK':
        return 'This module proposal is submitted and waiting for review. You can cancel the submission if needed.';
      case 'ACCEPTED':
        return 'This module is approved.';
      case 'REQUIRES_REVIEW':
        return 'This module proposal requires your review. Create a new module version and update by the rejection feedback.';
      case 'REJECTED':
        return 'This proposal was rejected. Create a new module version to resubmit.';
      default:
        return status ?? '';
    }
  }
}
