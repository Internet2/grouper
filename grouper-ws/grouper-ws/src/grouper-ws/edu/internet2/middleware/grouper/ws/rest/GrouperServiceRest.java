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
 * @author mchyzer $Id: GrouperServiceRest.java,v 1.16 2009/12/29 07:39:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.GrouperService;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeDefNameInheritanceResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesBatchResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesLiteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionsLiteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefAssignActionResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefSaveLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindExternalSubjectsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignActionsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubjectLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMessageAcknowledgeResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsMessageResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemSaveLiteResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemSaveResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefActionsRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesBatchRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefsRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignActionsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignActionsRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsRequest;
import edu.internet2.middleware.grouper.ws.rest.externalSubject.WsRestExternalSubjectDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.externalSubject.WsRestExternalSubjectSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.externalSubject.WsRestFindExternalSubjectsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsRequest;
import edu.internet2.middleware.grouper.ws.rest.messaging.WsRestAcknowledgeMessageRequest;
import edu.internet2.middleware.grouper.ws.rest.messaging.WsRestReceiveMessageRequest;
import edu.internet2.middleware.grouper.ws.rest.messaging.WsRestSendMessageRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * consolidated static list of of rest web services (only web service methods here
 * to have clean javadoc).  the method name corresponds to the url and request method.
 * e.g. "GET /groups/a:b:c/members" will correspond to groupMembersGet()
 */
public class GrouperServiceRest {

  /**
   * <pre>
   * based on a group query, get the groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindGroupsRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindGroupsResults findGroups(GrouperVersion clientVersion,
      WsRestFindGroupsRequest wsRestFindGroupsRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindGroupsRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsFindGroupsResults wsFindGroupsResults = new GrouperService(false).findGroups(
        clientVersionString, wsRestFindGroupsRequest.getWsQueryFilter(),
        wsRestFindGroupsRequest.getActAsSubjectLookup(), wsRestFindGroupsRequest
            .getIncludeGroupDetail(), wsRestFindGroupsRequest.getParams(), 
            wsRestFindGroupsRequest.getWsGroupLookups());

    //return result
    return wsFindGroupsResults;
  }

  /**
   * <pre>
   * based on a group query, get the groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindGroupsLiteRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindGroupsResults findGroupsLite(GrouperVersion clientVersion,
      WsRestFindGroupsLiteRequest wsRestFindGroupsLiteRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindGroupsLiteRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsFindGroupsResults wsFindGroupsResults = new GrouperService(false).findGroupsLite(
        clientVersionString, wsRestFindGroupsLiteRequest.getQueryFilterType(),
        wsRestFindGroupsLiteRequest.getGroupName(), wsRestFindGroupsLiteRequest
            .getStemName(), wsRestFindGroupsLiteRequest.getStemNameScope(),
        wsRestFindGroupsLiteRequest.getGroupUuid(), wsRestFindGroupsLiteRequest
            .getGroupAttributeName(), wsRestFindGroupsLiteRequest
            .getGroupAttributeValue(), wsRestFindGroupsLiteRequest.getGroupTypeName(),
        wsRestFindGroupsLiteRequest.getActAsSubjectId(), wsRestFindGroupsLiteRequest
            .getActAsSubjectSourceId(), wsRestFindGroupsLiteRequest
            .getActAsSubjectIdentifier(), wsRestFindGroupsLiteRequest
            .getIncludeGroupDetail(), wsRestFindGroupsLiteRequest.getParamName0(),
        wsRestFindGroupsLiteRequest.getParamValue0(), wsRestFindGroupsLiteRequest
            .getParamName1(), wsRestFindGroupsLiteRequest.getParamValue1(),
            wsRestFindGroupsLiteRequest.getPageSize(), wsRestFindGroupsLiteRequest.getPageNumber(),
            wsRestFindGroupsLiteRequest.getSortString(), wsRestFindGroupsLiteRequest.getAscending(),
            wsRestFindGroupsLiteRequest.getTypeOfGroups());

    //return result
    return wsFindGroupsResults;
  }

  /**
   * <pre>
   * based on a group name, get the members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestGetMembersLiteRequest is the request body converted to an object
   * @return the results
   */
  public static WsGetMembersLiteResult getMembersLite(GrouperVersion clientVersion,
      String groupName, WsRestGetMembersLiteRequest wsRestGetMembersLiteRequest) {

    //make sure not null
    wsRestGetMembersLiteRequest = wsRestGetMembersLiteRequest == null ? new WsRestGetMembersLiteRequest()
        : wsRestGetMembersLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetMembersLiteRequest.getClientVersion()), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestGetMembersLiteRequest
        .getGroupName(), false, "groupName");

    //get the results
    WsGetMembersLiteResult wsGetMembersLiteResult = new GrouperService(false).getMembersLite(
        clientVersionString, groupName, wsRestGetMembersLiteRequest.getGroupUuid(),
        wsRestGetMembersLiteRequest.getMemberFilter(), wsRestGetMembersLiteRequest
            .getActAsSubjectId(), wsRestGetMembersLiteRequest.getActAsSubjectSourceId(),
        wsRestGetMembersLiteRequest.getActAsSubjectIdentifier(),
        wsRestGetMembersLiteRequest.getFieldName(), wsRestGetMembersLiteRequest
            .getIncludeGroupDetail(), wsRestGetMembersLiteRequest
            .getRetrieveSubjectDetail(), wsRestGetMembersLiteRequest
            .getSubjectAttributeNames(), wsRestGetMembersLiteRequest.getParamName0(),
        wsRestGetMembersLiteRequest.getParamValue0(), wsRestGetMembersLiteRequest
            .getParamName1(), wsRestGetMembersLiteRequest.getParamValue1(), 
            wsRestGetMembersLiteRequest.getSourceIds(), 
            wsRestGetMembersLiteRequest.getPointInTimeFrom(), 
            wsRestGetMembersLiteRequest.getPointInTimeTo(), wsRestGetMembersLiteRequest.getPageSize(),
            wsRestGetMembersLiteRequest.getPageNumber(), wsRestGetMembersLiteRequest.getSortString(), 
            wsRestGetMembersLiteRequest.getAscending());

    //return result
    return wsGetMembersLiteResult;

  }

  /**
   * <pre>
   * based on a group name, put the member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestAddMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsAddMemberLiteResult addMemberLite(GrouperVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestAddMemberLiteRequest wsRestAddMemberLiteRequest) {

    //make sure not null
    wsRestAddMemberLiteRequest = wsRestAddMemberLiteRequest == null ? new WsRestAddMemberLiteRequest()
        : wsRestAddMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAddMemberLiteRequest.getClientVersion()), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestAddMemberLiteRequest
        .getGroupName(), true, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestAddMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestAddMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsAddMemberLiteResult wsAddMemberLiteResult = new GrouperService(false).addMemberLite(
        clientVersionString, groupName, wsRestAddMemberLiteRequest.getGroupUuid(),
        subjectId, sourceId, wsRestAddMemberLiteRequest.getSubjectIdentifier(),
        wsRestAddMemberLiteRequest.getActAsSubjectId(), wsRestAddMemberLiteRequest
            .getActAsSubjectSourceId(), wsRestAddMemberLiteRequest
            .getActAsSubjectIdentifier(), wsRestAddMemberLiteRequest.getFieldName(),
        wsRestAddMemberLiteRequest.getIncludeGroupDetail(), wsRestAddMemberLiteRequest
            .getIncludeSubjectDetail(), wsRestAddMemberLiteRequest
            .getSubjectAttributeNames(), wsRestAddMemberLiteRequest.getParamName0(),
        wsRestAddMemberLiteRequest.getParamValue0(), wsRestAddMemberLiteRequest
            .getParamName1(), wsRestAddMemberLiteRequest.getParamValue1(), 
            wsRestAddMemberLiteRequest.getDisabledTime(), wsRestAddMemberLiteRequest.getEnabledTime(),
            wsRestAddMemberLiteRequest.getAddExternalSubjectIfNotFound());

    //return result
    return wsAddMemberLiteResult;

  }

  /**
   * <pre>
   * based on a group name, put multiple members, or all members.  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestAddMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsAddMemberResults addMember(GrouperVersion clientVersion,
      String groupName, WsRestAddMemberRequest wsRestAddMembersRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestAddMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestAddMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAddMembersRequest.getClientVersion()), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestAddMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }

    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        true, "groupName");
    wsGroupLookup.setGroupName(groupName);

    //get the results
    WsAddMemberResults wsAddMemberResults = new GrouperService(false).addMember(
        clientVersionString, wsGroupLookup, wsRestAddMembersRequest.getSubjectLookups(),
        wsRestAddMembersRequest.getReplaceAllExisting(), wsRestAddMembersRequest
            .getActAsSubjectLookup(), wsRestAddMembersRequest.getFieldName(),
        wsRestAddMembersRequest.getTxType(), wsRestAddMembersRequest
            .getIncludeGroupDetail(), wsRestAddMembersRequest.getIncludeSubjectDetail(),
        wsRestAddMembersRequest.getSubjectAttributeNames(), wsRestAddMembersRequest.getParams(),
        wsRestAddMembersRequest.getDisabledTime(), wsRestAddMembersRequest.getEnabledTime(),
        wsRestAddMembersRequest.getAddExternalSubjectIfNotFound());

    //return result
    return wsAddMemberResults;

  }

  /**
   * <pre>
   * assign privileges.  e.g. url:
   * /v1_3_000/grouperPrivileges
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestAssignGrouperPrivilegeRequest is the request body converted to an object
   * @return the result
   */
  public static WsAssignGrouperPrivilegesResults assignGrouperPrivileges(GrouperVersion clientVersion,
      WsRestAssignGrouperPrivilegesRequest wsRestAssignGrouperPrivilegeRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestAssignGrouperPrivilegeRequest != null,
        "Body of request must contain an instance of "
            + WsRestAssignGrouperPrivilegesRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignGrouperPrivilegeRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = new GrouperService(false).assignGrouperPrivileges(
        clientVersionString, wsRestAssignGrouperPrivilegeRequest.getWsSubjectLookups(), wsRestAssignGrouperPrivilegeRequest.getWsGroupLookup(), 
        wsRestAssignGrouperPrivilegeRequest.getWsStemLookup(), wsRestAssignGrouperPrivilegeRequest.getPrivilegeType(), wsRestAssignGrouperPrivilegeRequest.getPrivilegeNames(),
        wsRestAssignGrouperPrivilegeRequest.getAllowed(), wsRestAssignGrouperPrivilegeRequest.getReplaceAllExisting(), wsRestAssignGrouperPrivilegeRequest.getTxType(), wsRestAssignGrouperPrivilegeRequest
            .getActAsSubjectLookup(), wsRestAssignGrouperPrivilegeRequest.getIncludeSubjectDetail(),
            wsRestAssignGrouperPrivilegeRequest.getSubjectAttributeNames(), wsRestAssignGrouperPrivilegeRequest
            .getIncludeGroupDetail(), wsRestAssignGrouperPrivilegeRequest.getParams());

    //return result
    return wsAssignGrouperPrivilegesResults;

  }

  /**
   * <pre>
   * based on a group name, delete the member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestDeleteMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsDeleteMemberLiteResult deleteMemberLite(GrouperVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestDeleteMemberLiteRequest wsRestDeleteMemberLiteRequest) {

    //make sure not null
    wsRestDeleteMemberLiteRequest = wsRestDeleteMemberLiteRequest == null ? new WsRestDeleteMemberLiteRequest()
        : wsRestDeleteMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestDeleteMemberLiteRequest.getClientVersion()), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestDeleteMemberLiteRequest
        .getGroupName(), true, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestDeleteMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestDeleteMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsDeleteMemberLiteResult wsDeleteMemberLiteResult = new GrouperService(false)
        .deleteMemberLite(clientVersionString, groupName, wsRestDeleteMemberLiteRequest
            .getGroupUuid(), subjectId, sourceId, wsRestDeleteMemberLiteRequest
            .getSubjectIdentifier(), wsRestDeleteMemberLiteRequest.getActAsSubjectId(),
            wsRestDeleteMemberLiteRequest.getActAsSubjectSourceId(),
            wsRestDeleteMemberLiteRequest.getActAsSubjectIdentifier(),
            wsRestDeleteMemberLiteRequest.getFieldName(), wsRestDeleteMemberLiteRequest
                .getIncludeGroupDetail(), wsRestDeleteMemberLiteRequest
                .getIncludeSubjectDetail(), wsRestDeleteMemberLiteRequest
                .getSubjectAttributeNames(), wsRestDeleteMemberLiteRequest
                .getParamName0(), wsRestDeleteMemberLiteRequest.getParamValue0(),
            wsRestDeleteMemberLiteRequest.getParamName1(), wsRestDeleteMemberLiteRequest
                .getParamValue1());

    //return result
    return wsDeleteMemberLiteResult;

  }

  /**
   * <pre>
   * based on a group name, put multiple members, or all members.  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestDeleteMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsDeleteMemberResults deleteMember(GrouperVersion clientVersion,
      String groupName, WsRestDeleteMemberRequest wsRestDeleteMembersRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestDeleteMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestDeleteMemberRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestDeleteMembersRequest.getClientVersion()), false, "clientVersion");
  
    WsGroupLookup wsGroupLookup = wsRestDeleteMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }
  
    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        true, "groupName");
    wsGroupLookup.setGroupName(groupName);
  
    //get the results
    WsDeleteMemberResults wsDeleteMemberResults = new GrouperService(false).deleteMember(
        clientVersionString, wsGroupLookup, wsRestDeleteMembersRequest
            .getSubjectLookups(), wsRestDeleteMembersRequest.getActAsSubjectLookup(),
        wsRestDeleteMembersRequest.getFieldName(),
        wsRestDeleteMembersRequest.getTxType(), wsRestDeleteMembersRequest
            .getIncludeGroupDetail(), wsRestDeleteMembersRequest
            .getIncludeSubjectDetail(), wsRestDeleteMembersRequest
            .getSubjectAttributeNames(), wsRestDeleteMembersRequest.getParams());
  
    //return result
    return wsDeleteMemberResults;
  
  }

  /**
   * <pre>
   * based on a group name, and multiple subjects, see if they are members .  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestHasMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsHasMemberResults hasMember(GrouperVersion clientVersion,
      String groupName, WsRestHasMemberRequest wsRestHasMembersRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestHasMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestHasMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestHasMembersRequest.getClientVersion()), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestHasMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }

    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        true, "groupName");
    wsGroupLookup.setGroupName(groupName);

    //get the results
    WsHasMemberResults wsHasMemberResults = new GrouperService(false).hasMember(
        clientVersionString, wsGroupLookup, wsRestHasMembersRequest.getSubjectLookups(),
        wsRestHasMembersRequest.getMemberFilter(), wsRestHasMembersRequest
            .getActAsSubjectLookup(), wsRestHasMembersRequest.getFieldName(),
        wsRestHasMembersRequest.getIncludeGroupDetail(), wsRestHasMembersRequest
            .getIncludeSubjectDetail(), wsRestHasMembersRequest
            .getSubjectAttributeNames(), wsRestHasMembersRequest.getParams(),
        wsRestHasMembersRequest.getPointInTimeFrom(), 
        wsRestHasMembersRequest.getPointInTimeTo());

    //return result
    return wsHasMemberResults;

  }

  /**
   * <pre>
   * based on a group name, get members .  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestGetMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetMembersResults getMembers(GrouperVersion clientVersion,
      WsRestGetMembersRequest wsRestGetMembersRequest) {

    //cant be null
    GrouperUtil
        .assertion(wsRestGetMembersRequest != null,
            "Body of request must contain an instance of "
                + WsRestGetMembersRequest.class.getSimpleName()
                + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetMembersRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsGetMembersResults wsGetMembersResults = new GrouperService(false).getMembers(
        clientVersionString, wsRestGetMembersRequest.getWsGroupLookups(),
        wsRestGetMembersRequest.getMemberFilter(), wsRestGetMembersRequest
            .getActAsSubjectLookup(), wsRestGetMembersRequest.getFieldName(),
        wsRestGetMembersRequest.getIncludeGroupDetail(), wsRestGetMembersRequest
            .getIncludeSubjectDetail(), wsRestGetMembersRequest
            .getSubjectAttributeNames(), wsRestGetMembersRequest.getParams(),
            wsRestGetMembersRequest.getSourceIds(),
            wsRestGetMembersRequest.getPointInTimeFrom(),
            wsRestGetMembersRequest.getPointInTimeTo(), wsRestGetMembersRequest.getPageSize(),
            wsRestGetMembersRequest.getPageNumber(), wsRestGetMembersRequest.getSortString(), 
            wsRestGetMembersRequest.getAscending());

    //return result
    return wsGetMembersResults;

  }

  /**
   * <pre>
   * based on a group name, and a subject, see if member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestHasMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsHasMemberLiteResult hasMemberLite(GrouperVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestHasMemberLiteRequest wsRestHasMemberLiteRequest) {
  
    //make sure not null
    wsRestHasMemberLiteRequest = wsRestHasMemberLiteRequest == null ? new WsRestHasMemberLiteRequest()
        : wsRestHasMemberLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestHasMemberLiteRequest.getClientVersion()), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestHasMemberLiteRequest
        .getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestHasMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestHasMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");
  
    //get the results
    WsHasMemberLiteResult wsHasMemberLiteResult = new GrouperService(false).hasMemberLite(
        clientVersionString, groupName, wsRestHasMemberLiteRequest.getGroupUuid(),
        subjectId, sourceId, wsRestHasMemberLiteRequest.getSubjectIdentifier(),
        wsRestHasMemberLiteRequest.getMemberFilter(), wsRestHasMemberLiteRequest
            .getActAsSubjectId(), wsRestHasMemberLiteRequest.getActAsSubjectSourceId(),
        wsRestHasMemberLiteRequest.getActAsSubjectIdentifier(),
        wsRestHasMemberLiteRequest.getFieldName(), wsRestHasMemberLiteRequest
            .getIncludeGroupDetail(), wsRestHasMemberLiteRequest
            .getIncludeSubjectDetail(), wsRestHasMemberLiteRequest
            .getSubjectAttributeNames(), wsRestHasMemberLiteRequest.getParamName0(),
        wsRestHasMemberLiteRequest.getParamValue0(), wsRestHasMemberLiteRequest
            .getParamName1(), wsRestHasMemberLiteRequest.getParamValue1(),
        wsRestHasMemberLiteRequest.getPointInTimeFrom(), 
        wsRestHasMemberLiteRequest.getPointInTimeTo());
  
    //return result
    return wsHasMemberLiteResult;
  
  }

  /**
   * <pre>
   * based on a member, change the subject  e.g. url:
   * /v1_3_000/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param oldSubjectId from url if applicable
   * @param oldSubjectSourceId from url is applicable
   * @param wsRestMemberChangeSubjectRequest is the request body converted to an object
   * @return the result
   */
  public static WsMemberChangeSubjectResults memberChangeSubject(GrouperVersion clientVersion,
      WsRestMemberChangeSubjectRequest wsRestMemberChangeSubjectRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestMemberChangeSubjectRequest != null,
        "Body of request must contain an instance of "
            + WsRestDeleteMemberRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestMemberChangeSubjectRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsMemberChangeSubjectResults wsMemberChangeSubjectResults = new GrouperService(false).memberChangeSubject(
        clientVersionString, wsRestMemberChangeSubjectRequest.getWsMemberChangeSubjects(), 
          wsRestMemberChangeSubjectRequest.getActAsSubjectLookup(),
            wsRestMemberChangeSubjectRequest.getTxType(), wsRestMemberChangeSubjectRequest
            .getIncludeSubjectDetail(), wsRestMemberChangeSubjectRequest
            .getSubjectAttributeNames(), wsRestMemberChangeSubjectRequest.getParams());

    //return result
    return wsMemberChangeSubjectResults;

  }

  /**
   * <pre>
   * based on a member, change the subject
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param oldSubjectId from url, e.g. /v1_3_000/members/subjectId/123412345
   * @param oldSubjectSourceId from url (optional) e.g.
   * /v1_3_000/members/sourceId/someSource/subjectId/123412345/sourceId/12342
   * @param wsRestMemberChangeSubjectLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsMemberChangeSubjectLiteResult memberChangeSubjectLite(GrouperVersion clientVersion,
      String oldSubjectId, String oldSubjectSourceId, WsRestMemberChangeSubjectLiteRequest wsRestMemberChangeSubjectLiteRequest) {

    //make sure not null
    wsRestMemberChangeSubjectLiteRequest = wsRestMemberChangeSubjectLiteRequest == null ? new WsRestMemberChangeSubjectLiteRequest()
        : wsRestMemberChangeSubjectLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestMemberChangeSubjectLiteRequest.getClientVersion()), false, "clientVersion");
    oldSubjectId = GrouperServiceUtils.pickOne(oldSubjectId, wsRestMemberChangeSubjectLiteRequest
        .getOldSubjectId(), false, "oldSubjectId");
    oldSubjectSourceId = GrouperServiceUtils.pickOne(oldSubjectSourceId, wsRestMemberChangeSubjectLiteRequest
        .getOldSubjectSourceId(), true, "oldSubjectSourceId");

    //get the results
    WsMemberChangeSubjectLiteResult wsMemberChangeSubjectLiteResult = new GrouperService(false).memberChangeSubjectLite(
        clientVersionString,oldSubjectId, oldSubjectSourceId, wsRestMemberChangeSubjectLiteRequest.getOldSubjectIdentifier(),
        wsRestMemberChangeSubjectLiteRequest.getNewSubjectId(),
        wsRestMemberChangeSubjectLiteRequest.getNewSubjectSourceId(),
        wsRestMemberChangeSubjectLiteRequest.getNewSubjectIdentifier(),
        wsRestMemberChangeSubjectLiteRequest
            .getActAsSubjectId(), wsRestMemberChangeSubjectLiteRequest.getActAsSubjectSourceId(),
            wsRestMemberChangeSubjectLiteRequest.getActAsSubjectIdentifier(),
            wsRestMemberChangeSubjectLiteRequest.getDeleteOldMember(), 
            wsRestMemberChangeSubjectLiteRequest.getIncludeSubjectDetail(),
            wsRestMemberChangeSubjectLiteRequest
            .getSubjectAttributeNames(), wsRestMemberChangeSubjectLiteRequest.getParamName0(),
            wsRestMemberChangeSubjectLiteRequest.getParamValue0(), wsRestMemberChangeSubjectLiteRequest
            .getParamName1(), wsRestMemberChangeSubjectLiteRequest.getParamValue1());

    //return result
    return wsMemberChangeSubjectLiteResult;

  }

  /**
   * <pre>
   * get privileges for a group or stem and subject
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestGetGrouperPrivilegesLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetGrouperPrivilegesLiteResult getGrouperPrivilegesLite(GrouperVersion clientVersion,
      WsRestGetGrouperPrivilegesLiteRequest wsRestGetGrouperPrivilegesLiteRequest) {
  
    //make sure not null
    wsRestGetGrouperPrivilegesLiteRequest = wsRestGetGrouperPrivilegesLiteRequest == null ? new WsRestGetGrouperPrivilegesLiteRequest()
        : wsRestGetGrouperPrivilegesLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetGrouperPrivilegesLiteRequest.getClientVersion()), false, "clientVersion");
  
    //get the results
    WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = new GrouperService(false).getGrouperPrivilegesLite(
        clientVersionString,
        wsRestGetGrouperPrivilegesLiteRequest.getSubjectId(),
        wsRestGetGrouperPrivilegesLiteRequest.getSubjectSourceId(),
        wsRestGetGrouperPrivilegesLiteRequest.getSubjectIdentifier(), 
        wsRestGetGrouperPrivilegesLiteRequest.getGroupName(), 
        wsRestGetGrouperPrivilegesLiteRequest.getGroupUuid(), 
        wsRestGetGrouperPrivilegesLiteRequest.getStemName(), 
        wsRestGetGrouperPrivilegesLiteRequest.getStemUuid(), 
        wsRestGetGrouperPrivilegesLiteRequest.getPrivilegeType(), 
        wsRestGetGrouperPrivilegesLiteRequest.getPrivilegeName(),
        wsRestGetGrouperPrivilegesLiteRequest.getActAsSubjectId(), 
        wsRestGetGrouperPrivilegesLiteRequest.getActAsSubjectSourceId(),
        wsRestGetGrouperPrivilegesLiteRequest.getActAsSubjectIdentifier(),
        wsRestGetGrouperPrivilegesLiteRequest.getIncludeSubjectDetail(),
        wsRestGetGrouperPrivilegesLiteRequest.getSubjectAttributeNames(), 
        wsRestGetGrouperPrivilegesLiteRequest.getIncludeGroupDetail(),
        wsRestGetGrouperPrivilegesLiteRequest.getParamName0(),
        wsRestGetGrouperPrivilegesLiteRequest.getParamValue0(), 
        wsRestGetGrouperPrivilegesLiteRequest.getParamName1(), 
        wsRestGetGrouperPrivilegesLiteRequest.getParamValue1());
  
    //return result
    return wsGetGrouperPrivilegesLiteResult;
  
  }

  /**
   * <pre>
   * assign/revoke privileges for a group or stem and subject
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestAssignGrouperPrivilegesLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLite(GrouperVersion clientVersion,
      WsRestAssignGrouperPrivilegesLiteRequest wsRestAssignGrouperPrivilegesLiteRequest) {

    //make sure not null
    wsRestAssignGrouperPrivilegesLiteRequest = wsRestAssignGrouperPrivilegesLiteRequest == null 
      ? new WsRestAssignGrouperPrivilegesLiteRequest()
        : wsRestAssignGrouperPrivilegesLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignGrouperPrivilegesLiteRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult = new GrouperService(false)
          .assignGrouperPrivilegesLite(
        clientVersionString,
        wsRestAssignGrouperPrivilegesLiteRequest.getSubjectId(),
        wsRestAssignGrouperPrivilegesLiteRequest.getSubjectSourceId(),
        wsRestAssignGrouperPrivilegesLiteRequest.getSubjectIdentifier(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getGroupName(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getGroupUuid(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getStemName(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getStemUuid(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getPrivilegeType(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getPrivilegeName(),
        wsRestAssignGrouperPrivilegesLiteRequest.getAllowed(),
        wsRestAssignGrouperPrivilegesLiteRequest.getActAsSubjectId(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getActAsSubjectSourceId(),
        wsRestAssignGrouperPrivilegesLiteRequest.getActAsSubjectIdentifier(),
        wsRestAssignGrouperPrivilegesLiteRequest.getIncludeSubjectDetail(),
        wsRestAssignGrouperPrivilegesLiteRequest.getSubjectAttributeNames(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getIncludeGroupDetail(),
        wsRestAssignGrouperPrivilegesLiteRequest.getParamName0(),
        wsRestAssignGrouperPrivilegesLiteRequest.getParamValue0(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getParamName1(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getParamValue1());

    //return result
    return wsAssignGrouperPrivilegesLiteResult;

  }

  /**
   * <pre>
   * based a subject object of type WsRestGetGroupsRequest, get the groups
   * /v1_3_000/subjects/123/groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param sourceId is the source of the service
   * @param subjectId is the subject to search for groups
   * @param wsRestGetGroupsRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetGroupsResults getGroups(GrouperVersion clientVersion,
      String subjectId, String sourceId, WsRestGetGroupsRequest wsRestGetGroupsRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestGetGroupsRequest != null,
        "Body of request must contain an instance of "
            + WsRestGetGroupsRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetGroupsRequest.getClientVersion()), false, "clientVersion");

    Set<WsSubjectLookup> subjectLookups = null;
    WsSubjectLookup[] subjectLookupArray = wsRestGetGroupsRequest.getSubjectLookups();
    if (!StringUtils.isBlank(subjectId)) {
      
      if (GrouperUtil.length(subjectLookupArray) == 0) {
        subjectLookups = new HashSet<WsSubjectLookup>();
      } else {
        subjectLookups = GrouperUtil.toSet(subjectLookupArray);
      }
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, sourceId, null);
      subjectLookups.add(wsSubjectLookup);
      subjectLookupArray = GrouperUtil.toArray(subjectLookups, WsSubjectLookup.class);
    }

    //get the results
    WsGetGroupsResults wsGetGroupsResults = new GrouperService(false).getGroups(
        clientVersionString, subjectLookupArray,
        wsRestGetGroupsRequest.getMemberFilter(), wsRestGetGroupsRequest
            .getActAsSubjectLookup(), wsRestGetGroupsRequest.getIncludeGroupDetail(),
        wsRestGetGroupsRequest.getIncludeSubjectDetail(), wsRestGetGroupsRequest
            .getSubjectAttributeNames(), wsRestGetGroupsRequest.getParams(), wsRestGetGroupsRequest.getFieldName(),
            wsRestGetGroupsRequest.getScope(),
            wsRestGetGroupsRequest.getWsStemLookup(), wsRestGetGroupsRequest.getStemScope(), wsRestGetGroupsRequest.getEnabled(),
            wsRestGetGroupsRequest.getPageSize(), wsRestGetGroupsRequest.getPageNumber(), wsRestGetGroupsRequest.getSortString(), 
            wsRestGetGroupsRequest.getAscending(),
            wsRestGetGroupsRequest.getPointInTimeFrom(), wsRestGetGroupsRequest.getPointInTimeTo());

    //return result
    return wsGetGroupsResults;

  }

  /**
   * <pre>
   * based on a subject, get the groups associated
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestGetGroupsLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetGroupsLiteResult getGroupsLite(GrouperVersion clientVersion,
      String subjectId, String sourceId,
      WsRestGetGroupsLiteRequest wsRestGetGroupsLiteRequest) {

    //make sure not null
    wsRestGetGroupsLiteRequest = wsRestGetGroupsLiteRequest == null ? new WsRestGetGroupsLiteRequest()
        : wsRestGetGroupsLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetGroupsLiteRequest.getClientVersion()), false, "clientVersion");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestGetGroupsLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestGetGroupsLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsGetGroupsLiteResult wsGetGroupsLiteResult = new GrouperService(false).getGroupsLite(
        clientVersionString, subjectId, sourceId, wsRestGetGroupsLiteRequest
            .getSubjectIdentifier(), wsRestGetGroupsLiteRequest.getMemberFilter(),
        wsRestGetGroupsLiteRequest.getActAsSubjectId(), wsRestGetGroupsLiteRequest
            .getActAsSubjectSourceId(), wsRestGetGroupsLiteRequest
            .getActAsSubjectIdentifier(), wsRestGetGroupsLiteRequest
            .getIncludeGroupDetail(), wsRestGetGroupsLiteRequest
            .getIncludeSubjectDetail(), wsRestGetGroupsLiteRequest
            .getSubjectAttributeNames(), wsRestGetGroupsLiteRequest.getParamName0(),
        wsRestGetGroupsLiteRequest.getParamValue0(), wsRestGetGroupsLiteRequest
            .getParamName1(), wsRestGetGroupsLiteRequest.getParamValue1(), wsRestGetGroupsLiteRequest.getFieldName(),
            wsRestGetGroupsLiteRequest.getScope(), wsRestGetGroupsLiteRequest.getStemName(), 
            wsRestGetGroupsLiteRequest.getStemUuid(), wsRestGetGroupsLiteRequest.getStemScope(),
            wsRestGetGroupsLiteRequest.getEnabled(), wsRestGetGroupsLiteRequest.getPageSize(),
            wsRestGetGroupsLiteRequest.getPageNumber(), wsRestGetGroupsLiteRequest.getSortString(), 
            wsRestGetGroupsLiteRequest.getAscending(),
            wsRestGetGroupsLiteRequest.getPointInTimeFrom(), wsRestGetGroupsLiteRequest.getPointInTimeTo());

    //return result
    return wsGetGroupsLiteResult;

  }

  /**
   * <pre>
   * based on a stem query, get the stems
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindStemsRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindStemsResults findStems(GrouperVersion clientVersion,
      WsRestFindStemsRequest wsRestFindStemsRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindStemsRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsFindStemsResults wsFindStemsResults = new GrouperService(false).findStems(
        clientVersionString, wsRestFindStemsRequest.getWsStemQueryFilter(),
        wsRestFindStemsRequest.getActAsSubjectLookup(), wsRestFindStemsRequest
            .getParams(), wsRestFindStemsRequest.getWsStemLookups());

    //return result
    return wsFindStemsResults;
  }

  /**
   * <pre>
   * based on a stem query, get the stems
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindStemsLiteRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindStemsResults findStemsLite(GrouperVersion clientVersion,
      WsRestFindStemsLiteRequest wsRestFindStemsLiteRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindStemsLiteRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsFindStemsResults wsFindStemsResults = new GrouperService(false).findStemsLite(
        clientVersionString, wsRestFindStemsLiteRequest.getStemQueryFilterType(),
        wsRestFindStemsLiteRequest.getStemName(), wsRestFindStemsLiteRequest
            .getParentStemName(), wsRestFindStemsLiteRequest.getParentStemNameScope(),
        wsRestFindStemsLiteRequest.getStemUuid(), wsRestFindStemsLiteRequest
            .getStemAttributeName(), wsRestFindStemsLiteRequest.getStemAttributeValue(),
        wsRestFindStemsLiteRequest.getActAsSubjectId(), wsRestFindStemsLiteRequest
            .getActAsSubjectSourceId(), wsRestFindStemsLiteRequest
            .getActAsSubjectIdentifier(), wsRestFindStemsLiteRequest.getParamName0(),
        wsRestFindStemsLiteRequest.getParamValue0(), wsRestFindStemsLiteRequest
            .getParamName1(), wsRestFindStemsLiteRequest.getParamValue1());

    //return result
    return wsFindStemsResults;
  }

  /**
   * <pre>
   * based on a submitted object of type WsRestStemSaveRequest, save stems.  e.g. url:
   * /v1_3_000/stems
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestStemSaveRequest is the request body converted to an object
   * @return the result
   */
  public static WsStemSaveResults stemSave(GrouperVersion clientVersion,
      WsRestStemSaveRequest wsRestStemSaveRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestStemSaveRequest != null,
        "Body of request must contain an instance of "
            + WsRestStemSaveRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestStemSaveRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsStemSaveResults wsStemSaveResults = new GrouperService(false).stemSave(
        clientVersionString, wsRestStemSaveRequest.getWsStemToSaves(),
        wsRestStemSaveRequest.getActAsSubjectLookup(), wsRestStemSaveRequest.getTxType(),
        wsRestStemSaveRequest.getParams());

    //return result
    return wsStemSaveResults;

  }

  /**
   * <pre>
   * based on a stem name and submitted object type WsRestStemSaveLiteRequest,
   * save a stem.  url e.g. /v1_3_000/stems/aStem:aStem2
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param stemLookupName is the name of the stem to lookup and save (old name if changing) including parent stems, e.g. a:b:c
   * @param wsRestStemSaveLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsStemSaveLiteResult stemSaveLite(GrouperVersion clientVersion,
      String stemLookupName, WsRestStemSaveLiteRequest wsRestStemSaveLiteRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestStemSaveLiteRequest != null,
        "Body of request must contain an instance of "
            + WsRestStemSaveLiteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestStemSaveLiteRequest.getClientVersion()), false, "clientVersion");

    stemLookupName = GrouperServiceUtils.pickOne(stemLookupName,
        wsRestStemSaveLiteRequest.getStemLookupName(), false, "stemLookupName");

    //get the results
    WsStemSaveLiteResult wsStemSaveLiteResult = new GrouperService(false).stemSaveLite(
        clientVersionString, wsRestStemSaveLiteRequest.getStemLookupUuid(),
        stemLookupName, wsRestStemSaveLiteRequest.getStemUuid(), wsRestStemSaveLiteRequest.getStemName(),
        wsRestStemSaveLiteRequest.getDisplayExtension(),wsRestStemSaveLiteRequest
            .getDescription(), 
        wsRestStemSaveLiteRequest.getSaveMode(), wsRestStemSaveLiteRequest
            .getActAsSubjectId(), wsRestStemSaveLiteRequest.getActAsSubjectSourceId(),
        wsRestStemSaveLiteRequest.getActAsSubjectIdentifier(), wsRestStemSaveLiteRequest
            .getParamName0(), wsRestStemSaveLiteRequest.getParamValue0(),
        wsRestStemSaveLiteRequest.getParamName1(), wsRestStemSaveLiteRequest
            .getParamValue1());

    //return result
    return wsStemSaveLiteResult;

  }

  /**
   * <pre>
   * based on submitted object of type WsRestStemDeleteRequest, delete stems
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestStemDeleteRequest is the request body converted to an object
   * @return the result
   */
  public static WsStemDeleteResults stemDelete(GrouperVersion clientVersion,
      WsRestStemDeleteRequest wsRestStemDeleteRequest) {

    //cant be null
    GrouperUtil
        .assertion(wsRestStemDeleteRequest != null,
            "Body of request must contain an instance of "
                + WsRestStemDeleteRequest.class.getSimpleName()
                + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestStemDeleteRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsStemDeleteResults wsStemDeleteResults = new GrouperService(false).stemDelete(
        clientVersionString, wsRestStemDeleteRequest.getWsStemLookups(),
        wsRestStemDeleteRequest.getActAsSubjectLookup(), wsRestStemDeleteRequest
            .getTxType(), wsRestStemDeleteRequest.getParams());

    //return result
    return wsStemDeleteResults;

  }

  /**
   * <pre>
   * based on a stem name, delete the stem url e.g. /v1_3_000/stems/aStem:aStem2
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param stemName is the name of the stem to delete including parent stems, e.g. a:b:c
   * @param wsRestStemDeleteLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsStemDeleteLiteResult stemDeleteLite(GrouperVersion clientVersion,
      String stemName, WsRestStemDeleteLiteRequest wsRestStemDeleteLiteRequest) {

    //make sure not null
    wsRestStemDeleteLiteRequest = wsRestStemDeleteLiteRequest == null ? new WsRestStemDeleteLiteRequest()
        : wsRestStemDeleteLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestStemDeleteLiteRequest.getClientVersion()), false, "clientVersion");

    stemName = GrouperServiceUtils.pickOne(stemName, wsRestStemDeleteLiteRequest
        .getStemName(), false, "stemName");

    //get the results
    WsStemDeleteLiteResult wsStemDeleteLiteResult = new GrouperService(false).stemDeleteLite(
        clientVersionString, stemName, wsRestStemDeleteLiteRequest.getStemUuid(),
        wsRestStemDeleteLiteRequest.getActAsSubjectId(), wsRestStemDeleteLiteRequest
            .getActAsSubjectSourceId(), wsRestStemDeleteLiteRequest
            .getActAsSubjectIdentifier(), wsRestStemDeleteLiteRequest.getParamName0(),
        wsRestStemDeleteLiteRequest.getParamValue0(), wsRestStemDeleteLiteRequest
            .getParamName1(), wsRestStemDeleteLiteRequest.getParamValue1());

    //return result
    return wsStemDeleteLiteResult;

  }

  /**
   * <pre>
   * based on a submitted object of type WsRestGroupDeleteRequest, delete the groups.  e.g. url:
   * /v1_3_000/groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestGroupDeleteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupDeleteResults groupDelete(GrouperVersion clientVersion,
      WsRestGroupDeleteRequest wsRestGroupDeleteRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestGroupDeleteRequest != null,
        "Body of request must contain an instance of "
            + WsRestGroupDeleteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGroupDeleteRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsGroupDeleteResults wsGroupDeleteResults = new GrouperService(false).groupDelete(
        clientVersionString, wsRestGroupDeleteRequest.getWsGroupLookups(),
        wsRestGroupDeleteRequest.getActAsSubjectLookup(), wsRestGroupDeleteRequest
            .getTxType(), wsRestGroupDeleteRequest.getIncludeGroupDetail(), 
            wsRestGroupDeleteRequest.getParams());

    //return result
    return wsGroupDeleteResults;

  }

  /**
   * <pre>
   * based on a group name, delete a group
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group to delete including parent stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestGroupDeleteLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupDeleteLiteResult groupDeleteLite(GrouperVersion clientVersion,
      String groupName, WsRestGroupDeleteLiteRequest wsRestGroupDeleteLiteRequest) {

    //make sure not null
    wsRestGroupDeleteLiteRequest = wsRestGroupDeleteLiteRequest == null ? new WsRestGroupDeleteLiteRequest()
        : wsRestGroupDeleteLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGroupDeleteLiteRequest.getClientVersion()), false, "clientVersion");

    groupName = GrouperServiceUtils.pickOne(groupName, wsRestGroupDeleteLiteRequest
        .getGroupName(), false, "groupName");

    //get the results
    WsGroupDeleteLiteResult wsGroupDeleteLiteResult = new GrouperService(false)
        .groupDeleteLite(clientVersionString, groupName, wsRestGroupDeleteLiteRequest
            .getGroupUuid(), wsRestGroupDeleteLiteRequest.getActAsSubjectId(),
            wsRestGroupDeleteLiteRequest.getActAsSubjectSourceId(),
            wsRestGroupDeleteLiteRequest.getActAsSubjectIdentifier(),
            wsRestGroupDeleteLiteRequest.getIncludeGroupDetail(),
            wsRestGroupDeleteLiteRequest.getParamName0(), wsRestGroupDeleteLiteRequest
                .getParamValue0(), wsRestGroupDeleteLiteRequest.getParamName1(),
            wsRestGroupDeleteLiteRequest.getParamValue1());

    //return result
    return wsGroupDeleteLiteResult;

  }

  /**
   * <pre>
   * based on a submitted object of type WsRestGroupSaveRequest, save groups.  e.g. url:
   * /v1_3_000/groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestGroupSaveRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupSaveResults groupSave(GrouperVersion clientVersion,
      WsRestGroupSaveRequest wsRestGroupSaveRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestGroupSaveRequest != null,
        "Body of request must contain an instance of "
            + WsRestGroupSaveRequest.class.getSimpleName() + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGroupSaveRequest.getClientVersion()), false, "clientVersion");
  
    //get the results
    WsGroupSaveResults wsGroupSaveResults = new GrouperService(false).groupSave(
        clientVersionString, wsRestGroupSaveRequest.getWsGroupToSaves(),
        wsRestGroupSaveRequest.getActAsSubjectLookup(), wsRestGroupSaveRequest.getTxType(),
        wsRestGroupSaveRequest.getIncludeGroupDetail(),
        wsRestGroupSaveRequest.getParams());
  
    //return result
    return wsGroupSaveResults;
  
  }

  /**
   * <pre>
   * based on a group name and submitted object type WsRestGroupSaveLiteRequest,
   * save a group.  url e.g. /v1_3_000/groups/aStem:aGroup2
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupLookupName is the name of the group to lookup and save (old name if changing) including parent groups, e.g. a:b:c
   * @param wsRestGroupSaveLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupSaveLiteResult groupSaveLite(GrouperVersion clientVersion,
      String groupLookupName, WsRestGroupSaveLiteRequest wsRestGroupSaveLiteRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestGroupSaveLiteRequest != null,
        "Body of request must contain an instance of "
            + WsRestGroupSaveLiteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGroupSaveLiteRequest.getClientVersion()), false, "clientVersion");
  
    groupLookupName = GrouperServiceUtils.pickOne(groupLookupName,
        wsRestGroupSaveLiteRequest.getGroupLookupName(), false, "groupLookupName");
  
    //get the results
    WsGroupSaveLiteResult wsGroupSaveLiteResult = new GrouperService(false).groupSaveLite(
        clientVersionString, wsRestGroupSaveLiteRequest.getGroupLookupUuid(),
        groupLookupName, wsRestGroupSaveLiteRequest.getGroupUuid(), wsRestGroupSaveLiteRequest.getGroupName(),
        wsRestGroupSaveLiteRequest.getDisplayExtension(),wsRestGroupSaveLiteRequest
            .getDescription(), 
        wsRestGroupSaveLiteRequest.getSaveMode(), wsRestGroupSaveLiteRequest
            .getActAsSubjectId(), wsRestGroupSaveLiteRequest.getActAsSubjectSourceId(),
        wsRestGroupSaveLiteRequest.getActAsSubjectIdentifier(), 
        wsRestGroupSaveLiteRequest.getIncludeGroupDetail(), wsRestGroupSaveLiteRequest
            .getParamName0(), wsRestGroupSaveLiteRequest.getParamValue0(),
        wsRestGroupSaveLiteRequest.getParamName1(), wsRestGroupSaveLiteRequest
            .getParamValue1(), wsRestGroupSaveLiteRequest.getTypeOfGroup());
  
    //return result
    return wsGroupSaveLiteResult;
  
  }

  /**
   * <pre>
   * based on a group name, get memberships.  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/memberships
   * /v1_3_000/subjects/12345678/memberships
   * /v1_3_000/memberships
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group (optional)
   * @param subjectId is the subjectId (optional)
   * @param sourceId is the source id of the subject to search for (optional)
   * @param wsRestGetMembershipsRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetMembershipsResults getMemberships(GrouperVersion clientVersion,
      String groupName, String subjectId, String sourceId, 
      WsRestGetMembershipsRequest wsRestGetMembershipsRequest) {
  
    //cant be null
    wsRestGetMembershipsRequest = wsRestGetMembershipsRequest == null ? new WsRestGetMembershipsRequest()
      : wsRestGetMembershipsRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetMembershipsRequest.getClientVersion()), false, "clientVersion");
  
    Set<WsGroupLookup> groupLookups = null;
    WsGroupLookup[] groupLookupArray = wsRestGetMembershipsRequest.getWsGroupLookups();
    //add a lookup
    if (!StringUtils.isBlank(groupName)) {
      if (GrouperUtil.length(groupLookupArray) == 0) {
        groupLookups = new HashSet<WsGroupLookup>();
      } else {
        groupLookups = GrouperUtil.toSet(groupLookupArray);
      }
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, null);
      groupLookups.add(wsGroupLookup);
      groupLookupArray = GrouperUtil.toArray(groupLookups, WsGroupLookup.class);
    }

    Set<WsSubjectLookup> subjectLookups = null;
    WsSubjectLookup[] subjectLookupArray = wsRestGetMembershipsRequest.getWsSubjectLookups();
    if (!StringUtils.isBlank(subjectId)) {
      
      if (GrouperUtil.length(subjectLookupArray) == 0) {
        subjectLookups = new HashSet<WsSubjectLookup>();
      } else {
        subjectLookups = GrouperUtil.toSet(subjectLookupArray);
      }
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, sourceId, null);
      subjectLookups.add(wsSubjectLookup);
      subjectLookupArray = GrouperUtil.toArray(subjectLookups, WsSubjectLookup.class);
    }
        
    //get the results
    WsGetMembershipsResults wsGetMembershipsResults = new GrouperService(false).getMemberships(
        clientVersionString, groupLookupArray, subjectLookupArray,
        wsRestGetMembershipsRequest.getMemberFilter(), wsRestGetMembershipsRequest
            .getActAsSubjectLookup(), wsRestGetMembershipsRequest.getFieldName(), wsRestGetMembershipsRequest.getIncludeSubjectDetail(), 
            wsRestGetMembershipsRequest.getSubjectAttributeNames(),
        wsRestGetMembershipsRequest.getIncludeGroupDetail(), wsRestGetMembershipsRequest.getParams(),
            wsRestGetMembershipsRequest.getSourceIds(), wsRestGetMembershipsRequest.getScope(), 
            wsRestGetMembershipsRequest.getWsStemLookup(), wsRestGetMembershipsRequest.getStemScope(), 
            wsRestGetMembershipsRequest.getEnabled(), wsRestGetMembershipsRequest.getMembershipIds(),
            wsRestGetMembershipsRequest.getWsOwnerStemLookups(), 
            wsRestGetMembershipsRequest.getWsOwnerAttributeDefLookups(),
            wsRestGetMembershipsRequest.getFieldType(), wsRestGetMembershipsRequest.getServiceRole(), wsRestGetMembershipsRequest.getServiceLookup());
  
    //return result
    return wsGetMembershipsResults;
  
  }

  /**
   * <pre>
   * based on a group name, get memberships.  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/memberships
   * /v1_3_000/subjects/12345678/memberships
   * /v1_3_000/memberships
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group (optional)
   * @param subjectId is the subjectId (optional)
   * @param sourceId is the source id of the subject to search for (optional)
   * @param wsRestGetMembershipsLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetMembershipsResults getMembershipsLite(GrouperVersion clientVersion,
      String groupName, String subjectId, String sourceId, WsRestGetMembershipsLiteRequest wsRestGetMembershipsLiteRequest) {
  
    //make sure not null
    wsRestGetMembershipsLiteRequest = wsRestGetMembershipsLiteRequest == null ? new WsRestGetMembershipsLiteRequest()
        : wsRestGetMembershipsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetMembershipsLiteRequest.getClientVersion()), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestGetMembershipsLiteRequest
        .getGroupName(), true, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestGetMembershipsLiteRequest
        .getSubjectId(), true, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestGetMembershipsLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsGetMembershipsResults wsGetMembershipsResults = new GrouperService(false).getMembershipsLite(
        clientVersionString, groupName, wsRestGetMembershipsLiteRequest.getGroupUuid(), subjectId, sourceId, wsRestGetMembershipsLiteRequest.getSubjectIdentifier(),
        wsRestGetMembershipsLiteRequest.getMemberFilter(), wsRestGetMembershipsLiteRequest.getIncludeSubjectDetail(), wsRestGetMembershipsLiteRequest
            .getActAsSubjectId(), wsRestGetMembershipsLiteRequest.getActAsSubjectSourceId(),
        wsRestGetMembershipsLiteRequest.getActAsSubjectIdentifier(),
        wsRestGetMembershipsLiteRequest.getFieldName(), wsRestGetMembershipsLiteRequest
            .getSubjectAttributeNames(), wsRestGetMembershipsLiteRequest.getIncludeGroupDetail(), wsRestGetMembershipsLiteRequest.getParamName0(),
        wsRestGetMembershipsLiteRequest.getParamValue0(), wsRestGetMembershipsLiteRequest
            .getParamName1(), wsRestGetMembershipsLiteRequest.getParamValue1(), 
            wsRestGetMembershipsLiteRequest.getSourceIds(), wsRestGetMembershipsLiteRequest.getScope(), wsRestGetMembershipsLiteRequest.getStemName(),
            wsRestGetMembershipsLiteRequest.getStemUuid(), wsRestGetMembershipsLiteRequest.getStemScope(), 
            wsRestGetMembershipsLiteRequest.getEnabled(), wsRestGetMembershipsLiteRequest.getMembershipIds(),
            wsRestGetMembershipsLiteRequest.getOwnerStemName(),
            wsRestGetMembershipsLiteRequest.getOwnerStemUuid(), wsRestGetMembershipsLiteRequest.getNameOfOwnerAttributeDef(), 
            wsRestGetMembershipsLiteRequest.getOwnerAttributeDefUuid(), wsRestGetMembershipsLiteRequest.getFieldType(), 
            wsRestGetMembershipsLiteRequest.getServiceRole(), wsRestGetMembershipsLiteRequest.getServiceId(), 
            wsRestGetMembershipsLiteRequest.getServiceName());
  
    //return result
    return wsGetMembershipsResults;
  
  }

  /**
   * <pre>
   * find subjects by id or search string.  e.g. url:
   * /v1_6_000/subjects/12345678
   * /v1_6_000/subjects
   * </pre>
   * @param clientVersion version of client, e.g. v1_6_000
   * @param subjectId is the subjectId (optional)
   * @param sourceId is the source id of the subject to search for (optional)
   * @param wsRestGetSubjectsRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetSubjectsResults getSubjects(GrouperVersion clientVersion,
      String subjectId, String sourceId, 
      WsRestGetSubjectsRequest wsRestGetSubjectsRequest) {
  
    //cant be null
    wsRestGetSubjectsRequest = wsRestGetSubjectsRequest == null ? new WsRestGetSubjectsRequest()
      : wsRestGetSubjectsRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetSubjectsRequest.getClientVersion()), false, "clientVersion");
  
    Set<WsSubjectLookup> subjectLookups = null;
    WsSubjectLookup[] subjectLookupArray = wsRestGetSubjectsRequest.getWsSubjectLookups();
    if (!StringUtils.isBlank(subjectId)) {
      
      if (GrouperUtil.length(subjectLookupArray) == 0) {
        subjectLookups = new HashSet<WsSubjectLookup>();
      } else {
        subjectLookups = GrouperUtil.toSet(subjectLookupArray);
      }
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, sourceId, null);
      subjectLookups.add(wsSubjectLookup);
      subjectLookupArray = GrouperUtil.toArray(subjectLookups, WsSubjectLookup.class);
    }
        
    //get the results
    WsGetSubjectsResults wsGetSubjectsResults = new GrouperService(false).getSubjects(
        clientVersionString, subjectLookupArray, wsRestGetSubjectsRequest.getSearchString(),
        wsRestGetSubjectsRequest.getIncludeSubjectDetail(), wsRestGetSubjectsRequest.getSubjectAttributeNames(),
        wsRestGetSubjectsRequest.getActAsSubjectLookup(), wsRestGetSubjectsRequest.getSourceIds(), 
        wsRestGetSubjectsRequest.getWsGroupLookup(), wsRestGetSubjectsRequest.getMemberFilter(),
        wsRestGetSubjectsRequest.getFieldName(), wsRestGetSubjectsRequest.getIncludeGroupDetail(), 
        wsRestGetSubjectsRequest.getParams());
  
    //return result
    return wsGetSubjectsResults;
  
  }

  /**
   * <pre>
   * find subjects by id or search string.  e.g. url:
   * /v1_6_000/subjects/12345678
   * /v1_6_000/subjects
   * </pre>
   * @param clientVersion version of client, e.g. v1_6_000
   * @param subjectId is the subjectId (optional)
   * @param sourceId is the source id of the subject to search for (optional)
   * @param wsRestGetSubjectsLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetSubjectsResults getSubjectsLite(GrouperVersion clientVersion,
      String subjectId, String sourceId, 
      WsRestGetSubjectsLiteRequest wsRestGetSubjectsLiteRequest) {
  
    //cant be null
    wsRestGetSubjectsLiteRequest = wsRestGetSubjectsLiteRequest == null ? new WsRestGetSubjectsLiteRequest()
      : wsRestGetSubjectsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetSubjectsLiteRequest.getClientVersion()), false, "clientVersion");
    
    String theSubjectId = wsRestGetSubjectsLiteRequest.getSubjectId();
    
    if (!StringUtils.isBlank(subjectId)) {
      if (!StringUtils.isBlank(theSubjectId) 
          && !StringUtils.equals(subjectId, theSubjectId)) {
        throw new WsInvalidQueryException("subjectId in url '" + subjectId 
            + "' is different than in XML submission '" + theSubjectId + "'");
      }
      theSubjectId = subjectId;
    }
    
    String theSourceId = wsRestGetSubjectsLiteRequest.getSubjectSourceId();
    
    if (!StringUtils.isBlank(sourceId)) {
      if (!StringUtils.isBlank(theSourceId) 
          && !StringUtils.equals(sourceId, theSourceId)) {
        throw new WsInvalidQueryException("sourceId in url '" + sourceId 
            + "' is different than in XML submission '" + theSourceId + "'");
      }
      theSourceId = sourceId;
    }
    
    //get the results
    WsGetSubjectsResults wsGetSubjectsResults = new GrouperService(false).getSubjectsLite(
        clientVersionString, theSubjectId, theSourceId, 
        wsRestGetSubjectsLiteRequest.getSubjectIdentifier(), wsRestGetSubjectsLiteRequest.getSearchString(),
        wsRestGetSubjectsLiteRequest.getIncludeSubjectDetail(), wsRestGetSubjectsLiteRequest.getSubjectAttributeNames(),
        wsRestGetSubjectsLiteRequest.getActAsSubjectId(), wsRestGetSubjectsLiteRequest.getActAsSubjectSourceId(),
        wsRestGetSubjectsLiteRequest.getActAsSubjectIdentifier(), wsRestGetSubjectsLiteRequest.getSourceIds(),
        wsRestGetSubjectsLiteRequest.getGroupName(), wsRestGetSubjectsLiteRequest.getGroupUuid(),
        wsRestGetSubjectsLiteRequest.getMemberFilter(), wsRestGetSubjectsLiteRequest.getFieldName(),
        wsRestGetSubjectsLiteRequest.getIncludeGroupDetail(), wsRestGetSubjectsLiteRequest.getParamName0(),
        wsRestGetSubjectsLiteRequest.getParamValue0(), wsRestGetSubjectsLiteRequest.getParamName1(),
        wsRestGetSubjectsLiteRequest.getParamValue1());

    //return result
    return wsGetSubjectsResults;
  
  }
  
  /**
   * get attribute assignments rest
   * @param clientVersion
   * @param wsRestGetAttributesRequest
   * @return the result
   */
  public static WsGetAttributeAssignmentsResults getAttributeAssignments(GrouperVersion clientVersion,
      WsRestGetAttributeAssignmentsRequest wsRestGetAttributesRequest) {
    //cant be null
    wsRestGetAttributesRequest = wsRestGetAttributesRequest == null ? new WsRestGetAttributeAssignmentsRequest()
      : wsRestGetAttributesRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetAttributesRequest.getClientVersion()), false, "clientVersion");

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new GrouperService(false).getAttributeAssignments(
        clientVersionString, wsRestGetAttributesRequest.getAttributeAssignType(), 
        wsRestGetAttributesRequest.getWsAttributeAssignLookups(), wsRestGetAttributesRequest.getWsAttributeDefLookups(), 
        wsRestGetAttributesRequest.getWsAttributeDefNameLookups(), wsRestGetAttributesRequest.getWsOwnerGroupLookups(), 
        wsRestGetAttributesRequest.getWsOwnerStemLookups(), wsRestGetAttributesRequest.getWsOwnerSubjectLookups(), 
        wsRestGetAttributesRequest.getWsOwnerMembershipLookups(), wsRestGetAttributesRequest.getWsOwnerMembershipAnyLookups(), 
        wsRestGetAttributesRequest.getWsOwnerAttributeDefLookups(), wsRestGetAttributesRequest.getActions(), 
        wsRestGetAttributesRequest.getIncludeAssignmentsOnAssignments(), wsRestGetAttributesRequest.getActAsSubjectLookup(), 
        wsRestGetAttributesRequest.getIncludeSubjectDetail(), wsRestGetAttributesRequest.getSubjectAttributeNames(), 
        wsRestGetAttributesRequest.getIncludeGroupDetail(), wsRestGetAttributesRequest.getParams(), 
        wsRestGetAttributesRequest.getEnabled(), 
        wsRestGetAttributesRequest.getAttributeDefValueType(), 
        wsRestGetAttributesRequest.getTheValue(), 
        wsRestGetAttributesRequest.getIncludeAssignmentsFromAssignments(),
        wsRestGetAttributesRequest.getAttributeDefType(),
        wsRestGetAttributesRequest.getWsAssignAssignOwnerAttributeAssignLookups(),
        wsRestGetAttributesRequest.getWsAssignAssignOwnerAttributeDefLookups(), 
        wsRestGetAttributesRequest.getWsAssignAssignOwnerAttributeDefNameLookups(),
        wsRestGetAttributesRequest.getWsAssignAssignOwnerActions());
    
    return wsGetAttributeAssignmentsResults;
  }
  
  /**
   * get attribute assignments rest for one owner (lite)
   * @param clientVersion
   * @param wsRestGetAttributesLiteRequest
   * @return the results object
   */
  public static WsGetAttributeAssignmentsResults getAttributeAssignmentsLite(GrouperVersion clientVersion,
      WsRestGetAttributeAssignmentsLiteRequest wsRestGetAttributesLiteRequest) {

    //cant be null
    wsRestGetAttributesLiteRequest = wsRestGetAttributesLiteRequest == null ? new WsRestGetAttributeAssignmentsLiteRequest() : wsRestGetAttributesLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetAttributesLiteRequest.getClientVersion()), false, "clientVersion");

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new GrouperService(false).getAttributeAssignmentsLite(
        clientVersionString, wsRestGetAttributesLiteRequest.getAttributeAssignType(), wsRestGetAttributesLiteRequest.getAttributeAssignId(), wsRestGetAttributesLiteRequest.getWsAttributeDefName(), 
        wsRestGetAttributesLiteRequest.getWsAttributeDefId(), wsRestGetAttributesLiteRequest.getWsAttributeDefNameName(), wsRestGetAttributesLiteRequest.getWsAttributeDefNameId(), wsRestGetAttributesLiteRequest.getWsOwnerGroupName(), 
        wsRestGetAttributesLiteRequest.getWsOwnerGroupId(), wsRestGetAttributesLiteRequest.getWsOwnerStemName(), wsRestGetAttributesLiteRequest.getWsOwnerStemId(), wsRestGetAttributesLiteRequest.getWsOwnerSubjectId(), 
        wsRestGetAttributesLiteRequest.getWsOwnerSubjectSourceId(), wsRestGetAttributesLiteRequest.getWsOwnerSubjectIdentifier(), wsRestGetAttributesLiteRequest.getWsOwnerMembershipId(), 
        wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnyGroupName(), wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnyGroupId(), wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnySubjectId(), 
        wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnySubjectSourceId(), wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnySubjectIdentifier(), 
        wsRestGetAttributesLiteRequest.getWsOwnerAttributeDefName(), wsRestGetAttributesLiteRequest.getWsOwnerAttributeDefId(), wsRestGetAttributesLiteRequest.getAction(), 
        wsRestGetAttributesLiteRequest.getIncludeAssignmentsOnAssignments(), wsRestGetAttributesLiteRequest.getActAsSubjectId(), wsRestGetAttributesLiteRequest.getActAsSubjectSourceId(), wsRestGetAttributesLiteRequest.getActAsSubjectIdentifier(), 
        wsRestGetAttributesLiteRequest.getIncludeSubjectDetail(), 
        wsRestGetAttributesLiteRequest.getSubjectAttributeNames(), wsRestGetAttributesLiteRequest.getIncludeGroupDetail(), wsRestGetAttributesLiteRequest.getParamName0(), 
        wsRestGetAttributesLiteRequest.getParamValue0(), wsRestGetAttributesLiteRequest.getParamName1(), 
        wsRestGetAttributesLiteRequest.getParamValue1(), wsRestGetAttributesLiteRequest.getEnabled(), 
        wsRestGetAttributesLiteRequest.getAttributeDefValueType(), 
        wsRestGetAttributesLiteRequest.getTheValue(),
        wsRestGetAttributesLiteRequest.getIncludeAssignmentsFromAssignments(),
        wsRestGetAttributesLiteRequest.getAttributeDefType(), wsRestGetAttributesLiteRequest.getWsAssignAssignOwnerAttributeAssignId(), 
        wsRestGetAttributesLiteRequest.getWsAssignAssignOwnerIdOfAttributeDef(), wsRestGetAttributesLiteRequest.getWsAssignAssignOwnerNameOfAttributeDef(),
        wsRestGetAttributesLiteRequest.getWsAssignAssignOwnerIdOfAttributeDefName(), 
        wsRestGetAttributesLiteRequest.getWsAssignAssignOwnerNameOfAttributeDefName(), wsRestGetAttributesLiteRequest.getWsAssignAssignOwnerAction());
    
    return wsGetAttributeAssignmentsResults;
    
  }
  
  /**
   * assign actions to attribute def
   * @param clientVersion
   * @param wsRestAssignAttributeDefActionsRequest
   * @return the result
   */
  public static WsAttributeDefAssignActionResults assignAttributeDefActions(
      GrouperVersion clientVersion,
      WsRestAssignAttributeDefActionsRequest wsRestAssignAttributeDefActionsRequest) {
    //cant be null
    wsRestAssignAttributeDefActionsRequest = wsRestAssignAttributeDefActionsRequest == null ? new WsRestAssignAttributeDefActionsRequest()
        : wsRestAssignAttributeDefActionsRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignAttributeDefActionsRequest
            .getClientVersion()), false, "clientVersion");

    WsAttributeDefAssignActionResults wsAttributeDefAssignActionResults = new GrouperService(
        false).
        assignAttributeDefActions(clientVersionString,
            wsRestAssignAttributeDefActionsRequest.getWsAttributeDefLookup(),
            wsRestAssignAttributeDefActionsRequest.getActions(),
            wsRestAssignAttributeDefActionsRequest.getAssign(),
            wsRestAssignAttributeDefActionsRequest.getReplaceAllExisting(),
            wsRestAssignAttributeDefActionsRequest.getActAsSubjectLookup(),
            wsRestAssignAttributeDefActionsRequest.getParams());

    return wsAttributeDefAssignActionResults;

  }

  /**
   * get attribute assign actions rest
   * @param clientVersion
   * @param wsRestGetAttributeAssignActionsRequest
   * @return the result
   */
  public static WsGetAttributeAssignActionsResults getAttributeAssignActions(
      GrouperVersion clientVersion,
      WsRestGetAttributeAssignActionsRequest wsRestGetAttributeAssignActionsRequest) {
    //cant be null
    wsRestGetAttributeAssignActionsRequest = wsRestGetAttributeAssignActionsRequest == null ? new WsRestGetAttributeAssignActionsRequest()
        : wsRestGetAttributeAssignActionsRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetAttributeAssignActionsRequest
            .getClientVersion()), false, "clientVersion");

    WsGetAttributeAssignActionsResults wsGetAttributeAssignmentsResults = new GrouperService(
        false).
        getAttributeAssignActions(clientVersionString,
            wsRestGetAttributeAssignActionsRequest.getWsAttributeDefLookups(),
            wsRestGetAttributeAssignActionsRequest.getActions(),
            wsRestGetAttributeAssignActionsRequest.getActAsSubjectLookup(),
            wsRestGetAttributeAssignActionsRequest.getParams());

    return wsGetAttributeAssignmentsResults;
  }

  /**
   * get attribute assign actions rest for one attribute definition
   * @param clientVersion
   * @param wsRestGetAttributeAssignActionsLiteRequest
   * @return the results object
   */
  public static WsGetAttributeAssignActionsResults getAttributeAssignActionsLite(
      GrouperVersion clientVersion,
      WsRestGetAttributeAssignActionsLiteRequest wsRestGetAttributeAssignActionsLiteRequest) {

    //can't be null
    wsRestGetAttributeAssignActionsLiteRequest = wsRestGetAttributeAssignActionsLiteRequest == null ?
        new WsRestGetAttributeAssignActionsLiteRequest()
        : wsRestGetAttributeAssignActionsLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetAttributeAssignActionsLiteRequest
            .getClientVersion()), false, "clientVersion");

    WsGetAttributeAssignActionsResults wsGetAttributeAssignActionsResults = new GrouperService(
        false).getAttributeAssignActionsLite(clientVersionString,
        wsRestGetAttributeAssignActionsLiteRequest.getWsNameOfAttributeDef(),
        wsRestGetAttributeAssignActionsLiteRequest.getWsIdOfAttributeDef(),
        wsRestGetAttributeAssignActionsLiteRequest.getWsIdIndexOfAttributeDef(),
        wsRestGetAttributeAssignActionsLiteRequest.getAction(),
        wsRestGetAttributeAssignActionsLiteRequest.getActAsSubjectId(),
        wsRestGetAttributeAssignActionsLiteRequest.getActAsSubjectSourceId(),
        wsRestGetAttributeAssignActionsLiteRequest.getActAsSubjectIdentifier(),
        wsRestGetAttributeAssignActionsLiteRequest.getParamName0(),
        wsRestGetAttributeAssignActionsLiteRequest.getParamValue0(),
        wsRestGetAttributeAssignActionsLiteRequest.getParamName1(),
        wsRestGetAttributeAssignActionsLiteRequest.getParamValue1()
        );

    return wsGetAttributeAssignActionsResults;

  }

  /**
   * assign attributes rest
   * @param clientVersion
   * @param wsRestAssignAttributesRequest
   * @return the result
   */
  public static WsAssignAttributesResults assignAttributes(GrouperVersion clientVersion,
      WsRestAssignAttributesRequest wsRestAssignAttributesRequest) {
    //cant be null
    wsRestAssignAttributesRequest = wsRestAssignAttributesRequest == null ? new WsRestAssignAttributesRequest()
      : wsRestAssignAttributesRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignAttributesRequest.getClientVersion()), false, "clientVersion");
  
    WsAssignAttributesResults wsAssignAttributesResults = new GrouperService(false).assignAttributes(
        clientVersionString, wsRestAssignAttributesRequest.getAttributeAssignType(), 
        wsRestAssignAttributesRequest.getWsAttributeDefNameLookups(), wsRestAssignAttributesRequest.getAttributeAssignOperation(),
        wsRestAssignAttributesRequest.getValues(), wsRestAssignAttributesRequest.getAssignmentNotes(), 
        wsRestAssignAttributesRequest.getAssignmentEnabledTime(), wsRestAssignAttributesRequest.getAssignmentDisabledTime(),
        wsRestAssignAttributesRequest.getDelegatable(), wsRestAssignAttributesRequest.getAttributeAssignValueOperation(),
        wsRestAssignAttributesRequest.getWsAttributeAssignLookups(),
        wsRestAssignAttributesRequest.getWsOwnerGroupLookups(), 
        wsRestAssignAttributesRequest.getWsOwnerStemLookups(), wsRestAssignAttributesRequest.getWsOwnerSubjectLookups(), 
        wsRestAssignAttributesRequest.getWsOwnerMembershipLookups(), wsRestAssignAttributesRequest.getWsOwnerMembershipAnyLookups(), 
        wsRestAssignAttributesRequest.getWsOwnerAttributeDefLookups(), wsRestAssignAttributesRequest.getWsOwnerAttributeAssignLookups(),
        wsRestAssignAttributesRequest.getActions(), 
        wsRestAssignAttributesRequest.getActAsSubjectLookup(), 
        wsRestAssignAttributesRequest.getIncludeSubjectDetail(), wsRestAssignAttributesRequest.getSubjectAttributeNames(), 
        wsRestAssignAttributesRequest.getIncludeGroupDetail(), wsRestAssignAttributesRequest.getParams(),
        wsRestAssignAttributesRequest.getAttributeDefsToReplace(), wsRestAssignAttributesRequest.getActionsToReplace(), 
        wsRestAssignAttributesRequest.getAttributeDefTypesToReplace());
    
    return wsAssignAttributesResults;
  }

  /**
   * assign attributes rest for one owner (lite)
   * @param clientVersion
   * @param wsRestAssignAttributesLiteRequest
   * @return the results object
   */
  public static WsAssignAttributesLiteResults assignAttributesLite(GrouperVersion clientVersion,
      WsRestAssignAttributesLiteRequest wsRestAssignAttributesLiteRequest) {
  
    //cant be null
    wsRestAssignAttributesLiteRequest = wsRestAssignAttributesLiteRequest == null ? new WsRestAssignAttributesLiteRequest() : wsRestAssignAttributesLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignAttributesLiteRequest.getClientVersion()), false, "clientVersion");
  
    WsAssignAttributesLiteResults wsAssignAttributesLiteResults = new GrouperService(false).assignAttributesLite(
        clientVersionString, wsRestAssignAttributesLiteRequest.getAttributeAssignType(), 
        wsRestAssignAttributesLiteRequest.getWsAttributeDefNameName(), wsRestAssignAttributesLiteRequest.getWsAttributeDefNameId(), 
        wsRestAssignAttributesLiteRequest.getAttributeAssignOperation(), wsRestAssignAttributesLiteRequest.getValueId(),
        wsRestAssignAttributesLiteRequest.getValueSystem(), wsRestAssignAttributesLiteRequest.getValueFormatted(),
        wsRestAssignAttributesLiteRequest.getAssignmentNotes(), wsRestAssignAttributesLiteRequest.getAssignmentEnabledTime(),
        wsRestAssignAttributesLiteRequest.getAssignmentDisabledTime(), wsRestAssignAttributesLiteRequest.getDelegatable(), 
        wsRestAssignAttributesLiteRequest.getAttributeAssignValueOperation(), wsRestAssignAttributesLiteRequest.getAttributeAssignId(),
        wsRestAssignAttributesLiteRequest.getWsOwnerGroupName(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerGroupId(), wsRestAssignAttributesLiteRequest.getWsOwnerStemName(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerStemId(), wsRestAssignAttributesLiteRequest.getWsOwnerSubjectId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerSubjectSourceId(), wsRestAssignAttributesLiteRequest.getWsOwnerSubjectIdentifier(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnyGroupName(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnyGroupId(), wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnySubjectId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnySubjectSourceId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnySubjectIdentifier(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerAttributeDefName(), wsRestAssignAttributesLiteRequest.getWsOwnerAttributeDefId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerAttributeAssignId(), wsRestAssignAttributesLiteRequest.getAction(), 
        wsRestAssignAttributesLiteRequest.getActAsSubjectId(), 
        wsRestAssignAttributesLiteRequest.getActAsSubjectSourceId(), wsRestAssignAttributesLiteRequest.getActAsSubjectIdentifier(), 
        wsRestAssignAttributesLiteRequest.getIncludeSubjectDetail(), 
        wsRestAssignAttributesLiteRequest.getSubjectAttributeNames(), wsRestAssignAttributesLiteRequest.getIncludeGroupDetail(), 
        wsRestAssignAttributesLiteRequest.getParamName0(), 
        wsRestAssignAttributesLiteRequest.getParamValue0(), wsRestAssignAttributesLiteRequest.getParamName1(), 
        wsRestAssignAttributesLiteRequest.getParamValue1());
    
    return wsAssignAttributesLiteResults;
  }

  /**
   * get permission assignments rest
   * @param clientVersion
   * @param wsRestGetPermissionAssignmentsRequest
   * @return the result
   */
  public static WsGetPermissionAssignmentsResults getPermissionAssignments(GrouperVersion clientVersion,
      WsRestGetPermissionAssignmentsRequest wsRestGetPermissionAssignmentsRequest) {
    //cant be null
    wsRestGetPermissionAssignmentsRequest = wsRestGetPermissionAssignmentsRequest == null ? 
        new WsRestGetPermissionAssignmentsRequest()
      : wsRestGetPermissionAssignmentsRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetPermissionAssignmentsRequest.getClientVersion()), false, "clientVersion");
  
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = new GrouperService(false).getPermissionAssignments(
        clientVersionString, wsRestGetPermissionAssignmentsRequest.getWsAttributeDefLookups(), 
        wsRestGetPermissionAssignmentsRequest.getWsAttributeDefNameLookups(), wsRestGetPermissionAssignmentsRequest.getRoleLookups(), 
        wsRestGetPermissionAssignmentsRequest.getWsSubjectLookups(), wsRestGetPermissionAssignmentsRequest.getActions(), 
        wsRestGetPermissionAssignmentsRequest.getIncludePermissionAssignDetail(), wsRestGetPermissionAssignmentsRequest.getIncludeAttributeDefNames(),
        wsRestGetPermissionAssignmentsRequest.getIncludeAttributeAssignments(),
        wsRestGetPermissionAssignmentsRequest.getIncludeAssignmentsOnAssignments(), wsRestGetPermissionAssignmentsRequest.getActAsSubjectLookup(), 
        wsRestGetPermissionAssignmentsRequest.getIncludeSubjectDetail(), wsRestGetPermissionAssignmentsRequest.getSubjectAttributeNames(), 
        wsRestGetPermissionAssignmentsRequest.getIncludeGroupDetail(), wsRestGetPermissionAssignmentsRequest.getParams(), 
        wsRestGetPermissionAssignmentsRequest.getEnabled(),
        wsRestGetPermissionAssignmentsRequest.getPointInTimeFrom(), wsRestGetPermissionAssignmentsRequest.getPointInTimeTo(),
        wsRestGetPermissionAssignmentsRequest.getImmediateOnly(), wsRestGetPermissionAssignmentsRequest.getPermissionType(),
        wsRestGetPermissionAssignmentsRequest.getPermissionProcessor(), wsRestGetPermissionAssignmentsRequest.getLimitEnvVars(),
        wsRestGetPermissionAssignmentsRequest.getIncludeLimits());
    
    return wsGetPermissionAssignmentsResults;
  }

  /**
   * get permission assignments rest for one owner (lite)
   * @param clientVersion
   * @param wsRestGetPermissionAssignmentsLiteRequest
   * @return the results object
   */
  public static WsGetPermissionAssignmentsResults getPermissionAssignmentsLite(GrouperVersion clientVersion,
      WsRestGetPermissionAssignmentsLiteRequest wsRestGetPermissionAssignmentsLiteRequest) {
  
    //cant be null
    wsRestGetPermissionAssignmentsLiteRequest = wsRestGetPermissionAssignmentsLiteRequest == null ? new WsRestGetPermissionAssignmentsLiteRequest() : wsRestGetPermissionAssignmentsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestGetPermissionAssignmentsLiteRequest.getClientVersion()), false, "clientVersion");
  
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = new GrouperService(false)
      .getPermissionAssignmentsLite(
        clientVersionString, wsRestGetPermissionAssignmentsLiteRequest.getWsAttributeDefName(), 
        wsRestGetPermissionAssignmentsLiteRequest.getWsAttributeDefId(), wsRestGetPermissionAssignmentsLiteRequest.getWsAttributeDefNameName(), 
        wsRestGetPermissionAssignmentsLiteRequest.getWsAttributeDefNameId(), wsRestGetPermissionAssignmentsLiteRequest.getRoleName(), 
        wsRestGetPermissionAssignmentsLiteRequest.getRoleId(), wsRestGetPermissionAssignmentsLiteRequest.getWsSubjectId(), 
        wsRestGetPermissionAssignmentsLiteRequest.getWsSubjectSourceId(), wsRestGetPermissionAssignmentsLiteRequest.getWsSubjectIdentifier(), 
        wsRestGetPermissionAssignmentsLiteRequest.getAction(), 
        wsRestGetPermissionAssignmentsLiteRequest.getIncludePermissionAssignDetail(), wsRestGetPermissionAssignmentsLiteRequest.getIncludeAttributeDefNames(),
        wsRestGetPermissionAssignmentsLiteRequest.getIncludeAttributeAssignments(), 
        wsRestGetPermissionAssignmentsLiteRequest.getIncludeAssignmentsOnAssignments(), wsRestGetPermissionAssignmentsLiteRequest.getActAsSubjectId(), 
        wsRestGetPermissionAssignmentsLiteRequest.getActAsSubjectSourceId(), wsRestGetPermissionAssignmentsLiteRequest.getActAsSubjectIdentifier(), 
        wsRestGetPermissionAssignmentsLiteRequest.getIncludeSubjectDetail(), 
        wsRestGetPermissionAssignmentsLiteRequest.getSubjectAttributeNames(), wsRestGetPermissionAssignmentsLiteRequest.getIncludeGroupDetail(), 
        wsRestGetPermissionAssignmentsLiteRequest.getParamName0(), 
        wsRestGetPermissionAssignmentsLiteRequest.getParamValue0(), wsRestGetPermissionAssignmentsLiteRequest.getParamName1(), 
        wsRestGetPermissionAssignmentsLiteRequest.getParamValue1(), wsRestGetPermissionAssignmentsLiteRequest.getEnabled(),
        wsRestGetPermissionAssignmentsLiteRequest.getPointInTimeFrom(), 
        wsRestGetPermissionAssignmentsLiteRequest.getPointInTimeTo(),
        wsRestGetPermissionAssignmentsLiteRequest.getImmediateOnly(), wsRestGetPermissionAssignmentsLiteRequest.getPermissionType(),
        wsRestGetPermissionAssignmentsLiteRequest.getPermissionProcessor(), 
        wsRestGetPermissionAssignmentsLiteRequest.getLimitEnvVarName0(),
        wsRestGetPermissionAssignmentsLiteRequest.getLimitEnvVarValue0(),
        wsRestGetPermissionAssignmentsLiteRequest.getLimitEnvVarType0(),
        wsRestGetPermissionAssignmentsLiteRequest.getLimitEnvVarName1(),
        wsRestGetPermissionAssignmentsLiteRequest.getLimitEnvVarValue1(),
        wsRestGetPermissionAssignmentsLiteRequest.getLimitEnvVarType1(), 
        wsRestGetPermissionAssignmentsLiteRequest.getIncludeLimits());
    
    return wsGetPermissionAssignmentsResults;
    
  }

  /**
   * assign permissions rest
   * @param clientVersion
   * @param wsRestAssignPermissionsRequest
   * @return the result
   */
  public static WsAssignPermissionsResults assignPermissions(GrouperVersion clientVersion,
      WsRestAssignPermissionsRequest wsRestAssignPermissionsRequest) {
    //cant be null
    wsRestAssignPermissionsRequest = wsRestAssignPermissionsRequest == null ? new WsRestAssignPermissionsRequest()
      : wsRestAssignPermissionsRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignPermissionsRequest.getClientVersion()), false, "clientVersion");
  
    WsAssignPermissionsResults wsAssignPermissionsResults = new GrouperService(false).assignPermissions(
        clientVersionString, wsRestAssignPermissionsRequest.getPermissionType(),
        wsRestAssignPermissionsRequest.getPermissionDefNameLookups(), wsRestAssignPermissionsRequest.getPermissionAssignOperation(),
        wsRestAssignPermissionsRequest.getAssignmentNotes(), 
        wsRestAssignPermissionsRequest.getAssignmentEnabledTime(), wsRestAssignPermissionsRequest.getAssignmentDisabledTime(),
        wsRestAssignPermissionsRequest.getDelegatable(), 
        wsRestAssignPermissionsRequest.getWsAttributeAssignLookups(),
        wsRestAssignPermissionsRequest.getRoleLookups(), 
        wsRestAssignPermissionsRequest.getSubjectRoleLookups(),
        wsRestAssignPermissionsRequest.getActions(), 
        wsRestAssignPermissionsRequest.getActAsSubjectLookup(), 
        wsRestAssignPermissionsRequest.getIncludeSubjectDetail(), wsRestAssignPermissionsRequest.getSubjectAttributeNames(), 
        wsRestAssignPermissionsRequest.getIncludeGroupDetail(), wsRestAssignPermissionsRequest.getParams(),
        wsRestAssignPermissionsRequest.getAttributeDefsToReplace(), wsRestAssignPermissionsRequest.getActionsToReplace(),
        wsRestAssignPermissionsRequest.getDisallowed());
    
    return wsAssignPermissionsResults;
  }

  /**
   * assign attributes rest for one owner (lite)
   * @param clientVersion
   * @param wsRestAssignPermissionsLiteRequest
   * @return the results object
   */
  public static WsAssignPermissionsLiteResults assignPermissionsLite(GrouperVersion clientVersion,
      WsRestAssignPermissionsLiteRequest wsRestAssignPermissionsLiteRequest) {
  
    //cant be null
    wsRestAssignPermissionsLiteRequest = wsRestAssignPermissionsLiteRequest == null ? new WsRestAssignPermissionsLiteRequest() : wsRestAssignPermissionsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignPermissionsLiteRequest.getClientVersion()), false, "clientVersion");
  
    WsAssignPermissionsLiteResults wsAssignPermissionsLiteResults = new GrouperService(false).assignPermissionsLite(
        clientVersionString, wsRestAssignPermissionsLiteRequest.getPermissionType(), 
        wsRestAssignPermissionsLiteRequest.getPermissionDefNameName(), wsRestAssignPermissionsLiteRequest.getPermissionDefNameId(), 
        wsRestAssignPermissionsLiteRequest.getPermissionAssignOperation(), 
        wsRestAssignPermissionsLiteRequest.getAssignmentNotes(), wsRestAssignPermissionsLiteRequest.getAssignmentEnabledTime(),
        wsRestAssignPermissionsLiteRequest.getAssignmentDisabledTime(), wsRestAssignPermissionsLiteRequest.getDelegatable(), 
        wsRestAssignPermissionsLiteRequest.getAttributeAssignId(),
        wsRestAssignPermissionsLiteRequest.getRoleName(), 
        wsRestAssignPermissionsLiteRequest.getRoleId(),  
        wsRestAssignPermissionsLiteRequest.getSubjectRoleName(), 
        wsRestAssignPermissionsLiteRequest.getSubjectRoleId(), wsRestAssignPermissionsLiteRequest.getSubjectRoleSubjectId(), 
        wsRestAssignPermissionsLiteRequest.getSubjectRoleSubjectSourceId(), 
        wsRestAssignPermissionsLiteRequest.getSubjectRoleSubjectIdentifier(), 
        wsRestAssignPermissionsLiteRequest.getAction(), 
        wsRestAssignPermissionsLiteRequest.getActAsSubjectId(), 
        wsRestAssignPermissionsLiteRequest.getActAsSubjectSourceId(), wsRestAssignPermissionsLiteRequest.getActAsSubjectIdentifier(), 
        wsRestAssignPermissionsLiteRequest.getIncludeSubjectDetail(), 
        wsRestAssignPermissionsLiteRequest.getSubjectAttributeNames(), wsRestAssignPermissionsLiteRequest.getIncludeGroupDetail(), 
        wsRestAssignPermissionsLiteRequest.getParamName0(), 
        wsRestAssignPermissionsLiteRequest.getParamValue0(), wsRestAssignPermissionsLiteRequest.getParamName1(), 
        wsRestAssignPermissionsLiteRequest.getParamValue1(), wsRestAssignPermissionsLiteRequest.getDisallowed());
    
    return wsAssignPermissionsLiteResults;
  }
  
  /**
   * save an AttributeDef or many (insert or update).  Note, you cannot rename an existing AttributeDef.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestAttributeDefSaveRequest 
   * @return the results
   */

  public static WsAttributeDefSaveResults attributeDefSave(GrouperVersion clientVersion,
      WsRestAttributeDefSaveRequest wsRestAttributeDefSaveRequest) {

    //cant be null
    wsRestAttributeDefSaveRequest = wsRestAttributeDefSaveRequest == null ? new WsRestAttributeDefSaveRequest()
        : wsRestAttributeDefSaveRequest;

    String clientVersionString = GrouperServiceUtils
        .pickOne(clientVersion.toString(),
            GrouperVersion.stringValueOrNull(wsRestAttributeDefSaveRequest
                .getClientVersion()), false, "clientVersion");

    WsAttributeDefSaveResults wsAttributeDefSaveResults = new GrouperService(false)
        .attributeDefSave(
            clientVersionString,
            wsRestAttributeDefSaveRequest.getWsAttributeDefsToSave(),
            wsRestAttributeDefSaveRequest.getActAsSubjectLookup(),
            wsRestAttributeDefSaveRequest.getTxType(),
            wsRestAttributeDefSaveRequest.getParams());

    return wsAttributeDefSaveResults;

  }

  /**
   * save an AttributeDef (insert or update).  Note you cannot currently move an existing AttributeDef.
   * 
   * @see {@link AttributeDefSave#save()}
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestAttributeDefSaveLiteRequest 
   * @param attributeDefLookupName
   * @return the result of one attribute def save
   */
  public static WsAttributeDefSaveLiteResult attributeDefSaveLite(
      GrouperVersion clientVersion, String attributeDefLookupName,
      WsRestAttributeDefSaveLiteRequest wsRestAttributeDefSaveLiteRequest) {

    //cant be null
    wsRestAttributeDefSaveLiteRequest = wsRestAttributeDefSaveLiteRequest == null ? new WsRestAttributeDefSaveLiteRequest()
        : wsRestAttributeDefSaveLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAttributeDefSaveLiteRequest
            .getClientVersion()), false, "clientVersion");

    attributeDefLookupName = GrouperServiceUtils.pickOne(attributeDefLookupName,
        wsRestAttributeDefSaveLiteRequest.getAttributeDefLookupName(), true,
        "attributeDefLookupName");

    WsAttributeDefSaveLiteResult wsAttributeDefSaveLiteResult = new GrouperService(false)
        .attributeDefSaveLite(
            clientVersionString,
            wsRestAttributeDefSaveLiteRequest.getAttributeDefLookupUuid(),
            attributeDefLookupName,
            wsRestAttributeDefSaveLiteRequest.getUuidOfAttributeDef(),
            wsRestAttributeDefSaveLiteRequest.getNameOfAttributeDef(),
            wsRestAttributeDefSaveLiteRequest.getAssignToAttributeDef(),
            wsRestAttributeDefSaveLiteRequest.getAssignToAttributeDefAssignment(),
            wsRestAttributeDefSaveLiteRequest.getAssignToEffectiveMembership(),
            wsRestAttributeDefSaveLiteRequest.getAssignToEffectiveMembershipAssignment(),
            wsRestAttributeDefSaveLiteRequest.getAssignToGroup(),
            wsRestAttributeDefSaveLiteRequest.getAssignToGroupAssignment(),
            wsRestAttributeDefSaveLiteRequest.getAssignToImmediateMembership(),
            wsRestAttributeDefSaveLiteRequest.getAssignToImmediateMembershipAssignment(),
            wsRestAttributeDefSaveLiteRequest.getAssignToMember(),
            wsRestAttributeDefSaveLiteRequest.getAssignToMemberAssignment(),
            wsRestAttributeDefSaveLiteRequest.getAssignToStem(),
            wsRestAttributeDefSaveLiteRequest.getAssignToStemAssignment(),
            wsRestAttributeDefSaveLiteRequest.getAttributeDefType(),
            wsRestAttributeDefSaveLiteRequest.getMultiAssignable(),
            wsRestAttributeDefSaveLiteRequest.getMultiValued(),
            wsRestAttributeDefSaveLiteRequest.getValueType(),
            wsRestAttributeDefSaveLiteRequest.getDescription(),
            wsRestAttributeDefSaveLiteRequest.getSaveMode(),
            wsRestAttributeDefSaveLiteRequest.getCreateParentStemsIfNotExist(),
            wsRestAttributeDefSaveLiteRequest.getActAsSubjectId(),
            wsRestAttributeDefSaveLiteRequest.getActAsSubjectSourceId(),
            wsRestAttributeDefSaveLiteRequest.getActAsSubjectIdentifier(),
            wsRestAttributeDefSaveLiteRequest.getParamName0(),
            wsRestAttributeDefSaveLiteRequest.getParamValue0(),
            wsRestAttributeDefSaveLiteRequest.getParamName1(),
            wsRestAttributeDefSaveLiteRequest.getParamValue1());

    return wsAttributeDefSaveLiteResult;

  }
  
  /**
   * <pre>
   * based on a submitted object of type WsRestAttributeDefDeleteRequest, delete the attributeDefs.  e.g. url:
   * /v1_3_000/attributeDefs
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestAttributeDefDeleteRequest is the request body converted to an object
   * @return the result
   */
  public static WsAttributeDefDeleteResults attributeDefDelete(
      GrouperVersion clientVersion,
      WsRestAttributeDefDeleteRequest wsRestAttributeDefDeleteRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestAttributeDefDeleteRequest != null,
        "Body of request must contain an instance of "
            + WsRestAttributeDefDeleteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAttributeDefDeleteRequest
            .getClientVersion()), false, "clientVersion");

    //get the results
    WsAttributeDefDeleteResults wsAttributeDefDeleteResults = new GrouperService(false)
        .attributeDefDelete(
            clientVersionString,
            wsRestAttributeDefDeleteRequest.getWsAttributeDefLookups(),
            wsRestAttributeDefDeleteRequest.getActAsSubjectLookup(),
            wsRestAttributeDefDeleteRequest
                .getTxType(), wsRestAttributeDefDeleteRequest.getParams());

    //return result
    return wsAttributeDefDeleteResults;

  }

  /**
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestAttributeDefDeleteLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsAttributeDefDeleteLiteResult attributeDefDeleteLite(
      GrouperVersion clientVersion,
      WsRestAttributeDefDeleteLiteRequest wsRestAttributeDefDeleteLiteRequest) {

    //make sure not null
    wsRestAttributeDefDeleteLiteRequest = wsRestAttributeDefDeleteLiteRequest == null ? new WsRestAttributeDefDeleteLiteRequest()
        : wsRestAttributeDefDeleteLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAttributeDefDeleteLiteRequest
            .getClientVersion()), false, "clientVersion");

    //get the results
    WsAttributeDefDeleteLiteResult wsAttributeDefDeleteLiteResult = new GrouperService(
        false)
        .attributeDefDeleteLite(clientVersionString, wsRestAttributeDefDeleteLiteRequest
            .getNameOfAttributeDef(),
            wsRestAttributeDefDeleteLiteRequest.getIdOfAttributeDef(),
            wsRestAttributeDefDeleteLiteRequest.getIdIndexOfAttributeDef(),
            wsRestAttributeDefDeleteLiteRequest.getActAsSubjectId(),
            wsRestAttributeDefDeleteLiteRequest.getActAsSubjectSourceId(),
            wsRestAttributeDefDeleteLiteRequest.getActAsSubjectIdentifier(),
            wsRestAttributeDefDeleteLiteRequest.getParamName0(),
            wsRestAttributeDefDeleteLiteRequest
                .getParamValue0(), wsRestAttributeDefDeleteLiteRequest.getParamName1(),
            wsRestAttributeDefDeleteLiteRequest.getParamValue1());

    //return result
    return wsAttributeDefDeleteLiteResult;

  }
  
  /**
   * find an attribute def or attribute defs.  Each additional parameter sent will narow the search,
   * except the lookups will just lookup whatever is sent.
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestFindAttributeDefsRequest 
   * @return the attribute defs, or no attribute def if none found
   */
  public static WsFindAttributeDefsResults findAttributeDefs(
      GrouperVersion clientVersion,
      WsRestFindAttributeDefsRequest wsRestFindAttributeDefsRequest) {

    //cant be null
    wsRestFindAttributeDefsRequest = wsRestFindAttributeDefsRequest == null ? new WsRestFindAttributeDefsRequest()
        : wsRestFindAttributeDefsRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindAttributeDefsRequest
            .getClientVersion()), false, "clientVersion");

    WsFindAttributeDefsResults wsFindAttributeDefsResults = new GrouperService(false)
        .findAttributeDefs(
            clientVersionString, wsRestFindAttributeDefsRequest.getScope(),
            wsRestFindAttributeDefsRequest.getSplitScope(),
            wsRestFindAttributeDefsRequest.getWsAttributeDefLookups(),
            wsRestFindAttributeDefsRequest.getPrivilegeName(),
            wsRestFindAttributeDefsRequest.getStemScope(),
            wsRestFindAttributeDefsRequest.getParentStemId(),
            wsRestFindAttributeDefsRequest.getFindByUuidOrName(),
            wsRestFindAttributeDefsRequest.getPageSize(),
            wsRestFindAttributeDefsRequest.getPageNumber(),
            wsRestFindAttributeDefsRequest.getSortString(),
            wsRestFindAttributeDefsRequest.getAscending(),
            wsRestFindAttributeDefsRequest.getActAsSubjectLookup(),
            wsRestFindAttributeDefsRequest.getParams());

    return wsFindAttributeDefsResults;
  }

  /**
   * find an attribute def  or attribute defs.  Each additional parameter sent will narow the search,
   * except the lookups will just lookup whatever is sent.
   * @param clientVersion 
   * @param wsRestFindAttributeDefsLiteRequest 
   * @return the attribute defs, or no attribute def if none found
   */
  public static WsFindAttributeDefsResults findAttributeDefsLite(
      GrouperVersion clientVersion,
      WsRestFindAttributeDefsLiteRequest wsRestFindAttributeDefsLiteRequest) {

    //cant be null
    wsRestFindAttributeDefsLiteRequest = wsRestFindAttributeDefsLiteRequest == null ?
        new WsRestFindAttributeDefsLiteRequest() : wsRestFindAttributeDefsLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindAttributeDefsLiteRequest
            .getClientVersion()), false, "clientVersion");

    WsFindAttributeDefsResults wsFindAttributeDefsResults = new GrouperService(false)
        .findAttributeDefsLite(
            clientVersionString, wsRestFindAttributeDefsLiteRequest.getScope(),
            wsRestFindAttributeDefsLiteRequest.getSplitScope(),
            wsRestFindAttributeDefsLiteRequest.getUuidOfAttributeDef(),
            wsRestFindAttributeDefsLiteRequest.getNameOfAttributeDef(),
            wsRestFindAttributeDefsLiteRequest.getIdIndexOfAttributeDef(),
            wsRestFindAttributeDefsLiteRequest.getPrivilegeName(),
            wsRestFindAttributeDefsLiteRequest.getStemScope(),
            wsRestFindAttributeDefsLiteRequest.getParentStemId(),
            wsRestFindAttributeDefsLiteRequest.getFindByUuidOrName(),
            wsRestFindAttributeDefsLiteRequest.getPageSize(),
            wsRestFindAttributeDefsLiteRequest.getPageNumber(),
            wsRestFindAttributeDefsLiteRequest.getSortString(),
            wsRestFindAttributeDefsLiteRequest.getAscending(),
            wsRestFindAttributeDefsLiteRequest.getActAsSubjectId(),
            wsRestFindAttributeDefsLiteRequest.getActAsSubjectSourceId(),
            wsRestFindAttributeDefsLiteRequest.getActAsSubjectIdentifier(),
            wsRestFindAttributeDefsLiteRequest.getParamName0(),
            wsRestFindAttributeDefsLiteRequest.getParamValue0(),
            wsRestFindAttributeDefsLiteRequest.getParamName1(),
            wsRestFindAttributeDefsLiteRequest.getParamValue1()
        );

    return wsFindAttributeDefsResults;

  }

  /**
   * assign or unassign attribute def name permission inheritance
   * @param clientVersion 
   * @param wsRestAssignAttributeDefNameInheritanceRequest 
   * @return the result
   */
  public static  WsAssignAttributeDefNameInheritanceResults assignAttributeDefNameInheritance(GrouperVersion clientVersion, 
      WsRestAssignAttributeDefNameInheritanceRequest wsRestAssignAttributeDefNameInheritanceRequest) {
    //cant be null
    wsRestAssignAttributeDefNameInheritanceRequest = wsRestAssignAttributeDefNameInheritanceRequest == null ? 
        new WsRestAssignAttributeDefNameInheritanceRequest() : wsRestAssignAttributeDefNameInheritanceRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignAttributeDefNameInheritanceRequest.getClientVersion()), false, "clientVersion");
  
    WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = new GrouperService(false).assignAttributeDefNameInheritance(
        clientVersionString, wsRestAssignAttributeDefNameInheritanceRequest.getWsAttributeDefNameLookup(), 
        wsRestAssignAttributeDefNameInheritanceRequest.getRelatedWsAttributeDefNameLookups(), wsRestAssignAttributeDefNameInheritanceRequest.getAssign(),
        wsRestAssignAttributeDefNameInheritanceRequest.getReplaceAllExisting(), wsRestAssignAttributeDefNameInheritanceRequest.getActAsSubjectLookup(),
        wsRestAssignAttributeDefNameInheritanceRequest.getTxType(), wsRestAssignAttributeDefNameInheritanceRequest.getParams());
    
    return wsAssignAttributeDefNameInheritanceResults;

  }

  /**
   * assign or unassign attribute def name permission inheritance
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestAssignAttributeDefNameInheritanceLiteRequest
   * @return the result
   */
  public static WsAssignAttributeDefNameInheritanceResults assignAttributeDefNameInheritanceLite(GrouperVersion clientVersion,
      WsRestAssignAttributeDefNameInheritanceLiteRequest wsRestAssignAttributeDefNameInheritanceLiteRequest) {
    //cant be null
    wsRestAssignAttributeDefNameInheritanceLiteRequest = wsRestAssignAttributeDefNameInheritanceLiteRequest == null ? 
        new WsRestAssignAttributeDefNameInheritanceLiteRequest() : wsRestAssignAttributeDefNameInheritanceLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignAttributeDefNameInheritanceLiteRequest.getClientVersion()), false, "clientVersion");
  
    WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameInheritanceResults = new GrouperService(false).assignAttributeDefNameInheritanceLite(
        clientVersionString, wsRestAssignAttributeDefNameInheritanceLiteRequest.getAttributeDefNameUuid(), 
        wsRestAssignAttributeDefNameInheritanceLiteRequest.getAttributeDefNameName(), wsRestAssignAttributeDefNameInheritanceLiteRequest.getRelatedAttributeDefNameUuid(),
        wsRestAssignAttributeDefNameInheritanceLiteRequest.getRelatedAttributeDefNameName(), wsRestAssignAttributeDefNameInheritanceLiteRequest.getAssign(),
        wsRestAssignAttributeDefNameInheritanceLiteRequest.getActAsSubjectId(), wsRestAssignAttributeDefNameInheritanceLiteRequest.getActAsSubjectSourceId(),
        wsRestAssignAttributeDefNameInheritanceLiteRequest.getActAsSubjectIdentifier(), wsRestAssignAttributeDefNameInheritanceLiteRequest.getParamName0(),
        wsRestAssignAttributeDefNameInheritanceLiteRequest.getParamValue0(), wsRestAssignAttributeDefNameInheritanceLiteRequest.getParamName1(),
        wsRestAssignAttributeDefNameInheritanceLiteRequest.getParamValue1());
    
    return wsAssignAttributeDefNameInheritanceResults;
  }

  /**
   * delete an AttributeDefName or many.  Note, you cannot rename an existing AttributeDefName.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestAttributeDefNameDeleteRequest 
   * @return the results
   */
  public static WsAttributeDefNameDeleteResults attributeDefNameDelete(GrouperVersion clientVersion,
      WsRestAttributeDefNameDeleteRequest wsRestAttributeDefNameDeleteRequest) {
  
    //cant be null
    wsRestAttributeDefNameDeleteRequest = wsRestAttributeDefNameDeleteRequest == null ? 
        new WsRestAttributeDefNameDeleteRequest() : wsRestAttributeDefNameDeleteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAttributeDefNameDeleteRequest.getClientVersion()), false, "clientVersion");
  
    WsAttributeDefNameDeleteResults wsAttributeDefNameDeleteResults = new GrouperService(false).attributeDefNameDelete(
        clientVersionString, wsRestAttributeDefNameDeleteRequest.getWsAttributeDefNameLookups(), wsRestAttributeDefNameDeleteRequest.getActAsSubjectLookup(),
        wsRestAttributeDefNameDeleteRequest.getTxType(), wsRestAttributeDefNameDeleteRequest.getParams());
    
    return wsAttributeDefNameDeleteResults;
    
  }

  /**
   * delete an AttributeDefName
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param attributeDefName 
   * @param wsRestAttributeDefNameDeleteLiteRequest 
   * @return the result of one member add
   */
  public static WsAttributeDefNameDeleteLiteResult attributeDefNameDeleteLite(GrouperVersion clientVersion, String attributeDefName,
    WsRestAttributeDefNameDeleteLiteRequest wsRestAttributeDefNameDeleteLiteRequest) {
  
    //cant be null
    wsRestAttributeDefNameDeleteLiteRequest = wsRestAttributeDefNameDeleteLiteRequest == null ? 
        new WsRestAttributeDefNameDeleteLiteRequest() : wsRestAttributeDefNameDeleteLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAttributeDefNameDeleteLiteRequest.getClientVersion()), false, "clientVersion");
  
    attributeDefName = GrouperServiceUtils.pickOne(attributeDefName, wsRestAttributeDefNameDeleteLiteRequest
        .getAttributeDefNameName(), true, "attributeDefName");

    WsAttributeDefNameDeleteLiteResult wsAttributeDefNameDeleteLiteResult = new GrouperService(false).attributeDefNameDeleteLite(
        clientVersionString, wsRestAttributeDefNameDeleteLiteRequest.getAttributeDefNameUuid(), 
        attributeDefName, wsRestAttributeDefNameDeleteLiteRequest.getActAsSubjectId(), 
        wsRestAttributeDefNameDeleteLiteRequest.getActAsSubjectSourceId(), wsRestAttributeDefNameDeleteLiteRequest.getActAsSubjectIdentifier(),
        wsRestAttributeDefNameDeleteLiteRequest.getParamName0(),
        wsRestAttributeDefNameDeleteLiteRequest.getParamValue0(), wsRestAttributeDefNameDeleteLiteRequest.getParamName1(),
        wsRestAttributeDefNameDeleteLiteRequest.getParamValue1() );
    
    return wsAttributeDefNameDeleteLiteResult;
  }
  

  /**
   * save an AttributeDefName or many (insert or update).  Note, you cannot rename an existing AttributeDefName.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestAttributeDefNameSaveRequest 
   * @return the results
   */
  
  public static WsAttributeDefNameSaveResults attributeDefNameSave(GrouperVersion clientVersion,
      WsRestAttributeDefNameSaveRequest wsRestAttributeDefNameSaveRequest) {
  
    //cant be null
    wsRestAttributeDefNameSaveRequest = wsRestAttributeDefNameSaveRequest == null ? new WsRestAttributeDefNameSaveRequest() : wsRestAttributeDefNameSaveRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAttributeDefNameSaveRequest.getClientVersion()), false, "clientVersion");
  
    WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults = new GrouperService(false).attributeDefNameSave(
        clientVersionString, wsRestAttributeDefNameSaveRequest.getWsAttributeDefNameToSaves(), 
        wsRestAttributeDefNameSaveRequest.getActAsSubjectLookup(), 
        wsRestAttributeDefNameSaveRequest.getTxType(), 
        wsRestAttributeDefNameSaveRequest.getParams());
    
    return wsAttributeDefNameSaveResults;
    
  }

  /**
   * save an AttributeDefName (insert or update).  Note you cannot currently move an existing AttributeDefName.
   * 
   * @see {@link AttributeDefNameSave#save()}
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestAttributeDefNameSaveLiteRequest 
   * @param attributeDefNameLookupName
   * @return the result of one member add
   */
  public static WsAttributeDefNameSaveLiteResult attributeDefNameSaveLite(GrouperVersion clientVersion, String attributeDefNameLookupName,
      WsRestAttributeDefNameSaveLiteRequest wsRestAttributeDefNameSaveLiteRequest) {

    //cant be null
    wsRestAttributeDefNameSaveLiteRequest = wsRestAttributeDefNameSaveLiteRequest == null ? new WsRestAttributeDefNameSaveLiteRequest() : wsRestAttributeDefNameSaveLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAttributeDefNameSaveLiteRequest.getClientVersion()), false, "clientVersion");
  
    attributeDefNameLookupName = GrouperServiceUtils.pickOne(attributeDefNameLookupName,
        wsRestAttributeDefNameSaveLiteRequest.getAttributeDefNameLookupName(), true, "attributeDefNameLookupName");

    
    WsAttributeDefNameSaveLiteResult wsAttributeDefNameSaveLiteResult = new GrouperService(false).attributeDefNameSaveLite(
        clientVersionString, wsRestAttributeDefNameSaveLiteRequest.getAttributeDefNameLookupUuid(), 
        attributeDefNameLookupName, 
        wsRestAttributeDefNameSaveLiteRequest.getAttributeDefLookupUuid(), 
        wsRestAttributeDefNameSaveLiteRequest.getAttributeDefLookupName(), 
        wsRestAttributeDefNameSaveLiteRequest.getAttributeDefNameUuid(), 
        wsRestAttributeDefNameSaveLiteRequest.getAttributeDefNameName(), 
        wsRestAttributeDefNameSaveLiteRequest.getDisplayExtension(), 
        wsRestAttributeDefNameSaveLiteRequest.getDescription(), 
        wsRestAttributeDefNameSaveLiteRequest.getSaveMode(), 
        wsRestAttributeDefNameSaveLiteRequest.getCreateParentStemsIfNotExist(), 
        wsRestAttributeDefNameSaveLiteRequest.getActAsSubjectId(), 
        wsRestAttributeDefNameSaveLiteRequest.getActAsSubjectSourceId(), 
        wsRestAttributeDefNameSaveLiteRequest.getActAsSubjectIdentifier(), 
        wsRestAttributeDefNameSaveLiteRequest.getParamName0(), 
        wsRestAttributeDefNameSaveLiteRequest.getParamValue0(), 
        wsRestAttributeDefNameSaveLiteRequest.getParamName1(), 
        wsRestAttributeDefNameSaveLiteRequest.getParamValue1());
    
    return wsAttributeDefNameSaveLiteResult;
  
  }

  /**
   * find an attribute def name or attribute def names.  Each additional parameter sent will narow the search,
   * except the lookups will just lookup whatever is sent.
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestFindAttributeDefNamesRequest 
   * @return the attribute def names, or no attribute def names if none found
   */
  public static WsFindAttributeDefNamesResults findAttributeDefNames(GrouperVersion clientVersion, 
      WsRestFindAttributeDefNamesRequest wsRestFindAttributeDefNamesRequest) {
  
    //cant be null
    wsRestFindAttributeDefNamesRequest = wsRestFindAttributeDefNamesRequest == null ? new WsRestFindAttributeDefNamesRequest() : wsRestFindAttributeDefNamesRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindAttributeDefNamesRequest.getClientVersion()), false, "clientVersion");
  
    WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = new GrouperService(false).findAttributeDefNames(
        clientVersionString, wsRestFindAttributeDefNamesRequest.getScope(), 
        wsRestFindAttributeDefNamesRequest.getSplitScope(), 
        wsRestFindAttributeDefNamesRequest.getWsAttributeDefLookup(), 
        wsRestFindAttributeDefNamesRequest.getAttributeAssignType(), 
        wsRestFindAttributeDefNamesRequest.getAttributeDefType(), 
        wsRestFindAttributeDefNamesRequest.getWsAttributeDefNameLookups(), 
        wsRestFindAttributeDefNamesRequest.getPageSize(), 
        wsRestFindAttributeDefNamesRequest.getPageNumber(), 
        wsRestFindAttributeDefNamesRequest.getSortString(), 
        wsRestFindAttributeDefNamesRequest.getAscending(), 
        wsRestFindAttributeDefNamesRequest.getWsInheritanceSetRelation(), 
        wsRestFindAttributeDefNamesRequest.getActAsSubjectLookup(), 
        wsRestFindAttributeDefNamesRequest.getParams(),
        wsRestFindAttributeDefNamesRequest.getSubjectLookup(),
        wsRestFindAttributeDefNamesRequest.getServiceRole() );
    
    return wsFindAttributeDefNamesResults;
  }

  /**
   * find an attribute def name or attribute def names.  Each additional parameter sent will narow the search,
   * except the lookups will just lookup whatever is sent.
   * @param clientVersion 
   * @param attributeDefNameName if in url
   * @param wsRestFindAttributeDefNamesLiteRequest 
   * @return the attribute def names, or no attribute def names if none found
   */
  public static WsFindAttributeDefNamesResults findAttributeDefNamesLite(GrouperVersion clientVersion, String attributeDefNameName, 
      WsRestFindAttributeDefNamesLiteRequest wsRestFindAttributeDefNamesLiteRequest) {
        
    
    //cant be null
    wsRestFindAttributeDefNamesLiteRequest = wsRestFindAttributeDefNamesLiteRequest == null ? 
        new WsRestFindAttributeDefNamesLiteRequest() : wsRestFindAttributeDefNamesLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindAttributeDefNamesLiteRequest.getClientVersion()), false, "clientVersion");
  
    attributeDefNameName = GrouperServiceUtils.pickOne(attributeDefNameName, wsRestFindAttributeDefNamesLiteRequest.getAttributeDefNameName(), true, "attributeDefNameName");

    WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults = new GrouperService(false).findAttributeDefNamesLite(
        clientVersionString, wsRestFindAttributeDefNamesLiteRequest.getScope(), 
        wsRestFindAttributeDefNamesLiteRequest.getSplitScope(), 
        wsRestFindAttributeDefNamesLiteRequest.getUuidOfAttributeDef(), 
        wsRestFindAttributeDefNamesLiteRequest.getNameOfAttributeDef(), 
        wsRestFindAttributeDefNamesLiteRequest.getAttributeAssignType(), 
        wsRestFindAttributeDefNamesLiteRequest.getAttributeDefType(), 
        wsRestFindAttributeDefNamesLiteRequest.getAttributeDefNameUuid(), 
        attributeDefNameName,
        wsRestFindAttributeDefNamesLiteRequest.getPageSize(), 
        wsRestFindAttributeDefNamesLiteRequest.getPageNumber(), 
        wsRestFindAttributeDefNamesLiteRequest.getSortString(), 
        wsRestFindAttributeDefNamesLiteRequest.getAscending(), 
        wsRestFindAttributeDefNamesLiteRequest.getWsInheritanceSetRelation(), 
        wsRestFindAttributeDefNamesLiteRequest.getActAsSubjectId(), 
        wsRestFindAttributeDefNamesLiteRequest.getActAsSubjectSourceId(), 
        wsRestFindAttributeDefNamesLiteRequest.getActAsSubjectIdentifier(), 
        wsRestFindAttributeDefNamesLiteRequest.getParamName0(), 
        wsRestFindAttributeDefNamesLiteRequest.getParamValue0(), 
        wsRestFindAttributeDefNamesLiteRequest.getParamName1(), 
        wsRestFindAttributeDefNamesLiteRequest.getParamValue1(),
        wsRestFindAttributeDefNamesLiteRequest.getSubjectId(),
        wsRestFindAttributeDefNamesLiteRequest.getSubjectSourceId(),
        wsRestFindAttributeDefNamesLiteRequest.getSubjectIdentifier(),
        wsRestFindAttributeDefNamesLiteRequest.getServiceRole()
        );
    
    return wsFindAttributeDefNamesResults;
    
  }

  /**
   * assign attributes batch rest
   * @param clientVersion
   * @param wsRestAssignAttributesBatchRequest 
   * @return the result
   */
  public static WsAssignAttributesBatchResults assignAttributesBatch(GrouperVersion clientVersion,
      WsRestAssignAttributesBatchRequest wsRestAssignAttributesBatchRequest) {
    
    //cant be null
    wsRestAssignAttributesBatchRequest = wsRestAssignAttributesBatchRequest == null ? new WsRestAssignAttributesBatchRequest()
      : wsRestAssignAttributesBatchRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestAssignAttributesBatchRequest.getClientVersion()), false, "clientVersion");
  
    WsAssignAttributesBatchResults wsAssignAttributesBatchResults = new GrouperService(false).assignAttributesBatch(
        clientVersionString, wsRestAssignAttributesBatchRequest.getWsAssignAttributeBatchEntries(), 
        wsRestAssignAttributesBatchRequest.getActAsSubjectLookup(), 
        wsRestAssignAttributesBatchRequest.getIncludeSubjectDetail(), 
        wsRestAssignAttributesBatchRequest.getTxType(),
        wsRestAssignAttributesBatchRequest.getSubjectAttributeNames(),
        wsRestAssignAttributesBatchRequest.getIncludeGroupDetail(),
        wsRestAssignAttributesBatchRequest.getParams());
        
    return wsAssignAttributesBatchResults;
  }
  
  /**
   * send message(s)
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestSendMessageRequest 
   * @return the messages sent
   */
  public static WsMessageResults sendMessage(GrouperVersion clientVersion,
      WsRestSendMessageRequest wsRestSendMessageRequest) {

    //cant be null
    wsRestSendMessageRequest = wsRestSendMessageRequest == null
        ? new WsRestSendMessageRequest() : wsRestSendMessageRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestSendMessageRequest.getClientVersion()),
        false, "clientVersion");

    WsMessageResults wsSendMessageResults = new GrouperService(false).sendMessage(
        clientVersionString, wsRestSendMessageRequest.getQueueOrTopic(),
        wsRestSendMessageRequest.getQueueOrTopicName(),
        wsRestSendMessageRequest.getMessageSystemName(),
        wsRestSendMessageRequest.getMessages(),
        wsRestSendMessageRequest.getActAsSubjectLookup(),
        wsRestSendMessageRequest.getParams());

    return wsSendMessageResults;
  }

  /**
   * receive message(s)
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestReceiveMessageRequest 
   * @return the messages received
   */
  public static WsMessageResults receiveMessage(GrouperVersion clientVersion,
      WsRestReceiveMessageRequest wsRestReceiveMessageRequest) {

    //cant be null
    wsRestReceiveMessageRequest = wsRestReceiveMessageRequest == null
        ? new WsRestReceiveMessageRequest() : wsRestReceiveMessageRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestReceiveMessageRequest.getClientVersion()),
        false, "clientVersion");

    WsMessageResults wsReceiveMessageResults = new GrouperService(false).receiveMessage(
        clientVersionString, wsRestReceiveMessageRequest.getQueueOrTopicName(),
        wsRestReceiveMessageRequest.getMessageSystemName(),
        wsRestReceiveMessageRequest.getBlockMillis(),
        wsRestReceiveMessageRequest.getMaxMessagesToReceiveAtOnce(),
        wsRestReceiveMessageRequest.getActAsSubjectLookup(),
        wsRestReceiveMessageRequest.getParams());

    return wsReceiveMessageResults;
  }

  /**
   * acknowledge message(s)
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsRestMessageAcknowledgeRequest 
   * @return the processed messages
   */
  public static WsMessageAcknowledgeResults acknowledgeMessages(
      GrouperVersion clientVersion,
      WsRestAcknowledgeMessageRequest wsRestMessageAcknowledgeRequest) {

    //cant be null
    wsRestMessageAcknowledgeRequest = wsRestMessageAcknowledgeRequest == null
        ? new WsRestAcknowledgeMessageRequest() : wsRestMessageAcknowledgeRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(
            wsRestMessageAcknowledgeRequest.getClientVersion()),
        false, "clientVersion");

    WsMessageAcknowledgeResults results = new GrouperService(false).acknowledge(
        clientVersionString, wsRestMessageAcknowledgeRequest.getQueueOrTopicName(),
        wsRestMessageAcknowledgeRequest.getMessageSystemName(),
        wsRestMessageAcknowledgeRequest.getAcknowledgeType(),
        wsRestMessageAcknowledgeRequest.getMessageIds(),
        wsRestMessageAcknowledgeRequest.getAnotherQueueOrTopicName(),
        wsRestMessageAcknowledgeRequest.getAnotherQueueOrTopic(),
        wsRestMessageAcknowledgeRequest.getActAsSubjectLookup(),
        wsRestMessageAcknowledgeRequest.getParams());

    return results;
  }
  
  /**
   * <pre>
   * based on an external subject query, get the external subjects
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindExternalSubjectsRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindExternalSubjectsResults findExternalSubjects(GrouperVersion clientVersion,
      WsRestFindExternalSubjectsRequest wsRestFindExternalSubjectsRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestFindExternalSubjectsRequest.getClientVersion()), false, "clientVersion");

    //get the results
    WsFindExternalSubjectsResults wsFindExternalSubjectsResults = new GrouperService(false).findExternalSubjects(
        clientVersionString, wsRestFindExternalSubjectsRequest.getWsExternalSubjectLookups(),
        wsRestFindExternalSubjectsRequest.getActAsSubjectLookup(), wsRestFindExternalSubjectsRequest.getParams());

    //return result
    return wsFindExternalSubjectsResults;
  }

  /**
   * <pre>
   * based on a submitted object of type WsRestExternalSubjectDeleteRequest, delete the external subjects.  e.g. url:
   * /v1_3_000/externalSubjects
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestExternalSubjectDeleteRequest is the request body converted to an object
   * @return the result
   */
  public static WsExternalSubjectDeleteResults externalSubjectDelete(GrouperVersion clientVersion,
      WsRestExternalSubjectDeleteRequest wsRestExternalSubjectDeleteRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestExternalSubjectDeleteRequest != null,
        "Body of request must contain an instance of "
            + WsRestExternalSubjectDeleteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestExternalSubjectDeleteRequest.getClientVersion()), false, "clientVersion");
  
    //get the results
    WsExternalSubjectDeleteResults wsExternalSubjectDeleteResults = new GrouperService(false).externalSubjectDelete(
        clientVersionString, wsRestExternalSubjectDeleteRequest.getWsExternalSubjectLookups(),
        wsRestExternalSubjectDeleteRequest.getActAsSubjectLookup(), wsRestExternalSubjectDeleteRequest
            .getTxType(), wsRestExternalSubjectDeleteRequest.getParams());
  
    //return result
    return wsExternalSubjectDeleteResults;
  
  }

  /**
   * <pre>
   * based on a submitted object of type WsRestExternalSubjectSaveRequest, save external subjects.  e.g. url:
   * /v1_3_000/externalSubjects
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestExternalSubjectSaveRequest is the request body converted to an object
   * @return the result
   */
  public static WsExternalSubjectSaveResults externalSubjectSave(GrouperVersion clientVersion,
      WsRestExternalSubjectSaveRequest wsRestExternalSubjectSaveRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestExternalSubjectSaveRequest != null,
        "Body of request must contain an instance of "
            + WsRestExternalSubjectSaveRequest.class.getSimpleName() + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.toString(),
        GrouperVersion.stringValueOrNull(wsRestExternalSubjectSaveRequest.getClientVersion()), false, "clientVersion");
  
    //get the results
    WsExternalSubjectSaveResults wsExternalSubjectSaveResults = new GrouperService(false).externalSubjectSave(
        clientVersionString, wsRestExternalSubjectSaveRequest.getWsExternalSubjectToSaves(),
        wsRestExternalSubjectSaveRequest.getActAsSubjectLookup(), wsRestExternalSubjectSaveRequest.getTxType(),
        wsRestExternalSubjectSaveRequest.getParams());
  
    //return result
    return wsExternalSubjectSaveResults;
  
  }


}
