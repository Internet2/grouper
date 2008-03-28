/*
 * @author mchyzer $Id: GrouperServiceRest.java,v 1.4 2008-03-28 16:45:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetMembersLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberRequest;
import edu.internet2.middleware.grouper.ws.soap.GrouperService;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * consolidated static list of of rest web services (only web service methods here
 * to have clean javadoc).  the method name corresponds to the url and request method.
 * e.g. "GET /group/a:b:c/members" will correspond to groupMembersGet()
 */
public class GrouperServiceRest {

  /**
   * <pre>
   * based on a group name, get the members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestGroupGetMembersRequest is the request body converted to an object
   * @return the results
   */  //TODO change result type to a Lite response
  public static WsGetMembersResults getMembersLite(
      GrouperWsVersion clientVersion, String groupName,
      WsRestGetMembersLiteRequest wsRestGroupGetMembersRequest) {

    //make sure not null
    wsRestGroupGetMembersRequest = wsRestGroupGetMembersRequest == null ? new WsRestGetMembersLiteRequest()
        : wsRestGroupGetMembersRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), 
        wsRestGroupGetMembersRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, 
        wsRestGroupGetMembersRequest.getGroupName(), false, "groupName");

    //get the results
    WsGetMembersResults wsGetMembersResults = new GrouperService().getMembersLite(
        clientVersionString, groupName, null, wsRestGroupGetMembersRequest
            .getMemberFilter(), wsRestGroupGetMembersRequest.getRetrieveSubjectDetail(),
        wsRestGroupGetMembersRequest.getActAsSubjectId(), wsRestGroupGetMembersRequest
            .getActAsSubjectSourceId(), wsRestGroupGetMembersRequest
            .getActAsSubjectIdentifier(), wsRestGroupGetMembersRequest.getFieldName(),
        wsRestGroupGetMembersRequest.getSubjectAttributeNames(),
        wsRestGroupGetMembersRequest.getIncludeGroupDetail(),
        wsRestGroupGetMembersRequest.getParamName0(), wsRestGroupGetMembersRequest
            .getParamValue0(), wsRestGroupGetMembersRequest.getParamName1(),
        wsRestGroupGetMembersRequest.getParamName1());

    //return result
    return wsGetMembersResults;

  }

  /**
   * <pre>
   * based on a group name, put the member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/group/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/group/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestAddMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsAddMemberLiteResult addMemberLite(
      GrouperWsVersion clientVersion, String groupName, String subjectId, String sourceId,
      WsRestAddMemberLiteRequest wsRestAddMemberLiteRequest) {
  
    //make sure not null
    wsRestAddMemberLiteRequest = wsRestAddMemberLiteRequest == null ? new WsRestAddMemberLiteRequest()
        : wsRestAddMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsRestAddMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestAddMemberLiteRequest.getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestAddMemberLiteRequest.getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestAddMemberLiteRequest.getSubjectSourceId(), true, "sourceId");
    
    //get the results
    WsAddMemberLiteResult wsAddMemberLiteResult = new GrouperService().addMemberLite(
        clientVersionString, groupName, wsRestAddMemberLiteRequest.getGroupUuid(), 
        subjectId, sourceId,
        wsRestAddMemberLiteRequest.getSubjectIdentifier(), wsRestAddMemberLiteRequest.getActAsSubjectId(),
        wsRestAddMemberLiteRequest.getActAsSubjectSourceId(), wsRestAddMemberLiteRequest.getActAsSubjectIdentifier(),
        wsRestAddMemberLiteRequest.getFieldName(), wsRestAddMemberLiteRequest.getIncludeGroupDetail(),
        wsRestAddMemberLiteRequest.getIncludeSubjectDetail(), wsRestAddMemberLiteRequest.getSubjectAttributeNames(),
        wsRestAddMemberLiteRequest.getParamName0(), wsRestAddMemberLiteRequest.getParamValue0(),
        wsRestAddMemberLiteRequest.getParamName1(), wsRestAddMemberLiteRequest.getParamValue1());
  
    //return result
    return wsAddMemberLiteResult;
  
  }

  /**
   * <pre>
   * based on a group name, put multiple members, or all members.  e.g. url:
   * /v1_3_000/group/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestAddMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsAddMemberResults addMember(
      GrouperWsVersion clientVersion, String groupName,
      WsRestAddMemberRequest wsRestAddMembersRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestAddMembersRequest != null, "Body of request must contain an instance of " 
        + WsRestAddMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsRestAddMembersRequest.getClientVersion(), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestAddMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }
    
    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(), false, "groupName");
    wsGroupLookup.setGroupName(groupName);
    
    //get the results
    WsAddMemberResults wsAddMemberResults = new GrouperService().addMember(
        clientVersionString, wsGroupLookup, wsRestAddMembersRequest.getSubjectLookups(), 
        wsRestAddMembersRequest.getReplaceAllExisting(), wsRestAddMembersRequest.getActAsSubjectLookup(),
        wsRestAddMembersRequest.getFieldName(), wsRestAddMembersRequest.getTxType(), 
        wsRestAddMembersRequest.getIncludeGroupDetail(),
        wsRestAddMembersRequest.getIncludeSubjectDetail(), wsRestAddMembersRequest.getSubjectAttributeNames(), 
        wsRestAddMembersRequest.getParams());

    //return result
    return wsAddMemberResults;
  
  }

  /**
   * <pre>
   * based on a group name, delete the member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/group/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/group/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestDeleteMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsDeleteMemberLiteResult deleteMemberLite(
      GrouperWsVersion clientVersion, String groupName, String subjectId, String sourceId,
      WsRestDeleteMemberLiteRequest wsRestDeleteMemberLiteRequest) {
  
    //make sure not null
    wsRestDeleteMemberLiteRequest = wsRestDeleteMemberLiteRequest == null ? new WsRestDeleteMemberLiteRequest()
        : wsRestDeleteMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsRestDeleteMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestDeleteMemberLiteRequest.getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestDeleteMemberLiteRequest.getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestDeleteMemberLiteRequest.getSubjectSourceId(), true, "sourceId");
    
    //get the results
    WsDeleteMemberLiteResult wsDeleteMemberLiteResult = new GrouperService().deleteMemberLite(
        clientVersionString, groupName, wsRestDeleteMemberLiteRequest.getGroupUuid(), 
        subjectId, sourceId,
        wsRestDeleteMemberLiteRequest.getSubjectIdentifier(), wsRestDeleteMemberLiteRequest.getActAsSubjectId(),
        wsRestDeleteMemberLiteRequest.getActAsSubjectSourceId(), wsRestDeleteMemberLiteRequest.getActAsSubjectIdentifier(),
        wsRestDeleteMemberLiteRequest.getFieldName(), wsRestDeleteMemberLiteRequest.getIncludeGroupDetail(),
        wsRestDeleteMemberLiteRequest.getIncludeSubjectDetail(), wsRestDeleteMemberLiteRequest.getSubjectAttributeNames(),
        wsRestDeleteMemberLiteRequest.getParamName0(), wsRestDeleteMemberLiteRequest.getParamValue0(),
        wsRestDeleteMemberLiteRequest.getParamName1(), wsRestDeleteMemberLiteRequest.getParamValue1());
  
    //return result
    return wsDeleteMemberLiteResult;
  
  }

  /**
   * <pre>
   * based on a group name, put multiple members, or all members.  e.g. url:
   * /v1_3_000/group/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestDeleteMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsDeleteMemberResults deleteMember(
      GrouperWsVersion clientVersion, String groupName,
      WsRestDeleteMemberRequest wsRestDeleteMembersRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestDeleteMembersRequest != null, "Body of request must contain an instance of " 
        + WsRestDeleteMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsRestDeleteMembersRequest.getClientVersion(), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestDeleteMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }
    
    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(), false, "groupName");
    wsGroupLookup.setGroupName(groupName);
    
    //get the results
    WsDeleteMemberResults wsDeleteMemberResults = new GrouperService().deleteMember(
        clientVersionString, wsGroupLookup, wsRestDeleteMembersRequest.getSubjectLookups(), 
        wsRestDeleteMembersRequest.getActAsSubjectLookup(),
        wsRestDeleteMembersRequest.getFieldName(), wsRestDeleteMembersRequest.getTxType(), 
        wsRestDeleteMembersRequest.getIncludeGroupDetail(),
        wsRestDeleteMembersRequest.getIncludeSubjectDetail(), wsRestDeleteMembersRequest.getSubjectAttributeNames(), 
        wsRestDeleteMembersRequest.getParams());

    //return result
    return wsDeleteMemberResults;
  
  }

  /**
   * <pre>
   * based on a group name, and multiple subjects, see if they are members .  e.g. url:
   * /v1_3_000/group/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestHasMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsHasMemberResults hasMember(
      GrouperWsVersion clientVersion, String groupName,
      WsRestHasMemberRequest wsRestHasMembersRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestHasMembersRequest != null, "Body of request must contain an instance of " 
        + WsRestHasMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsRestHasMembersRequest.getClientVersion(), false, "clientVersion");
  
    WsGroupLookup wsGroupLookup = wsRestHasMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }
    
    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(), false, "groupName");
    wsGroupLookup.setGroupName(groupName);
    
    //get the results
    WsHasMemberResults wsHasMemberResults = new GrouperService().hasMember(
        clientVersionString, wsGroupLookup, wsRestHasMembersRequest.getSubjectLookups(), 
        wsRestHasMembersRequest.getMemberFilter(), wsRestHasMembersRequest.getActAsSubjectLookup(),
        wsRestHasMembersRequest.getFieldName(),  
        wsRestHasMembersRequest.getIncludeGroupDetail(),
        wsRestHasMembersRequest.getIncludeSubjectDetail(), wsRestHasMembersRequest.getSubjectAttributeNames(), 
        wsRestHasMembersRequest.getParams());
  
    //return result
    return wsHasMemberResults;
  
  }

  /**
   * <pre>
   * based on a group name, and a subject, see if member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/group/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/group/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestHasMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsHasMemberLiteResult hasMemberLite(
      GrouperWsVersion clientVersion, String groupName, String subjectId, String sourceId,
      WsRestHasMemberLiteRequest wsRestHasMemberLiteRequest) {
  
    //make sure not null
    wsRestHasMemberLiteRequest = wsRestHasMemberLiteRequest == null ? new WsRestHasMemberLiteRequest()
        : wsRestHasMemberLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsRestHasMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestHasMemberLiteRequest.getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestHasMemberLiteRequest.getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestHasMemberLiteRequest.getSubjectSourceId(), true, "sourceId");
    
    //get the results
    WsHasMemberLiteResult wsHasMemberLiteResult = new GrouperService().hasMemberLite(
        clientVersionString, groupName, wsRestHasMemberLiteRequest.getGroupUuid(), 
        subjectId, sourceId,
        wsRestHasMemberLiteRequest.getSubjectIdentifier(), wsRestHasMemberLiteRequest.getMemberFilter(),
        wsRestHasMemberLiteRequest.getActAsSubjectId(), 
        wsRestHasMemberLiteRequest.getActAsSubjectSourceId(), wsRestHasMemberLiteRequest.getActAsSubjectIdentifier(),
        wsRestHasMemberLiteRequest.getFieldName(), wsRestHasMemberLiteRequest.getIncludeGroupDetail(),
        wsRestHasMemberLiteRequest.getIncludeSubjectDetail(), wsRestHasMemberLiteRequest.getSubjectAttributeNames(),
        wsRestHasMemberLiteRequest.getParamName0(), wsRestHasMemberLiteRequest.getParamValue0(),
        wsRestHasMemberLiteRequest.getParamName1(), wsRestHasMemberLiteRequest.getParamValue1());
  
    //return result
    return wsHasMemberLiteResult;
  
  }

  /**
   * <pre>
   * based on a subject id name, get groups
   * /v1_3_000/subjects/123/groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param sourceId is the source of the service
   * @param subjectId is the subject to search for groups
   * @param wsRestGetGroupsRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetGroupsResults getGroups(
      GrouperWsVersion clientVersion,
      WsRestGetGroupsRequest wsRestGetGroupsRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestGetGroupsRequest != null, "Body of request must contain an instance of " 
        + WsRestGetGroupsRequest.class.getSimpleName() + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsRestGetGroupsRequest.getClientVersion(), false, "clientVersion");
    
    //get the results
    WsGetGroupsResults wsGetGroupsResults = new GrouperService().getGroups(
        clientVersionString, wsRestGetGroupsRequest.getSubjectLookups(), wsRestGetGroupsRequest.getMemberFilter(), 
        wsRestGetGroupsRequest.getActAsSubjectLookup(),
        wsRestGetGroupsRequest.getIncludeGroupDetail(),
        wsRestGetGroupsRequest.getIncludeSubjectDetail(), wsRestGetGroupsRequest.getSubjectAttributeNames(), 
        wsRestGetGroupsRequest.getParams());
  
    //return result
    return wsGetGroupsResults;
  
  }

  /**
   * <pre>
   * based on a group name, put the member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/group/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/group/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestGetGroupsLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetGroupsLiteResult getGroupsLite(
      GrouperWsVersion clientVersion, String subjectId, String sourceId,
      WsRestGetGroupsLiteRequest wsRestGetGroupsLiteRequest) {
  
    //make sure not null
    wsRestGetGroupsLiteRequest = wsRestGetGroupsLiteRequest == null ? new WsRestGetGroupsLiteRequest()
        : wsRestGetGroupsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsRestGetGroupsLiteRequest.getClientVersion(), false, "clientVersion");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestGetGroupsLiteRequest.getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestGetGroupsLiteRequest.getSubjectSourceId(), true, "sourceId");
    
    //get the results
    WsGetGroupsLiteResult wsGetGroupsLiteResult = new GrouperService().getGroupsLite(
        clientVersionString,  
        subjectId, sourceId,
        wsRestGetGroupsLiteRequest.getSubjectIdentifier(), wsRestGetGroupsLiteRequest.getMemberFilter(), wsRestGetGroupsLiteRequest.getActAsSubjectId(),
        wsRestGetGroupsLiteRequest.getActAsSubjectSourceId(), wsRestGetGroupsLiteRequest.getActAsSubjectIdentifier(),
        wsRestGetGroupsLiteRequest.getIncludeGroupDetail(),
        wsRestGetGroupsLiteRequest.getIncludeSubjectDetail(), wsRestGetGroupsLiteRequest.getSubjectAttributeNames(),
        wsRestGetGroupsLiteRequest.getParamName0(), wsRestGetGroupsLiteRequest.getParamValue0(),
        wsRestGetGroupsLiteRequest.getParamName1(), wsRestGetGroupsLiteRequest.getParamValue1());
  
    //return result
    return wsGetGroupsLiteResult;
  
  }
}

