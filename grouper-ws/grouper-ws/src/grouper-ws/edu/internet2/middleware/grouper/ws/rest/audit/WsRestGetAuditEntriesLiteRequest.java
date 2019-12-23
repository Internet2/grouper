package edu.internet2.middleware.grouper.ws.rest.audit;

import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * 
 */
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
   * fetch audit entries for this group
   */
  private String wsGroupName;
  
  /**
   * fetch audit entries for this group
   */
  private String wsGroupId;
  
  /**
   * fetch audit entries for this stem
   */
  private String wsStemName;
  
  /**
   * fetch audit entries for this stem
   */
  private String wsStemId;
  
  /**
   * fetch audit entries for this attribute def id
   */
  private String wsAttributeDefName;
  
  /**
   * fetch audit entries for this attribute def id
   */
  private String wsAttributeDefId;
  
  /**
   * fetch audit entries for this attribute def name
   */
  private String wsAttributeDefNameName;
    
  /**
   * fetch audit entries for this attribute def name
   */
  private String wsAttributeDefNameId;
  
  /**
   * fetch audit entries for this subject
   */
  private String wsSubjectId; 
  
  /**
   * fetch audit entries for this subject
   */
  private String wsSubjectSourceId;
  
  /**
   * fetch audit entries for this subject
   */
  private String wsSubjectIdentifier;
  
  /**
   * fetch audit entries for actions performed by this subject
   */
  private String actionsPerformedByWsSubjectId;
  
  /**
   * fetch audit entries for actions performed by this subject
   */
  private String actionsPerformedByWsSubjectSourceId;
  
  /**
   * fetch audit entries for actions performed by this subject
   */
  private String actionsPerformedByWsSubjectIdentifier;
  
  /**
   * from date
   */
  private String fromDate;
  
  /**
   * to date   
   */
  private String toDate;
  
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

  /** page size if paging */
  private String pageSize;

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
  
  /** sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;
  
  /** ascending or null for ascending, F for descending.  If you pass T or F, must pass a sort string */
  private String ascending;
  
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
   * @return the wsGroupName
   */
  public String getWsGroupName() {
    return this.wsGroupName;
  }



  /**
   * @param wsGroupName1 the wsGroupName to set
   */
  public void setWsGroupName(String wsGroupName1) {
    this.wsGroupName = wsGroupName1;
  }



  /**
   * @return the wsGroupId
   */
  public String getWsGroupId() {
    return this.wsGroupId;
  }



  /**
   * @param wsGroupId1 the wsGroupId to set
   */
  public void setWsGroupId(String wsGroupId1) {
    this.wsGroupId = wsGroupId1;
  }



  /**
   * @return the wsStemName
   */
  public String getWsStemName() {
    return this.wsStemName;
  }



  /**
   * @param wsStemName1 the wsStemName to set
   */
  public void setWsStemName(String wsStemName1) {
    this.wsStemName = wsStemName1;
  }



  /**
   * @return the wsStemId
   */
  public String getWsStemId() {
    return this.wsStemId;
  }



  /**
   * @param wsStemId1 the wsStemId to set
   */
  public void setWsStemId(String wsStemId1) {
    this.wsStemId = wsStemId1;
  }



  /**
   * @return the wsAttributeDefName
   */
  public String getWsAttributeDefName() {
    return this.wsAttributeDefName;
  }



  /**
   * @param wsAttributeDefName1 the wsAttributeDefName to set
   */
  public void setWsAttributeDefName(String wsAttributeDefName1) {
    this.wsAttributeDefName = wsAttributeDefName1;
  }



  /**
   * @return the wsAttributeDefId
   */
  public String getWsAttributeDefId() {
    return this.wsAttributeDefId;
  }



  /**
   * @param wsAttributeDefId1 the wsAttributeDefId to set
   */
  public void setWsAttributeDefId(String wsAttributeDefId1) {
    this.wsAttributeDefId = wsAttributeDefId1;
  }



  /**
   * @return the wsAttributeDefNameName
   */
  public String getWsAttributeDefNameName() {
    return this.wsAttributeDefNameName;
  }



  /**
   * @param wsAttributeDefNameName1 the wsAttributeDefNameName to set
   */
  public void setWsAttributeDefNameName(String wsAttributeDefNameName1) {
    this.wsAttributeDefNameName = wsAttributeDefNameName1;
  }



  /**
   * @return the wsAttributeDefNameId
   */
  public String getWsAttributeDefNameId() {
    return this.wsAttributeDefNameId;
  }



  /**
   * @param wsAttributeDefNameId1 the wsAttributeDefNameId to set
   */
  public void setWsAttributeDefNameId(String wsAttributeDefNameId1) {
    this.wsAttributeDefNameId = wsAttributeDefNameId1;
  }



  /**
   * @return the wsSubjectId
   */
  public String getWsSubjectId() {
    return this.wsSubjectId;
  }



  /**
   * @param wsSubjectId1 the wsSubjectId to set
   */
  public void setWsSubjectId(String wsSubjectId1) {
    this.wsSubjectId = wsSubjectId1;
  }



  /**
   * @return the wsSubjectSourceId
   */
  public String getWsSubjectSourceId() {
    return this.wsSubjectSourceId;
  }



  /**
   * @param wsSubjectSourceId1 the wsSubjectSourceId to set
   */
  public void setWsSubjectSourceId(String wsSubjectSourceId1) {
    this.wsSubjectSourceId = wsSubjectSourceId1;
  }



  /**
   * @return the wsSubjectIdentifier
   */
  public String getWsSubjectIdentifier() {
    return this.wsSubjectIdentifier;
  }



  /**
   * @param wsSubjectIdentifier1 the wsSubjectIdentifier to set
   */
  public void setWsSubjectIdentifier(String wsSubjectIdentifier1) {
    this.wsSubjectIdentifier = wsSubjectIdentifier1;
  }

  

  /**
   * @return the actionsPerformedByWsSubjectId
   */
  public String getActionsPerformedByWsSubjectId() {
    return this.actionsPerformedByWsSubjectId;
  }



  /**
   * @param actionsPerformedByWsSubjectId1 the actionsPerformedByWsSubjectId to set
   */
  public void setActionsPerformedByWsSubjectId(String actionsPerformedByWsSubjectId1) {
    this.actionsPerformedByWsSubjectId = actionsPerformedByWsSubjectId1;
  }



  /**
   * @return the actionsPerformedByWsSubjectSourceId
   */
  public String getActionsPerformedByWsSubjectSourceId() {
    return this.actionsPerformedByWsSubjectSourceId;
  }



  /**
   * @param actionsPerformedByWsSubjectSourceId1 the actionsPerformedByWsSubjectSourceId to set
   */
  public void setActionsPerformedByWsSubjectSourceId(String actionsPerformedByWsSubjectSourceId1) {
    this.actionsPerformedByWsSubjectSourceId = actionsPerformedByWsSubjectSourceId1;
  }



  /**
   * @return the actionsPerformedByWsSubjectIdentifier
   */
  public String getActionsPerformedByWsSubjectIdentifier() {
    return this.actionsPerformedByWsSubjectIdentifier;
  }



  /**
   * @param actionsPerformedByWsSubjectIdentifier1 the actionsPerformedByWsSubjectIdentifier to set
   */
  public void setActionsPerformedByWsSubjectIdentifier(String actionsPerformedByWsSubjectIdentifier1) {
    this.actionsPerformedByWsSubjectIdentifier = actionsPerformedByWsSubjectIdentifier1;
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



  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }

}
