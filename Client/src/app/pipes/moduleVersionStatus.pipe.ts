import { Pipe, PipeTransform } from '@angular/core';
import { ModuleVersionCompactDTO } from '../core/modules/openapi';
import { Tag } from 'primeng/tag';

@Pipe({
  name: 'moduleVersionStatus',
  standalone: true
})
export class ModuleVersionStatusPipe implements PipeTransform {
  transform(status: ModuleVersionCompactDTO.StatusEnum): { text: string; colorClass: string } {
    switch (status) {
      case ModuleVersionCompactDTO.StatusEnum.PendingSubmission:
        return { text: 'Pending Submission', colorClass: 'bg-gray-500' };
      case ModuleVersionCompactDTO.StatusEnum.PendingFeedback:
        return { text: 'Pending Feedback', colorClass: 'bg-yellow-500' };
      case ModuleVersionCompactDTO.StatusEnum.Accepted:
        return { text: 'Accepted', colorClass: 'bg-green-500' };
      case ModuleVersionCompactDTO.StatusEnum.FeedbackGiven:
        return { text: 'Feedback given', colorClass: 'bg-blue-500' };
      case ModuleVersionCompactDTO.StatusEnum.Rejected:
        return { text: 'Rejected', colorClass: 'bg-red-500' };
      case ModuleVersionCompactDTO.StatusEnum.Cancelled:
        return { text: 'Cancelled', colorClass: 'bg-gray-300' };
      default:
        return { text: status, colorClass: 'bg-gray-400' };
    }
  }
}

@Pipe({
  name: 'moduleVersionStatusTag',
  standalone: true
})
export class ModuleVersionStatusTagPipe implements PipeTransform {
  transform(status: ModuleVersionCompactDTO.StatusEnum): { text: string; severity: Tag['severity'] } {
    switch (status) {
      case ModuleVersionCompactDTO.StatusEnum.PendingSubmission:
        return { text: 'Pending Submission', severity: 'secondary' };
      case ModuleVersionCompactDTO.StatusEnum.PendingFeedback:
        return { text: 'Pending Feedback', severity: 'warn' };
      case ModuleVersionCompactDTO.StatusEnum.Accepted:
        return { text: 'Accepted', severity: 'success' };
      case ModuleVersionCompactDTO.StatusEnum.FeedbackGiven:
        return { text: 'Feedback given', severity: 'info' };
      case ModuleVersionCompactDTO.StatusEnum.Rejected:
        return { text: 'Rejected', severity: 'danger' };
      case ModuleVersionCompactDTO.StatusEnum.Cancelled:
        return { text: 'Cancelled', severity: 'secondary' };
      default:
        return { text: status, severity: 'secondary' };
    }
  }
}

@Pipe({
  name: 'fadedModuleVersionStatus',
  standalone: true
})
export class FadedModuleVersionStatusPipe implements PipeTransform {
  transform(status: ModuleVersionCompactDTO.StatusEnum): { text: string; colorClass: string } {
    switch (status) {
      case ModuleVersionCompactDTO.StatusEnum.PendingSubmission:
        return { text: 'Pending Submission', colorClass: 'bg-gray-300' };
      case ModuleVersionCompactDTO.StatusEnum.PendingFeedback:
        return { text: 'Pending Feedback', colorClass: 'bg-yellow-300' };
      case ModuleVersionCompactDTO.StatusEnum.Accepted:
        return { text: 'Accepted', colorClass: 'bg-green-300' };
      case ModuleVersionCompactDTO.StatusEnum.FeedbackGiven:
        return { text: 'Feedback given', colorClass: 'bg-blue-300' };
      case ModuleVersionCompactDTO.StatusEnum.Rejected:
        return { text: 'Rejected', colorClass: 'bg-red-300' };
      case ModuleVersionCompactDTO.StatusEnum.Cancelled:
        return { text: 'Cancelled', colorClass: 'bg-gray-100' };
      default:
        return { text: status, colorClass: 'bg-gray-200' };
    }
  }
}
