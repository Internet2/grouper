package edu.internet2.middleware.grouper.ws.rest.audit;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

public class WsRestGetAuditEntriesRequest implements WsRequestBean {
  
 /** field */
 private String clientVersion;
  
 /** field */
 private WsSubjectLookup actAsSubjectLookup;
  
 private String auditType;
  
 private String auditActionId;
 
 private String afterAuditEntryId;
  
  
  /** field */
  private WsParam[] params;
  
  /** wsGroupLookups if you want to just pass in a list of uuids and/or names. */
  private WsGroupLookup[] wsOwnerGroupLookups;
  
  private WsStemLookup[] wsOwnerStemLookups;
  
  private WsAttributeDefLookup[] wsOwnerAttributeDefLookups;
  
  private WsAttributeDefNameLookup[] wsOwnerAttributeDefNameLookups;
  
  private WsSubjectLookup[] wsOwnerSubjectLookups;
  
  /** true or null for ascending, false for descending.  If you pass true or false, must pass a sort string */
  private String ascending;

  /** page size if paging */
  private String pageSize;

  /** must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;
  
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
  

  public String getClientVersion() {
    return clientVersion;
  }



  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }



  public WsSubjectLookup getActAsSubjectLookup() {
    return actAsSubjectLookup;
  }



  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup) {
    this.actAsSubjectLookup = actAsSubjectLookup;
  }



  public WsParam[] getParams() {
    return params;
  }



  public void setParams(WsParam[] params) {
    this.params = params;
  }



  public WsGroupLookup[] getWsOwnerGroupLookups() {
    return wsOwnerGroupLookups;
  }



  public void setWsOwnerGroupLookups(WsGroupLookup[] wsOwnerGroupLookups) {
    this.wsOwnerGroupLookups = wsOwnerGroupLookups;
  }



  public WsStemLookup[] getWsOwnerStemLookups() {
    return wsOwnerStemLookups;
  }



  public void setWsOwnerStemLookups(WsStemLookup[] wsOwnerStemLookups) {
    this.wsOwnerStemLookups = wsOwnerStemLookups;
  }



  public WsAttributeDefLookup[] getWsOwnerAttributeDefLookups() {
    return wsOwnerAttributeDefLookups;
  }



  public void setWsOwnerAttributeDefLookups(WsAttributeDefLookup[] wsOwnerAttributeDefLookups) {
    this.wsOwnerAttributeDefLookups = wsOwnerAttributeDefLookups;
  }



  public WsAttributeDefNameLookup[] getWsOwnerAttributeDefNameLookups() {
    return wsOwnerAttributeDefNameLookups;
  }



  public void setWsOwnerAttributeDefNameLookups(WsAttributeDefNameLookup[] wsOwnerAttributeDefNameLookups) {
    this.wsOwnerAttributeDefNameLookups = wsOwnerAttributeDefNameLookups;
  }



  public WsSubjectLookup[] getWsOwnerSubjectLookups() {
    return wsOwnerSubjectLookups;
  }



  public void setWsOwnerSubjectLookups(WsSubjectLookup[] wsOwnerSubjectLookups) {
    this.wsOwnerSubjectLookups = wsOwnerSubjectLookups;
  }

  

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



  public String getAscending() {
    return ascending;
  }



  public void setAscending(String ascending) {
    this.ascending = ascending;
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

  

  public String getAfterAuditEntryId() {
    return afterAuditEntryId;
  }



  public void setAfterAuditEntryId(String afterAuditEntryId) {
    this.afterAuditEntryId = afterAuditEntryId;
  }



  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }

}
