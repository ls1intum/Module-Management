import { Pipe, PipeTransform } from '@angular/core';
import { Feedback } from '../core/modules/openapi';

@Pipe({ name: 'feedbackDepartment', standalone: true })
export class FeedbackDepartmentPipe implements PipeTransform {
  transform(role: Feedback.RequiredRoleEnum | null | undefined): { text: string } {
    if (role == null) return { text: 'Specialization area responsible' };
    switch (role) {
      case 'QUALITY_MANAGEMENT':
        return { text: 'Quality Management' };
      case 'EXAMINATION_BOARD':
        return { text: 'Examination Board' };
      case 'ACADEMIC_PROGRAM_ADVISOR':
        return { text: 'Academic Program Advisor' };
      case 'PROGRAM_COORDINATOR':
        return { text: 'Program coordinator' };
      case 'SPECIALIZATION_AREA_RESPONSIBLE':
        return { text: 'Specialization area responsible' };
      default:
        return { text: role };
    }
  }
}
