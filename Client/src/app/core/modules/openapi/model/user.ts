/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


export interface User { 
    userId?: string;
    userName?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    role?: User.RoleEnum;
}
export namespace User {
    export type RoleEnum = 'QUALITY_MANAGEMENT' | 'ACADEMIC_PROGRAM_ADVISOR' | 'EXAMINATION_BOARD' | 'PROFESSOR' | 'UNDEFINED';
    export const RoleEnum = {
        QualityManagement: 'QUALITY_MANAGEMENT' as RoleEnum,
        AcademicProgramAdvisor: 'ACADEMIC_PROGRAM_ADVISOR' as RoleEnum,
        ExaminationBoard: 'EXAMINATION_BOARD' as RoleEnum,
        Professor: 'PROFESSOR' as RoleEnum,
        Undefined: 'UNDEFINED' as RoleEnum
    };
}


