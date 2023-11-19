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
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * returned from the external subject find query
 * 
 * @author mchyzer
 * 
 */
public class WsFindExternalSubjectsResults implements ResultMetadataHolder {

  /**
   * has 0 to many externalSubjects that match the query
   */
  private WsExternalSubject[] externalSubjectResults;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * metadata about the result
   */
  private WsResponseMeta responseMetadata = new WsResponseMeta();

  /**
   * has 0 to many externalSubjects that match the query by example
   * 
   * @return the externalSubjectResults
   */
  public WsExternalSubject[] getExternalSubjectResults() {
    return this.externalSubjectResults;
  }

  /**
   * basic results to the query
   * @param externalSubjectResults1 the groupResults to set
   */
  public void setExternalSubjectResults(WsExternalSubject[] externalSubjectResults1) {
    this.externalSubjectResults = externalSubjectResults1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * @see WsResponseBean#getResponseMetadata()
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

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }
}
