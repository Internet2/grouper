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
 * @author mchyzer $Id: WsRestGetMembersLiteRequest.java,v 1.2 2009-12-07 07:31:14 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * request bean for rest get members request
 */
public class WsRestGetMembersLiteRequest implements WsRequestBean {

  /** client version */
  private String clientVersion;
  
  /** group name */
  private String groupName;
  
  /** group uuid */
  private String groupUuid;
  
  /** member filter */
  private String memberFilter;

  /** retrieveSubjectDetail */
  private String retrieveSubjectDetail;

  /** actAsSubjectId */
  private String actAsSubjectId;

  /** actAsSubjectSource */
  private String actAsSubjectSourceId;

  /** actAsSubjectIdentifier */
  private String actAsSubjectIdentifier;

  /** fieldName */
  private String fieldName;

  /** subjectAttributeNames */
  private String subjectAttributeNames;

  /** includeGroupDetail */
  private String includeGroupDetail;

  /** paramName0 */
  private String paramName0;

  /** paramValue0 */
  private String paramValue0;

  /** paramName1 */
  private String paramName1;

  /** paramValue1 */
  private String paramValue1;

  /** sourceids to limit request to, or null for all */
  private String sourceIds;
  
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
   * page size if paging
   */
  private String pageSize;
      
  /**
   * page number 1 indexed if paging
   */
  private String pageNumber;
  
  /**
   * sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   */
  private String sortString;
  
  /**
   * ascending T or null for ascending, F for descending.  
   */
  private String ascending;
  
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
   * sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   * @return sort string
   */
  public String getSortString() {
    return this.sortString;
  }


  /**
   * sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   * @param sortString1
   */
  public void setSortString(String sortString1) {
    this.sortString = sortString1;
  }


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
   * 
   * @return member filter
   */
  public String getMemberFilter() {
    return this.memberFilter;
  }

  /**
   * memberFilter1
   * @param memberFilter1
   */
  public void setMemberFilter(String memberFilter1) {
    this.memberFilter = memberFilter1;
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
   * fieldName
   * @return fieldName
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /**
   * fieldName
   * @param fieldName1
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
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
   * includeGroupDetail
   * @return includeGroupDetail
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  /**
   * includeGroupDetail
   * @param includeGroupDetail1
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
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
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }
  
  /**
   * @return the groupUuid
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }
  
  /**
   * @param groupUuid1 the groupUuid to set
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
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
  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @return the pointInTimeFrom
   */
  public String getPointInTimeFrom() {
    return this.pointInTimeFrom;
  }

  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * Format:  yyyy/MM/dd HH:mm:ss.SSS
   * @param pointInTimeFrom1 the pointInTimeFrom to set
   */
  public void setPointInTimeFrom(String pointInTimeFrom1) {
    this.pointInTimeFrom = pointInTimeFrom1;
  }

  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS 
   * @return the pointInTimeTo
   */
  public String getPointInTimeTo() {
    return this.pointInTimeTo;
  }

  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.  Format: yyyy/MM/dd HH:mm:ss.SSS 
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
