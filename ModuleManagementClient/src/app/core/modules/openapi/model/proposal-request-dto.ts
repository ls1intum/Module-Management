/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface ProposalRequestDTO { 
    userId: number;
    titleEng?: string;
    levelEng?: string;
    languageEng?: ProposalRequestDTO.LanguageEngEnum;
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
}
export namespace ProposalRequestDTO {
    export type LanguageEngEnum = 'English' | 'German' | 'undefined';
    export const LanguageEngEnum = {
        English: 'English' as LanguageEngEnum,
        German: 'German' as LanguageEngEnum,
        Undefined: 'undefined' as LanguageEngEnum
    };
}


