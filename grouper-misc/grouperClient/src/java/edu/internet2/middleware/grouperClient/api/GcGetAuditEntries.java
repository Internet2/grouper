/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer
 * $Id: GcGetGroups.java,v 1.6 2009-12-10 08:54:32 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAuditEntry;
import edu.internet2.middleware.grouperClient.ws.beans.WsAuditEntryColumn;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAuditEntriesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetAuditEntriesRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get audit entries web service call
 */
public class GcGetAuditEntries {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetAuditEntries assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcGetAuditEntries addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetAuditEntries addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  

  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcGetAuditEntries assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
//    if (GrouperClientUtils.length(this.subjectLookups) == 0) {
//      throw new RuntimeException("Need at least one subject to add to group: " + this);
//    }
    
    
  }
  

  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   */
  private Timestamp fromDate;
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.
   */
  private Timestamp toDate;
  

  /** audit type eg: group */
  private String auditType;
  
  /**
   * audit type eg: group
   * @param theScope
   * @return this for chaining
   */
  public GcGetAuditEntries assignAuditType(String auditType) {
    this.auditType = auditType;
    return this;
  }
  
  /** audit action id eg: addGroup */
  private String auditActionId;
  
  /**
   * audit action id eg: addGroup
   * @param theScope
   * @return this for chaining
   */
  public GcGetAuditEntries assignAuditActionId(String auditActionId) {
    this.auditActionId = auditActionId;
    return this;
  }
  
  /** look up audit entries for this stem */
  private WsStemLookup wsStemLookup;
  
  /**
   * look up audit entries for this stem
   * @param theWsStemLookup
   * @return this for chaining
   */
  public GcGetAuditEntries assignWsStemLookup(WsStemLookup theWsStemLookup) {
    this.wsStemLookup = theWsStemLookup;   
    return this;
  }
  
  
  /** look up audit entries for this subject */
  private WsSubjectLookup wsSubjectLookup;
  
  /**
   * look up audit entries for this subject
   * @param theWsAttributeDefLookup
   * @return this for chaining
   */
  public GcGetAuditEntries assignWsSubjectLookup(WsSubjectLookup theWsSubjectLookup) {
    this.wsSubjectLookup = theWsSubjectLookup;   
    return this;
  }
  
  /** look up audit entries for this group */
  private WsGroupLookup wsGroupLookup;
  
  /**
   * look up audit entries for this group
   * @param theWsGroupLookup
   * @return this for chaining
   */
  public GcGetAuditEntries assignWsGroupLookup(WsGroupLookup theWsGroupLookup) {
    this.wsGroupLookup = theWsGroupLookup;   
    return this;
  }
  
  /** look up audit entries for this attribute def */
  private WsAttributeDefLookup wsAttributeDefLookup;
  
  /**
   * look up audit entries for this attribute def
   * @param theWsAttributeDefLookup
   * @return this for chaining
   */
  public GcGetAuditEntries assignWsAttributeDefLookup(WsAttributeDefLookup theWsAttributeDefLookup) {
    this.wsAttributeDefLookup = theWsAttributeDefLookup;   
    return this;
  }
  
  /** look up audit entries for this attribute def name */
  private WsAttributeDefNameLookup wsAttributeDefNameLookup;
  
  /**
   * look up audit entries for this attribute def name
   * @param theWsAttributeDefNameLookup
   * @return this for chaining
   */
  public GcGetAuditEntries assignWsAttributeDefNameLookup(WsAttributeDefNameLookup theWsAttributeDefNameLookup) {
    this.wsAttributeDefNameLookup = theWsAttributeDefNameLookup;   
    return this;
  }
  
  
  /** pageSize page size if paging */
  private Integer pageSize;
  
  /**
   * pageSize page size if paging
   * @param thePageSize
   * @return this for chaining
   */
  public GcGetAuditEntries assignPageSize(Integer thePageSize) {
    this.pageSize = thePageSize;
    return this;
  }
  
  
  /** sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;

  /**
   * sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param theSortString
   * @return this for chaining
   */
  public GcGetAuditEntries assignSortString(String theSortString) {
    this.sortString = theSortString;
    return this;
  }

  
  /** ascending or null for ascending, F for descending.  If you pass T or F, must pass a sort string */
  private Boolean ascending;

  /**
   * ascending or null for ascending, F for descending.  If you pass T or F, must pass a sort string
   * @param theAscending
   * @return this for chaining
   */
  public GcGetAuditEntries assignAscending(Boolean theAscending) {
    this.ascending = theAscending;
    return this;
  }

  /**
   * from date
   * @param pointInTimeFrom
   * @return this for chaining
   */
  public GcGetAuditEntries assignFromDate(Timestamp fromDate) {
    this.fromDate = fromDate;
    return this;
  }
  
  /**
   * to date
   * @param pointInTimeTo
   * @return this for chaining
   */
  public GcGetAuditEntries assignToDate(Timestamp toDate) {
    this.toDate = toDate;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetAuditEntriesResults execute() {
    this.validate();
    WsGetAuditEntriesResults wsGetAuditEntriesResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetAuditEntriesRequest getAuditEntries = new WsRestGetAuditEntriesRequest();

      getAuditEntries.setActAsSubjectLookup(this.actAsSubject);

      getAuditEntries.setWsOwnerStemLookup(this.wsStemLookup);

      getAuditEntries.setWsOwnerGroupLookup(this.wsGroupLookup);
      
      getAuditEntries.setWsOwnerAttributeDefLookup(this.wsAttributeDefLookup);
      
      getAuditEntries.setWsOwnerAttributeDefNameLookup(this.wsAttributeDefNameLookup);
      
      getAuditEntries.setWsOwnerSubjectLookup(this.wsSubjectLookup);
      
      getAuditEntries.setAuditType(this.auditType);
      
      getAuditEntries.setAuditActionId(this.auditActionId);
      
      getAuditEntries.setPageSize(this.pageSize == null ? null : this.pageSize.toString());
      
      getAuditEntries.setSortString(this.sortString);
      
      getAuditEntries.setAscending(this.ascending == null ? null : (this.ascending ? "T" : "F"));
      
      //add params if there are any
      if (this.params.size() > 0) {
        getAuditEntries.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      getAuditEntries.setFromDate(GrouperClientUtils.dateToString(this.fromDate));
      getAuditEntries.setToDate(GrouperClientUtils.dateToString(this.toDate));
      
      if (this.pageIsCursor != null) {
        getAuditEntries.setPageIsCursor(this.pageIsCursor ? "T": "F");
      }
      
      if (this.pageCursorFieldIncludesLastRetrieved != null) {
        getAuditEntries.setPageCursorFieldIncludesLastRetrieved(this.pageCursorFieldIncludesLastRetrieved ? "T": "F");
      }
      
      getAuditEntries.setPageLastCursorField(this.pageLastCursorField);
      getAuditEntries.setPageLastCursorFieldType(this.pageLastCursorFieldType);
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetAuditEntriesResults = (WsGetAuditEntriesResults)
        grouperClientWs.executeService("audits", getAuditEntries, "getAuditEntries", this.clientVersion, true);
      
      String resultMessage = wsGetAuditEntriesResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetAuditEntriesResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetAuditEntriesResults;
    
  }

  /**
   * T for when pagination is of cursor type. F or null otherwise
   */
  private Boolean pageIsCursor;
  
  /**
   * T for when pagination is of cursor type. F or null otherwise
   * @param pageIsCursor
   * @return
   */
  public GcGetAuditEntries assignPageIsCursor(Boolean pageIsCursor) {
    this.pageIsCursor = pageIsCursor;
    return this;
  }
  
  /**
   * value of last cursor field
   */
  private String pageLastCursorField;
  
  /**
   * value of last cursor field
   * @param pageLastCursorField
   * @return
   */
  public GcGetAuditEntries assignPageLastCursorField(String pageLastCursorField) {
    this.pageLastCursorField = pageLastCursorField;
    return this;
  }
  
  /**
   * type of last cursor field (string, int, long, date, timestamp)
   */
  private String pageLastCursorFieldType;
  
  /**
   * type of last cursor field (string, int, long, date, timestamp)
   * @param pageLastCursorFieldType
   * @return
   */
  public GcGetAuditEntries assignPageLastCursorFieldType(String pageLastCursorFieldType) {
    this.pageLastCursorFieldType = pageLastCursorFieldType;
    return this;
  }
  
  /**
   * should the last retrieved item be included again in the current result set
   */
  private Boolean pageCursorFieldIncludesLastRetrieved;
  
  /**
   * should the last retrieved item be included again in the current result set
   * @param pageCursorFieldIncludesLastRetrieved
   * @return
   */
  public GcGetAuditEntries assignPageCursorFieldIncludesLastRetrieved(Boolean pageCursorFieldIncludesLastRetrieved) {
    this.pageCursorFieldIncludesLastRetrieved = pageCursorFieldIncludesLastRetrieved;
    return this;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    WsGetAuditEntriesResults wsGetAuditEntriesResults = new GcGetAuditEntries()
        .assignAuditType("group")
        .assignAuditActionId("addGroup")
        .execute();
    WsAuditEntry wsAuditEntry = wsGetAuditEntriesResults.getWsAuditEntries()[0];
    for (WsAuditEntryColumn col : wsAuditEntry.getAuditEntryColumns()) {
      System.out.println(col);
    }
  }
  
}
