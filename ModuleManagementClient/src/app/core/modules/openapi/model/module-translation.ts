/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface ModuleTranslation { 
    id?: number;
    language?: ModuleTranslation.LanguageEnum;
    title?: string;
    recommendedPrerequisites?: string;
    assessmentMethod?: string;
    learningOutcomes?: string;
    mediaUsed?: string;
    literature?: string;
}
export namespace ModuleTranslation {
    export type LanguageEnum = 'en' | 'de';
    export const LanguageEnum = {
        En: 'en' as LanguageEnum,
        De: 'de' as LanguageEnum
    };
}


