/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { Feedback } from './feedback';


export interface ModuleVersion { 
    moduleVersionId?: number;
    version?: number;
    moduleId?: string;
    creationDate?: string;
    status: ModuleVersion.StatusEnum;
    bulletPoints?: string;
    titleEng?: string;
    levelEng?: string;
    languageEng?: ModuleVersion.LanguageEngEnum;
    frequencyEng?: string;
    credits?: number;
    hoursTotal?: number;
    hoursSelfStudy?: number;
    hoursPresence?: number;
    examinationAchievementsEng?: string;
    repetitionEng?: string;
    recommendedPrerequisitesEng?: string;
    contentEng?: string;
    learningOutcomesEng?: string;
    teachingMethodsEng?: string;
    mediaEng?: string;
    literatureEng?: string;
    responsiblesEng?: string;
    lvSwsLecturerEng?: string;
    requiredFeedbacks?: Array<Feedback>;
}
export namespace ModuleVersion {
    export type StatusEnum = 'PENDING_SUBMISSION' | 'PENDING_FEEDBACK' | 'ACCEPTED' | 'REJECTED' | 'OBSOLETE' | 'CANCELLED';
    export const StatusEnum = {
        PendingSubmission: 'PENDING_SUBMISSION' as StatusEnum,
        PendingFeedback: 'PENDING_FEEDBACK' as StatusEnum,
        Accepted: 'ACCEPTED' as StatusEnum,
        Rejected: 'REJECTED' as StatusEnum,
        Obsolete: 'OBSOLETE' as StatusEnum,
        Cancelled: 'CANCELLED' as StatusEnum
    };
    export type LanguageEngEnum = 'English' | 'German';
    export const LanguageEngEnum = {
        English: 'English' as LanguageEngEnum,
        German: 'German' as LanguageEngEnum
    };
}


