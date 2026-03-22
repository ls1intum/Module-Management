import { ModuleVersionViewDTO } from '../../core/modules/openapi';
import { StepperStatus } from './module-edit-steps.config';

type Status = ModuleVersionViewDTO.StatusEnum;

export function coordinatorFeedbackStepStatus(status: Status | undefined): StepperStatus {
  if (!status) return StepperStatus.Default;
  switch (status) {
    case 'WAITING_FOR_COORDINATORS_SUBMISSION':
      return StepperStatus.Default;
    case 'PENDING_COORDINATORS_FEEDBACK':
      return StepperStatus.Pending;
    case 'COORDINATORS_FEEDBACK_GIVEN':
      return StepperStatus.FeedbackGiven;
    case 'REQUIRES_REVIEW':
      return StepperStatus.FeedbackGiven;
    case 'WAITING_FOR_EXAMINATION_BOARD_SUBMISSION':
    case 'PENDING_EXAMINATION_BOARD_FEEDBACK':
    case 'EXAMINATION_BOARD_FEEDBACK_GIVEN':
    case 'WAITING_FOR_QUALITY_MANAGEMENT_SUBMISSION':
    case 'PENDING_QUALITY_MANAGEMENT_FEEDBACK':
    case 'ACCEPTED':
      return StepperStatus.Completed;
    case 'REJECTED':
      return StepperStatus.Rejected;
    case 'CANCELLED':
      return StepperStatus.Default;
    default:
      return StepperStatus.Default;
  }
}
export function examinationBoardFeedbackStepStatus(status: Status | undefined): StepperStatus {
  if (!status) return StepperStatus.Default;
  switch (status) {
    case 'WAITING_FOR_COORDINATORS_SUBMISSION':
    case 'PENDING_COORDINATORS_FEEDBACK':
    case 'COORDINATORS_FEEDBACK_GIVEN':
    case 'REQUIRES_REVIEW':
      return StepperStatus.Default;
    case 'WAITING_FOR_EXAMINATION_BOARD_SUBMISSION':
      return StepperStatus.Default;
    case 'PENDING_EXAMINATION_BOARD_FEEDBACK':
      return StepperStatus.Pending;
    case 'EXAMINATION_BOARD_FEEDBACK_GIVEN':
      return StepperStatus.FeedbackGiven;
    case 'WAITING_FOR_QUALITY_MANAGEMENT_SUBMISSION':
    case 'PENDING_QUALITY_MANAGEMENT_FEEDBACK':
    case 'ACCEPTED':
      return StepperStatus.Completed;
    case 'REJECTED':
      return StepperStatus.Rejected;
    case 'CANCELLED':
      return StepperStatus.Default;
    default:
      return StepperStatus.Default;
  }
}

export function qualityManagementFeedbackStepStatus(status: Status | undefined): StepperStatus {
  if (!status) return StepperStatus.Default;
  switch (status) {
    case 'WAITING_FOR_COORDINATORS_SUBMISSION':
    case 'PENDING_COORDINATORS_FEEDBACK':
    case 'COORDINATORS_FEEDBACK_GIVEN':
    case 'REQUIRES_REVIEW':
    case 'WAITING_FOR_EXAMINATION_BOARD_SUBMISSION':
    case 'PENDING_EXAMINATION_BOARD_FEEDBACK':
    case 'EXAMINATION_BOARD_FEEDBACK_GIVEN':
      return StepperStatus.Default;
    case 'WAITING_FOR_QUALITY_MANAGEMENT_SUBMISSION':
      return StepperStatus.Default;
    case 'PENDING_QUALITY_MANAGEMENT_FEEDBACK':
      return StepperStatus.Pending;
    case 'ACCEPTED':
      return StepperStatus.Completed;
    case 'REJECTED':
      return StepperStatus.Rejected;
    case 'CANCELLED':
      return StepperStatus.Default;
    default:
      return StepperStatus.Default;
  }
}
