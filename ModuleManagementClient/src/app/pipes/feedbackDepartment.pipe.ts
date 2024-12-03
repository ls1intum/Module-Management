import { Pipe, PipeTransform } from '@angular/core';
import { Feedback } from '../core/modules/openapi';

@Pipe({ name: 'feedbackDepartment', standalone: true })
export class FeedbackDepartmentPipe implements PipeTransform {
  transform(status: Feedback.RequiredRoleEnum): { text: string } {
    switch (status) {
      case 'QUALITY_MANAGEMENT':
        return { text: 'Quality Management' };
      case 'EXAMINATION_BOARD':
        return { text: 'Examination Board' };
      case 'ACADEMIC_PROGRAM_ADVISOR':
        return { text: 'Academic Program Advisor' };
      default:
        return { text: status };
    }
  }
}
