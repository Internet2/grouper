/*
 * @author mchyzer $Id: GrouperServiceRest.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetMembersLiteRequest;
import edu.internet2.middleware.grouper.ws.soap.GrouperService;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
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
            .getActAsSubjectSource(), wsRestGroupGetMembersRequest
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
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestAddMemberLiteRequest.getSubjectSource(), true, "sourceId");
    
    //get the results
    WsAddMemberLiteResult wsAddMemberLiteResult = new GrouperService().addMemberLite(
        clientVersionString, groupName, wsRestAddMemberLiteRequest.getGroupUuid(), 
        subjectId, sourceId,
        wsRestAddMemberLiteRequest.getSubjectIdentifier(), wsRestAddMemberLiteRequest.getActAsSubjectId(),
        wsRestAddMemberLiteRequest.getActAsSubjectSource(), wsRestAddMemberLiteRequest.getActAsSubjectIdentifier(),
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

}

