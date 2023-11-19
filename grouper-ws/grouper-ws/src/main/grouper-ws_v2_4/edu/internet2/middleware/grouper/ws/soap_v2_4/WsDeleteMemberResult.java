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
package edu.internet2.middleware.grouper.ws.soap_v2_4;



/**
 * Result of one subject being deleted from a group.  The number of
 * subjects will equal the number of subjects sent in to the method
 * 
 * @author mchyzer
 */
public class WsDeleteMemberResult   {

  /** subject that was added */
  private WsSubject wsSubject;

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
   * @return the wsSubject
   */
  public WsSubject getWsSubject() {
    return this.wsSubject;
  }

  
  /**
   * @param wsSubject1 the wsSubject to set
   */
  public void setWsSubject(WsSubject wsSubject1) {
    this.wsSubject = wsSubject1;
  }

  
  /**
   * @param resultMetadata1 the resultMetadata to set
   */
  public void setResultMetadata(WsResultMeta resultMetadata1) {
    this.resultMetadata = resultMetadata1;
  }

}
