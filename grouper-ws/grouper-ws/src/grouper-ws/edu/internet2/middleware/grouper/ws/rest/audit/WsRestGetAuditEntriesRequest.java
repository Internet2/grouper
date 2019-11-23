package edu.internet2.middleware.grouper.ws.rest.audit;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * 
 * @author vsachdeva
 *
 */
public class WsRestGetAuditEntriesRequest implements WsRequestBean {
  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
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
  
  
  /** field */
  private WsParam[] params;
  
  /** fetch audit entries for these groups */
  private WsGroupLookup[] wsOwnerGroupLookups;
  
  /**
   * fetch audit entries for these stems
   */
  private WsStemLookup[] wsOwnerStemLookups;
  
  /**
   * fetch audit entries for these attribute defs
   */
  private WsAttributeDefLookup[] wsOwnerAttributeDefLookups;
  
  /**
   * fetch audit entries for these attribute defs names
   */
  private WsAttributeDefNameLookup[] wsOwnerAttributeDefNameLookups;
  
  /**
   * fetch audit entries for these subjects
   */
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
   * @return the wsOwnerGroupLookups
   */
  public WsGroupLookup[] getWsOwnerGroupLookups() {
    return this.wsOwnerGroupLookups;
  }





  /**
   * @param wsOwnerGroupLookups1 the wsOwnerGroupLookups to set
   */
  public void setWsOwnerGroupLookups(WsGroupLookup[] wsOwnerGroupLookups1) {
    this.wsOwnerGroupLookups = wsOwnerGroupLookups1;
  }





  /**
   * @return the wsOwnerStemLookups
   */
  public WsStemLookup[] getWsOwnerStemLookups() {
    return this.wsOwnerStemLookups;
  }





  /**
   * @param wsOwnerStemLookups1 the wsOwnerStemLookups to set
   */
  public void setWsOwnerStemLookups(WsStemLookup[] wsOwnerStemLookups1) {
    this.wsOwnerStemLookups = wsOwnerStemLookups1;
  }





  /**
   * @return the wsOwnerAttributeDefLookups
   */
  public WsAttributeDefLookup[] getWsOwnerAttributeDefLookups() {
    return this.wsOwnerAttributeDefLookups;
  }





  /**
   * @param wsOwnerAttributeDefLookups1 the wsOwnerAttributeDefLookups to set
   */
  public void setWsOwnerAttributeDefLookups(WsAttributeDefLookup[] wsOwnerAttributeDefLookups1) {
    this.wsOwnerAttributeDefLookups = wsOwnerAttributeDefLookups1;
  }





  /**
   * @return the wsOwnerAttributeDefNameLookups
   */
  public WsAttributeDefNameLookup[] getWsOwnerAttributeDefNameLookups() {
    return this.wsOwnerAttributeDefNameLookups;
  }





  /**
   * @param wsOwnerAttributeDefNameLookups1 the wsOwnerAttributeDefNameLookups to set
   */
  public void setWsOwnerAttributeDefNameLookups(WsAttributeDefNameLookup[] wsOwnerAttributeDefNameLookups1) {
    this.wsOwnerAttributeDefNameLookups = wsOwnerAttributeDefNameLookups1;
  }





  /**
   * @return the wsOwnerSubjectLookups
   */
  public WsSubjectLookup[] getWsOwnerSubjectLookups() {
    return this.wsOwnerSubjectLookups;
  }





  /**
   * @param wsOwnerSubjectLookups1 the wsOwnerSubjectLookups to set
   */
  public void setWsOwnerSubjectLookups(WsSubjectLookup[] wsOwnerSubjectLookups1) {
    this.wsOwnerSubjectLookups = wsOwnerSubjectLookups1;
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





  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }

}
