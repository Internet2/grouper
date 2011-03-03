/*
 * @author mchyzer
 * $Id: WsRestHasMemberRequest.java,v 1.2 2008-03-28 16:45:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsParam;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;

/**
 * bean that will be the data from rest request
 * @see GrouperServiceLogic#hasMember(edu.internet2.middleware.grouper.ws.GrouperWsVersion, WsGroupLookup, WsSubjectLookup[], edu.internet2.middleware.grouper.ws.member.WsMemberFilter, WsSubjectLookup, edu.internet2.middleware.grouper.Field, boolean, boolean, String[], WsParam[])
 * for method
 */
public class WsRestHasMemberRequest implements WsRequestBean {
  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsGroupLookup wsGroupLookup;
  
  /** field */
  private WsSubjectLookup[] subjectLookups;
  
  /** field */
  private String memberFilter;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private String fieldName;
  
  /** field */
  private String includeGroupDetail;
  
  /** field */
  private String includeSubjectDetail;
  
  /** field */
  private String[] subjectAttributeNames;
  
  /** field */
  private WsParam[] params;
  
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
   * @return the replaceAllExisting
   */
  public String getMemberFilter() {
    return this.memberFilter;
  }

  
  /**
   * @param replaceAllExisting1 the replaceAllExisting to set
   */
  public void setMemberFilter(String replaceAllExisting1) {
    this.memberFilter = replaceAllExisting1;
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
