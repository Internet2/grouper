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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameToSave;


/**
 * request bean in body of rest request
 */
public class WsRestAttributeDefNameSaveRequest implements WsRequestBean {

  /**
   * AttributeDefNames to save
   */
  private WsAttributeDefNameToSave[] wsAttributeDefNameToSaves;
  
  /**
   * AttributeDefNames to save
   * @return attribute def names to save
   */
  public WsAttributeDefNameToSave[] getWsAttributeDefNameToSaves() {
    return this.wsAttributeDefNameToSaves;
  }

  /**
   * AttributeDefNames to save
   * @param wsAttributeDefNameToSaves1
   */
  public void setWsAttributeDefNameToSaves(
      WsAttributeDefNameToSave[] wsAttributeDefNameToSaves1) {
    this.wsAttributeDefNameToSaves = wsAttributeDefNameToSaves1;
  }

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @return txType
   */
  public String getTxType() {
    return this.txType;
  }

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param txType1
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
  }

  /**
   * is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   */
  private String txType;
  
  
  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }
  
  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /** if acting as someone else */
  private WsSubjectLookup actAsSubjectLookup;
  
  /**
   * if acting as someone else
   * @return act as subject
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * if acting as someone else
   * @param actAsSubjectLookup1
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /** optional: reserved for future use */
  private  WsParam[] params;

  
  
  /**
   * optional: reserved for future use
   * @return params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * optional: reserved for future use
   * @param params1
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  


}
