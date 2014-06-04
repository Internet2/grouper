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
 * $Id: GcFindGroups.java,v 1.5 2009-12-19 21:38:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run find groups
 */
public class GcFindGroups {

  /** query filters */
  private WsQueryFilter queryFilter;

  /**
   * assign a query filter
   * @param theQueryFilter
   * @return this for chaining
   */
  public GcFindGroups assignQueryFilter(WsQueryFilter theQueryFilter) {
    this.queryFilter = theQueryFilter;
    return this;
  }
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcFindGroups assignClientVersion(String theClientVersion) {
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
  public GcFindGroups addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcFindGroups addParam(WsParam wsParam) {
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
  public GcFindGroups assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (this.queryFilter == null && GrouperClientUtils.length(this.groupUuids) == 0 
        && GrouperClientUtils.length(this.groupNames) == 0
        && GrouperClientUtils.length(this.groupIdIndexes) == 0) {
      throw new RuntimeException("Need to pass in a query filter, or groupNames or groupUuids or groupIdIndexes: " + this);
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;

  /** group names to query */
  private Set<String> groupNames = new LinkedHashSet<String>();
  
  /** group names to query */
  private Set<String> groupUuids = new LinkedHashSet<String>();
  
  /** group names to query */
  private Set<Long> groupIdIndexes = new LinkedHashSet<Long>();
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcFindGroups assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsFindGroupsResults execute() {
    this.validate();
    WsFindGroupsResults wsFindGroupsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestFindGroupsRequest findGroups = new WsRestFindGroupsRequest();

      findGroups.setActAsSubjectLookup(this.actAsSubject);

      if (this.includeGroupDetail != null) {
        findGroups.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }
      
      findGroups.setWsQueryFilter(this.queryFilter);
      
      //add params if there are any
      if (this.params.size() > 0) {
        findGroups.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      List<WsGroupLookup> groupLookups = new ArrayList<WsGroupLookup>();
      //add names and/or uuids
      for (String groupName : this.groupNames) {
        groupLookups.add(new WsGroupLookup(groupName, null));
      }
      for (String groupUuid : this.groupUuids) {
        groupLookups.add(new WsGroupLookup(null, groupUuid));
      }
      for (Long groupIdIndex : this.groupIdIndexes) {
        groupLookups.add(new WsGroupLookup(null, null, groupIdIndex.toString()));
      }
      findGroups.setWsGroupLookups(GrouperClientUtils.toArray(groupLookups, WsGroupLookup.class));

      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsFindGroupsResults = (WsFindGroupsResults)
        grouperClientWs.executeService("groups", findGroups, "findGroups", this.clientVersion, true);
      
      String resultMessage = wsFindGroupsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsFindGroupsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsFindGroupsResults;
    
  }

  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcFindGroups addGroupName(String theGroupName) {
    this.groupNames.add(theGroupName);
    return this;
  }

  /**
   * set the group uuid
   * @param theGroupUuid
   * @return this for chaining
   */
  public GcFindGroups addGroupUuid(String theGroupUuid) {
    this.groupUuids.add(theGroupUuid);
    return this;
  }

  /**
   * set the group id index
   * @param theGroupIdIndex
   * @return this for chaining
   */
  public GcFindGroups addGroupIdIndex(Long theGroupIdIndex) {
    this.groupIdIndexes.add(theGroupIdIndex);
    return this;
  }
  
}
