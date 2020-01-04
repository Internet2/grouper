package edu.internet2.middleware.grouperClient.ws.beans;

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
 
  
  /** field */
  private WsParam[] params;
  
  /** fetch audit entries for this group */
  private WsGroupLookup wsOwnerGroupLookup;
  
  /**
   * fetch audit entries for this stem
   */
  private WsStemLookup wsOwnerStemLookup;
  
  /**
   * fetch audit entries for this attribute def
   */
  private WsAttributeDefLookup wsOwnerAttributeDefLookup;
  
  /**
   * fetch audit entries for this attribute def name
   */
  private WsAttributeDefNameLookup wsOwnerAttributeDefNameLookup;
  
  /**
   * fetch audit entries for these subjects
   */
  private WsSubjectLookup wsOwnerSubjectLookup;
  
  /** page size if paging */
  private String pageSize;
  
  /** sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;
  
  /** ascending or null for ascending, F for descending.  If you pass T or F, must pass a sort string */
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
   * from date
   */
  private String fromDate;
  
  /**
   * to date   
   */
  private String toDate;
  
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
   * @return the fromDate
   */
  public String getFromDate() {
    return this.fromDate;
  }



  /**
   * @param fromDate1 the fromDate to set
   */
  public void setFromDate(String fromDate1) {
    this.fromDate = fromDate1;
  }



  /**
   * @return the toDate
   */
  public String getToDate() {
    return this.toDate;
  }


  /**
   * @param toDate1 the toDate to set
   */
  public void setToDate(String toDate1) {
    this.toDate = toDate1;
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
   * @return the wsOwnerGroupLookup
   */
  public WsGroupLookup getWsOwnerGroupLookup() {
    return this.wsOwnerGroupLookup;
  }



  /**
   * @param wsOwnerGroupLookup1 the wsOwnerGroupLookup to set
   */
  public void setWsOwnerGroupLookup(WsGroupLookup wsOwnerGroupLookup1) {
    this.wsOwnerGroupLookup = wsOwnerGroupLookup1;
  }



  /**
   * @return the wsOwnerStemLookup
   */
  public WsStemLookup getWsOwnerStemLookup() {
    return this.wsOwnerStemLookup;
  }



  /**
   * @param wsOwnerStemLookup1 the wsOwnerStemLookup to set
   */
  public void setWsOwnerStemLookup(WsStemLookup wsOwnerStemLookup1) {
    this.wsOwnerStemLookup = wsOwnerStemLookup1;
  }



  /**
   * @return the wsOwnerAttributeDefLookup
   */
  public WsAttributeDefLookup getWsOwnerAttributeDefLookup() {
    return this.wsOwnerAttributeDefLookup;
  }



  /**
   * @param wsOwnerAttributeDefLookup1 the wsOwnerAttributeDefLookup to set
   */
  public void setWsOwnerAttributeDefLookup(WsAttributeDefLookup wsOwnerAttributeDefLookup1) {
    this.wsOwnerAttributeDefLookup = wsOwnerAttributeDefLookup1;
  }



  /**
   * @return the wsOwnerAttributeDefNameLookup
   */
  public WsAttributeDefNameLookup getWsOwnerAttributeDefNameLookup() {
    return this.wsOwnerAttributeDefNameLookup;
  }



  /**
   * @param wsOwnerAttributeDefNameLookup1 the wsOwnerAttributeDefNameLookup to set
   */
  public void setWsOwnerAttributeDefNameLookup(WsAttributeDefNameLookup wsOwnerAttributeDefNameLookup1) {
    this.wsOwnerAttributeDefNameLookup = wsOwnerAttributeDefNameLookup1;
  }



  /**
   * @return the wsOwnerSubjectLookup
   */
  public WsSubjectLookup getWsOwnerSubjectLookup() {
    return this.wsOwnerSubjectLookup;
  }



  /**
   * @param wsOwnerSubjectLookup1 the wsOwnerSubjectLookup to set
   */
  public void setWsOwnerSubjectLookup(WsSubjectLookup wsOwnerSubjectLookup1) {
    this.wsOwnerSubjectLookup = wsOwnerSubjectLookup1;
  }

}
