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
 * $Id: GcGetMemberships.java,v 1.1 2009-12-19 21:38:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeBatchEntry;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesBatchResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignAttributesBatchRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run an assign attributes web service call
 */
public class GcAssignAttributesBatch {

//  actAsSubjectLookup : WsSubjectLookup
//  clientVersion : String
//  includeGroupDetail : String
//  includeSubjectDetail : String
//  params : WsParam[]
//  subjectAttributeNames : String[]
//  txType : String
//  wsAssignAttributeBatchEntries : WsAssignAttributeBatchEntry[]
  
  /** batch of attribute assignments */
  private Set<WsAssignAttributeBatchEntry> assignAttributeBatchEntries = new LinkedHashSet<WsAssignAttributeBatchEntry>();
  
  /**
   * add to the batch of attribute assignments
   * @param wsAssignAttributeBatchEntry
   * @return this for chaining
   */
  public GcAssignAttributesBatch addAssignAttributeBatchEntry(WsAssignAttributeBatchEntry wsAssignAttributeBatchEntry) {
    this.assignAttributeBatchEntries.add(wsAssignAttributeBatchEntry);
    return this;
  }
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAssignAttributesBatch assignClientVersion(String theClientVersion) {
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
  public GcAssignAttributesBatch addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAssignAttributesBatch addParam(WsParam wsParam) {
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
  public GcAssignAttributesBatch assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.assignAttributeBatchEntries) == 0) {
      throw new RuntimeException("you need to pass in at least one assignAttributeBatchEntry: " + this);
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcAssignAttributesBatch addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcAssignAttributesBatch assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcAssignAttributesBatch assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAssignAttributesBatchResults execute() {
    this.validate();
    WsAssignAttributesBatchResults wsAssignAttributesBatchResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignAttributesBatchRequest assignAttributesBatch = new WsRestAssignAttributesBatchRequest();

      assignAttributesBatch.setActAsSubjectLookup(this.actAsSubject);

      //########### ATTRIBUTE ASSIGNMENT ENTRIES
      assignAttributesBatch.setWsAssignAttributeBatchEntries(GrouperClientUtils.toArray(this.assignAttributeBatchEntries, WsAssignAttributeBatchEntry.class));
      
      if (this.includeGroupDetail != null) {
        assignAttributesBatch.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        assignAttributesBatch.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
            
      //add params if there are any
      if (this.params.size() > 0) {
        assignAttributesBatch.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        assignAttributesBatch.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      assignAttributesBatch.setTxType(this.txType == null ? null : this.txType.name());

      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsAssignAttributesBatchResults = (WsAssignAttributesBatchResults)
        grouperClientWs.executeService("attributeAssignments", assignAttributesBatch, "assignAttributesBatch", this.clientVersion, false);
      
      String resultMessage = wsAssignAttributesBatchResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsAssignAttributesBatchResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAssignAttributesBatchResults;
    
  }

  /** tx type for request */
  private GcTransactionType txType;
  
  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcAssignAttributesBatch assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
}
