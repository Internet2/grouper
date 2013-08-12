/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.soap_v2_1;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDef;
import edu.internet2.middleware.grouper.ws.coresoap.WsStem;



/**
 * <pre>
 * results for the get memberships call, or the get memberships lite call
 * 
 * result code:
 * code of the result for this group overall
 * SUCCESS: means everything ok
 * GROUP_NOT_FOUND: cant find the group
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsGetMembershipsResults {

  /**
   * result metadata
   * @param resultMetadata1
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }


  /**
   * results for each assignment sent in
   */
  private WsMembership[] wsMemberships;

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * groups that are in the results
   */
  private WsGroup[] wsGroups;


  /**
   * stems that are in the results
   */
  private WsStem[] wsStems;

  /**
   * attributeDefs that are in the results
   */
  private WsAttributeDef[] wsAttributeDefs;

  /**
   * attributeDefs that are in the results
   * @return attributeDefs
   */
  public WsAttributeDef[] getWsAttributeDefs() {
    return this.wsAttributeDefs;
  }

  /**
   * attributeDefs that are in the results
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDefs(WsAttributeDef[] wsAttributeDefs1) {
    this.wsAttributeDefs = wsAttributeDefs1;    
  }

  /**
   * stems that are in the results
   * @return stems
   */
  public WsStem[] getWsStems() {
    return this.wsStems;
  }

  /**
   * stems that are in the results
   * @param wsStems1
   */
  public void setWsStems(WsStem[] wsStems1) {
    this.wsStems = wsStems1;
  }

  /**
   * subjects that are in the results
   */
  private WsSubject[] wsSubjects;

  /**
   * subjects that are in the results
   * @return the subjects
   */
  public WsSubject[] getWsSubjects() {
    return this.wsSubjects;
  }

  /**
   * subjects that are in the results
   * @param wsSubjects1
   */
  public void setWsSubjects(WsSubject[] wsSubjects1) {
    this.wsSubjects = wsSubjects1;
  }

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsMembership[] getWsMemberships() {
    return this.wsMemberships;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setWsMemberships(WsMembership[] results1) {
    this.wsMemberships = results1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

  /**
   * @return the wsGroups
   */
  public WsGroup[] getWsGroups() {
    return this.wsGroups;
  }

  /**
   * @param wsGroup1 the wsGroups to set
   */
  public void setWsGroups(WsGroup[] wsGroup1) {
    this.wsGroups = wsGroup1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

}
