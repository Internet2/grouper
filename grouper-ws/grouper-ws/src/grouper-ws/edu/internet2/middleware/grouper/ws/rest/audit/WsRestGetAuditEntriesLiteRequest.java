package edu.internet2.middleware.grouper.ws.rest.audit;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

public class WsRestGetAuditEntriesLiteRequest implements WsRequestBean {
  
  /**
   * audit type
   */
  private String auditType;
  
  /**
   * audit action id
   */
  private String auditActionId;
  
  /**
   * audit entry id for pagination
   */
  private String afterAuditEntryId;
  
  /**
   * fetch audit entries for this group
   */
  private String wsOwnerGroupName;
  
  /**
   * fetch audit entries for this group
   */
  private String wsOwnerGroupId;
  
  /**
   * fetch audit entries for this stem
   */
  private String wsOwnerStemName;
  
  /**
   * fetch audit entries for this stem
   */
  private String wsOwnerStemId;
  
  /**
   * fetch audit entries for this attribute def id
   */
  private String wsOwnerAttributeDefName;
  
  /**
   * fetch audit entries for this attribute def id
   */
  private String wsOwnerAttributeDefId;
  
  /**
   * fetch audit entries for this attribute def name
   */
  private String wsOwnerAttributeDefNameName;
    
  /**
   * fetch audit entries for this attribute def name
   */
  private String wsOwnerAttributeDefNameId;
  
  /**
   * fetch audit entries for this subject
   */
  private String wsOwnerSubjectId; 
  
  /**
   * fetch audit entries for this subject
   */
  private String wsOwnerSubjectSourceId;
  
  /**
   * fetch audit entries for this subject
   */
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
  
  
  
  /**
   * @return the auditType
   */
  public String getAuditType() {
    return this.auditType;
  }



  /**
   * @param auditType1 the auditType to set
   */
  public void setAuditType(String auditType1) {
    this.auditType = auditType1;
  }



  /**
   * @return the auditActionId
   */
  public String getAuditActionId() {
    return this.auditActionId;
  }



  /**
   * @param auditActionId1 the auditActionId to set
   */
  public void setAuditActionId(String auditActionId1) {
    this.auditActionId = auditActionId1;
  }



  /**
   * @return the afterAuditEntryId
   */
  public String getAfterAuditEntryId() {
    return this.afterAuditEntryId;
  }



  /**
   * @param afterAuditEntryId1 the afterAuditEntryId to set
   */
  public void setAfterAuditEntryId(String afterAuditEntryId1) {
    this.afterAuditEntryId = afterAuditEntryId1;
  }



  /**
   * @return the wsOwnerGroupName
   */
  public String getWsOwnerGroupName() {
    return this.wsOwnerGroupName;
  }



  /**
   * @param wsOwnerGroupName1 the wsOwnerGroupName to set
   */
  public void setWsOwnerGroupName(String wsOwnerGroupName1) {
    this.wsOwnerGroupName = wsOwnerGroupName1;
  }



  /**
   * @return the wsOwnerGroupId
   */
  public String getWsOwnerGroupId() {
    return this.wsOwnerGroupId;
  }



  /**
   * @param wsOwnerGroupId1 the wsOwnerGroupId to set
   */
  public void setWsOwnerGroupId(String wsOwnerGroupId1) {
    this.wsOwnerGroupId = wsOwnerGroupId1;
  }



  /**
   * @return the wsOwnerStemName
   */
  public String getWsOwnerStemName() {
    return this.wsOwnerStemName;
  }



  /**
   * @param wsOwnerStemName1 the wsOwnerStemName to set
   */
  public void setWsOwnerStemName(String wsOwnerStemName1) {
    this.wsOwnerStemName = wsOwnerStemName1;
  }



  /**
   * @return the wsOwnerStemId
   */
  public String getWsOwnerStemId() {
    return this.wsOwnerStemId;
  }



  /**
   * @param wsOwnerStemId1 the wsOwnerStemId to set
   */
  public void setWsOwnerStemId(String wsOwnerStemId1) {
    this.wsOwnerStemId = wsOwnerStemId1;
  }



  /**
   * @return the wsOwnerAttributeDefName
   */
  public String getWsOwnerAttributeDefName() {
    return this.wsOwnerAttributeDefName;
  }



  /**
   * @param wsOwnerAttributeDefName1 the wsOwnerAttributeDefName to set
   */
  public void setWsOwnerAttributeDefName(String wsOwnerAttributeDefName1) {
    this.wsOwnerAttributeDefName = wsOwnerAttributeDefName1;
  }



  /**
   * @return the wsOwnerAttributeDefId
   */
  public String getWsOwnerAttributeDefId() {
    return this.wsOwnerAttributeDefId;
  }



  /**
   * @param wsOwnerAttributeDefId1 the wsOwnerAttributeDefId to set
   */
  public void setWsOwnerAttributeDefId(String wsOwnerAttributeDefId1) {
    this.wsOwnerAttributeDefId = wsOwnerAttributeDefId1;
  }



  /**
   * @return the wsOwnerAttributeDefNameName
   */
  public String getWsOwnerAttributeDefNameName() {
    return this.wsOwnerAttributeDefNameName;
  }



  /**
   * @param wsOwnerAttributeDefNameName1 the wsOwnerAttributeDefNameName to set
   */
  public void setWsOwnerAttributeDefNameName(String wsOwnerAttributeDefNameName1) {
    this.wsOwnerAttributeDefNameName = wsOwnerAttributeDefNameName1;
  }



  /**
   * @return the wsOwnerAttributeDefNameId
   */
  public String getWsOwnerAttributeDefNameId() {
    return this.wsOwnerAttributeDefNameId;
  }



  /**
   * @param wsOwnerAttributeDefNameId1 the wsOwnerAttributeDefNameId to set
   */
  public void setWsOwnerAttributeDefNameId(String wsOwnerAttributeDefNameId1) {
    this.wsOwnerAttributeDefNameId = wsOwnerAttributeDefNameId1;
  }



  /**
   * @return the wsOwnerSubjectId
   */
  public String getWsOwnerSubjectId() {
    return this.wsOwnerSubjectId;
  }



  /**
   * @param wsOwnerSubjectId1 the wsOwnerSubjectId to set
   */
  public void setWsOwnerSubjectId(String wsOwnerSubjectId1) {
    this.wsOwnerSubjectId = wsOwnerSubjectId1;
  }



  /**
   * @return the wsOwnerSubjectSourceId
   */
  public String getWsOwnerSubjectSourceId() {
    return this.wsOwnerSubjectSourceId;
  }



  /**
   * @param wsOwnerSubjectSourceId1 the wsOwnerSubjectSourceId to set
   */
  public void setWsOwnerSubjectSourceId(String wsOwnerSubjectSourceId1) {
    this.wsOwnerSubjectSourceId = wsOwnerSubjectSourceId1;
  }



  /**
   * @return the wsOwnerSubjectIdentifier
   */
  public String getWsOwnerSubjectIdentifier() {
    return this.wsOwnerSubjectIdentifier;
  }



  /**
   * @param wsOwnerSubjectIdentifier1 the wsOwnerSubjectIdentifier to set
   */
  public void setWsOwnerSubjectIdentifier(String wsOwnerSubjectIdentifier1) {
    this.wsOwnerSubjectIdentifier = wsOwnerSubjectIdentifier1;
  }



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
   * @return the actAsSubjectId
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }



  /**
   * @param actAsSubjectId1 the actAsSubjectId to set
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }



  /**
   * @return the actAsSubjectSourceId
   */
  public String getActAsSubjectSourceId() {
    return this.actAsSubjectSourceId;
  }



  /**
   * @param actAsSubjectSourceId1 the actAsSubjectSourceId to set
   */
  public void setActAsSubjectSourceId(String actAsSubjectSourceId1) {
    this.actAsSubjectSourceId = actAsSubjectSourceId1;
  }



  /**
   * @return the actAsSubjectIdentifier
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }



  /**
   * @param actAsSubjectIdentifier1 the actAsSubjectIdentifier to set
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
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
   * @return the paramName0
   */
  public String getParamName0() {
    return this.paramName0;
  }



  /**
   * @param paramName01 the paramName0 to set
   */
  public void setParamName0(String paramName01) {
    this.paramName0 = paramName01;
  }



  /**
   * @return the paramValue0
   */
  public String getParamValue0() {
    return this.paramValue0;
  }



  /**
   * @param paramValue01 the paramValue0 to set
   */
  public void setParamValue0(String paramValue01) {
    this.paramValue0 = paramValue01;
  }



  /**
   * @return the paramName1
   */
  public String getParamName1() {
    return this.paramName1;
  }



  /**
   * @param paramName11 the paramName1 to set
   */
  public void setParamName1(String paramName11) {
    this.paramName1 = paramName11;
  }



  /**
   * @return the paramValue1
   */
  public String getParamValue1() {
    return this.paramValue1;
  }



  /**
   * @param paramValue11 the paramValue1 to set
   */
  public void setParamValue1(String paramValue11) {
    this.paramValue1 = paramValue11;
  }



  /**
   * @return the ascending
   */
  public String getAscending() {
    return this.ascending;
  }



  /**
   * @param ascending1 the ascending to set
   */
  public void setAscending(String ascending1) {
    this.ascending = ascending1;
  }



  /**
   * @return the pageSize
   */
  public String getPageSize() {
    return this.pageSize;
  }



  /**
   * @param pageSize1 the pageSize to set
   */
  public void setPageSize(String pageSize1) {
    this.pageSize = pageSize1;
  }



  /**
   * @return the sortString
   */
  public String getSortString() {
    return this.sortString;
  }



  /**
   * @param sortString1 the sortString to set
   */
  public void setSortString(String sortString1) {
    this.sortString = sortString1;
  }



  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }

}
