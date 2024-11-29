/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface FeedbackListItemDto { 
    feedbackId: number;
    moduleVersionId: number;
    proposalId: number;
    status: FeedbackListItemDto.StatusEnum;
    proposalCreatedByName: string;
    proposalCreatedById: string;
    moduleVersionTitleEng?: string;
}
export namespace FeedbackListItemDto {
    export type StatusEnum = 'PENDING_SUBMISSION' | 'PENDING_FEEDBACK' | 'APPROVED' | 'REJECTED' | 'OBSOLETE' | 'CANCELLED';
    export const StatusEnum = {
        PendingSubmission: 'PENDING_SUBMISSION' as StatusEnum,
        PendingFeedback: 'PENDING_FEEDBACK' as StatusEnum,
        Approved: 'APPROVED' as StatusEnum,
        Rejected: 'REJECTED' as StatusEnum,
        Obsolete: 'OBSOLETE' as StatusEnum,
        Cancelled: 'CANCELLED' as StatusEnum
    };
}


