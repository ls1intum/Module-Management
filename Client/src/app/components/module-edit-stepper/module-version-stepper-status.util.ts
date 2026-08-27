import { ModuleVersionViewDTO } from '../../core/modules/openapi';
import { StepperStatus } from './module-edit-steps.config';

type Status = ModuleVersionViewDTO.StatusEnum;

/**
 * Maps module-version workflow status to the coordinator-feedback stepper segment.
 * Uses only {@link ModuleVersionViewDTO.status} (same enum values as proposal workflow when synced).
 */
export function coordinatorFeedbackStepStatus(status: Status | undefined): StepperStatus {
  if (!status) return StepperStatus.Default;
  switch (status) {
    case 'WAITING_FOR_COORDINATORS_SUBMISSION':
      return StepperStatus.Default;
    case 'PENDING_COORDINATORS_FEEDBACK':
      return StepperStatus.Pending;
    case 'COORDINATORS_FEEDBACK_GIVEN':
      return StepperStatus.FeedbackGiven;
    case 'WAITING_FOR_EXAMINATION_BOARD_SUBMISSION':
    case 'PENDING_EXAMINATION_BOARD_FEEDBACK':
    case 'EXAMINATION_BOARD_FEEDBACK_GIVEN':
    case 'ACCEPTED':
      return StepperStatus.Completed;
    case 'REJECTED_AT_COORDINATORS_FEEDBACK':
      return StepperStatus.Rejected;
    case 'REJECTED_AT_EXAMINATION_BOARD_FEEDBACK':
      return StepperStatus.Completed;
    case 'CANCELLED':
      return StepperStatus.Default;
    default:
      return StepperStatus.Default;
  }
}

/**
 * Maps module-version workflow status to the examination-board feedback stepper segment.
 * Uses only {@link ModuleVersionViewDTO.status}.
 */
export function examinationBoardFeedbackStepStatus(status: Status | undefined): StepperStatus {
  if (!status) return StepperStatus.Default;
  switch (status) {
    case 'WAITING_FOR_COORDINATORS_SUBMISSION':
    case 'PENDING_COORDINATORS_FEEDBACK':
    case 'COORDINATORS_FEEDBACK_GIVEN':
      return StepperStatus.Default;
    case 'WAITING_FOR_EXAMINATION_BOARD_SUBMISSION':
      return StepperStatus.Default;
    case 'PENDING_EXAMINATION_BOARD_FEEDBACK':
      return StepperStatus.Pending;
    case 'EXAMINATION_BOARD_FEEDBACK_GIVEN':
      return StepperStatus.FeedbackGiven;
    case 'ACCEPTED':
      return StepperStatus.Completed;
    case 'REJECTED_AT_EXAMINATION_BOARD_FEEDBACK':
      return StepperStatus.Rejected;
    case 'REJECTED_AT_COORDINATORS_FEEDBACK':
      return StepperStatus.Default;
    case 'CANCELLED':
      return StepperStatus.Default;
    default:
      return StepperStatus.Default;
  }
}
