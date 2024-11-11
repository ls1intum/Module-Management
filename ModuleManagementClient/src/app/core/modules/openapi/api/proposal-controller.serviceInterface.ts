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

import { Proposal } from '../model/models';
import { ProposalRequestDTO } from '../model/models';
import { ProposalViewDTO } from '../model/models';
import { ProposalsCompactDTO } from '../model/models';
import { UserIdDTO } from '../model/models';


import { Configuration }                                     from '../configuration';



export interface ProposalControllerServiceInterface {
    defaultHeaders: HttpHeaders;
    configuration: Configuration;

    /**
     * 
     * 
     * @param proposalRequestDTO 
     */
    createProposal(proposalRequestDTO: ProposalRequestDTO, extraHttpRequestParams?: any): Observable<Proposal>;

    /**
     * 
     * 
     * @param proposalId 
     * @param userIdDTO 
     */
    deleteProposal(proposalId: number, userIdDTO: UserIdDTO, extraHttpRequestParams?: any): Observable<string>;

    /**
     * 
     * 
     */
    getAllProposals(extraHttpRequestParams?: any): Observable<Array<Proposal>>;

    /**
     * 
     * 
     */
    getAllProposalsCompact(extraHttpRequestParams?: any): Observable<Array<ProposalsCompactDTO>>;

    /**
     * 
     * 
     * @param id 
     */
    getProposalById(id: number, extraHttpRequestParams?: any): Observable<Proposal>;

    /**
     * 
     * 
     * @param id 
     */
    getProposalView(id: number, extraHttpRequestParams?: any): Observable<ProposalViewDTO>;

    /**
     * 
     * 
     * @param id 
     */
    getProposalsByUserId(id: number, extraHttpRequestParams?: any): Observable<Array<Proposal>>;

    /**
     * 
     * 
     * @param id 
     */
    getProposalsByUserIdFromCompact(id: number, extraHttpRequestParams?: any): Observable<Array<ProposalsCompactDTO>>;

    /**
     * 
     * 
     * @param proposalId 
     * @param userIdDTO 
     */
    submitProposal(proposalId: number, userIdDTO: UserIdDTO, extraHttpRequestParams?: any): Observable<ProposalViewDTO>;

}
