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
 * $Id: WsRestGetMembershipsRequest.java,v 1.2 2009-12-19 21:38:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.membership;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * bean that will be the data from rest request
 * @see GrouperServiceLogic#getMemberships(edu.internet2.middleware.grouper.ws.GrouperWsVersion, WsGroupLookup[], WsSubjectLookup[], edu.internet2.middleware.grouper.ws.member.WsMemberFilter, WsSubjectLookup, edu.internet2.middleware.grouper.Field, boolean, String[], boolean, WsParam[], String[], String, edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup, edu.internet2.middleware.grouper.ws.query.StemScope, String, String[])
 * for method
 */
public class WsRestGetMembershipsRequest implements WsRequestBean {
  
  /** field is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;
  
  /** are groups to look in */
  private WsGroupLookup[] wsGroupLookups;
  
  /** stem lookups if looking for memberships on certain stems */
  private WsStemLookup[] wsOwnerStemLookups;
  
  /** attribute definition lookups if looking for memberships on certain attribute definitions */
  private WsAttributeDefLookup[] wsOwnerAttributeDefLookups;

  /**
   * stem lookups if looking for memberships on certain stems
   * @return stem lookups
   */
  public WsStemLookup[] getWsOwnerStemLookups() {
    return this.wsOwnerStemLookups;
  }

  /**
   * stem lookups if looking for memberships on certain stems
   * @param wsStemLookups1
   */
  public void setWsOwnerStemLookups(WsStemLookup[] wsStemLookups1) {
    this.wsOwnerStemLookups = wsStemLookups1;
  }

  /**
   * attribute definition lookups if looking for memberships on certain attribute definitions
   * @return attributeDef lookups
   */
  public WsAttributeDefLookup[] getWsOwnerAttributeDefLookups() {
    return this.wsOwnerAttributeDefLookups;
  }

  /**
   * attribute definition lookups if looking for memberships on certain attribute definitions
   * @param wsAttributeDefLookups1
   */
  public void setWsOwnerAttributeDefLookups(WsAttributeDefLookup[] wsAttributeDefLookups1) {
    this.wsOwnerAttributeDefLookups = wsAttributeDefLookups1;
  }

  /** must be one of All, Effective, Immediate, Composite, NonImmediate */
  private String memberFilter;
  
  /** subject to act as instead of logged in user */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** is if the memberships should be retrieved from a certain field membership
   * of the group (certain list) */
  private String fieldName;
  
  /**
   * fieldType is the type of field to look at, e.g. list (default, memberships), 
   * access (privs on groups), attribute_def (privs on attribute definitions), naming (privs on folders)
   */
  private String fieldType;

  /**
   * fieldType is the type of field to look at, e.g. list (default, memberships), 
   * access (privs on groups), attribute_def (privs on attribute definitions), naming (privs on folders)
   * @return field type
   */
  public String getFieldType() {
    return this.fieldType;
  }

  /**
   * fieldType is the type of field to look at, e.g. list (default, memberships), 
   * access (privs on groups), attribute_def (privs on attribute definitions), naming (privs on folders)
   * @param fieldType1
   */
  public void setFieldType(String fieldType1) {
    this.fieldType = fieldType1;
  }

  /** T or F as to if the group detail should be returned */
  private String includeGroupDetail;
  
  /** T|F, for if the extended subject information should be
   * returned (anything more than just the id) */
  private String includeSubjectDetail;
  
  /**  are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent */
  private String[] subjectAttributeNames;
  
  /** optional: reserved for future use */
  private WsParam[] params;
  
  /** sourceIds are sources to look in for memberships, or null if all */
  private String[] sourceIds;

  /** are subjects to look in */
  private WsSubjectLookup[] wsSubjectLookups;
  
  /** scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names) */
  private String scope;
  
  /** wsStemLookup is the stem to look in for memberships */
  private WsStemLookup wsStemLookup;
  
  /** stemScope is StemScope to search only in one stem or in substems: ONE_LEVEL, ALL_IN_SUBTREE */
  private String stemScope;
  
  /** enabled is A for all, T or null for enabled only, F for disabled */
  private String enabled;
 
  /** membershipIds are the ids to search for if they are known */
  private String[] membershipIds;
  
  /** serviceRole to filter attributes that a user has a certain role */
  private String serviceRole;

  /** serviceLookup if filtering by users in a service, then this is the service to look in */
  private WsAttributeDefNameLookup serviceLookup;
  
  /**
   * serviceRole to filter attributes that a user has a certain role
   * @return the service role
   */
  public String getServiceRole() {
    return this.serviceRole;
  }

  /**
   * serviceRole to filter attributes that a user has a certain role
   * @param serviceRole1
   */
  public void setServiceRole(String serviceRole1) {
    this.serviceRole = serviceRole1;
  }

  /**
   * serviceLookup if filtering by users in a service, then this is the service to look in
   * @return service lookup
   */
  public WsAttributeDefNameLookup getServiceLookup() {
    return this.serviceLookup;
  }

  /**
   * serviceLookup if filtering by users in a service, then this is the service to look in
   * @param serviceLookup1
   */
  public void setServiceLookup(WsAttributeDefNameLookup serviceLookup1) {
    this.serviceLookup = serviceLookup1;
  }

  /**
   * membershipIds are the ids to search for if they are known
   * @return the ids
   */
  public String[] getMembershipIds() {
    return this.membershipIds;
  }

  /**
   * membershipIds are the ids to search for if they are known
   * @param membershipsIds1
   */
  public void setMembershipIds(String[] membershipsIds1) {
    this.membershipIds = membershipsIds1;
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
   * stemScope is StemScope to search only in one stem or in substems: ONE_LEVEL, ALL_IN_SUBTREE
   * @return stem scope
   */
  public String getStemScope() {
    return this.stemScope;
  }

  /**
   * stemScope is StemScope to search only in one stem or in substems: ONE_LEVEL, ALL_IN_SUBTREE
   * @param stemScope1
   */
  public void setStemScope(String stemScope1) {
    this.stemScope = stemScope1;
  }

  /**
   * wsStemLookup is the stem to look in for memberships
   * @return stem lookup
   */
  public WsStemLookup getWsStemLookup() {
    return this.wsStemLookup;
  }

  /**
   * wsStemLookup is the stem to look in for memberships
   * @param wsStemLookup1
   */
  public void setWsStemLookup(WsStemLookup wsStemLookup1) {
    this.wsStemLookup = wsStemLookup1;
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
   * scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names)
   * @param scope1
   */
  public void setScope(String scope1) {
    this.scope = scope1;
  }

  /**
   * are subjects to look in
   * @return subjects
   */
  public WsSubjectLookup[] getWsSubjectLookups() {
    return this.wsSubjectLookups;
  }

  /**
   * are subjects to look in
   * @param wsSubjectLookups1
   */
  public void setWsSubjectLookups(WsSubjectLookup[] wsSubjectLookups1) {
    this.wsSubjectLookups = wsSubjectLookups1;
  }

  /**
   * sourceIds are sources to look in for memberships, or null if all
   * @return the sourceIds
   */
  public String[] getSourceIds() {
    return this.sourceIds;
  }
  
  /**
   * sourceIds are sources to look in for memberships, or null if all
   * @param sourceIds1 the sourceIds to set
   */
  public void setSourceIds(String[] sourceIds1) {
    this.sourceIds = sourceIds1;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return the clientVersion
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  
  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  
  /**
   * are groups to look in
   * @return the wsGroupLookups
   */
  public WsGroupLookup[] getWsGroupLookups() {
    return this.wsGroupLookups;
  }

  
  /**
   * are groups to look in
   * @param wsGroupLookups1 the wsGroupLookup to set
   */
  public void setWsGroupLookups(WsGroupLookup[] wsGroupLookups1) {
    this.wsGroupLookups = wsGroupLookups1;
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
   * subject to act as instead of logged in user
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  
  /**
   * subject to act as instead of logged in user
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
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
   * T or F as to if the group detail should be returned
   * @return the includeGroupDetail
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  
  /**
   * T or F as to if the group detail should be returned
   * @param includeGroupDetail1 the includeGroupDetail to set
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
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
   *  are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @return the subjectAttributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  
  /**
   *  are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }


  
  /**
   * optional: reserved for future use
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }


  
  /**
   * optional: reserved for future use
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }

  /**
   * page size if paging
   */
  private String pageSize;

  /**
   * page size if paging
   * @return page size
   */
  public String getPageSize() {
    return this.pageSize;
  }

  /**
   * page size if paging
   * @param pageSize1
   */
  public void setPageSize(String pageSize1) {
    this.pageSize = pageSize1;
  }

  /**
   * page number 1 indexed if paging
   */
  private String pageNumber;
  
  /**
   * page number 1 indexed if paging
   * @return page number
   */
  public String getPageNumber() {
    return this.pageNumber;
  }

  /**
   * page number 1 indexed if paging
   * @param pageNumber1
   */
  public void setPageNumber(String pageNumber1) {
    this.pageNumber = pageNumber1;
  }

  /**
   * sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   */
  private String sortString;

  /**
   * sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @return sort string
   */
  public String getSortString() {
    return this.sortString;
  }

  /**
   * sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param sortString1
   */
  public void setSortString(String sortString1) {
    this.sortString = sortString1;
  }

  /**
   * ascending T or null for ascending, F for descending.  
   */
  private String ascending;

  /**
   * ascending T or null for ascending, F for descending.  
   * @return ascending
   */
  public String getAscending() {
    return this.ascending;
  }

  /**
   * ascending T or null for ascending, F for descending.  
   * @param ascending1
   */
  public void setAscending(String ascending1) {
    this.ascending = ascending1;
  }
  
  /**
   * T|F default to F.  if this is T then we are doing cursor paging
   */
  private String pageIsCursor;
  
  /**
   * field that will be sent back for cursor based paging
   */
  private String pageLastCursorField;
  
  /**
   * could be: string, int, long, date, timestamp
   */
  private String pageLastCursorFieldType;
  
  /**
   * T|F
   */
  private String pageCursorFieldIncludesLastRetrieved;

  /**
   * page size if paging in the members part
   */
  private String pageSizeForMember;
    
  /**
   * page size if paging in the members part
   * @return page size for member
   */
  public String getPageSizeForMember() {
    return this.pageSizeForMember;
  }

  /**
   * page size if paging in the members part
   * @param pageSizeForMember1
   */
  public void setPageSizeForMember(String pageSizeForMember1) {
    this.pageSizeForMember = pageSizeForMember1;
  }

  /**
   * page number 1 indexed if paging in the members part
   */
  private String pageNumberForMember;
  
  /**
   * page number 1 indexed if paging in the members part
   * @return page number for member
   */
  public String getPageNumberForMember() {
    return this.pageNumberForMember;
  }

  /**
   * page number 1 indexed if paging in the members part
   * @param pageNumberForMember1
   */
  public void setPageNumberForMember(String pageNumberForMember1) {
    this.pageNumberForMember = pageNumberForMember1;
  }

  /**
   * must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * in the members part
   */
  private String sortStringForMember;
  
  /**
   * must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * in the members part
   * @return sort string for member
   */
  public String getSortStringForMember() {
    return this.sortStringForMember;
  }

  /**
   * 
   * @param sortStringForMember1
   */
  public void setSortStringForMember(String sortStringForMember1) {
    this.sortStringForMember = sortStringForMember1;
  }

  /**
   * T or null for ascending, F for descending in the members part
   */
  private String ascendingForMember;

  /**
   * T or null for ascending, F for descending in the members part
   * @return if ascending for member
   */
  public String getAscendingForMember() {
    return this.ascendingForMember;
  }

  /**
   * T or null for ascending, F for descending in the members part
   * @param ascendingForMember1
   */
  public void setAscendingForMember(String ascendingForMember1) {
    this.ascendingForMember = ascendingForMember1;
  }
  
  /**
   * T|F default to F.  if this is T then we are doing cursor paging
   */
  private String pageIsCursorForMember;
  
  /**
   * field that will be sent back for cursor based paging
   */
  private String pageLastCursorFieldForMember;
  
  /**
   * could be: string, int, long, date, timestamp
   */
  private String pageLastCursorFieldTypeForMember;
  
  /**
   * T|F
   */
  private String pageCursorFieldIncludesLastRetrievedForMember;
  
  /**
   * T|F|null
   */
  private String pointInTimeRetrieve;
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * Format:  yyyy/MM/dd HH:mm:ss.SSS
   */
  private String pointInTimeFrom;
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS   
   */
  private String pointInTimeTo;

  /**
   * @return the pointInTimeFrom
   */
  public String getPointInTimeFrom() {
    return this.pointInTimeFrom;
  }

  /**
   * @param pointInTimeFrom1 the pointInTimeFrom to set
   */
  public void setPointInTimeFrom(String pointInTimeFrom1) {
    this.pointInTimeFrom = pointInTimeFrom1;
  }

  /**
   * @return the pointInTimeTo
   */
  public String getPointInTimeTo() {
    return this.pointInTimeTo;
  }

  /**
   * @param pointInTimeTo1 the pointInTimeTo to set
   */
  public void setPointInTimeTo(String pointInTimeTo1) {
    this.pointInTimeTo = pointInTimeTo1;
  }

  /**
   * @return the pageIsCursor
   */
  public String getPageIsCursor() {
    return this.pageIsCursor;
  }

  /**
   * @param pageIsCursor1 the pageIsCursor to set
   */
  public void setPageIsCursor(String pageIsCursor1) {
    this.pageIsCursor = pageIsCursor1;
  }

  /**
   * @return the pageLastCursorField
   */
  public String getPageLastCursorField() {
    return this.pageLastCursorField;
  }

  /**
   * @param pageLastCursorField1 the pageLastCursorField to set
   */
  public void setPageLastCursorField(String pageLastCursorField1) {
    this.pageLastCursorField = pageLastCursorField1;
  }

  /**
   * @return the pageLastCursorFieldType
   */
  public String getPageLastCursorFieldType() {
    return this.pageLastCursorFieldType;
  }

  /**
   * @param pageLastCursorFieldType1 the pageLastCursorFieldType to set
   */
  public void setPageLastCursorFieldType(String pageLastCursorFieldType1) {
    this.pageLastCursorFieldType = pageLastCursorFieldType1;
  }

  /**
   * @return the pageCursorFieldIncludesLastRetrieved
   */
  public String getPageCursorFieldIncludesLastRetrieved() {
    return this.pageCursorFieldIncludesLastRetrieved;
  }

  /**
   * @param pageCursorFieldIncludesLastRetrieved1 the pageCursorFieldIncludesLastRetrieved to set
   */
  public void setPageCursorFieldIncludesLastRetrieved(String pageCursorFieldIncludesLastRetrieved1) {
    this.pageCursorFieldIncludesLastRetrieved = pageCursorFieldIncludesLastRetrieved1;
  }

  /**
   * @return the pageIsCursorForMember
   */
  public String getPageIsCursorForMember() {
    return this.pageIsCursorForMember;
  }

  /**
   * @param pageIsCursorForMember1 the pageIsCursorForMember to set
   */
  public void setPageIsCursorForMember(String pageIsCursorForMember1) {
    this.pageIsCursorForMember = pageIsCursorForMember1;
  }

  /**
   * @return the pageLastCursorFieldForMember
   */
  public String getPageLastCursorFieldForMember() {
    return this.pageLastCursorFieldForMember;
  }

  /**
   * @param pageLastCursorFieldForMember1 the pageLastCursorFieldForMember to set
   */
  public void setPageLastCursorFieldForMember(String pageLastCursorFieldForMember1) {
    this.pageLastCursorFieldForMember = pageLastCursorFieldForMember1;
  }

  /**
   * @return the pageLastCursorFieldTypeForMember
   */
  public String getPageLastCursorFieldTypeForMember() {
    return this.pageLastCursorFieldTypeForMember;
  }

  /**
   * @param pageLastCursorFieldTypeForMember1 the pageLastCursorFieldTypeForMember to set
   */
  public void setPageLastCursorFieldTypeForMember(String pageLastCursorFieldTypeForMember1) {
    this.pageLastCursorFieldTypeForMember = pageLastCursorFieldTypeForMember1;
  }

  /**
   * @return the pageCursorFieldIncludesLastRetrievedForMember
   */
  public String getPageCursorFieldIncludesLastRetrievedForMember() {
    return this.pageCursorFieldIncludesLastRetrievedForMember;
  }

  /**
   * @param pageCursorFieldIncludesLastRetrievedForMember1 the pageCursorFieldIncludesLastRetrievedForMember to set
   */
  public void setPageCursorFieldIncludesLastRetrievedForMember(String pageCursorFieldIncludesLastRetrievedForMember1) {
    this.pageCursorFieldIncludesLastRetrievedForMember = pageCursorFieldIncludesLastRetrievedForMember1;
  }

  /**
   * @return the pointInTimeRetrieve
   */
  public String getPointInTimeRetrieve() {
    return this.pointInTimeRetrieve;
  }

  /**
   * @param pointInTimeRetrieve1 the pointInTimeRetrieve to set
   */
  public void setPointInTimeRetrieve(String pointInTimeRetrieve1) {
    this.pointInTimeRetrieve = pointInTimeRetrieve1;
  }
  
  
}
