import { Pipe, PipeTransform } from '@angular/core';
import { ModuleVersionCompactDTO } from '../core/modules/openapi';
import { Tag } from 'primeng/tag';

@Pipe({
  name: 'moduleVersionStatus',
  standalone: true
})
export class ModuleVersionStatusPipe implements PipeTransform {
  transform(status: ModuleVersionCompactDTO.StatusEnum): {
    text: string;
    normalColor: string;
    fadedColor: string;
    severity: Tag['severity'];
  } {
    switch (status) {
      case ModuleVersionCompactDTO.StatusEnum.PendingFirstSubmission:
        return {
          text: 'Pending first submission',
          normalColor: 'bg-gray-500',
          fadedColor: 'bg-gray-300',
          severity: 'secondary'
        };
      case ModuleVersionCompactDTO.StatusEnum.PendingCoordinatorFeedback:
        return {
          text: 'Pending coordinator feedback',
          normalColor: 'bg-yellow-500',
          fadedColor: 'bg-yellow-300',
          severity: 'warn'
        };
      case ModuleVersionCompactDTO.StatusEnum.CoordinatorFeedbackGiven:
        return {
          text: 'Coordinator feedback given',
          normalColor: 'bg-blue-500',
          fadedColor: 'bg-blue-300',
          severity: 'info'
        };
      case ModuleVersionCompactDTO.StatusEnum.PendingFullSubmission:
        return {
          text: 'Pending full submission',
          normalColor: 'bg-gray-500',
          fadedColor: 'bg-gray-300',
          severity: 'secondary'
        };
      case ModuleVersionCompactDTO.StatusEnum.PendingFullFeedback:
        return {
          text: 'Pending full feedback',
          normalColor: 'bg-yellow-500',
          fadedColor: 'bg-yellow-300',
          severity: 'warn'
        };
      case ModuleVersionCompactDTO.StatusEnum.Accepted:
        return {
          text: 'Accepted',
          normalColor: 'bg-green-500',
          fadedColor: 'bg-green-300',
          severity: 'success'
        };
      case ModuleVersionCompactDTO.StatusEnum.RequiresReview:
        return {
          text: 'Requires review',
          normalColor: 'bg-sky-500',
          fadedColor: 'bg-sky-300',
          severity: 'info'
        };
      case ModuleVersionCompactDTO.StatusEnum.Rejected:
        return {
          text: 'Rejected',
          normalColor: 'bg-red-500',
          fadedColor: 'bg-red-300',
          severity: 'danger'
        };
      case ModuleVersionCompactDTO.StatusEnum.Cancelled:
        return {
          text: 'Cancelled',
          normalColor: 'bg-gray-300',
          fadedColor: 'bg-gray-100',
          severity: 'secondary'
        };
      default:
        return {
          text: status,
          normalColor: 'bg-gray-400',
          fadedColor: 'bg-gray-200',
          severity: 'secondary'
        };
    }
  }
}
