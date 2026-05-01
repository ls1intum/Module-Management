import { Pipe, PipeTransform } from '@angular/core';
import { ProposalViewDTO } from '../core/modules/openapi';
import { Tag } from 'primeng/tag';
@Pipe({ name: 'statusDisplay', standalone: true })
export class StatusDisplayPipe implements PipeTransform {
  transform(status: ProposalViewDTO.StatusEnum): { text: string; severity: Tag['severity'] } {
    switch (status) {
      case 'WAITING_FOR_COORDINATORS_SUBMISSION':
        return { text: 'Waiting for coordinator submission', severity: 'secondary' };
      case 'PENDING_COORDINATORS_FEEDBACK':
        return { text: 'Pending coordinators feedback', severity: 'warn' };
      case 'COORDINATORS_FEEDBACK_GIVEN':
        return { text: 'Coordinators feedback given', severity: 'info' };
      case 'WAITING_FOR_EXAMINATION_BOARD_SUBMISSION':
        return { text: 'Waiting for examination board submission', severity: 'secondary' };
      case 'PENDING_EXAMINATION_BOARD_FEEDBACK':
        return { text: 'Pending examination board feedback', severity: 'warn' };
      case 'EXAMINATION_BOARD_FEEDBACK_GIVEN':
        return { text: 'Examination board feedback given', severity: 'info' };
      case 'WAITING_FOR_QUALITY_MANAGEMENT_SUBMISSION':
        return { text: 'Waiting for quality management submission', severity: 'secondary' };
      case 'PENDING_QUALITY_MANAGEMENT_FEEDBACK':
        return { text: 'Pending quality management feedback', severity: 'warn' };
      case 'ACCEPTED':
        return { text: 'Accepted', severity: 'success' };
      case 'REQUIRES_REVIEW':
        return { text: 'Requires Review', severity: 'info' };
      case 'REJECTED_AT_COORDINATORS_FEEDBACK':
      case 'REJECTED_AT_EXAMINATION_BOARD_FEEDBACK':
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
      case 'WAITING_FOR_COORDINATORS_SUBMISSION':
        return 'Complete step 1 and submit for coordinator feedback when ready.';
      case 'PENDING_COORDINATORS_FEEDBACK':
        return 'Submitted for coordinator feedback. Program and area coordinators are reviewing.';
      case 'COORDINATORS_FEEDBACK_GIVEN':
        return 'All coordinators have responded; at least one response needs your attention before approval.';
      case 'WAITING_FOR_EXAMINATION_BOARD_SUBMISSION':
        return 'Coordinator feedback was accepted. Complete all steps, then submit for examination board feedback.';
      case 'PENDING_EXAMINATION_BOARD_FEEDBACK':
        return 'Waiting for examination board feedback.';
      case 'EXAMINATION_BOARD_FEEDBACK_GIVEN':
        return 'Examination board has responded; review feedback before requesting quality management feedback.';
      case 'WAITING_FOR_QUALITY_MANAGEMENT_SUBMISSION':
        return 'Examination board feedback is complete. Submit for quality management feedback.';
      case 'PENDING_QUALITY_MANAGEMENT_FEEDBACK':
        return 'Waiting for quality management feedback.';
      case 'ACCEPTED':
        return 'This module is approved.';
      case 'REQUIRES_REVIEW':
        return 'This module proposal requires your review. Create a new module version and update by the rejection feedback.';
      case 'REJECTED_AT_COORDINATORS_FEEDBACK':
      case 'REJECTED_AT_EXAMINATION_BOARD_FEEDBACK':
        return 'This proposal was rejected. Create a new module version to resubmit.';
      default:
        return status ?? '';
    }
  }
}
