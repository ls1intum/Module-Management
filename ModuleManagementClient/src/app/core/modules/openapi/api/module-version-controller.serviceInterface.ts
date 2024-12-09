/**
 * OpenAPI definition
 *
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
import { HttpHeaders }                                       from '@angular/common/http';

import { Observable }                                        from 'rxjs';

import { ModuleVersionUpdateRequestDTO } from '../model/models';
import { ModuleVersionUpdateResponseDTO } from '../model/models';
import { ModuleVersionViewDTO } from '../model/models';


import { Configuration }                                     from '../configuration';



export interface ModuleVersionControllerServiceInterface {
    defaultHeaders: HttpHeaders;
    configuration: Configuration;

    /**
     * 
     * 
     * @param moduleVersionId 
     */
    getModuleVersionUpdateDtoFromId(moduleVersionId: number, extraHttpRequestParams?: any): Observable<ModuleVersionUpdateResponseDTO>;

    /**
     * 
     * 
     * @param moduleVersionId 
     */
    getModuleVersionViewDto(moduleVersionId: number, extraHttpRequestParams?: any): Observable<ModuleVersionViewDTO>;

    /**
     * 
     * 
     * @param moduleVersionId 
     * @param moduleVersionUpdateRequestDTO 
     */
    updateModuleVersion(moduleVersionId: number, moduleVersionUpdateRequestDTO: ModuleVersionUpdateRequestDTO, extraHttpRequestParams?: any): Observable<ModuleVersionUpdateResponseDTO>;

}
