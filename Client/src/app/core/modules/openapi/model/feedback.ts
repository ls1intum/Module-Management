/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { User } from './user';


export interface Feedback { 
    feedbackId?: number;
    feedbackFrom?: User;
    requiredRole?: Feedback.RequiredRoleEnum;
    status: Feedback.StatusEnum;
    submissionDate?: string;
    titleFeedback?: string;
    titleAccepted?: boolean;
    levelFeedback?: string;
    levelAccepted?: boolean;
    languageFeedback?: string;
    languageAccepted?: boolean;
    frequencyFeedback?: string;
    frequencyAccepted?: boolean;
    creditsFeedback?: string;
    creditsAccepted?: boolean;
    durationFeedback?: string;
    durationAccepted?: boolean;
    hoursTotalFeedback?: string;
    hoursTotalAccepted?: boolean;
    hoursSelfStudyFeedback?: string;
    hoursSelfStudyAccepted?: boolean;
    hoursPresenceFeedback?: string;
    hoursPresenceAccepted?: boolean;
    examinationAchievementsFeedback?: string;
    examinationAchievementsAccepted?: boolean;
    repetitionFeedback?: string;
    repetitionAccepted?: boolean;
    recommendedPrerequisitesFeedback?: string;
    recommendedPrerequisitesAccepted?: boolean;
    contentFeedback?: string;
    contentAccepted?: boolean;
    learningOutcomesFeedback?: string;
    learningOutcomesAccepted?: boolean;
    teachingMethodsFeedback?: string;
    teachingMethodsAccepted?: boolean;
    mediaFeedback?: string;
    mediaAccepted?: boolean;
    literatureFeedback?: string;
    literatureAccepted?: boolean;
    responsiblesFeedback?: string;
    responsiblesAccepted?: boolean;
    lvSwsLecturerFeedback?: string;
    lvSwsLecturerAccepted?: boolean;
    comment?: string;
    allFeedbackPositive?: boolean;
    feedbackGiven?: boolean;
}
export namespace Feedback {
    export type RequiredRoleEnum = 'QUALITY_MANAGEMENT' | 'ACADEMIC_PROGRAM_ADVISOR' | 'EXAMINATION_BOARD' | 'PROFESSOR' | 'UNDEFINED';
    export const RequiredRoleEnum = {
        QualityManagement: 'QUALITY_MANAGEMENT' as RequiredRoleEnum,
        AcademicProgramAdvisor: 'ACADEMIC_PROGRAM_ADVISOR' as RequiredRoleEnum,
        ExaminationBoard: 'EXAMINATION_BOARD' as RequiredRoleEnum,
        Professor: 'PROFESSOR' as RequiredRoleEnum,
        Undefined: 'UNDEFINED' as RequiredRoleEnum
    };
    export type StatusEnum = 'PENDING_SUBMISSION' | 'PENDING_FEEDBACK' | 'APPROVED' | 'FEEDBACK_GIVEN' | 'REJECTED' | 'OBSOLETE' | 'CANCELLED';
    export const StatusEnum = {
        PendingSubmission: 'PENDING_SUBMISSION' as StatusEnum,
        PendingFeedback: 'PENDING_FEEDBACK' as StatusEnum,
        Approved: 'APPROVED' as StatusEnum,
        FeedbackGiven: 'FEEDBACK_GIVEN' as StatusEnum,
        Rejected: 'REJECTED' as StatusEnum,
        Obsolete: 'OBSOLETE' as StatusEnum,
        Cancelled: 'CANCELLED' as StatusEnum
    };
}


