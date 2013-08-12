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
 * @author mchyzer $Id: WsRestGetMembershipsLiteRequest.java,v 1.2 2009-12-19 21:38:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.membership;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * request bean for rest get members request
 */
public class WsRestGetMembershipsLiteRequest implements WsRequestBean {

  /** client version: field is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /** group name to look in */
  private String groupName;
  
  /** group uuid to look in */
  private String groupUuid;

  /** 
   * if looking for privileges on stems, put the stem name to look for here
   */
  private String ownerStemName;
  
  /**
   * if looking for privileges on stems, put the stem uuid here
   */
  private String ownerStemUuid;
  
  /**
   * if looking for privileges on attribute definitions, put the name of the attribute definition here
   */
  private String nameOfOwnerAttributeDef;
  
  /**
   * if looking for privileges on attribute definitions, put the uuid of the attribute definition here
   */
  private String ownerAttributeDefUuid;

  /**
   * if looking for privileges on stems, put the stem name to look for here
   * @return stem name
   */
  public String getOwnerStemName() {
    return this.ownerStemName;
  }


  /**
   * if looking for privileges on stems, put the stem name to look for here
   * @param ownerStemName1
   */
  public void setOwnerStemName(String ownerStemName1) {
    this.ownerStemName = ownerStemName1;
  }

  /**
   * if looking for privileges on stems, put the stem uuid here
   * @return privs on stems
   */
  public String getOwnerStemUuid() {
    return this.ownerStemUuid;
  }


  /**
   * if looking for privileges on stems, put the stem uuid here
   * @param ownerStemUuid1
   */
  public void setOwnerStemUuid(String ownerStemUuid1) {
    this.ownerStemUuid = ownerStemUuid1;
  }


  /**
   * if looking for privileges on attribute definitions, put the name of the attribute definition here
   * @return name
   */
  public String getNameOfOwnerAttributeDef() {
    return this.nameOfOwnerAttributeDef;
  }


  /**
   * if looking for privileges on attribute definitions, put the name of the attribute definition here
   * @param nameOfOwnerAttributeDef1
   */
  public void setNameOfOwnerAttributeDef(String nameOfOwnerAttributeDef1) {
    this.nameOfOwnerAttributeDef = nameOfOwnerAttributeDef1;
  }


  /**
   * if looking for privileges on attribute definitions, put the uuid of the attribute definition here
   * @return uuid
   */
  public String getOwnerAttributeDefUuid() {
    return this.ownerAttributeDefUuid;
  }


  /**
   * if looking for privileges on attribute definitions, put the uuid of the attribute definition here
   * @param ownerAttributeDefUuid1
   */
  public void setOwnerAttributeDefUuid(String ownerAttributeDefUuid1) {
    this.ownerAttributeDefUuid = ownerAttributeDefUuid1;
  }


  /** retrieveSubjectDetail */
  private String retrieveSubjectDetail;

  /** actAsSubjectId subject to act as instead of logged in user */
  private String actAsSubjectId;

  /** actAsSubjectSource  subject to act as instead of logged in user */
  private String actAsSubjectSourceId;

  /** actAsSubjectIdentifier subject to act as instead of logged in user  */
  private String actAsSubjectIdentifier;

  /** subjectAttributeNames */
  private String subjectAttributeNames;

  /** T or F as to if the group detail should be returned */
  private String includeGroupDetail;

  /** optional: reserved for future use */
  private String paramName0;

  /** optional: reserved for future use */
  private String paramValue0;

  /** optional: reserved for future use */
  private String paramName1;

  /** optional: reserved for future use */
  private String paramValue1;

  /** sourceids to limit request to, or null for all */
  private String sourceIds;

  /** enabled is A for all, T or null for enabled only, F for disabled */
  private String enabled;

  /** is if the memberships should be retrieved from a certain field membership
   * of the group (certain list) */
  private String fieldName;

  /** T|F, for if the extended subject information should be
   * returned (anything more than just the id) */
  private String includeSubjectDetail;

  /** must be one of All, Effective, Immediate, Composite, NonImmediate */
  private String memberFilter;

  /** membershipIds are the comma separated list of ids to search for if known */
  private String membershipIds;

  /** scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names) */
  private String scope;

  /** stemScope is StemScope to search only in one stem or in substems: ONE_LEVEL, ALL_IN_SUBTREE */
  private String stemScope;

  /** stem to look in for memberships */
  private String stemUuid;
  
  /** stem to look in for memberships */
  private String stemName;

  /** subject to look for memberships of */
  private String subjectId;

  /** subject to look for memberships of */
  private String subjectIdentifier;

  /** subject to look for memberships of */
  private String subjectSourceId;
  
  /**
   * subject id to look for memberships
   * @return subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }


  /**
   * subject id to look for memberships
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }


  /**
   * subject id to look for memberships
   * @return subject identifier
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }


  /**
   * subject id to look for memberships
   * @param subjectIdentifier1
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
  }


  /**
   * subject id to look for memberships
   * @return source id
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }


  /**
   * subject id to look for memberships
   * @param subjectSourceId1
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }


  /**
   * stem to look in for memberships
   * @return stem uuid
   */
  public String getStemUuid() {
    return this.stemUuid;
  }


  /**
   * stem to look in for memberships
   * @param stemUuid1
   */
  public void setStemUuid(String stemUuid1) {
    this.stemUuid = stemUuid1;
  }

  /**
   * stem to look in for memberships
   * @return stem name
   */
  public String getStemName() {
    return this.stemName;
  }

  /**
   * stem to look in for memberships
   * @param stemName1
   */
  public void setStemName(String stemName1) {
    this.stemName = stemName1;
  }


  /**
   * membershipIds are the comma separated list of ids to search for if known
   * @return membership id
   */
  public String getMembershipIds() {
    return this.membershipIds;
  }


  /**
   * membershipIds are the comma separated list of ids to search for if known
   * @param membershipsId1
   */
  public void setMembershipIds(String membershipsId1) {
    this.membershipIds = membershipsId1;
  }


  /**
   * sourceids to limit request to, or null for all
   * @return the sourceIds
   */
  public String getSourceIds() {
    return this.sourceIds;
  }

  
  /**
   * sourceids to limit request to, or null for all
   * @param sourceIds1 the sourceIds to set
   */
  public void setSourceIds(String sourceIds1) {
    this.sourceIds = sourceIds1;
  }

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
   * actAsSubjectId subject to act as instead of logged in user
   * @return actAsSubjectId
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }

  /**
   * actAsSubjectId subject to act as instead of logged in user
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }

  /**
   * actAsSubjectSource subject to act as instead of logged in user
   * @return actAsSubjectSource
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }

  /**
   * actAsSubjectSource subject to act as instead of logged in user
   * @param actAsSubjectSource1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSource1) {
    this.actAsSubjectSourceId = actAsSubjectSource1;
  }

  /**
   * actAsSubjectIdentifier subject to act as instead of logged in user
   * @return actAsSubjectIdentifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }

  /**
   * actAsSubjectIdentifier subject to act as instead of logged in user
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
   * T or F as to if the group detail should be returned
   * @return includeGroupDetail
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  /**
   * T or F as to if the group detail should be returned
   * @param includeGroupDetail1
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

  /**
   * paramName0 optional: reserved for future use
   * @return paramName0
   */
  public String getParamName0() {
    return this.paramName0;
  }

  /**
   * paramName0 optional: reserved for future use
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }

  /**
   * paramValue0 optional: reserved for future use
   * @return paramValue0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }

  /**
   * _paramValue0 optional: reserved for future use
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }

  /**
   * paramName1 optional: reserved for future use
   * @return paramName1
   */
  public String getParamName1() {
    return this.paramName1;
  }

  /**
   * paramName1 optional: reserved for future use
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }

  /**
   * paramValue1 optional: reserved for future use
   * @return paramValue1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }

  /**
   * paramValue1 optional: reserved for future use
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }
  
  /**
   * group name to look in
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * group name to look in
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }
  
  /**
   * group uuid to look in
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }
  
  /**
   * group uuid to look in
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }
  
  /**
   * field is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return the clientVersion
   */
  public String getClientVersion() {
    return this.clientVersion;
  }
  
  /**
   * field is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }


  /**
   * enabled is A for all, T or null for enabled only, F for disabled 
   * @return enabled
   */
  public String getEnabled() {
    return this.enabled;
  }


  /**
   * enabled is A for all, T or null for enabled only, F for disabled 
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }


  /**
   * is if the memberships should be retrieved from a certain field membership
   * of the group (certain list)
   * @return the fieldName
   */
  public String getFieldName() {
    return this.fieldName;
  }


  /**
   * is if the memberships should be retrieved from a certain field membership
   * of the group (certain list)
   * @param fieldName1 the fieldName to set
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }


  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @return the includeSubjectDetail
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }


  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @param includeSubjectDetail1 the includeSubjectDetail to set
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }


  /**
   * must be one of All, Effective, Immediate, Composite, NonImmediate
   * @return the replaceAllExisting
   */
  public String getMemberFilter() {
    return this.memberFilter;
  }


  /**
   * must be one of All, Effective, Immediate, Composite, NonImmediate
   * @param replaceAllExisting1 the replaceAllExisting to set
   */
  public void setMemberFilter(String replaceAllExisting1) {
    this.memberFilter = replaceAllExisting1;
  }


  /**
   * scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names)
   * @return scope
   */
  public String getScope() {
    return this.scope;
  }


  /**
   * stemScope is StemScope to search only in one stem or in substems: ONE_LEVEL, ALL_IN_SUBTREE
   * @return stem scope
   */
  public String getStemScope() {
    return this.stemScope;
  }


  /**
   * scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names)
   * @param scope1
   */
  public void setScope(String scope1) {
    this.scope = scope1;
  }


  /**
   * stemScope is StemScope to search only in one stem or in substems: ONE_LEVEL, ALL_IN_SUBTREE
   * @param stemScope1
   */
  public void setStemScope(String stemScope1) {
    this.stemScope = stemScope1;
  }
}
