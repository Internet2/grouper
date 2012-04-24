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
 * $Id: WsRestFindGroupsLiteRequest.java,v 1.1 2008-03-29 10:50:43 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;



/**
 * lite bean that will be the data from rest request
 * @see GrouperServiceLogic#getGroupsLite(edu.internet2.middleware.grouper.ws.GrouperWsVersion, String, String, String, edu.internet2.middleware.grouper.ws.member.WsMemberFilter, String, String, String, boolean, boolean, String, String, String, String, String)
 * for lite method
 */
public class WsRestFindGroupsLiteRequest implements WsRequestBean {
  
  /** field */
  private String queryFilterType; 
  
  /** field */
  private String groupName; 
  
  /** field */
  private String stemName; 
  
  /** field */
  private String stemNameScope;
  
  /** field */
  private String groupUuid; 
  
  /** field */
  private String groupAttributeName; 
  
  /** field */
  private String groupAttributeValue;
  
  /** field */
  private String groupTypeName; 
  
  /** 
   * field
   */
  private String clientVersion;
  
  /** field */
  private String actAsSubjectId;
  /** field */
  private String actAsSubjectSourceId;
  
  /** field */
  private String actAsSubjectIdentifier;
  /** field */
  private String includeGroupDetail;
  /** field */
  private String paramName0;
  
  /** field */
  private String paramValue0;
  /** field */
  private String paramName1;
  /** field */
  private String paramValue1;

  /** true or null for ascending, false for descending.  If you pass true or false, must pass a sort string */
  private String ascending;

  /** page number 1 indexed if paging */
  private String pageNumber;

  /** page size if paging */
  private String pageSize;

  /** must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;

  /** comma separated type of groups can be an enum of TypeOfGroup, e.g. group, role, entity */
  private String typeOfGroups;
  
  /**
   * field
   * @return field
   */
  public String getClientVersion() {
    return this.clientVersion;
  }
  /**
   * field
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }
  /**
   * field
   * @return field
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }
  
  /**
   * field
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }
  
  /**
   * field
   * @return field
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }
  
  /**
   * field
   * @param actAsSubjectSource1
   */
  public void setActAsSubjectSourceId(String actAsSubjectSource1) {
    this.actAsSubjectSourceId = actAsSubjectSource1;
  }
  
  /**
   * field
   * @return field
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }
  
  /**
   * field
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }
  
  /**
   * field
   * @return field
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }
  
  /**
   * field
   * @param includeGroupDetail1
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }
  
  /**
   * field
   * @return field
   */
  public String getParamName0() {
    return this.paramName0;
  }
  /**
   * field
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }
  /**
   * field
   * @return field
   */
  public String getParamValue0() {
    return this.paramValue0;
  }
  /**
   * field
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }
  /**
   * field
   * @return field
   */
  public String getParamName1() {
    return this.paramName1;
  }
  /**
   * field
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }
  
  /**
   * field
   * @return field
   */
  public String getParamValue1() {
    return this.paramValue1;
  }
  
  /**
   * field
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }
  
  /**
   * field
   * @return the queryFilterType
   */
  public String getQueryFilterType() {
    return this.queryFilterType;
  }
  
  /**
   * @param queryFilterType1 the queryFilterType to set
   */
  public void setQueryFilterType(String queryFilterType1) {
    this.queryFilterType = queryFilterType1;
  }
  
  /**
   * field
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * field
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }
  
  /**
   * field
   * @return the stemName
   */
  public String getStemName() {
    return this.stemName;
  }
  
  /**
   * field
   * @param stemName1 the stemName to set
   */
  public void setStemName(String stemName1) {
    this.stemName = stemName1;
  }
  
  /**
   * field
   * @return the stemNameScope
   */
  public String getStemNameScope() {
    return this.stemNameScope;
  }
  
  /**
   * field
   * @param stemNameScope1 the stemNameScope to set
   */
  public void setStemNameScope(String stemNameScope1) {
    this.stemNameScope = stemNameScope1;
  }
  
  /**
   * field
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }
  
  /**
   * field
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }
  
  /**
   * field
   * @return the groupAttributeName
   */
  public String getGroupAttributeName() {
    return this.groupAttributeName;
  }
  
  /**
   * field
   * @param groupAttributeName1 the groupAttributeName to set
   */
  public void setGroupAttributeName(String groupAttributeName1) {
    this.groupAttributeName = groupAttributeName1;
  }
  
  /**
   * field
   * @return the groupAttributeValue
   */
  public String getGroupAttributeValue() {
    return this.groupAttributeValue;
  }
  
  /**
   * field
   * @param groupAttributeValue1 the groupAttributeValue to set
   */
  public void setGroupAttributeValue(String groupAttributeValue1) {
    this.groupAttributeValue = groupAttributeValue1;
  }
  
  /**
   * @return the groupTypeName
   */
  public String getGroupTypeName() {
    return this.groupTypeName;
  }
  
  /**
   * field
   * @param groupTypeName1 the groupTypeName to set
   */
  public void setGroupTypeName(String groupTypeName1) {
    this.groupTypeName = groupTypeName1;
  }
  /**
   * true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @return the ascending
   */
  public String getAscending() {
    return this.ascending;
  }
  /**
   * page number 1 indexed if paging
   * @return the pageNumber
   */
  public String getPageNumber() {
    return this.pageNumber;
  }
  /**
   * page size if paging
   * @return the pageSize
   */
  public String getPageSize() {
    return this.pageSize;
  }
  /**
   * must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @return the sortString
   */
  public String getSortString() {
    return this.sortString;
  }
  /**
   * true or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @param ascending1 the ascending to set
   */
  public void setAscending(String ascending1) {
    this.ascending = ascending1;
  }
  /**
   * page number 1 indexed if paging
   * @param pageNumber1 the pageNumber to set
   */
  public void setPageNumber(String pageNumber1) {
    this.pageNumber = pageNumber1;
  }
  /**
   * page size if paging
   * @param pageSize1 the pageSize to set
   */
  public void setPageSize(String pageSize1) {
    this.pageSize = pageSize1;
  }
  /**
   * must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param sortString1 the sortString to set
   */
  public void setSortString(String sortString1) {
    this.sortString = sortString1;
  }
  /**
   * comma separated type of groups can be an enum of TypeOfGroup, e.g. group, role, entity
   * @return type of group
   */
  public String getTypeOfGroups() {
    return this.typeOfGroups;
  }
  /**
   * comma separated type of groups can be an enum of TypeOfGroup, e.g. group, role, entity
   * @param typeOfGroups1
   */
  public void setTypeOfGroups(String typeOfGroups1) {
    this.typeOfGroups = typeOfGroups1;
  }

}
