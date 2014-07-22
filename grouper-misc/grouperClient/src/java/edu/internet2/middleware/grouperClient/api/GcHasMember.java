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
 * $Id: GcHasMember.java,v 1.6 2009-12-07 07:33:04 mchyzer Exp $
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
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestHasMemberRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a has member web service call
 */
public class GcHasMember {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcHasMember assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** group name to add member to */
  private String groupName;
  
  /** group uuid to add member to */
  private String groupUuid;
  
  /** group idIndex to add member to */
  private Long groupIdIndex;
  
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcHasMember assignGroupName(String theGroupName) {
    this.groupName = theGroupName;
    return this;
  }
  
  /**
   * set the group uuid
   * @param theGroupUuid
   * @return this for chaining
   */
  public GcHasMember assignGroupUuid(String theGroupUuid) {
    this.groupUuid = theGroupUuid;
    return this;
  }
  
  /**
   * set the group id index
   * @param theGroupIdIndex
   * @return this for chaining
   */
  public GcHasMember assignGroupIdIndex(Long theGroupIdIndex) {
    this.groupIdIndex = theGroupIdIndex;
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
  public GcHasMember addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcHasMember addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcHasMember addSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectId
   * @return this for chaining
   */
  public GcHasMember addSubjectId(String subjectId) {
    this.subjectLookups.add(new WsSubjectLookup(subjectId, null, null));
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectIdentifier
   * @return this for chaining
   */
  public GcHasMember addSubjectIdentifier(String subjectIdentifier) {
    this.subjectLookups.add(new WsSubjectLookup(null, null, subjectIdentifier));
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcHasMember assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.groupName)
        && GrouperClientUtils.isBlank(this.groupUuid)
        && GrouperClientUtils.isBlank(this.groupIdIndex)) {
      throw new RuntimeException("Group name or uuid is required: " + this);
    }
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
    }
  }
  
  /** field name to add member */
  private String fieldName;
  
  /**
   * assign the field name to the request
   * @param theFieldName
   * @return this for chaining
   */
  public GcHasMember assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /**
   * member filter 
   */
  private WsMemberFilter memberFilter;

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
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcHasMember addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcHasMember assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcHasMember assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
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
  public GcHasMember assignPointInTimeFrom(Timestamp pointInTimeFrom) {
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
  public GcHasMember assignPointInTimeTo(Timestamp pointInTimeTo) {
    this.pointInTimeTo = pointInTimeTo;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsHasMemberResults execute() {
    this.validate();
    WsHasMemberResults wsHasMemberResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestHasMemberRequest hasMember = new WsRestHasMemberRequest();

      hasMember.setActAsSubjectLookup(this.actAsSubject);

      hasMember.setFieldName(this.fieldName);
      
      hasMember.setMemberFilter(this.memberFilter == null ? null : this.memberFilter.name());

      if (this.includeGroupDetail != null) {
        hasMember.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        hasMember.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup();
      
      if (!GrouperClientUtils.isBlank(this.groupName)) {
        wsGroupLookup.setGroupName(this.groupName);
      }
      if (!GrouperClientUtils.isBlank(this.groupUuid)) {
        wsGroupLookup.setUuid(this.groupUuid);
      }
      if (!GrouperClientUtils.isBlank(this.groupIdIndex)) {
        wsGroupLookup.setIdIndex(this.groupIdIndex.toString());
      }
      
      hasMember.setWsGroupLookup(wsGroupLookup);
      
      if (this.subjectAttributeNames.size() > 0) {
        hasMember.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      WsSubjectLookup[] subjectLookupsResults = GrouperClientUtils.toArray(this.subjectLookups, 
          WsSubjectLookup.class);
      hasMember.setSubjectLookups(subjectLookupsResults);

      //add params if there are any
      if (this.params.size() > 0) {
        hasMember.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      hasMember.setPointInTimeFrom(GrouperClientUtils.dateToString(this.pointInTimeFrom));
      hasMember.setPointInTimeTo(GrouperClientUtils.dateToString(this.pointInTimeTo));
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      //String urlSuffix = "groups/" + groupHandle + "/members";
      //MCH lets switch this to not send group name, so we can do id or name
      String urlSuffix = "groups";
      wsHasMemberResults = (WsHasMemberResults)
        grouperClientWs.executeService(urlSuffix, hasMember, "hasMember", this.clientVersion, true);
      
      String resultMessage = wsHasMemberResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsHasMemberResults, wsHasMemberResults.getResults(), resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsHasMemberResults;
    
  }

  /**
   * assign the member filter
   * @param theMemberFilter
   * @return this for chaining
   */
  public GcHasMember assignMemberFilter(WsMemberFilter theMemberFilter) {
    this.memberFilter = theMemberFilter;
    return this;
  }
  
}
