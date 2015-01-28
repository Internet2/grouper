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
/*
 * @author mchyzer
 * $Id: WsRestAddMemberRequest.java,v 1.1 2008/03/30 09:01:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * bean that will be the data from rest request
 * @see GrouperServiceLogic#addMember(edu.internet2.middleware.grouper.ws.GrouperWsVersion, WsGroupLookup, WsSubjectLookup[], boolean, WsSubjectLookup, edu.internet2.middleware.grouper.Field, edu.internet2.middleware.grouper.hibernate.GrouperTransactionType, boolean, boolean, String[], String[], String[])
 * for method
 */
public class WsRestAddMemberRequest implements WsRequestBean {
  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsGroupLookup wsGroupLookup;
  
  /** subjects to assign to */
  private WsSubjectLookup[] subjectLookups;
  
  /** field */
  private String replaceAllExisting;
  
  /** who to act as if not the connecting user */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private String fieldName;
  
  /** field */
  private String txType;
  
  /** field */
  private String includeGroupDetail;
  
  /** field */
  private String includeSubjectDetail;
  
  /** attribute names to return */
  private String[] subjectAttributeNames;
  
  /** field */
  private WsParam[] params;

  /**  date this membership will be disabled, yyyy/MM/dd HH:mm:ss.SSS */
  private String disabledTime;
  
  /**  date this membership will be enabled, yyyy/MM/dd HH:mm:ss.SSS */
  private String enabledTime;
  
  /**
   * T or F, if this is a search by id or identifier, with no source, or the external source,
   * and the subject is not found, then add an external subject (if the user is allowed
   */
  private String addExternalSubjectIfNotFound;
  
  /**
   * T or F, if this is a search by id or identifier, with no source, or the external source,
   * and the subject is not found, then add an external subject (if the user is allowed
   * @return T or F or blank
   */
  public String getAddExternalSubjectIfNotFound() {
    return this.addExternalSubjectIfNotFound;
  }


  /**
   * T or F, if this is a search by id or identifier, with no source, or the external source,
   * and the subject is not found, then add an external subject (if the user is allowed
   * @param addExternalSubjectIfNotFound1
   */
  public void setAddExternalSubjectIfNotFound(String addExternalSubjectIfNotFound1) {
    this.addExternalSubjectIfNotFound = addExternalSubjectIfNotFound1;
  }


  /**
   * date this membership will be disabled, yyyy/MM/dd HH:mm:ss.SSS
   * @return disabled time
   */
  public String getDisabledTime() {
    return this.disabledTime;
  }


  /**
   * date this membership will be disabled, yyyy/MM/dd HH:mm:ss.SSS
   * @param disabledTime1
   */
  public void setDisabledTime(String disabledTime1) {
    this.disabledTime = disabledTime1;
  }


  /**
   * date this membership will be enabled, yyyy/MM/dd HH:mm:ss.SSS
   * @return date
   */
  public String getEnabledTime() {
    return this.enabledTime;
  }


  /**
   * date this membership will be enabled, yyyy/MM/dd HH:mm:ss.SSS
   * @param enabledTime1
   */
  public void setEnabledTime(String enabledTime1) {
    this.enabledTime = enabledTime1;
  }


  /**
   * @return the clientVersion
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  
  /**
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  
  /**
   * @return the wsGroupLookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }

  
  /**
   * @param wsGroupLookup1 the wsGroupLookup to set
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }

  
  /**
   * subjects to assign to
   * @return the subjectLookups
   */
  public WsSubjectLookup[] getSubjectLookups() {
    return this.subjectLookups;
  }

  
  /**
   * subjects to assign to
   * @param subjectLookups1 the subjectLookups to set
   */
  public void setSubjectLookups(WsSubjectLookup[] subjectLookups1) {
    this.subjectLookups = subjectLookups1;
  }

  
  /**
   * @return the replaceAllExisting
   */
  public String getReplaceAllExisting() {
    return this.replaceAllExisting;
  }

  
  /**
   * @param replaceAllExisting1 the replaceAllExisting to set
   */
  public void setReplaceAllExisting(String replaceAllExisting1) {
    this.replaceAllExisting = replaceAllExisting1;
  }

  
  /**
   * who to act as if not the connecting user
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  
  /**
   * who to act as if not the connecting user
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  
  /**
   * @return the fieldName
   */
  public String getFieldName() {
    return this.fieldName;
  }

  
  /**
   * @param fieldName1 the fieldName to set
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }

  
  /**
   * @return the txType
   */
  public String getTxType() {
    return this.txType;
  }

  
  /**
   * @param txType1 the txType to set
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
  }

  
  /**
   * @return the includeGroupDetail
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  
  /**
   * @param includeGroupDetail1 the includeGroupDetail to set
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

  
  /**
   * @return the includeSubjectDetail
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }

  
  /**
   * @param includeSubjectDetail1 the includeSubjectDetail to set
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }

  
  /**
   * @return the subjectAttributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  
  /**
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }


  
  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }


  
  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }

}
