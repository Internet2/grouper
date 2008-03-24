/*
 * @author mchyzer $Id: GrouperServiceLite.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.lite.group.WsLiteAddMemberRequest;
import edu.internet2.middleware.grouper.ws.lite.group.WsLiteAddMemberSimpleRequest;
import edu.internet2.middleware.grouper.ws.lite.group.WsLiteGetMembersSimpleRequest;
import edu.internet2.middleware.grouper.ws.soap.GrouperService;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberSimpleResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * consolidated static list of of lite web services (only web service methods here
 * to have clean javadoc).  the method name corresponds to the url and request method.
 * e.g. "GET /group/a:b:c/members" will correspond to groupMembersGet()
 */
public class GrouperServiceLite {

  /**
   * <pre>
   * based on a group name, get the members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsLiteGroupGetMembersRequest is the request body converted to an object
   * @return the results
   */
  public static WsGetMembersResults getMembersSimple(
      GrouperWsVersion clientVersion, String groupName,
      WsLiteGetMembersSimpleRequest wsLiteGroupGetMembersRequest) {

    //make sure not null
    wsLiteGroupGetMembersRequest = wsLiteGroupGetMembersRequest == null ? new WsLiteGetMembersSimpleRequest()
        : wsLiteGroupGetMembersRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), 
        wsLiteGroupGetMembersRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, 
        wsLiteGroupGetMembersRequest.getGroupName(), false, "groupName");

    //get the results
    WsGetMembersResults wsGetMembersResults = new GrouperService().getMembersSimple(
        clientVersionString, groupName, null, wsLiteGroupGetMembersRequest
            .getMemberFilter(), wsLiteGroupGetMembersRequest.getRetrieveSubjectDetail(),
        wsLiteGroupGetMembersRequest.getActAsSubjectId(), wsLiteGroupGetMembersRequest
            .getActAsSubjectSource(), wsLiteGroupGetMembersRequest
            .getActAsSubjectIdentifier(), wsLiteGroupGetMembersRequest.getFieldName(),
        wsLiteGroupGetMembersRequest.getSubjectAttributeNames(),
        wsLiteGroupGetMembersRequest.getIncludeGroupDetail(),
        wsLiteGroupGetMembersRequest.getParamName0(), wsLiteGroupGetMembersRequest
            .getParamValue0(), wsLiteGroupGetMembersRequest.getParamName1(),
        wsLiteGroupGetMembersRequest.getParamName1());

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
   * @param wsLiteAddMemberSimpleRequest is the request body converted to an object
   * @return the result
   */
  public static WsAddMemberSimpleResult addMemberSimple(
      GrouperWsVersion clientVersion, String groupName, String subjectId, String sourceId,
      WsLiteAddMemberSimpleRequest wsLiteAddMemberSimpleRequest) {
  
    //make sure not null
    wsLiteAddMemberSimpleRequest = wsLiteAddMemberSimpleRequest == null ? new WsLiteAddMemberSimpleRequest()
        : wsLiteAddMemberSimpleRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsLiteAddMemberSimpleRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsLiteAddMemberSimpleRequest.getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsLiteAddMemberSimpleRequest.getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsLiteAddMemberSimpleRequest.getSubjectSource(), true, "sourceId");
    
    //get the results
    WsAddMemberSimpleResult wsAddMemberSimpleResult = new GrouperService().addMemberSimple(
        clientVersionString, groupName, wsLiteAddMemberSimpleRequest.getGroupUuid(), 
        subjectId, sourceId,
        wsLiteAddMemberSimpleRequest.getSubjectIdentifier(), wsLiteAddMemberSimpleRequest.getActAsSubjectId(),
        wsLiteAddMemberSimpleRequest.getActAsSubjectSource(), wsLiteAddMemberSimpleRequest.getActAsSubjectIdentifier(),
        wsLiteAddMemberSimpleRequest.getFieldName(), wsLiteAddMemberSimpleRequest.getIncludeGroupDetail(),
        wsLiteAddMemberSimpleRequest.getIncludeSubjectDetail(), wsLiteAddMemberSimpleRequest.getSubjectAttributeNames(),
        wsLiteAddMemberSimpleRequest.getParamName0(), wsLiteAddMemberSimpleRequest.getParamValue0(),
        wsLiteAddMemberSimpleRequest.getParamName1(), wsLiteAddMemberSimpleRequest.getParamValue1());
  
    //return result
    return wsAddMemberSimpleResult;
  
  }

  /**
   * <pre>
   * based on a group name, put multiple members, or all members.  e.g. url:
   * /v1_3_000/group/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsLiteAddMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsAddMemberResults addMember(
      GrouperWsVersion clientVersion, String groupName,
      WsLiteAddMemberRequest wsLiteAddMembersRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsLiteAddMembersRequest != null, "Body of request must contain an instance of " 
        + WsLiteAddMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(), wsLiteAddMembersRequest.getClientVersion(), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsLiteAddMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }
    
    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(), false, "groupName");
    wsGroupLookup.setGroupName(groupName);
    
    //get the results
    WsAddMemberResults wsAddMemberResults = new GrouperService().addMember(
        clientVersionString, wsGroupLookup, wsLiteAddMembersRequest.getSubjectLookups(), 
        wsLiteAddMembersRequest.getReplaceAllExisting(), wsLiteAddMembersRequest.getActAsSubjectLookup(),
        wsLiteAddMembersRequest.getFieldName(), wsLiteAddMembersRequest.getTxType(), 
        wsLiteAddMembersRequest.getIncludeGroupDetail(),
        wsLiteAddMembersRequest.getIncludeSubjectDetail(), wsLiteAddMembersRequest.getSubjectAttributeNames(), 
        wsLiteAddMembersRequest.getParams());

    //return result
    return wsAddMemberResults;
  
  }

}

