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
package edu.internet2.middleware.grouper.ws.soap_v2_2;



/**
 * <pre>
 * results for assigning attributes call.
 * 
 * result code:
 * code of the result for this attribute assignment overall
 * SUCCESS: means everything ok
 * INSUFFICIENT_PRIVILEGES: problem with some input where privileges are not sufficient
 * INVALID_QUERY: bad inputs
 * EXCEPTION: something bad happened
 * </pre>
 * @author mchyzer
 */
public class WsAssignAttributesLiteResults {

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   */
  private WsAttributeDef[] wsAttributeDefs;
  
  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @return attribute defs
   */
  public WsAttributeDef[] getWsAttributeDefs() {
    return this.wsAttributeDefs;
  }

  /**
   * attribute def references in the assignments or inputs (and able to be read)
   * @param wsAttributeDefs1
   */
  public void setWsAttributeDefs(WsAttributeDef[] wsAttributeDefs1) {
    this.wsAttributeDefs = wsAttributeDefs1;
  }

  /**
   * attribute def name referenced in the assignments or inputs (and able to read)
   */
  private WsAttributeDefName wsAttributeDefName;
  
  /**
   * attribute def name referenced in the assignments or inputs (and able to read)
   * @return attribute def name
   */
  public WsAttributeDefName getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }

  /**
   * attribute def names referenced in the assignments or inputs (and able to read)
   * @param wsAttributeDefName1
   */
  public void setWsAttributeDefName(WsAttributeDefName wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }

  /**
   * the assignment results being queried
   */
  private WsAssignAttributeResult wsAttributeAssignResult;
  
  /**
   * the assignment results being queried
   * @return the assignments being queried
   */
  public WsAssignAttributeResult getWsAttributeAssignResult() {
    return this.wsAttributeAssignResult;
  }

  /**
   * the assignment results being queried
   * @param wsAttributeAssignResult1
   */
  public void setWsAttributeAssignResult(WsAssignAttributeResult wsAttributeAssignResult1) {
    this.wsAttributeAssignResult = wsAttributeAssignResult1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * 
   */
  public WsAssignAttributesLiteResults() {
    //default
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
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * group that is in the result
   */
  private WsGroup wsGroup;

  /**
   * stems that are in the results
   */
  private WsStem wsStem;

  /**
   * stem that is in the results
   * @return stem
   */
  public WsStem getWsStem() {
    return this.wsStem;
  }

  /**
   * stems that are in the results
   * @param wsStem1
   */
  public void setWsStem(WsStem wsStem1) {
    this.wsStem = wsStem1;
  }

  /**
   * results for each assignment sent in
   */
  private WsMembership wsMembership;

  /**
   * subjects that are in the result
   */
  private WsSubject wsSubject;

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsResponseBean#getResponseMetadata()
   * @return the response metadata
   */
  public WsResponseMeta getResponseMetadata() {
    return this.responseMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

  
  /**
   * @param responseMetadata1 the responseMetadata to set
   */
  public void setResponseMetadata(WsResponseMeta responseMetadata1) {
    this.responseMetadata = responseMetadata1;
  }

  /**
   * @return the wsGroup
   */
  public WsGroup getWsGroup() {
    return this.wsGroup;
  }

  /**
   * results for each assignment sent in
   * @return the results
   */
  public WsMembership getWsMembership() {
    return this.wsMembership;
  }

  /**
   * subject that is in the results
   * @return the subject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  /**
   * @param wsGroup1 the wsGroup to set
   */
  public void setWsGroup(WsGroup wsGroup1) {
    this.wsGroup = wsGroup1;
  }

  /**
   * results for each assignment sent in
   * @param result1 the results to set
   */
  public void setWsMembership(WsMembership result1) {
    this.wsMembership = result1;
  }

  /**
   * subject that is in the results
   * @param wsSubject1
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }
}
