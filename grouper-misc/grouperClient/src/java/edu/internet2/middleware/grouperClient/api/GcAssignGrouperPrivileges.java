/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: GcAssignGrouperPrivilegesLite.java,v 1.2 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignGrouperPrivilegesRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a assign grouper privileges web service call
 */
public class GcAssignGrouperPrivileges {

  /** if we are assigning or unassigning */
  private Boolean allowed = null;
  
  /**
   * assign if allowed
   * @param isAllowed
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignAllowed(boolean isAllowed) {
    this.allowed = isAllowed;
    return this;
  }
  
  /** group name to assign privileges */
  private WsGroupLookup wsGroupLookup;
  
  /** privilege type, e.g. access or naming */
  private String privilegeType;

  /**
   * assign privilege type
   * @param thePrivilegeType
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignPrivilegeType(String thePrivilegeType) {
    this.privilegeType = thePrivilegeType;
    return this;
  }
  
  /** privilege name, e.g. admin */
  private Set<String> privilegeNames = new LinkedHashSet<String>();

  /**
   * assign the privilege name to query
   * @param thePrivilegeName
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges addPrivilegeName(String thePrivilegeName) {
    this.privilegeNames.add(thePrivilegeName);
    return this;
  }
   
  /**
   * set the group name
   * @param theWsGroupLookup
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignGroupLookup(WsGroupLookup theWsGroupLookup) {
    this.wsGroupLookup = theWsGroupLookup;
    return this;
  }
  
  /** stem name to query about privileges */
  private WsStemLookup wsStemLookup;
  
  /**
   * set the stem name to query privs
   * @param theStemLookup
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignStemLookup(WsStemLookup theStemLookup) {
    this.wsStemLookup = theStemLookup;
    return this;
  }
  
  /** subject lookups */
  private Set<WsSubjectLookup> subjectLookups = new LinkedHashSet<WsSubjectLookup>();

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges addSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.params) > 2) {
      throw new RuntimeException("Params can only be size 2 for this request");
    }
    if (this.allowed == null) {
      throw new RuntimeException("Specify true or false for is allowed");
    }
    if (this.privilegeNames.size() == 0) {
      throw new RuntimeException("Specify at least one privilege name");
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
  public GcAssignGrouperPrivileges addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /** client version */
  private String clientVersion;

  /** tx type for request */
  private GcTransactionType txType;

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcAssignGrouperPrivileges assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
  
  
  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** if we should replace all existing */
  private Boolean replaceAllExisting = null;

  /**
   * set if we should replace all existing members with new list
   * @param isReplaceAllExisting
   * @return this for chaining
   */
  public GcAssignGrouperPrivileges assignReplaceAllExisting(Boolean isReplaceAllExisting) {
    this.replaceAllExisting = isReplaceAllExisting;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAssignGrouperPrivilegesResults execute() {
    this.validate();
    WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignGrouperPrivilegesRequest wsAssignGrouperPrivileges = new WsRestAssignGrouperPrivilegesRequest();

      wsAssignGrouperPrivileges.setActAsSubjectLookup(this.actAsSubject);

      if (this.includeGroupDetail != null) {
        wsAssignGrouperPrivileges.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        wsAssignGrouperPrivileges.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      wsAssignGrouperPrivileges.setWsGroupLookup(this.wsGroupLookup);
      wsAssignGrouperPrivileges.setWsStemLookup(this.wsStemLookup);
      
      if (this.replaceAllExisting != null) {
        wsAssignGrouperPrivileges.setReplaceAllExisting(this.replaceAllExisting ? "T" : "F");
      }

      wsAssignGrouperPrivileges.setPrivilegeType(this.privilegeType);
      
      wsAssignGrouperPrivileges.setTxType(this.txType == null ? null : this.txType.name());

      wsAssignGrouperPrivileges.setPrivilegeNames(
          GrouperClientUtils.toArray(this.privilegeNames, String.class));
      
      if (this.subjectAttributeNames.size() > 0) {
        wsAssignGrouperPrivileges.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      if (this.subjectLookups.size() > 0) {
        wsAssignGrouperPrivileges.setWsSubjectLookups(GrouperClientUtils.toArray(this.subjectLookups, WsSubjectLookup.class));
      }

      //add params if there are any
      if (this.params.size() > 0) {
        wsAssignGrouperPrivileges.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      //right now we only have allowed permissions
      if (this.allowed != null) {
        wsAssignGrouperPrivileges.setAllowed(this.allowed ? "T" : "F");
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsAssignGrouperPrivilegesResults = (WsAssignGrouperPrivilegesResults)
        grouperClientWs.executeService("grouperPrivileges", wsAssignGrouperPrivileges, 
            "assignGrouperPrivileges", this.clientVersion, false);
      
      String resultMessage = wsAssignGrouperPrivilegesResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsAssignGrouperPrivilegesResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAssignGrouperPrivilegesResults;
    
  }
  
}
