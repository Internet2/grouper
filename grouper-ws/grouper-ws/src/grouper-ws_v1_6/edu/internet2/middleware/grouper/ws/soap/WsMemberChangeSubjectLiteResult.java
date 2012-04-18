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
/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;


/**
 * Result of one member changing its subject
 * 
 * @author mchyzer
 */
public class WsMemberChangeSubjectLiteResult {

  /**
   * empty
   */
  public WsMemberChangeSubjectLiteResult() {
    //empty
  }

  /**
    * metadata about the result
    */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * subject to switch to
   */
  private WsSubject wsSubjectNew;

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
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
   * subject that was changed to
   * @return the subjectId
   */
  public WsSubject getWsSubjectNew() {
    return this.wsSubjectNew;
  }

  /**
   * subject that was changed to
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubjectNew(WsSubject wsSubject1) {
    this.wsSubjectNew = wsSubject1;
  }

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * subject to switch from
   */
  private WsSubject wsSubjectOld;

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
   * subject that was swapped out
   * @return the subjectId
   */
  public WsSubject getWsSubjectOld() {
    return this.wsSubjectOld;
  }

  /**
   * subject that was swapped out
   * @param wsSubject1 the wsSubject1 to set
   */
  public void setWsSubjectOld(WsSubject wsSubject1) {
    this.wsSubjectOld = wsSubject1;
  }

}
