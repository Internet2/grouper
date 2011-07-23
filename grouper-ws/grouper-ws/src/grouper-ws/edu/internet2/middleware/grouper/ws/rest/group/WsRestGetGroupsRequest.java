/*
 * @author mchyzer
 * $Id: WsRestGetGroupsRequest.java,v 1.2 2009-12-10 08:54:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup;

/**
 * bean that will be the data from rest request
 * @see GrouperServiceLogic#getGroups(edu.internet2.middleware.grouper.ws.GrouperWsVersion, WsSubjectLookup[], edu.internet2.middleware.grouper.ws.member.WsMemberFilter, WsSubjectLookup, boolean, boolean, String[], WsParam[])
 * for method
 */
public class WsRestGetGroupsRequest implements WsRequestBean {
  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsSubjectLookup[] subjectLookups;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private String memberFilter;
  
  /** field */
  private String includeGroupDetail;
  
  /** field */
  private String includeSubjectDetail;
  
  /** field */
  private String[] subjectAttributeNames;
  
  /** field */
  private WsParam[] params;
  
  /** scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent: */
  private String scope;
  
  /** is the stem to check in, or null if all.  If has stem, must have stemScope */
  private WsStemLookup wsStemLookup;
  
  /** stemScope is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath.  You must pass stemScope if you pass a stem */
  private String stemScope;
  
  /** enabled is A for all, T or null for enabled only, F for disabled */
  private String enabled;
  
  /** pageSize page size if paging */
  private String pageSize;
  
  /** pageNumber page number 1 indexed if paging */
  private String pageNumber;
  
  /** sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;
  
  /** ascending or null for ascending, F for descending.  If you pass T or F, must pass a sort string */
  private String ascending;
  
  /** field name (list) to search, blank for members list */
  private String fieldName;
  
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
   * field name (list) to search, blank for members list
   * @return field name
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /**
   * field name (list) to search, blank for members list
   * @param fieldName1
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }

  /**
   * scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @return scope
   */
  public String getScope() {
    return this.scope;
  }


  /**
   * scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param scope1
   */
  public void setScope(String scope1) {
    this.scope = scope1;
  }


  /**
   * is the stem to check in, or null if all.  If has stem, must have stemScope
   * @return stem lookup
   */
  public WsStemLookup getWsStemLookup() {
    //TODO return the stem in the response somewhere
    return this.wsStemLookup;
  }


  /**
   * is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param wsStemLookup1
   */
  public void setWsStemLookup(WsStemLookup wsStemLookup1) {
    this.wsStemLookup = wsStemLookup1;
  }


  /**
   * stemScope is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath.  You must pass stemScope if you pass a stem
   * @return stem scope
   */
  public String getStemScope() {
    return this.stemScope;
  }


  /**
   * stemScope is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath.  You must pass stemScope if you pass a stem
   * @param stemScope1
   */
  public void setStemScope(String stemScope1) {
    this.stemScope = stemScope1;
  }


  /**
   *  enabled is A for all, T or null for enabled only, F for disabled
   * @return enabled string
   */
  public String getEnabled() {
    return this.enabled;
  }


  /**
   *  enabled is A for all, T or null for enabled only, F for disabled
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }


  /**
   *  pageSize page size if paging
   * @return page size
   */
  public String getPageSize() {
    return this.pageSize;
  }


  /**
   *  pageSize page size if paging
   * @param pageSize1
   */
  public void setPageSize(String pageSize1) {
    this.pageSize = pageSize1;
  }


  /**
   *  pageNumber page number 1 indexed if paging
   * @return page number
   */
  public String getPageNumber() {
    return this.pageNumber;
  }


  /**
   *  pageNumber page number 1 indexed if paging
   * @param pageNumber1
   */
  public void setPageNumber(String pageNumber1) {
    this.pageNumber = pageNumber1;
  }


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
   * ascending or null for ascending, F for descending.  If you pass T or F, must pass a sort string
   * @return if ascending
   */
  public String getAscending() {
    return this.ascending;
  }


  /**
   * ascending or null for ascending, F for descending.  If you pass T or F, must pass a sort string
   * @param ascending1
   */
  public void setAscending(String ascending1) {
    this.ascending = ascending1;
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
   * @return the subjectLookups
   */
  public WsSubjectLookup[] getSubjectLookups() {
    return this.subjectLookups;
  }

  
  /**
   * @param subjectLookups1 the subjectLookups to set
   */
  public void setSubjectLookups(WsSubjectLookup[] subjectLookups1) {
    this.subjectLookups = subjectLookups1;
  }

  
  /**
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  
  /**
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  
  /**
   * @return the fieldName
   */
  public String getMemberFilter() {
    return this.memberFilter;
  }

  
  /**
   * @param fieldName1 the fieldName to set
   */
  public void setMemberFilter(String fieldName1) {
    this.memberFilter = fieldName1;
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
}
