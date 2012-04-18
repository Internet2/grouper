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
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * <pre>
 * Class with data about retrieving privileges for a subject and group
 * 
 * </pre>
 * @author mchyzer
 */
public class WsRestGetGrouperPrivilegesLiteRequest implements WsRequestBean {

  /**
   * version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   */
  private String clientVersion;
  
  /**
   * subject id of subject to search for privileges.  Mutually exclusive with subjectIdentifier
   */
  private String subjectId;
  
  /**
   * source id of subject object (optional)
   */
  private String subjectSourceId;
  
  /**
   * subject identifier of subject.  Mutuallyexclusive with subjectId
   */
  private String subjectIdentifier;
  
  /**
   * if this is a group privilege.  mutually exclusive with groupUuid
   */
  private String groupName;
  
  /**
   * if this is a group privilege.  mutually exclusive with groupName
   */
  private String groupUuid;
  
  /**
   * if this is a stem privilege.  mutually exclusive with stemUuid
   */
  private String stemName;
  
  /**
   * if this is a stem privilege.  mutually exclusive with stemName
   */
  private String stemUuid;
  
  /**
   * privilegeType (e.g. "access" for groups and "naming" for stems)
   */
  private String privilegeType;
  
  /**
   * (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   */
  private String privilegeName;
  
  /**
   * optional: is the subject id of subject to act as (if
   * proxying). Only pass one of actAsSubjectId or
   * actAsSubjectIdentifer
   */
  private String actAsSubjectId;
  
  /**
   * is source of act as subject to narrow the result and prevent
   * duplicates
   */
  private String actAsSubjectSourceId;
  
  /**
   * optional: is the subject identifier of subject to act as (if
   * proxying). Only pass one of actAsSubjectId or
   * actAsSubjectIdentifer
   */
  private String actAsSubjectIdentifier;
  
  /**
   * 
   */
  private String includeSubjectDetail;
  
  /**
   * additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   */
  private String subjectAttributeNames;
  
  /**
   * T or F as for if group detail should be included
   */
  private String includeGroupDetail;
  
  /**
   * reserved for future use
   */
  private String paramName0;
  
  /**
   * reserved for future use
   */
  private String paramValue0;
  
  /**
   * reserved for future use
   */
  private String paramName1;
  
  /**
   * reserved for future use
   */
  private String paramValue1;

  /**
   * version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * 
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /**
   * subject id of subject to search for privileges.  Mutually exclusive with subjectIdentifier
   * @return subjectid
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject id of subject to search for privileges.  Mutually exclusive with subjectIdentifier
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * source id of subject object (optional)
   * @return source id
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * source id of subject object (optional)
   * @param subjectSourceId1
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }

  /**
   * subject identifier of subject.  Mutuallyexclusive with subjectId
   * @return id
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }

  /**
   * subject identifier of subject.  Mutuallyexclusive with subjectId
   * @param subjectIdentifier1
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
  }

  /**
   * if this is a group privilege.  mutually exclusive with groupUuid
   * @return name
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * if this is a group privilege.  mutually exclusive with groupUuid
   * @param groupName1
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * if this is a group privilege.  mutually exclusive with groupName
   * @return uuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }

  /**
   * if this is a group privilege.  mutually exclusive with groupName
   * @param groupUuid1
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }

  /**
   * if this is a stem privilege.  mutually exclusive with stemUuid
   * @return name
   */
  public String getStemName() {
    return this.stemName;
  }

  /**
   * if this is a stem privilege.  mutually exclusive with stemUuid
   * @param stemName1
   */
  public void setStemName(String stemName1) {
    this.stemName = stemName1;
  }

  /**
   * if this is a stem privilege.  mutually exclusive with stemName
   * @return stem uuid
   */
  public String getStemUuid() {
    return this.stemUuid;
  }

  /**
   * if this is a stem privilege.  mutually exclusive with stemName
   * @param stemUuid1
   */
  public void setStemUuid(String stemUuid1) {
    this.stemUuid = stemUuid1;
  }

  /**
   * privilegeType (e.g. "access" for groups and "naming" for stems)
   * @return type
   */
  public String getPrivilegeType() {
    return this.privilegeType;
  }

  /**
   * privilegeType (e.g. "access" for groups and "naming" for stems)
   * @param privilegeType1
   */
  public void setPrivilegeType(String privilegeType1) {
    this.privilegeType = privilegeType1;
  }

  /**
   * (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @return name
   */
  public String getPrivilegeName() {
    return this.privilegeName;
  }

  /**
   * (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @param privilegeName1
   */
  public void setPrivilegeName(String privilegeName1) {
    this.privilegeName = privilegeName1;
  }

  /**
   * optional: is the subject id of subject to act as (if
   * proxying). Only pass one of actAsSubjectId or
   * actAsSubjectIdentifer
   * @return id
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * optional: is the subject id of subject to act as (if
   * proxying). Only pass one of actAsSubjectId or
   * actAsSubjectIdentifer
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * is source of act as subject to narrow the result and prevent
   * duplicates
   * @return id
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectSourceId1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSourceId1) {
    this.actAsSubjectSourceId = actAsSubjectSourceId1;
  }

  /**
   * optional: is the subject identifier of subject to act as (if
   * proxying). Only pass one of actAsSubjectId or
   * actAsSubjectIdentifer
   * @return id
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * optional: is the subject identifier of subject to act as (if
   * proxying). Only pass one of actAsSubjectId or
   * actAsSubjectIdentifer
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }

  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @return include detail
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }

  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @param includeSubjectDetail1
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }

  /**
   * additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @return names to return
   */
  public String getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param subjectAttributeNames1
   */
  public void setSubjectAttributeNames(String subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }

  /**
   * T or F as for if group detail should be included
   * @return T of F
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  /**
   * T or F as for if group detail should be included
   * @param includeGroupDetail1
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

  /**
   * reserved for future use
   * @return param
   */
  public String getParamName0() {
    return this.paramName0;
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
   * @return param
   */
  public String getParamValue0() {
    return this.paramValue0;
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
   * @return param
   */
  public String getParamName1() {
    return this.paramName1;
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
   * @return param
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * reserved for future use
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }
  

  
}
