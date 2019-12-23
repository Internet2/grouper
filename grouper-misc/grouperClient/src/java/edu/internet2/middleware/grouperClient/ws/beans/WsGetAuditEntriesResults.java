/**
 * Copyright 2014 Internet2
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
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * results for the get audit entries call.
 * 
 * result code:
 * code of the result for this call
 * SUCCESS: means everything ok
 * EXCEPTION: something bad happened
 * etc.
 * </pre>
 * 
 * @author vsachdeva
 */
public class WsGetAuditEntriesResults implements WsResponseBean {

  /**
   * 
   */
  private WsAuditEntry[] wsAuditEntries;

  /**
   * get audit entries
   * @return audit entries
   */
  public WsAuditEntry[] getWsAuditEntries() {
    return this.wsAuditEntries;
  }

  /**
   * set audit entries
   * @param wsAuditEntries1
   */
  public void setWsAuditEntries(WsAuditEntry[] wsAuditEntries1) {
    this.wsAuditEntries = wsAuditEntries1;
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

}
