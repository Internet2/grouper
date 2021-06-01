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

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetMembershipsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.morphString.Crypto;


/**
 * class to run a get memberships web service call
 */
public class GcGetMemberships {


  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   */
  private String wsEndpoint;

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   * @param theWsEndpoint
   * @return this for chaining
   */
  public GcGetMemberships assignWsEndpoint(String theWsEndpoint) {
    this.wsEndpoint = theWsEndpoint;
    return this;
  }
  
  /**
   * ws user
   */
  private String wsUser;

  /**
   * ws user
   * @param theWsUser
   * @return this for chaining
   */
  public GcGetMemberships assignWsUser(String theWsUser) {
    this.wsUser = theWsUser;
    return this;
  }
  
  /**
   * ws pass
   */
  private String wsPass;

  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcGetMemberships assignWsPass(String theWsPass) {
    this.wsPass = theWsPass;
    return this;
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcGetMemberships assignWsPassEncrypted(String theWsPassEncrypted) {
    String encryptKey = GrouperClientUtils.encryptKey();
    return this.assignWsPass(new Crypto(encryptKey).decrypt(theWsPassEncrypted));
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcGetMemberships assignWsPassFile(File theFile) {
    return this.assignWsPass(GrouperClientUtils.readFileIntoString(theFile));
  }

  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcGetMemberships assignWsPassFileEncrypted(File theFile) {
    return this.assignWsPassEncrypted(GrouperClientUtils.readFileIntoString(theFile));
  }

  /** A for all, T or null for enabled only, F for disabled only */
  private String enabled;
  
  /** ws subject lookups to find memberships about */
  private Set<WsSubjectLookup> wsSubjectLookups = new LinkedHashSet<WsSubjectLookup>();
  
  /** either ONE_LEVEL|ALL_IN_SUBTREE */
  private String stemScope = null;
  
  /** stem that limits the memberships either directly inside or in all descendents */
  private WsStemLookup wsStemLookup = null;
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetMemberships assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * assign stem to limit memberships
   * @param theWsStemLookup
   * @return this for chaining
   */
  public GcGetMemberships assignWsStem(WsStemLookup theWsStemLookup) {
    this.wsStemLookup = theWsStemLookup;
    return this;
  }
  
  /** group names to query */
  private Set<String> groupNames = new LinkedHashSet<String>();
  
  /** group uuids to query */
  private Set<String> groupUuids = new LinkedHashSet<String>();
  
  /** names of attribute defs to query */
  private Set<String> ownerNamesOfAttributeDefs = new LinkedHashSet<String>();
  
  /** uuids of attribute defs to query */
  private Set<String> ownerUuidsOfAttributeDefs = new LinkedHashSet<String>();
  
  /** names of owner stems to query */
  private Set<String> ownerStemNames = new LinkedHashSet<String>();
  
  /** uuids of owner stems to query */
  private Set<String> ownerStemUuids = new LinkedHashSet<String>();
  
  /** group id indexes to query */
  private Set<Long> groupIdIndexes = new LinkedHashSet<Long>();

  /**
   * pageSize page size if paging
   */
  private Integer pageSize;
  
  /**
   * pageSize page size if paging
   * @param thePageSize
   * @return this for chaining
   */
  public GcGetMemberships assignPageSize(Integer thePageSize) {
    this.pageSize = thePageSize;
    return this;
  }

  /**
   * page number 1 indexed if paging
   */
  private Integer pageNumber;

  /**
   * 
   * @param thePageNumber
   * @return this for chaining
   */
  public GcGetMemberships assignPageNumber(Integer thePageNumber) {
    this.pageNumber = thePageNumber;
    return this;
  }
  
  /**
   * must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   */
  private String sortString;

  /**
   * must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param theSortString
   * @return this for chaining
   */
  public GcGetMemberships assignSortString(String theSortString) {
    this.sortString = theSortString;
    return this;
  }
  
  /**
   * T or null for ascending, F for descending.  
   */
  private Boolean ascending;

  /**
   * T or null for ascending, F for descending.  
   * @param theAscending
   * @return this for chaining
   */
  public GcGetMemberships assignAscending(Boolean theAscending) {
    this.ascending = theAscending;
    return this;
  }
  
  /**
   * page size if paging in the members part
   */
  private Integer pageSizeForMember;
  
  /**
   * page size if paging in the members part
   * @param thePageSizeForMember
   * @return this for chaining
   */
  public GcGetMemberships assignPageSizeForMember(Integer thePageSizeForMember) {
    this.pageSizeForMember = thePageSizeForMember;
    return this;
  }
  
  /**
   * page number 1 indexed if paging in the members part
   */
  private Integer pageNumberForMember;

  /**
   * page number 1 indexed if paging in the members part
   * @param thePageNumberForMember
   * @return this for chaining
   */
  public GcGetMemberships assignPageNumberForMember(Integer thePageNumberForMember) {
    this.pageNumberForMember = thePageNumberForMember;
    return this;
  }
  
  /**
   * must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * in the members part
   */
  private String sortStringForMember;

  /**
   * must be an hql query field, e.g. 
   * can sort on uuid, subjectId, sourceId, sourceString0, sortString1, sortString2, sortString3, sortString4, name, description
   * in the members part
   * @param theSortStringForMember
   * @return this for chaining
   */
  public GcGetMemberships assignSortStringForMember(String theSortStringForMember) {
    this.sortStringForMember = theSortStringForMember;
    return this;
  }
  
  /**
   * T or null for ascending, F for descending in the members part
   */
  private Boolean ascendingForMember;

  /**
   * T or null for ascending, F for descending in the members part
   * @param theAscendingForMember
   * @return this for chaining
   */
  public GcGetMemberships assignAscendingForMember(Boolean theAscendingForMember) {
    this.ascendingForMember = theAscendingForMember;
    return this;
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
  public GcGetMemberships assignPageIsCursor(Boolean pageIsCursor) {
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
  public GcGetMemberships assignPageLastCursorField(String pageLastCursorField) {
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
  public GcGetMemberships assignPageLastCursorFieldType(String pageLastCursorFieldType) {
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
  public GcGetMemberships assignPageCursorFieldIncludesLastRetrieved(Boolean pageCursorFieldIncludesLastRetrieved) {
    this.pageCursorFieldIncludesLastRetrieved = pageCursorFieldIncludesLastRetrieved;
    return this;
  }
  
  /**
   * T for when pagination is of cursor type. F or null otherwise
   */
  private Boolean pageIsCursorForMember;
  
  /**
   * T for when pagination is of cursor type. F or null otherwise
   * @param pageIsCursorForMember
   * @return
   */
  public GcGetMemberships assignPageIsCursorForMember(Boolean pageIsCursorForMember) {
    this.pageIsCursorForMember = pageIsCursorForMember;
    return this;
  }
  
  /**
   * value of last cursor field for member results
   */
  private String pageLastCursorFieldForMember;
  
  /**
   * value of last cursor field for member results
   * @param pageLastCursorFieldForMember
   * @return
   */
  public GcGetMemberships assignPageLastCursorFieldForMember(String pageLastCursorFieldForMember) {
    this.pageLastCursorFieldForMember = pageLastCursorFieldForMember;
    return this;
  }
  
  /**
   * type of last cursor field (string, int, long, date, timestamp) for member results
   */
  private String pageLastCursorFieldTypeForMember;
  
  /**
   * type of last cursor field (string, int, long, date, timestamp) for member results
   * @param pageLastCursorFieldTypeForMember
   * @return
   */
  public GcGetMemberships assignPageLastCursorFieldTypeForMember(String pageLastCursorFieldTypeForMember) {
    this.pageLastCursorFieldTypeForMember = pageLastCursorFieldTypeForMember;
    return this;
  }
  
  /**
   * should the last retrieved item be included again in the current result set for member results
   */
  private Boolean pageCursorFieldIncludesLastRetrievedForMember;
  
  /**
   * should the last retrieved item be included again in the current result set for member results
   * @param pageCursorFieldIncludesLastRetrievedForMember
   * @return
   */
  public GcGetMemberships assignPageCursorFieldIncludesLastRetrievedForMember(Boolean pageCursorFieldIncludesLastRetrievedForMember) {
    this.pageCursorFieldIncludesLastRetrievedForMember = pageCursorFieldIncludesLastRetrievedForMember;
    return this;
  }
  
  /**
   * is the query point in time retrieve
   */
  private Boolean pointInTimeRetrieve;
  
  /**
   * is the query point in time retrieve
   * @param pointInTimeRetrieve
   * @return
   */
  public GcGetMemberships assignPointInTimeRetrieve(Boolean pointInTimeRetrieve) {
    this.pointInTimeRetrieve = pointInTimeRetrieve;
    return this;
  }
  
  /**
   * if the query is point in time retrieve, begin date
   */
  private Timestamp pointInTimeFrom;
  
  public GcGetMemberships assignPointInTimeFrom(Timestamp pointInTimeFrom) {
    this.pointInTimeFrom = pointInTimeFrom;
    return this;
  }
  
  /**
   * if the query is point in time, end date
   */
  private Timestamp pointInTimeTo;
  
  /**
   * if the query is point in time, end date
   * @param pointInTimeTo
   * @return
   */
  public GcGetMemberships assignPointInTimeTo(Timestamp pointInTimeTo) {
    this.pointInTimeTo = pointInTimeTo;
    return this;
  }
  
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcGetMemberships addGroupName(String theGroupName) {
    this.groupNames.add(theGroupName);
    return this;
  }
  
  /**
   * set the group id index
   * @param theGroupIdIndex
   * @return this for chaining
   */
  public GcGetMemberships addGroupIdIndex(Long theGroupIdIndex) {
    this.groupIdIndexes.add(theGroupIdIndex);
    return this;
  }
  
  /**
   * set the owner stem name
   * @param theOwnerStemName
   * @return this for chaining
   */
  public GcGetMemberships addOwnerStemName(String theOwnerStemName) {
    this.ownerStemNames.add(theOwnerStemName);
    return this;
  }
  
  /**
   * set the owner stem uuid
   * @param theOwnerStemUuid
   * @return this for chaining
   */
  public GcGetMemberships addOwnerStemUuid(String theOwnerStemUuid) {
    this.ownerStemUuids.add(theOwnerStemUuid);
    return this;
  }
  
  /**
   * set the owner attributeDef name
   * @param theOwnerNameOfAttributeDef
   * @return this for chaining
   */
  public GcGetMemberships addOwnerNameOfAttributeDef(String theOwnerNameOfAttributeDef) {
    this.ownerNamesOfAttributeDefs.add(theOwnerNameOfAttributeDef);
    return this;
  }
  
  /**
   * set the owner attributeDef uuid
   * @param theOwnerUuidOfAttributeDef
   * @return this for chaining
   */
  public GcGetMemberships addOwnerUuidOfAttributeDef(String theOwnerUuidOfAttributeDef) {
    this.ownerUuidsOfAttributeDefs.add(theOwnerUuidOfAttributeDef);
    return this;
  }

  /**
   * set the subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcGetMemberships addWsSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.wsSubjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /**
   * set the group uuid
   * @param theGroupUuid
   * @return this for chaining
   */
  public GcGetMemberships addGroupUuid(String theGroupUuid) {
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
  public GcGetMemberships addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetMemberships addParam(WsParam wsParam) {
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
  public GcGetMemberships assignMemberFilter(WsMemberFilter theMemberFilter) {
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
  public GcGetMemberships assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.groupNames) == 0
        && GrouperClientUtils.length(this.groupUuids) == 0
        && GrouperClientUtils.length(this.ownerStemNames) == 0
        && GrouperClientUtils.length(this.ownerStemUuids) == 0
        && GrouperClientUtils.length(this.ownerNamesOfAttributeDefs) == 0
        && GrouperClientUtils.length(this.ownerUuidsOfAttributeDefs) == 0
        && GrouperClientUtils.length(this.groupIdIndexes) == 0
        && GrouperClientUtils.length(this.membershipIds) == 0
        && GrouperClientUtils.length(this.wsSubjectLookups) == 0
        && GrouperClientUtils.isBlank(this.serviceRole)
        && this.wsStemLookup == null) {
      throw new RuntimeException("Group name or uuid or id index or subject lookup or membership id or owner stem uuid or name or owner uuid or name of attribute def or serviceRole or stem lookup is required: " + this);
    }
    if (GrouperClientUtils.isBlank(this.serviceRole) != (this.serviceLookup == null 
          || (GrouperClientUtils.isBlank(this.serviceLookup.getIdIndex())
              && GrouperClientUtils.isBlank(this.serviceLookup.getName())
              && GrouperClientUtils.isBlank(this.serviceLookup.getUuid())))) {
     throw new RuntimeException("If serviceRole is passed in, then a serviceId or serviceName needs to be passed in");
    }
  }
  
  /** field name to add member */
  private String fieldName;
  
  /**
   * fieldType is the type of field to look at, e.g. list (default, memberships), 
   * access (privs on groups), attribute_def (privs on attribute definitions), naming (privs on folders)
   */
  private String fieldType;

  /**
   * fieldType is the type of field to look at, e.g. list (default, memberships), 
   * access (privs on groups), attribute_def (privs on attribute definitions), naming (privs on folders)
   * @param fieldType1
   * @return this for chaining
   */
  public GcGetMemberships assignFieldType(String fieldType1) {
    this.fieldType = fieldType1;
    return this;
  }

  /** sql like string where percent will be added to the end, this limits the memberships */
  private String scope;
  
  /** serviceRole to filter attributes that a user has a certain role */
  private String serviceRole;

  /** serviceLookup if filtering by users in a service, then this is the service to look in */
  private WsAttributeDefNameLookup serviceLookup;

  /**
   * serviceLookup if filtering by users in a service, then this is the service to look in
   * @param serviceLookup1
   * @return this for chaining
   */
  public GcGetMemberships assignServiceLookup(WsAttributeDefNameLookup serviceLookup1) {
    this.serviceLookup = serviceLookup1;
    return this;
  }
  
  /**
   * serviceRole to filter attributes that a user has a certain role
   * @param serviceRole1
   * @return this for chaining
   */
  public GcGetMemberships assignServiceRole(String serviceRole1) {
    this.serviceRole = serviceRole1;
    return this;
  }
  
  /**
   * assign the field name to the request
   * @param theFieldName
   * @return this for chaining
   */
  public GcGetMemberships assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  /**
   * assign the sql like string which filters the memberships
   * @param theScope
   * @return this for chaining
   */
  public GcGetMemberships assignScope(String theScope) {
    this.scope = theScope;
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
  
  /** membership ids search for */
  private Set<String> membershipIds = null;
  
  /**
   * add a source id to filter by (or none for all sources)
   * @param sourceId
   * @return this for chaining
   */
  public GcGetMemberships addSourceId(String sourceId) {
    if (this.sourceIds == null) {
      this.sourceIds = new LinkedHashSet<String>();
    }
    this.sourceIds.add(sourceId);
    return this;
  }
  
  /**
   * add a source id to filter by (or none for all sources)
   * @param sourceId
   * @return this for chaining
   */
  public GcGetMemberships addMembershipId(String sourceId) {
    if (this.membershipIds == null) {
      this.membershipIds = new LinkedHashSet<String>();
    }
    this.membershipIds.add(sourceId);
    return this;
  }
  
  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetMemberships addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetMemberships assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetMemberships assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetMembershipsResults execute() {
    this.validate();
    WsGetMembershipsResults wsGetMembershipsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetMembershipsRequest getMemberships = new WsRestGetMembershipsRequest();

      getMemberships.setActAsSubjectLookup(this.actAsSubject);

      getMemberships.setEnabled(this.enabled);
      
      getMemberships.setFieldName(this.fieldName);
      getMemberships.setFieldType(this.fieldType);
      
      {
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
        
        if (GrouperClientUtils.length(groupLookups) > 0) {
          getMemberships.setWsGroupLookups(GrouperClientUtils.toArray(groupLookups, WsGroupLookup.class));
        }
      }
      
      {
        List<WsStemLookup> ownerStemLookups = new ArrayList<WsStemLookup>();
        //add stem names and/or uuids
        for (String ownerStemName : this.ownerStemNames) {
          ownerStemLookups.add(new WsStemLookup(ownerStemName, null));
        }
        for (String ownerStemUuid : this.ownerStemUuids) {
          ownerStemLookups.add(new WsStemLookup(null, ownerStemUuid));
        }
        if (GrouperClientUtils.length(ownerStemLookups) > 0) {
          getMemberships.setWsOwnerStemLookups(GrouperClientUtils.toArray(ownerStemLookups, WsStemLookup.class));
        }
      }
      
      {
        List<WsAttributeDefLookup> ownerAttributeDefLookups = new ArrayList<WsAttributeDefLookup>();
        //add attributeDef names and/or uuids
        for (String ownerNameOfAttributeDef : this.ownerNamesOfAttributeDefs) {
          ownerAttributeDefLookups.add(new WsAttributeDefLookup(ownerNameOfAttributeDef, null));
        }
        for (String ownerUuidOfAttributeDef : this.ownerUuidsOfAttributeDefs) {
          ownerAttributeDefLookups.add(new WsAttributeDefLookup(null, ownerUuidOfAttributeDef));
        }
        if (GrouperClientUtils.length(ownerAttributeDefLookups) > 0) {
          getMemberships.setWsOwnerAttributeDefLookups(GrouperClientUtils.toArray(ownerAttributeDefLookups, WsAttributeDefLookup.class));
        }
      }

      if (this.includeGroupDetail != null) {
        getMemberships.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        getMemberships.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.serviceLookup != null) {
        getMemberships.setServiceLookup(this.serviceLookup);
      }
      
      if (this.serviceRole != null) {
        getMemberships.setServiceRole(this.serviceRole);
      }
     
      getMemberships.setMemberFilter(this.memberFilter == null ? null : this.memberFilter.name());

      if (GrouperClientUtils.length(this.membershipIds) > 0) {
        getMemberships.setMembershipIds(GrouperClientUtils.toArray(this.membershipIds, String.class));
      }

      //add params if there are any
      if (this.params.size() > 0) {
        getMemberships.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      getMemberships.setScope(this.scope);
      
      if (this.pageSize != null) {
        getMemberships.setPageSize(this.pageSize.toString());
      }
      
      if (this.pageNumber != null) {
        getMemberships.setPageNumber(this.pageNumber.toString());
      }

      getMemberships.setSortString(this.sortString);

      if (this.ascending != null) {
        getMemberships.setAscending(this.ascending ? "T" : "F");
      }

      if (this.pageSizeForMember != null) {
        getMemberships.setPageSizeForMember(this.pageSizeForMember.toString());
      }

      if (this.pageNumberForMember != null) {
        getMemberships.setPageNumberForMember(this.pageNumberForMember.toString());
      }

      getMemberships.setSortStringForMember(this.sortStringForMember);

      if (this.ascendingForMember != null) {
        getMemberships.setAscendingForMember(this.ascendingForMember ? "T" : "F");
      }

      if (GrouperClientUtils.length(this.sourceIds) > 0) {
        getMemberships.setSourceIds(GrouperClientUtils.toArray(this.sourceIds, String.class));
      }
      
      getMemberships.setStemScope(this.stemScope);
      
      if (this.subjectAttributeNames.size() > 0) {
        getMemberships.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      getMemberships.setWsStemLookup(this.wsStemLookup);
      
      if (GrouperClientUtils.length(this.wsSubjectLookups) > 0) {
        getMemberships.setWsSubjectLookups(GrouperClientUtils.toArray(this.wsSubjectLookups, WsSubjectLookup.class));
      }
      
      if (this.pointInTimeRetrieve != null) {
        getMemberships.setPointInTimeRetrieve(this.pointInTimeRetrieve ? "T" : "F");
      }
      
      getMemberships.setPointInTimeFrom(GrouperClientUtils.dateToString(this.pointInTimeFrom));
      getMemberships.setPointInTimeTo(GrouperClientUtils.dateToString(this.pointInTimeTo));
      
      if (this.pageIsCursor != null) {
        getMemberships.setPageIsCursor(this.pageIsCursor ? "T": "F");
      }
      
      if (this.pageCursorFieldIncludesLastRetrieved != null) {
        getMemberships.setPageCursorFieldIncludesLastRetrieved(this.pageCursorFieldIncludesLastRetrieved ? "T": "F");
      }
      
      getMemberships.setPageLastCursorField(this.pageLastCursorField);
      getMemberships.setPageLastCursorFieldType(this.pageLastCursorFieldType);
      
      
      if (this.pageIsCursorForMember != null) {
        getMemberships.setPageIsCursorForMember(this.pageIsCursorForMember ? "T": "F");
      }
      
      if (this.pageCursorFieldIncludesLastRetrievedForMember != null) {
        getMemberships.setPageCursorFieldIncludesLastRetrievedForMember(this.pageCursorFieldIncludesLastRetrievedForMember ? "T": "F");
      }
      
      getMemberships.setPageLastCursorFieldForMember(this.pageLastCursorFieldForMember);
      getMemberships.setPageLastCursorFieldTypeForMember(this.pageLastCursorFieldTypeForMember);
      
      
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      grouperClientWs.assignWsUser(this.wsUser);
      grouperClientWs.assignWsPass(this.wsPass);
      grouperClientWs.assignWsEndpoint(this.wsEndpoint);
      
      //kick off the web service
      wsGetMembershipsResults = (WsGetMembershipsResults)
        grouperClientWs.executeService("memberships", getMemberships, "getMemberships", this.clientVersion, true);
      
      String resultMessage = wsGetMembershipsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetMembershipsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetMembershipsResults;
    
  }

  /**
   * assign ONE_LEVEL|ALL_IN_SUBTREE to stem scope
   * @param theStemScope
   * @return this for chaining
   * @deprecated use assignStemScope
   */
  @Deprecated
  public GcGetMemberships assigStemScope(String theStemScope) {
    return assignStemScope(theStemScope);
  }

  /**
   * assign ONE_LEVEL|ALL_IN_SUBTREE to stem scope
   * @param theStemScope
   * @return this for chaining
   */
  public GcGetMemberships assignStemScope(String theStemScope) {
    this.stemScope = theStemScope;
    return this;
  }

  /**
   * assign A for all, T or null for enabled only, F for disabled only
   * @param theEnabled
   * @return this for chaining
   */
  public GcGetMemberships assignEnabled(String theEnabled) {
    this.enabled = theEnabled;
    return this;
  }
  
}
