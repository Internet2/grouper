/**
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
 */
/*
 * @author mchyzer
 * $Id: GcGetGroups.java,v 1.6 2009-12-10 08:54:32 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get groups web service call
 */
public class GcGetGroups {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetGroups assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  

  /** subject lookups */
  private List<WsSubjectLookup> subjectLookups = new ArrayList<WsSubjectLookup>();

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcGetGroups addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetGroups addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcGetGroups addSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectId
   * @return this for chaining
   */
  public GcGetGroups addSubjectId(String subjectId) {
    this.subjectLookups.add(new WsSubjectLookup(subjectId, null, null));
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectIdentifier
   * @return this for chaining
   */
  public GcGetGroups addSubjectIdentifier(String subjectIdentifier) {
    this.subjectLookups.add(new WsSubjectLookup(null, null, subjectIdentifier));
    return this;
  }
  
  /** field name to search */
  private String fieldName;
  
  /**
   * assign field name, blank for default members list
   * @param theFieldName
   * @return this for chaining
   */
  public GcGetGroups assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcGetGroups assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.subjectLookups) == 0) {
      throw new RuntimeException("Need at least one subject to add to group: " + this);
    }
    
    if (pointInTimeFrom != null || pointInTimeTo != null) {
      if (this.includeGroupDetail != null && this.includeGroupDetail) {
        throw new RuntimeException("Cannot specify includeGroupDetail for point in time queries.");
      }
      
      if (this.memberFilter != null && !this.memberFilter.equals(WsMemberFilter.All)) {
        throw new RuntimeException("Cannot specify a member filter for point in time queries.");
      }
      
      if (this.enabled == null || !this.enabled) {
        throw new RuntimeException("Cannot search for disabled memberships for point in time queries.");
      }
      
      if (sortString != null && !sortString.equals("name")) {
        throw new RuntimeException("Can only sort by name for point in time queries.");
      }
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   */
  private Timestamp pointInTimeFrom;
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.
   */
  private Timestamp pointInTimeTo;
  
  /**
   * member filter 
   */
  private WsMemberFilter memberFilter;

  /** scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent: */
  private String scope;
  
  /**
   * scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param theScope
   * @return this for chaining
   */
  public GcGetGroups assignScope(String theScope) {
    this.scope = theScope;
    return this;
  }
  
  /** is the stem to check in, or null if all.  If has stem, must have stemScope */
  private WsStemLookup wsStemLookup;
  
  /**
   * is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param theWsStemLookup
   * @return this for chaining
   */
  public GcGetGroups assignWsStemLookup(WsStemLookup theWsStemLookup) {
    this.wsStemLookup = theWsStemLookup;   
    return this;
  }
  
  /** stemScope is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath.  You must pass stemScope if you pass a stem */
  private StemScope stemScope;
  
  /**
   * stemScope is ONE_LEVEL if in this stem, or ALL_IN_SUBTREE for any stem underneath.  You must pass stemScope if you pass a stem
   * @param theStemScope
   * @return this for chaining
   */
  public GcGetGroups assignStemScope(StemScope theStemScope) {
    this.stemScope = theStemScope;
    return this;
  }
  
  /** enabled is A for all, T or null for enabled only, F for disabled */
  private Boolean enabled = Boolean.TRUE;
  
  /**
   * enabled is null for all, true for only enabled, false for only disabled
   * @param theEnabled
   * @return this for chaining
   */
  public GcGetGroups assignEnabled(Boolean theEnabled) {
    this.enabled = theEnabled;
    return this;
  }
  
  /** pageSize page size if paging */
  private Integer pageSize;
  
  /**
   * pageSize page size if paging
   * @param thePageSize
   * @return this for chaining
   */
  public GcGetGroups assignPageSize(Integer thePageSize) {
    this.pageSize = thePageSize;
    return this;
  }
  
  /** pageNumber page number 1 indexed if paging */
  private Integer pageNumber;
  
  /**
   * pageNumber page number 1 indexed if paging
   * @param thePageNumber
   * @return this for chaining
   */
  public GcGetGroups assignPageNumber(Integer thePageNumber) {
    this.pageNumber = thePageNumber;
    return this;
  }
  
  
  /** sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension */
  private String sortString;

  /**
   * sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param theSortString
   * @return this for chaining
   */
  public GcGetGroups assignSortString(String theSortString) {
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
  public GcGetGroups assignAscending(Boolean theAscending) {
    this.ascending = theAscending;
    return this;
  }

  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetGroups addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetGroups assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetGroups assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeTo.  This parameter specifies the start of the range
   * of the point in time query.  If this is specified but pointInTimeTo is not specified, 
   * then the point in time query range will be from the time specified to now.  
   * @param pointInTimeFrom
   * @return this for chaining
   */
  public GcGetGroups assignPointInTimeFrom(Timestamp pointInTimeFrom) {
    this.pointInTimeFrom = pointInTimeFrom;
    return this;
  }
  
  /**
   * To query members at a certain point in time or time range in the past, set this value
   * and/or the value of pointInTimeFrom.  This parameter specifies the end of the range 
   * of the point in time query.  If this is the same as pointInTimeFrom, then the query 
   * will be done at a single point in time rather than a range.  If this is specified but 
   * pointInTimeFrom is not specified, then the point in time query range will be from the 
   * minimum point in time to the time specified.
   * @param pointInTimeTo
   * @return this for chaining
   */
  public GcGetGroups assignPointInTimeTo(Timestamp pointInTimeTo) {
    this.pointInTimeTo = pointInTimeTo;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetGroupsResults execute() {
    this.validate();
    WsGetGroupsResults wsGetGroupsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetGroupsRequest getGroups = new WsRestGetGroupsRequest();

      getGroups.setActAsSubjectLookup(this.actAsSubject);

      if (this.includeGroupDetail != null) {
        getGroups.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        getGroups.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        getGroups.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      getGroups.setMemberFilter(this.memberFilter == null ? null : this.memberFilter.name());

      getGroups.setScope(this.scope);

      getGroups.setWsStemLookup(this.wsStemLookup);

      getGroups.setStemScope(this.stemScope == null ? null : this.stemScope.name());
      
      getGroups.setEnabled(this.enabled == null ? "A" : (this.enabled ? "T" : "F"));
      
      getGroups.setPageSize(this.pageSize == null ? null : this.pageSize.toString());
      
      getGroups.setPageNumber(this.pageNumber == null ? null : this.pageNumber.toString());
      
      getGroups.setSortString(this.sortString);
      
      getGroups.setAscending(this.ascending == null ? null : (this.ascending ? "T" : "F"));
      
      getGroups.setFieldName(this.fieldName);
      
      WsSubjectLookup[] subjectLookupsResults = GrouperClientUtils.toArray(this.subjectLookups, 
          WsSubjectLookup.class);
      getGroups.setSubjectLookups(subjectLookupsResults);

      //add params if there are any
      if (this.params.size() > 0) {
        getGroups.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      getGroups.setPointInTimeFrom(GrouperClientUtils.dateToString(this.pointInTimeFrom));
      getGroups.setPointInTimeTo(GrouperClientUtils.dateToString(this.pointInTimeTo));
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetGroupsResults = (WsGetGroupsResults)
        grouperClientWs.executeService("subjects", getGroups, "getGroups", this.clientVersion, true);
      
      String resultMessage = wsGetGroupsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetGroupsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetGroupsResults;
    
  }

  /**
   * assign the member filter
   * @param theMemberFilter
   * @return this for chaining
   */
  public GcGetGroups assignMemberFilter(WsMemberFilter theMemberFilter) {
    this.memberFilter = theMemberFilter;
    return this;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    WsGetGroupsResults wsGetGroupsResults = new GcGetGroups()
      .addSubjectLookup(new WsSubjectLookup("10021368", null, null)).execute();
    WsGetGroupsResult wsGroupsResult = wsGetGroupsResults.getResults()[0];
    for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGroupsResult.getWsGroups(), WsGroup.class)) {
      System.out.println(wsGroup.getName());
    }
  }
  
}
