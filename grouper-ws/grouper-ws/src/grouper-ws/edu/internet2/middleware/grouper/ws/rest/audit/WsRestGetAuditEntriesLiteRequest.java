package edu.internet2.middleware.grouper.ws.rest.audit;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

public class WsRestGetAuditEntriesLiteRequest implements WsRequestBean {
  
  private String auditType;
  
  private String auditActionId;
  
  private String afterAuditEntryId;
  
  private String wsOwnerGroupName;
  
  private String wsOwnerGroupId;
  
  private String wsOwnerStemName;
  
  private String wsOwnerStemId;
  
  private String wsOwnerAttributeDefName;
  
  private String wsOwnerAttributeDefId;
  
  private String wsOwnerAttributeDefNameName;
    
  private String wsOwnerAttributeDefNameId;
  
  private String wsOwnerSubjectId; 
  
  private String wsOwnerSubjectSourceId;
  
  private String wsOwnerSubjectIdentifier;
  
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

  /** page size if paging */
  private String pageSize;

  /** must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;
  
  
  public String getAuditType() {
    return auditType;
  }




  public void setAuditType(String auditType) {
    this.auditType = auditType;
  }




  public String getAuditActionId() {
    return auditActionId;
  }




  public void setAuditActionId(String auditActionId) {
    this.auditActionId = auditActionId;
  }




  public String getWsOwnerGroupName() {
    return wsOwnerGroupName;
  }




  public void setWsOwnerGroupName(String wsOwnerGroupName) {
    this.wsOwnerGroupName = wsOwnerGroupName;
  }




  public String getWsOwnerGroupId() {
    return wsOwnerGroupId;
  }




  public void setWsOwnerGroupId(String wsOwnerGroupId) {
    this.wsOwnerGroupId = wsOwnerGroupId;
  }




  public String getWsOwnerStemName() {
    return wsOwnerStemName;
  }




  public void setWsOwnerStemName(String wsOwnerStemName) {
    this.wsOwnerStemName = wsOwnerStemName;
  }




  public String getWsOwnerStemId() {
    return wsOwnerStemId;
  }




  public void setWsOwnerStemId(String wsOwnerStemId) {
    this.wsOwnerStemId = wsOwnerStemId;
  }




  public String getWsOwnerAttributeDefName() {
    return wsOwnerAttributeDefName;
  }




  public void setWsOwnerAttributeDefName(String wsOwnerAttributeDefName) {
    this.wsOwnerAttributeDefName = wsOwnerAttributeDefName;
  }




  public String getWsOwnerAttributeDefId() {
    return wsOwnerAttributeDefId;
  }




  public void setWsOwnerAttributeDefId(String wsOwnerAttributeDefId) {
    this.wsOwnerAttributeDefId = wsOwnerAttributeDefId;
  }




  public String getWsOwnerAttributeDefNameName() {
    return wsOwnerAttributeDefNameName;
  }




  public void setWsOwnerAttributeDefNameName(String wsOwnerAttributeDefNameName) {
    this.wsOwnerAttributeDefNameName = wsOwnerAttributeDefNameName;
  }




  public String getWsOwnerAttributeDefNameId() {
    return wsOwnerAttributeDefNameId;
  }




  public void setWsOwnerAttributeDefNameId(String wsOwnerAttributeDefNameId) {
    this.wsOwnerAttributeDefNameId = wsOwnerAttributeDefNameId;
  }




  public String getWsOwnerSubjectId() {
    return wsOwnerSubjectId;
  }




  public void setWsOwnerSubjectId(String wsOwnerSubjectId) {
    this.wsOwnerSubjectId = wsOwnerSubjectId;
  }




  public String getWsOwnerSubjectSourceId() {
    return wsOwnerSubjectSourceId;
  }




  public void setWsOwnerSubjectSourceId(String wsOwnerSubjectSourceId) {
    this.wsOwnerSubjectSourceId = wsOwnerSubjectSourceId;
  }




  public String getWsOwnerSubjectIdentifier() {
    return wsOwnerSubjectIdentifier;
  }




  public void setWsOwnerSubjectIdentifier(String wsOwnerSubjectIdentifier) {
    this.wsOwnerSubjectIdentifier = wsOwnerSubjectIdentifier;
  }




  public String getPointInTimeFrom() {
    return pointInTimeFrom;
  }




  public void setPointInTimeFrom(String pointInTimeFrom) {
    this.pointInTimeFrom = pointInTimeFrom;
  }




  public String getPointInTimeTo() {
    return pointInTimeTo;
  }




  public void setPointInTimeTo(String pointInTimeTo) {
    this.pointInTimeTo = pointInTimeTo;
  }




  public String getClientVersion() {
    return clientVersion;
  }




  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }




  public String getActAsSubjectId() {
    return actAsSubjectId;
  }




  public void setActAsSubjectId(String actAsSubjectId) {
    this.actAsSubjectId = actAsSubjectId;
  }




  public String getActAsSubjectSourceId() {
    return actAsSubjectSourceId;
  }




  public void setActAsSubjectSourceId(String actAsSubjectSourceId) {
    this.actAsSubjectSourceId = actAsSubjectSourceId;
  }




  public String getActAsSubjectIdentifier() {
    return actAsSubjectIdentifier;
  }




  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier;
  }




  public String getIncludeGroupDetail() {
    return includeGroupDetail;
  }




  public void setIncludeGroupDetail(String includeGroupDetail) {
    this.includeGroupDetail = includeGroupDetail;
  }




  public String getParamName0() {
    return paramName0;
  }




  public void setParamName0(String paramName0) {
    this.paramName0 = paramName0;
  }




  public String getParamValue0() {
    return paramValue0;
  }




  public void setParamValue0(String paramValue0) {
    this.paramValue0 = paramValue0;
  }




  public String getParamName1() {
    return paramName1;
  }




  public void setParamName1(String paramName1) {
    this.paramName1 = paramName1;
  }




  public String getParamValue1() {
    return paramValue1;
  }




  public void setParamValue1(String paramValue1) {
    this.paramValue1 = paramValue1;
  }




  public String getAscending() {
    return ascending;
  }




  public void setAscending(String ascending) {
    this.ascending = ascending;
  }



  public String getAfterAuditEntryId() {
    return afterAuditEntryId;
  }

  public void setAfterAuditEntryId(String afterAuditEntryId) {
    this.afterAuditEntryId = afterAuditEntryId;
  }


  public String getPageSize() {
    return pageSize;
  }

  public void setPageSize(String pageSize) {
    this.pageSize = pageSize;
  }


  public String getSortString() {
    return sortString;
  }

  public void setSortString(String sortString) {
    this.sortString = sortString;
  }




  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }

}
