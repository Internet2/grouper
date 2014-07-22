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
 * $Id: GcGetMembers.java,v 1.5 2009-12-07 07:33:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetMembersRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get members web service call
 */
public class GcGetMembers {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    new GcGetMembers().addGroupName("test:testGroup").execute();
  }
  
  /**
   * page size if paging
   */
  private Integer pageSize;
      
  /**
   * page number 1 indexed if paging
   */
  private Integer pageNumber;
  
  /**
   * sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   */
  private String sortString;
  
  /**
   * ascending T or null for ascending, F for descending.  
   */
  private Boolean ascending;
  

  /**
   * page size if paging
   * @param pageSize1
   */
  public void assignPageSize(Integer pageSize1) {
    this.pageSize = pageSize1;
  }



  /**
   * page number 1 indexed if paging
   * @param pageNumber1
   */
  public void assignPageNumber(Integer pageNumber1) {
    this.pageNumber = pageNumber1;
  }


  /**
   * sortString must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, name, description, sortString0, sortString1, sortString2, sortString3, sortString4
   * @param sortString1
   */
  public void assignSortString(String sortString1) {
    this.sortString = sortString1;
  }

  /**
   * ascending T or null for ascending, F for descending.  
   * @param ascending1
   */
  public void assignAscending(Boolean ascending1) {
    this.ascending = ascending1;
  }


  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetMembers assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** group names to query */
  private Set<String> groupNames = new LinkedHashSet<String>();
  
  /** group uuids to query */
  private Set<String> groupUuids = new LinkedHashSet<String>();
  
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcGetMembers addGroupName(String theGroupName) {
    this.groupNames.add(theGroupName);
    return this;
  }
  
  /** group id indexes to query */
  private Set<Long> groupIdIndexes = new LinkedHashSet<Long>();
  
  /**
   * set the group id index
   * @param theGroupIdIndex
   * @return this for chaining
   */
  public GcGetMembers addGroupIdIndex(Long theGroupIdIndex) {
    this.groupIdIndexes.add(theGroupIdIndex);
    return this;
  }
  
  /**
   * set the group uuid
   * @param theGroupUuid
   * @return this for chaining
   */
  public GcGetMembers addGroupUuid(String theGroupUuid) {
    this.groupUuids.add(theGroupUuid);
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
  public GcGetMembers addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetMembers addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** member filter */
  private WsMemberFilter memberFilter;
  
  /**
   * assign the member filter
   * @param theMemberFilter
   * @return this for chaining
   */
  public GcGetMembers assignMemberFilter(WsMemberFilter theMemberFilter) {
    this.memberFilter = theMemberFilter;
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcGetMembers assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.groupNames) == 0
        && GrouperClientUtils.length(this.groupUuids) == 0
        && GrouperClientUtils.length(this.groupIdIndexes) == 0) {
      throw new RuntimeException("Group name or uuid or id index is required: " + this);
    }
    
    if (pointInTimeFrom != null || pointInTimeTo != null) {
      if (this.includeGroupDetail != null && this.includeGroupDetail) {
        throw new RuntimeException("Cannot specify includeGroupDetail for point in time queries.");
      }
      
      if (this.memberFilter != null && !this.memberFilter.equals(WsMemberFilter.All)) {
        throw new RuntimeException("Cannot specify a member filter for point in time queries.");
      }
    }
  }
  
  /** field name to add member */
  private String fieldName;
  
  /**
   * assign the field name to the request
   * @param theFieldName
   * @return this for chaining
   */
  public GcGetMembers assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /** source ids to limit the results to, or null for all sources */
  private Set<String> sourceIds = null;
  
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
   * add a source id to filter by (or none for all sources)
   * @param sourceId
   * @return this for chaining
   */
  public GcGetMembers addSourceId(String sourceId) {
    if (this.sourceIds == null) {
      this.sourceIds = new LinkedHashSet<String>();
    }
    this.sourceIds.add(sourceId);
    return this;
  }
  
  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetMembers addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetMembers assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetMembers assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
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
  public GcGetMembers assignPointInTimeFrom(Timestamp pointInTimeFrom) {
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
  public GcGetMembers assignPointInTimeTo(Timestamp pointInTimeTo) {
    this.pointInTimeTo = pointInTimeTo;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetMembersResults execute() {
    this.validate();
    WsGetMembersResults wsGetMembersResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetMembersRequest getMembers = new WsRestGetMembersRequest();

      getMembers.setActAsSubjectLookup(this.actAsSubject);

      getMembers.setFieldName(this.fieldName);
      
      getMembers.setMemberFilter(this.memberFilter == null ? null : this.memberFilter.name());

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
      getMembers.setWsGroupLookups(GrouperClientUtils.toArray(groupLookups, WsGroupLookup.class));
      
      if (this.includeGroupDetail != null) {
        getMembers.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        getMembers.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        getMembers.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      //add params if there are any
      if (this.params.size() > 0) {
        getMembers.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      if (GrouperClientUtils.length(this.sourceIds) > 0) {
        getMembers.setSourceIds(GrouperClientUtils.toArray(this.sourceIds, String.class));
      }
      
      if (this.ascending != null) {
        getMembers.setAscending(this.ascending ? "T" : "F");
      }
      
      getMembers.setSortString(this.sortString);
      
      if (this.pageNumber != null) {
        getMembers.setPageNumber(this.pageNumber.toString());
      }

      if (this.pageSize != null) {
        getMembers.setPageSize(this.pageSize.toString());
      }
      
      getMembers.setPointInTimeFrom(GrouperClientUtils.dateToString(this.pointInTimeFrom));
      getMembers.setPointInTimeTo(GrouperClientUtils.dateToString(this.pointInTimeTo));
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetMembersResults = (WsGetMembersResults)
        grouperClientWs.executeService("groups", getMembers, "getMembers", this.clientVersion, true);
      
      String resultMessage = wsGetMembersResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetMembersResults, wsGetMembersResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetMembersResults;
    
  }
}
