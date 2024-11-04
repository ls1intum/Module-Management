/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { ModuleVersionCompactDTO } from './module-version-compact-dto';


export interface ProposalViewDTO { 
    proposalId?: number;
    creationDate?: string;
    latestVersion?: number;
    status?: ProposalViewDTO.StatusEnum;
    moduleVersionsCompact?: Array<ModuleVersionCompactDTO>;
}
export namespace ProposalViewDTO {
    export type StatusEnum = 'PENDING_SUBMISSION' | 'PENDING_FEEDBACK' | 'ACCEPTED' | 'REQUIRES_REVIEW' | 'CANCELED';
    export const StatusEnum = {
        PendingSubmission: 'PENDING_SUBMISSION' as StatusEnum,
        PendingFeedback: 'PENDING_FEEDBACK' as StatusEnum,
        Accepted: 'ACCEPTED' as StatusEnum,
        RequiresReview: 'REQUIRES_REVIEW' as StatusEnum,
        Canceled: 'CANCELED' as StatusEnum
    };
}


