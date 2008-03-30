/*
 * @author mchyzer $Id: GrouperServiceRest.java,v 1.6 2008-03-30 09:01:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveRequest;
import edu.internet2.middleware.grouper.ws.soap.GrouperService;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResults;
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
  public static WsFindGroupsResults findGroups(GrouperWsVersion clientVersion,
      WsRestFindGroupsRequest wsRestFindGroupsRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestFindGroupsRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsFindGroupsResults wsFindGroupsResults = new GrouperService().findGroups(
        clientVersionString, wsRestFindGroupsRequest.getWsQueryFilter(),
        wsRestFindGroupsRequest.getActAsSubjectLookup(), wsRestFindGroupsRequest
            .getIncludeGroupDetail(), wsRestFindGroupsRequest.getParams());

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
  public static WsFindGroupsResults findGroupsLite(GrouperWsVersion clientVersion,
      WsRestFindGroupsLiteRequest wsRestFindGroupsLiteRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestFindGroupsLiteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsFindGroupsResults wsFindGroupsResults = new GrouperService().findGroupsLite(
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
            .getParamName1(), wsRestFindGroupsLiteRequest.getParamValue1());

    //return result
    return wsFindGroupsResults;
  }

  /**
   * <pre>
   * based on a group name, get the members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestGroupGetMembersRequest is the request body converted to an object
   * @return the results
   */
  public static WsGetMembersLiteResult getMembersLite(GrouperWsVersion clientVersion,
      String groupName, WsRestGetMembersLiteRequest wsRestGroupGetMembersRequest) {

    //make sure not null
    wsRestGroupGetMembersRequest = wsRestGroupGetMembersRequest == null ? new WsRestGetMembersLiteRequest()
        : wsRestGroupGetMembersRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGroupGetMembersRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestGroupGetMembersRequest
        .getGroupName(), false, "groupName");

    //get the results
    WsGetMembersLiteResult wsGetMembersLiteResult = new GrouperService().getMembersLite(
        clientVersionString, groupName, wsRestGroupGetMembersRequest.getGroupUuid(),
        wsRestGroupGetMembersRequest.getMemberFilter(), wsRestGroupGetMembersRequest
            .getActAsSubjectId(), wsRestGroupGetMembersRequest.getActAsSubjectSourceId(),
        wsRestGroupGetMembersRequest.getActAsSubjectIdentifier(),
        wsRestGroupGetMembersRequest.getFieldName(), wsRestGroupGetMembersRequest
            .getIncludeGroupDetail(), wsRestGroupGetMembersRequest
            .getRetrieveSubjectDetail(), wsRestGroupGetMembersRequest
            .getSubjectAttributeNames(), wsRestGroupGetMembersRequest.getParamName0(),
        wsRestGroupGetMembersRequest.getParamValue0(), wsRestGroupGetMembersRequest
            .getParamName1(), wsRestGroupGetMembersRequest.getParamName1());

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
  public static WsAddMemberLiteResult addMemberLite(GrouperWsVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestAddMemberLiteRequest wsRestAddMemberLiteRequest) {

    //make sure not null
    wsRestAddMemberLiteRequest = wsRestAddMemberLiteRequest == null ? new WsRestAddMemberLiteRequest()
        : wsRestAddMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAddMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestAddMemberLiteRequest
        .getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestAddMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestAddMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsAddMemberLiteResult wsAddMemberLiteResult = new GrouperService().addMemberLite(
        clientVersionString, groupName, wsRestAddMemberLiteRequest.getGroupUuid(),
        subjectId, sourceId, wsRestAddMemberLiteRequest.getSubjectIdentifier(),
        wsRestAddMemberLiteRequest.getActAsSubjectId(), wsRestAddMemberLiteRequest
            .getActAsSubjectSourceId(), wsRestAddMemberLiteRequest
            .getActAsSubjectIdentifier(), wsRestAddMemberLiteRequest.getFieldName(),
        wsRestAddMemberLiteRequest.getIncludeGroupDetail(), wsRestAddMemberLiteRequest
            .getIncludeSubjectDetail(), wsRestAddMemberLiteRequest
            .getSubjectAttributeNames(), wsRestAddMemberLiteRequest.getParamName0(),
        wsRestAddMemberLiteRequest.getParamValue0(), wsRestAddMemberLiteRequest
            .getParamName1(), wsRestAddMemberLiteRequest.getParamValue1());

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
  public static WsAddMemberResults addMember(GrouperWsVersion clientVersion,
      String groupName, WsRestAddMemberRequest wsRestAddMembersRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestAddMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestAddMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAddMembersRequest.getClientVersion(), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestAddMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }

    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        false, "groupName");
    wsGroupLookup.setGroupName(groupName);

    //get the results
    WsAddMemberResults wsAddMemberResults = new GrouperService().addMember(
        clientVersionString, wsGroupLookup, wsRestAddMembersRequest.getSubjectLookups(),
        wsRestAddMembersRequest.getReplaceAllExisting(), wsRestAddMembersRequest
            .getActAsSubjectLookup(), wsRestAddMembersRequest.getFieldName(),
        wsRestAddMembersRequest.getTxType(), wsRestAddMembersRequest
            .getIncludeGroupDetail(), wsRestAddMembersRequest.getIncludeSubjectDetail(),
        wsRestAddMembersRequest.getSubjectAttributeNames(), wsRestAddMembersRequest
            .getParams());

    //return result
    return wsAddMemberResults;

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
  public static WsDeleteMemberLiteResult deleteMemberLite(GrouperWsVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestDeleteMemberLiteRequest wsRestDeleteMemberLiteRequest) {

    //make sure not null
    wsRestDeleteMemberLiteRequest = wsRestDeleteMemberLiteRequest == null ? new WsRestDeleteMemberLiteRequest()
        : wsRestDeleteMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestDeleteMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestDeleteMemberLiteRequest
        .getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestDeleteMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestDeleteMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsDeleteMemberLiteResult wsDeleteMemberLiteResult = new GrouperService()
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
  public static WsDeleteMemberResults deleteMember(GrouperWsVersion clientVersion,
      String groupName, WsRestDeleteMemberRequest wsRestDeleteMembersRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestDeleteMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestDeleteMemberRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestDeleteMembersRequest.getClientVersion(), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestDeleteMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }

    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        false, "groupName");
    wsGroupLookup.setGroupName(groupName);

    //get the results
    WsDeleteMemberResults wsDeleteMemberResults = new GrouperService().deleteMember(
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
  public static WsHasMemberResults hasMember(GrouperWsVersion clientVersion,
      String groupName, WsRestHasMemberRequest wsRestHasMembersRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestHasMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestHasMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestHasMembersRequest.getClientVersion(), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestHasMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }

    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        false, "groupName");
    wsGroupLookup.setGroupName(groupName);

    //get the results
    WsHasMemberResults wsHasMemberResults = new GrouperService().hasMember(
        clientVersionString, wsGroupLookup, wsRestHasMembersRequest.getSubjectLookups(),
        wsRestHasMembersRequest.getMemberFilter(), wsRestHasMembersRequest
            .getActAsSubjectLookup(), wsRestHasMembersRequest.getFieldName(),
        wsRestHasMembersRequest.getIncludeGroupDetail(), wsRestHasMembersRequest
            .getIncludeSubjectDetail(), wsRestHasMembersRequest
            .getSubjectAttributeNames(), wsRestHasMembersRequest.getParams());

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
  public static WsGetMembersResults getMembers(GrouperWsVersion clientVersion,
      WsRestGetMembersRequest wsRestGetMembersRequest) {

    //cant be null
    GrouperUtil
        .assertion(wsRestGetMembersRequest != null,
            "Body of request must contain an instance of "
                + WsRestGetMembersRequest.class.getSimpleName()
                + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetMembersRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsGetMembersResults wsGetMembersResults = new GrouperService().getMembers(
        clientVersionString, wsRestGetMembersRequest.getWsGroupLookups(),
        wsRestGetMembersRequest.getMemberFilter(), wsRestGetMembersRequest
            .getActAsSubjectLookup(), wsRestGetMembersRequest.getFieldName(),
        wsRestGetMembersRequest.getIncludeGroupDetail(), wsRestGetMembersRequest
            .getIncludeSubjectDetail(), wsRestGetMembersRequest
            .getSubjectAttributeNames(), wsRestGetMembersRequest.getParams());

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
  public static WsHasMemberLiteResult hasMemberLite(GrouperWsVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestHasMemberLiteRequest wsRestHasMemberLiteRequest) {

    //make sure not null
    wsRestHasMemberLiteRequest = wsRestHasMemberLiteRequest == null ? new WsRestHasMemberLiteRequest()
        : wsRestHasMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestHasMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestHasMemberLiteRequest
        .getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestHasMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestHasMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsHasMemberLiteResult wsHasMemberLiteResult = new GrouperService().hasMemberLite(
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
            .getParamName1(), wsRestHasMemberLiteRequest.getParamValue1());

    //return result
    return wsHasMemberLiteResult;

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
  public static WsGetGroupsResults getGroups(GrouperWsVersion clientVersion,
      WsRestGetGroupsRequest wsRestGetGroupsRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestGetGroupsRequest != null,
        "Body of request must contain an instance of "
            + WsRestGetGroupsRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetGroupsRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsGetGroupsResults wsGetGroupsResults = new GrouperService().getGroups(
        clientVersionString, wsRestGetGroupsRequest.getSubjectLookups(),
        wsRestGetGroupsRequest.getMemberFilter(), wsRestGetGroupsRequest
            .getActAsSubjectLookup(), wsRestGetGroupsRequest.getIncludeGroupDetail(),
        wsRestGetGroupsRequest.getIncludeSubjectDetail(), wsRestGetGroupsRequest
            .getSubjectAttributeNames(), wsRestGetGroupsRequest.getParams());

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
  public static WsGetGroupsLiteResult getGroupsLite(GrouperWsVersion clientVersion,
      String subjectId, String sourceId,
      WsRestGetGroupsLiteRequest wsRestGetGroupsLiteRequest) {

    //make sure not null
    wsRestGetGroupsLiteRequest = wsRestGetGroupsLiteRequest == null ? new WsRestGetGroupsLiteRequest()
        : wsRestGetGroupsLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetGroupsLiteRequest.getClientVersion(), false, "clientVersion");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestGetGroupsLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestGetGroupsLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsGetGroupsLiteResult wsGetGroupsLiteResult = new GrouperService().getGroupsLite(
        clientVersionString, subjectId, sourceId, wsRestGetGroupsLiteRequest
            .getSubjectIdentifier(), wsRestGetGroupsLiteRequest.getMemberFilter(),
        wsRestGetGroupsLiteRequest.getActAsSubjectId(), wsRestGetGroupsLiteRequest
            .getActAsSubjectSourceId(), wsRestGetGroupsLiteRequest
            .getActAsSubjectIdentifier(), wsRestGetGroupsLiteRequest
            .getIncludeGroupDetail(), wsRestGetGroupsLiteRequest
            .getIncludeSubjectDetail(), wsRestGetGroupsLiteRequest
            .getSubjectAttributeNames(), wsRestGetGroupsLiteRequest.getParamName0(),
        wsRestGetGroupsLiteRequest.getParamValue0(), wsRestGetGroupsLiteRequest
            .getParamName1(), wsRestGetGroupsLiteRequest.getParamValue1());

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
  public static WsFindStemsResults findStems(GrouperWsVersion clientVersion,
      WsRestFindStemsRequest wsRestFindStemsRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestFindStemsRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsFindStemsResults wsFindStemsResults = new GrouperService().findStems(
        clientVersionString, wsRestFindStemsRequest.getWsStemQueryFilter(),
        wsRestFindStemsRequest.getActAsSubjectLookup(), wsRestFindStemsRequest
            .getParams());

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
  public static WsFindStemsResults findStemsLite(GrouperWsVersion clientVersion,
      WsRestFindStemsLiteRequest wsRestFindStemsLiteRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestFindStemsLiteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsFindStemsResults wsFindStemsResults = new GrouperService().findStemsLite(
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
  public static WsStemSaveResults stemSave(GrouperWsVersion clientVersion,
      WsRestStemSaveRequest wsRestStemSaveRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestStemSaveRequest != null,
        "Body of request must contain an instance of "
            + WsRestStemSaveRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestStemSaveRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsStemSaveResults wsStemSaveResults = new GrouperService().stemSave(
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
  public static WsStemSaveLiteResult stemSaveLite(GrouperWsVersion clientVersion,
      String stemLookupName, WsRestStemSaveLiteRequest wsRestStemSaveLiteRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestStemSaveLiteRequest != null,
        "Body of request must contain an instance of "
            + WsRestStemSaveLiteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestStemSaveLiteRequest.getClientVersion(), false, "clientVersion");

    stemLookupName = GrouperServiceUtils.pickOne(stemLookupName,
        wsRestStemSaveLiteRequest.getStemName(), false, "stemLookupName");

    //get the results
    WsStemSaveLiteResult wsStemSaveLiteResult = new GrouperService().stemSaveLite(
        clientVersionString, wsRestStemSaveLiteRequest.getStemLookupUuid(),
        stemLookupName, wsRestStemSaveLiteRequest.getStemName(),
        wsRestStemSaveLiteRequest.getStemUuid(), wsRestStemSaveLiteRequest
            .getDescription(), wsRestStemSaveLiteRequest.getDisplayExtension(),
        wsRestStemSaveLiteRequest.getSaveMode(), wsRestStemSaveLiteRequest
            .getActAsSubjectId(), wsRestStemSaveLiteRequest.getActAsSubjectSourceId(),
        wsRestStemSaveLiteRequest.getActAsSubjectIdentifier(), wsRestStemSaveLiteRequest
            .getParamName0(), wsRestStemSaveLiteRequest.getParamValue0(),
        wsRestStemSaveLiteRequest.getParamName1(), wsRestStemSaveLiteRequest
            .getParamValue0());

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
  public static WsStemDeleteResults stemDelete(GrouperWsVersion clientVersion,
      WsRestStemDeleteRequest wsRestStemDeleteRequest) {

    //cant be null
    GrouperUtil
        .assertion(wsRestStemDeleteRequest != null,
            "Body of request must contain an instance of "
                + WsRestStemDeleteRequest.class.getSimpleName()
                + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestStemDeleteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsStemDeleteResults wsStemDeleteResults = new GrouperService().stemDelete(
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
  public static WsStemDeleteLiteResult stemDeleteLite(GrouperWsVersion clientVersion,
      String stemName, WsRestStemDeleteLiteRequest wsRestStemDeleteLiteRequest) {

    //make sure not null
    wsRestStemDeleteLiteRequest = wsRestStemDeleteLiteRequest == null ? new WsRestStemDeleteLiteRequest()
        : wsRestStemDeleteLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestStemDeleteLiteRequest.getClientVersion(), false, "clientVersion");

    stemName = GrouperServiceUtils.pickOne(stemName, wsRestStemDeleteLiteRequest
        .getStemName(), false, "stemName");

    //get the results
    WsStemDeleteLiteResult wsStemDeleteLiteResult = new GrouperService().stemDeleteLite(
        clientVersionString, stemName, wsRestStemDeleteLiteRequest.getStemUuid(),
        wsRestStemDeleteLiteRequest.getActAsSubjectId(), wsRestStemDeleteLiteRequest
            .getActAsSubjectSourceId(), wsRestStemDeleteLiteRequest
            .getActAsSubjectIdentifier(), wsRestStemDeleteLiteRequest.getParamName0(),
        wsRestStemDeleteLiteRequest.getParamValue0(), wsRestStemDeleteLiteRequest
            .getParamName1(), wsRestStemDeleteLiteRequest.getParamValue0());

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
  public static WsGroupDeleteResults groupDelete(GrouperWsVersion clientVersion,
      WsRestGroupDeleteRequest wsRestGroupDeleteRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestGroupDeleteRequest != null,
        "Body of request must contain an instance of "
            + WsRestGroupDeleteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGroupDeleteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsGroupDeleteResults wsGroupDeleteResults = new GrouperService().groupDelete(
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
   * @param subjectId from url, e.g. /v1_3_000/groups/aGroup:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aGroup:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestGroupDeleteLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupDeleteLiteResult groupDeleteLite(GrouperWsVersion clientVersion,
      String groupName, WsRestGroupDeleteLiteRequest wsRestGroupDeleteLiteRequest) {

    //make sure not null
    wsRestGroupDeleteLiteRequest = wsRestGroupDeleteLiteRequest == null ? new WsRestGroupDeleteLiteRequest()
        : wsRestGroupDeleteLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGroupDeleteLiteRequest.getClientVersion(), false, "clientVersion");

    groupName = GrouperServiceUtils.pickOne(groupName, wsRestGroupDeleteLiteRequest
        .getGroupName(), false, "groupName");

    //get the results
    WsGroupDeleteLiteResult wsGroupDeleteLiteResult = new GrouperService()
        .groupDeleteLite(clientVersionString, groupName, wsRestGroupDeleteLiteRequest
            .getGroupUuid(), wsRestGroupDeleteLiteRequest.getActAsSubjectId(),
            wsRestGroupDeleteLiteRequest.getActAsSubjectSourceId(),
            wsRestGroupDeleteLiteRequest.getActAsSubjectIdentifier(),
            wsRestGroupDeleteLiteRequest.getIncludeGroupDetail(),
            wsRestGroupDeleteLiteRequest.getParamName0(), wsRestGroupDeleteLiteRequest
                .getParamValue0(), wsRestGroupDeleteLiteRequest.getParamName1(),
            wsRestGroupDeleteLiteRequest.getParamValue0());

    //return result
    return wsGroupDeleteLiteResult;

  }
}
