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
package edu.internet2.middleware.grouper.ws.soap_v2_3;


/**
 * Result of one externalSubject being deleted.  The number of
 * these result objects will equal the number of externalSubjects sent in to the method
 * to be deleted
 * 
 * @author mchyzer
 */
public class WsExternalSubjectDeleteResult {

  /**
   * empty constructor
   */
  public WsExternalSubjectDeleteResult() {
    //nothing to do
  }

  /**
   * externalSubject to be deleted
   */
  private WsExternalSubject wsExternalSubject;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

  /**
   * @return the wsExternalSubject
   */
  public WsExternalSubject getWsExternalSubject() {
    return this.wsExternalSubject;
  }

  /**
   * @param wsExternalSubjectResult1 the wsExternalSubject to set
   */
  public void setWsExternalSubject(WsExternalSubject wsExternalSubjectResult1) {
    this.wsExternalSubject = wsExternalSubjectResult1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
