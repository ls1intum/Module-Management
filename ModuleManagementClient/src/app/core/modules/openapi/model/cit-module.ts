/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { ModuleTranslation } from './module-translation';


export interface CitModule { 
    id: number;
    moduleId?: string;
    translations?: Array<ModuleTranslation>;
    creationDate?: string;
}