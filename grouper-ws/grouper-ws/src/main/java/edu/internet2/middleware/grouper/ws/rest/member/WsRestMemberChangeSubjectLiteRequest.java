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
 * @author mchyzer $Id: WsRestMemberChangeSubjectLiteRequest.java,v 1.1 2008-10-21 03:51:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * request bean for rest member change subject lite
 */
public class WsRestMemberChangeSubjectLiteRequest implements WsRequestBean {

  /** client version */
  private String clientVersion;
  
  /** retrieveSubjectDetail */
  private String retrieveSubjectDetail;

  /** actAsSubjectId */
  private String actAsSubjectId;

  /** actAsSubjectSource */
  private String actAsSubjectSourceId;

  /** actAsSubjectIdentifier */
  private String actAsSubjectIdentifier;

  /** subjectAttributeNames */
  private String subjectAttributeNames;

  /** paramName0 */
  private String paramName0;

  /** paramValue0 */
  private String paramValue0;

  /** paramName1 */
  private String paramName1;

  /** paramValue1 */
  private String paramValue1;

  /**
   * oldSubjectId subject id of old member object.  This is the preferred way to look up the 
   * old subject, but subjectIdentifier could also be used
   */
  private String oldSubjectId;
  
  /**
   * oldSubjectSourceId source id of old member object (optional)
   */
  private String oldSubjectSourceId;

  /**
   * oldSubjectIdentifier subject identifier of old member object.  It is preferred to lookup the 
   * old subject by id, but if identifier is used, that is ok instead (as long as subject is resolvable).
   */
  private String oldSubjectIdentifier;
  
  /**
   * newSubjectId preferred way to identify the new subject id
   */
  private String newSubjectId;
  
  /**
   * newSubjectSourceId preferres way to identify the new subject id
   */
  private String newSubjectSourceId;
  
  /**
   * newSubjectIdentifier subjectId is the preferred way to lookup the new subject, but identifier is
   * ok to use instead
   */
  private String newSubjectIdentifier;
  
  /**
   * deleteOldMember T or F as to whether the old member should be deleted (if new member does exist).
   * This defaults to T if it is blank
   */
  private String deleteOldMember;

  /**
   * T of F for is subject detail should be added
   */
  private String includeSubjectDetail;
  
  /**
   * retrieveSubjectDetail
   * @return retrieveSubjectDetail
   */
  public String getRetrieveSubjectDetail() {
    return this.retrieveSubjectDetail;
  }

  /**
   * retrieveSubjectDetail1
   * @param retrieveSubjectDetail1
   */
  public void setRetrieveSubjectDetail(String retrieveSubjectDetail1) {
    this.retrieveSubjectDetail = retrieveSubjectDetail1;
  }

  /**
   * actAsSubjectId
   * @return actAsSubjectId
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * actAsSubjectId
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * actAsSubjectSource
   * @return actAsSubjectSource
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * actAsSubjectSource
   * @param actAsSubjectSource1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSource1) {
    this.actAsSubjectSourceId = actAsSubjectSource1;
  }

  /**
   * actAsSubjectIdentifier
   * @return actAsSubjectIdentifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * actAsSubjectIdentifier
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }

  /**
   * subjectAttributeNames
   * @return subjectAttributeNames
   */
  public String getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * subjectAttributeNames
   * @param subjectAttributeNames1
   */
  public void setSubjectAttributeNames(String subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }

  /**
   * paramName0
   * @return paramName0
   */
  public String getParamName0() {
    return this.paramName0;
  }

  /**
   * paramName0
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }

  /**
   * paramValue0
   * @return paramValue0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }

  /**
   * _paramValue0
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }

  /**
   * paramName1
   * @return paramName1
   */
  public String getParamName1() {
    return this.paramName1;
  }

  /**
   * paramName1
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }

  /**
   * paramValue1
   * @return paramValue1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * paramValue1
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
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
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }

  /**
   * oldSubjectId subject id of old member object.  This is the preferred way to look up the 
   * old subject, but subjectIdentifier could also be used
   * @return subject id
   */
  public String getOldSubjectId() {
    return this.oldSubjectId;
  }

  /**
   * oldSubjectId subject id of old member object.  This is the preferred way to look up the 
   * old subject, but subjectIdentifier could also be used
   * @param oldSubjectId1
   */
  public void setOldSubjectId(String oldSubjectId1) {
    this.oldSubjectId = oldSubjectId1;
  }

  /**
   * oldSubjectSourceId source id of old member object (optional)
   * @return old subject source id
   */
  public String getOldSubjectSourceId() {
    return this.oldSubjectSourceId;
  }

  /**
   * oldSubjectSourceId source id of old member object (optional)
   * @param oldSubjectSourceId1
   */
  public void setOldSubjectSourceId(String oldSubjectSourceId1) {
    this.oldSubjectSourceId = oldSubjectSourceId1;
  }

  /**
   * oldSubjectIdentifier subject identifier of old member object.  It is preferred to lookup the 
   * old subject by id, but if identifier is used, that is ok instead (as long as subject is resolvable).
   * @return old subject identifier
   */
  public String getOldSubjectIdentifier() {
    return this.oldSubjectIdentifier;
  }

  /**
   * oldSubjectIdentifier subject identifier of old member object.  It is preferred to lookup the 
   * old subject by id, but if identifier is used, that is ok instead (as long as subject is resolvable).
   * @param oldSubjectIdentifier1
   */
  public void setOldSubjectIdentifier(String oldSubjectIdentifier1) {
    this.oldSubjectIdentifier = oldSubjectIdentifier1;
  }

  /**
   * newSubjectId preferred way to identify the new subject id
   * @return new subject id
   */
  public String getNewSubjectId() {
    return this.newSubjectId;
  }

  /**
   * newSubjectId preferred way to identify the new subject id
   * @param newSubjectId1
   */
  public void setNewSubjectId(String newSubjectId1) {
    this.newSubjectId = newSubjectId1;
  }

  /**
   * newSubjectSourceId preferres way to identify the new subject id
   * @return source id
   */
  public String getNewSubjectSourceId() {
    return this.newSubjectSourceId;
  }

  /**
   * newSubjectSourceId preferres way to identify the new subject id
   * @param newSubjectSourceId1
   */
  public void setNewSubjectSourceId(String newSubjectSourceId1) {
    this.newSubjectSourceId = newSubjectSourceId1;
  }

  /**
   * newSubjectIdentifier subjectId is the preferred way to lookup the new subject, but identifier is
   * ok to use instead
   * @return subject identifier
   */
  public String getNewSubjectIdentifier() {
    return this.newSubjectIdentifier;
  }

  /**
   * newSubjectIdentifier subjectId is the preferred way to lookup the new subject, but identifier is
   * ok to use instead
   * @param newSubjectIdentifier1
   */
  public void setNewSubjectIdentifier(String newSubjectIdentifier1) {
    this.newSubjectIdentifier = newSubjectIdentifier1;
  }

  /**
   * deleteOldMember T or F as to whether the old member should be deleted (if new member does exist).
   * This defaults to T if it is blank
   * @return if delete
   */
  public String getDeleteOldMember() {
    return this.deleteOldMember;
  }

  /**
   * deleteOldMember T or F as to whether the old member should be deleted (if new member does exist).
   * This defaults to T if it is blank
   * @param deleteOldMember1
   */
  public void setDeleteOldMember(String deleteOldMember1) {
    this.deleteOldMember = deleteOldMember1;
  }

  /**
   * T of F for is subject detail should be added
   * @return
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }

  /**
   * T of F for is subject detail should be added
   * @param includeSubjectDetail1
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }
}
