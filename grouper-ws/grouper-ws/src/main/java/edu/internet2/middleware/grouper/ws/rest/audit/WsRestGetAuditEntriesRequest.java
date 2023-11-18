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
 
  
  /** field */
  private WsParam[] params;
  
  /** fetch audit entries for this group */
  private WsGroupLookup wsGroupLookup;
  
  /**
   * fetch audit entries for this stem
   */
  private WsStemLookup wsStemLookup;
  
  /**
   * fetch audit entries for this attribute def
   */
  private WsAttributeDefLookup wsAttributeDefLookup;
  
  /**
   * fetch audit entries for this attribute def name
   */
  private WsAttributeDefNameLookup wsAttributeDefNameLookup;
  
  /**
   * fetch audit entries for these subjects
   */
  private WsSubjectLookup wsSubjectLookup;
  
  /**
   * fetch audit entries for actions performed by these subjects 
   */
  private WsSubjectLookup actionsPerformedByWsSubjectLookup;
  
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
   * @return the wsStemLookup
   */
  public WsStemLookup getWsStemLookup() {
    return this.wsStemLookup;
  }



  /**
   * @param wsStemLookup1 the wsStemLookup to set
   */
  public void setWsStemLookup(WsStemLookup wsStemLookup1) {
    this.wsStemLookup = wsStemLookup1;
  }



  /**
   * @return the wsAttributeDefLookup
   */
  public WsAttributeDefLookup getWsAttributeDefLookup() {
    return this.wsAttributeDefLookup;
  }



  /**
   * @param wsAttributeDefLookup1 the wsAttributeDefLookup to set
   */
  public void setWsAttributeDefLookup(WsAttributeDefLookup wsAttributeDefLookup1) {
    this.wsAttributeDefLookup = wsAttributeDefLookup1;
  }



  /**
   * @return the wsAttributeDefNameLookup
   */
  public WsAttributeDefNameLookup getWsAttributeDefNameLookup() {
    return this.wsAttributeDefNameLookup;
  }



  /**
   * @param wsAttributeDefNameLookup1 the wsAttributeDefNameLookup to set
   */
  public void setWsAttributeDefNameLookup(WsAttributeDefNameLookup wsAttributeDefNameLookup1) {
    this.wsAttributeDefNameLookup = wsAttributeDefNameLookup1;
  }



  /**
   * @return the wsSubjectLookup
   */
  public WsSubjectLookup getWsSubjectLookup() {
    return this.wsSubjectLookup;
  }



  /**
   * @param wsSubjectLookup1 the wsSubjectLookup to set
   */
  public void setWsSubjectLookup(WsSubjectLookup wsSubjectLookup1) {
    this.wsSubjectLookup = wsSubjectLookup1;
  }

  
  /**
   * @return the actionsPerformedByWsSubjectLookup
   */
  public WsSubjectLookup getActionsPerformedByWsSubjectLookup() {
    return this.actionsPerformedByWsSubjectLookup;
  }


  /**
   * @param actionsPerformedByWsSubjectLookup1 the actionsPerformedByWsSubjectLookup to set
   */
  public void setActionsPerformedByWsSubjectLookup(WsSubjectLookup actionsPerformedByWsSubjectLookup1) {
    this.actionsPerformedByWsSubjectLookup = actionsPerformedByWsSubjectLookup1;
  }


  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }

}
