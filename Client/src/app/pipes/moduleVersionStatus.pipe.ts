import { Pipe, PipeTransform } from '@angular/core';
import { ModuleVersionCompactDTO } from '../core/modules/openapi';

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
      case ModuleVersionCompactDTO.StatusEnum.Rejected:
        return { text: 'Rejected', colorClass: 'bg-red-300' };
      case ModuleVersionCompactDTO.StatusEnum.Cancelled:
        return { text: 'Cancelled', colorClass: 'bg-gray-100' };
      default:
        return { text: status, colorClass: 'bg-gray-200' };
    }
  }
}
