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

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;


/**
 * request bean in body of rest request
 */
public class WsRestAssignAttributeDefNameInheritanceRequest implements WsRequestBean {

  /**
   * attributeDefName which is the container for the inherited attribute def names
   */
  private WsAttributeDefNameLookup wsAttributeDefNameLookup;
  
  /**
   * one or many attribute def names to add or remove from inheritance from the container
   */
  private WsAttributeDefNameLookup[] relatedWsAttributeDefNameLookups;
  
  /**
   * T to assign, or F to remove assignment
   */
  private String assign;
  
  /**
   * T if assigning, if this list should replace all existing immediately inherited attribute def names
   */
  private String replaceAllExisting;
  
  /**
   * attributeDefName which is the container for the inherited attribute def names
   * @return wsAttributeDefNameLookup
   */
  public WsAttributeDefNameLookup getWsAttributeDefNameLookup() {
    return this.wsAttributeDefNameLookup;
  }

  /**
   * attributeDefName which is the container for the inherited attribute def names
   * @param wsAttributeDefNameLookup1
   */
  public void setWsAttributeDefNameLookup(WsAttributeDefNameLookup wsAttributeDefNameLookup1) {
    this.wsAttributeDefNameLookup = wsAttributeDefNameLookup1;
  }

  /**
   * one or many attribute def names to add or remove from inheritance from the container
   * @return relatedWsAttributeDefNameLookups
   */
  public WsAttributeDefNameLookup[] getRelatedWsAttributeDefNameLookups() {
    return this.relatedWsAttributeDefNameLookups;
  }

  /**
   * one or many attribute def names to add or remove from inheritance from the container
   * @param relatedWsAttributeDefNameLookups1
   */
  public void setRelatedWsAttributeDefNameLookups(
      WsAttributeDefNameLookup[] relatedWsAttributeDefNameLookups1) {
    this.relatedWsAttributeDefNameLookups = relatedWsAttributeDefNameLookups1;
  }

  /**
   * T to assign, or F to remove assignment
   * @return assign
   */
  public String getAssign() {
    return this.assign;
  }

  /**
   * T to assign, or F to remove assignment
   * @param assign1
   */
  public void setAssign(String assign1) {
    this.assign = assign1;
  }

  /**
   * T if assigning, if this list should replace all existing immediately inherited attribute def names
   * @return replaceAllExisting
   */
  public String getReplaceAllExisting() {
    return this.replaceAllExisting;
  }

  /**
   * T if assigning, if this list should replace all existing immediately inherited attribute def names
   * @param replaceAllExisting1
   */
  public void setReplaceAllExisting(String replaceAllExisting1) {
    this.replaceAllExisting = replaceAllExisting1;
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
