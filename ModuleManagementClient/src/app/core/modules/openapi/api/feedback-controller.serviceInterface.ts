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

import { AcceptFeedbackDTO } from '../model/models';
import { Feedback } from '../model/models';
import { RejectFeedbackDTO } from '../model/models';


import { Configuration }                                     from '../configuration';



export interface FeedbackControllerServiceInterface {
    defaultHeaders: HttpHeaders;
    configuration: Configuration;

    /**
     * 
     * 
     * @param feedbackId 
     * @param acceptFeedbackDTO 
     */
    approveFeedback(feedbackId: number, acceptFeedbackDTO: AcceptFeedbackDTO, extraHttpRequestParams?: any): Observable<Feedback>;

    /**
     * 
     * 
     * @param id 
     */
    getFeedbacksForUser(id: number, extraHttpRequestParams?: any): Observable<Array<Feedback>>;

    /**
     * 
     * 
     * @param feedbackId 
     * @param rejectFeedbackDTO 
     */
    rejectFeedback(feedbackId: number, rejectFeedbackDTO: RejectFeedbackDTO, extraHttpRequestParams?: any): Observable<Feedback>;

}
