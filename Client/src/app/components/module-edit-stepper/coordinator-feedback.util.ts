import { ModuleVersionViewDTO, ModuleVersionViewFeedbackDTO } from '../../core/modules/openapi';

/**
 * Program / area coordinator feedback rows (assignee + specialization). Excludes
 * examination-board member rows, which also have {@code requiredRole == null}.
 */
export function isCoordinatorAssignmentFeedback(f: ModuleVersionViewFeedbackDTO): boolean {
  return (
    f.requiredRole == null &&
    f.examinationBoardId == null &&
    f.degreeProgramSpecializationId != null
  );
}

/**
 * Coordinator slice from {@code feedbacks}, scoped to specialization ids on {@code dto}
 * (when the module version has assignments). If there are no assignment ids, returns all
 * coordinator-assignment rows from the list.
 */
export function filterCoordinatorFeedbacksForAssignments(
  feedbacks: ModuleVersionViewFeedbackDTO[],
  dto: ModuleVersionViewDTO | null
): ModuleVersionViewFeedbackDTO[] {
  const coordinator = feedbacks.filter(isCoordinatorAssignmentFeedback);
  const specIds = new Set(
    (dto?.degreeProgramAssignments ?? [])
      .map((a) => a.degreeProgramSpecializationId)
      .filter((id): id is number => id != null)
  );
  if (specIds.size === 0) {
    return coordinator;
  }
  return coordinator.filter((f) => f.degreeProgramSpecializationId != null && specIds.has(f.degreeProgramSpecializationId));
}

/** Examination-board member rows (assignee-based; no specialization on the feedback row). */
export function isExaminationBoardMemberFeedback(f: ModuleVersionViewFeedbackDTO): boolean {
  return f.requiredRole == null && f.examinationBoardId != null && f.degreeProgramSpecializationId == null;
}

export function filterExaminationBoardMemberFeedbacks(
  feedbacks: ModuleVersionViewFeedbackDTO[]
): ModuleVersionViewFeedbackDTO[] {
  return feedbacks.filter(isExaminationBoardMemberFeedback);
}
