import { Pipe, PipeTransform } from '@angular/core';
import { Feedback } from '../core/modules/openapi';

/** Feedback with optional name/area (ModuleVersionViewFeedbackDTO) or role-only (Feedback). */
type FeedbackAuthorInput = {
  requestedFromUserName?: string | null;
  requestedFromSpecializationName?: string | null;
  feedbackRole?: Feedback.RequiredRoleEnum | null;
  requiredRole?: Feedback.RequiredRoleEnum | null;
};

const ROLE_LABELS: Record<string, string> = {
  QUALITY_MANAGEMENT: 'Quality Management',
  EXAMINATION_BOARD: 'Examination Board',
  ACADEMIC_PROGRAM_ADVISOR: 'Academic Program Advisor',
  PROGRAM_COORDINATOR: 'Program coordinator',
  SPECIALIZATION_AREA_COORDINATOR: 'Specialization area coordinator',
  ADMIN: 'Admin',
  PROFESSOR: 'Professor'
};

@Pipe({ name: 'feedbackAuthorDisplay', standalone: true })
export class FeedbackAuthorDisplayPipe implements PipeTransform {
  transform(fb: FeedbackAuthorInput | null | undefined): string {
    if (!fb) return '';
    const name = fb.requestedFromUserName;
    const role = fb.feedbackRole ?? fb.requiredRole;
    const area = fb.requestedFromSpecializationName ?? (role ? ROLE_LABELS[role] ?? role : null);
    const combined = name && [name, area && `(${area})`].filter(Boolean).join(' ');
    return combined ?? ROLE_LABELS[role ?? ''] ?? role ?? '-';
  }
}
