import { Pipe, PipeTransform } from '@angular/core';
import { Feedback } from '../core/modules/openapi';

/** ModuleVersionViewFeedbackDTO, Feedback, or similar with optional display fields. */
type FeedbackAuthorInput = {
  requestedFromUserName?: string | null;
  requestedFromSpecializationName?: string | null;
  examinationBoardName?: string | null;
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

/**
 * Same pattern for both flows: {@code Label: scope — reviewer name} (scope only when no name).
 * Examination board uses label "Examination board"; coordinators use "Coordinator".
 */
@Pipe({ name: 'feedbackAuthorDisplay', standalone: true })
export class FeedbackAuthorDisplayPipe implements PipeTransform {
  transform(fb: FeedbackAuthorInput | null | undefined): string {
    if (!fb) return '';
    const name = (fb.requestedFromUserName ?? '').trim();
    const role = fb.requiredRole;

    const hasBoard = fb.examinationBoardName != null && fb.examinationBoardName.trim() !== '';
    if (hasBoard) {
      const board = `Examination board: ${fb.examinationBoardName!.trim()}`;
      return name ? `${board} — ${name}` : board;
    }

    const scope =
      (fb.requestedFromSpecializationName ?? '').trim() ||
      (role ? ROLE_LABELS[role] ?? String(role) : '');
    if (scope) {
      const head = `Coordinator: ${scope}`;
      return name ? `${head} — ${name}` : head;
    }
    return name || (ROLE_LABELS[role ?? ''] ?? role) || '-';
  }
}
