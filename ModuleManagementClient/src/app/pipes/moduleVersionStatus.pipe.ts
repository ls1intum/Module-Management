import { Pipe, PipeTransform } from '@angular/core';
import { ModuleVersionCompactDTO } from '../core/modules/openapi';

@Pipe({
  name: 'moduleVersionStatus'
})
export class ModuleVersionStatusPipe implements PipeTransform {
  transform(status: ModuleVersionCompactDTO.StatusEnum) {
    switch (status) {
      case ModuleVersionCompactDTO.StatusEnum.PendingSubmission:
        return { text: 'Pending Submission', class: 'status-pending' };
      case ModuleVersionCompactDTO.StatusEnum.PendingFeedback:
        return { text: 'Pending Feedback', class: 'status-pending' };
      case ModuleVersionCompactDTO.StatusEnum.Accepted:
        return { text: 'Accepted', class: 'status-accepted' };
      case ModuleVersionCompactDTO.StatusEnum.Rejected:
        return { text: 'Rejected', class: 'status-rejected' };
      case ModuleVersionCompactDTO.StatusEnum.Obsolete:
        return { text: 'Obsolete', class: 'status-obsolete' };
      case ModuleVersionCompactDTO.StatusEnum.Cancelled:
        return { text: 'Cancelled', class: 'status-cancelled' };
      default:
        return { text: 'Unknown', class: 'status-unknown' };
    }
  }
}
