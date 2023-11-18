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

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;


/**
 * request bean in body of rest lite request
 */
public class WsRestAssignAttributeDefNameInheritanceLiteRequest implements WsRequestBean {

  /**
   * T to assign, or F to remove assignment
   */
  private String assign;
  
  /**
   * T if assigning, if this list should replace all existing immediately inherited attribute def names
   */
  private String replaceAllExisting;
  
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

  /** if acting as another user */
  private String actAsSubjectId;

  /** if acting as another user */
  private String actAsSubjectIdentifier;

  /** if acting as another user */
  private String actAsSubjectSourceId;

  /** reserved for future use */
  private String paramName0;

  /** reserved for future use */
  private String paramName1;

  /** reserved for future use */
  private String paramValue0;

  /** reserved for future use */
  private String paramValue1;

  /**
   * name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId
   */
  private String attributeDefNameName;

  /**
   * id of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameName
   */
  private String attributeDefNameUuid;

  /**
   * name of attribute def name to add or remove from inheritance from the container
   */
  private String relatedAttributeDefNameName;

  /**
   * id of attribute def name to add or remove from inheritance from the container
   */
  private String relatedAttributeDefNameUuid;
  
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

  /**
   * if acting as another user
   * @return id
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * if acting as another user
   * @return subject identifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * if acting as another user
   * @return source id 
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * reserved for future use
   * @return param name 0
   */
  public String getParamName0() {
    return this.paramName0;
  }

  /**
   * reserved for future use
   * @return paramname1
   */
  public String getParamName1() {
    return this.paramName1;
  }

  /**
   * reserved for future use
   * @return param value 0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }

  /**
   * reserved for future use
   * @return param value 1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }

  /**
   * if acting as another user
   * @param actAsSubjectSourceId1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSourceId1) {
    this.actAsSubjectSourceId = actAsSubjectSourceId1;
  }

  /**
   * reserved for future use
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }

  /**
   * reserved for future use
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }

  /**
   * reserved for future use
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }

  /**
   * reserved for future use
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }

  /**
   * T to assign, or F to remove assignment
   * @return T to assign, or F to remove assignment
   */
  public String getAssign() {
    return this.assign;
  }

  /**
   * name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId
   * @return name
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**
   * id of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameName
   * @return attributeDefNameUuid
   */
  public String getAttributeDefNameUuid() {
    return this.attributeDefNameUuid;
  }

  /**
   * name of attribute def name to add or remove from inheritance from the container
   * @return name of attribute def name to add or remove from inheritance from the container
   */
  public String getRelatedAttributeDefNameName() {
    return this.relatedAttributeDefNameName;
  }

  /**
   * id of attribute def name to add or remove from inheritance from the container
   * @return id of attribute def name to add or remove from inheritance from the container
   */
  public String getRelatedAttributeDefNameUuid() {
    return this.relatedAttributeDefNameUuid;
  }

  /**
   * T to assign, or F to remove assignment
   * @param assign1
   */
  public void setAssign(String assign1) {
    this.assign = assign1;
  }

  /**
   * name of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameId
   * @param attributeDefNameName1
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }

  /**
   * id of attributeDefName which is the container for the inherited attribute def names, mutually exclusive with attributeDefNameName
   * @param attributeDefNameUuid1
   */
  public void setAttributeDefNameUuid(String attributeDefNameUuid1) {
    this.attributeDefNameUuid = attributeDefNameUuid1;
  }

  /**
   * name of attribute def name to add or remove from inheritance from the container
   * @param relatedAttributeDefNameName1
   */
  public void setRelatedAttributeDefNameName(String relatedAttributeDefNameName1) {
    this.relatedAttributeDefNameName = relatedAttributeDefNameName1;
  }

  /**
   * id of attribute def name to add or remove from inheritance from the container
   * @param relatedAttributeDefNameUuid1
   */
  public void setRelatedAttributeDefNameUuid(String relatedAttributeDefNameUuid1) {
    this.relatedAttributeDefNameUuid = relatedAttributeDefNameUuid1;
  }

  


}
