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
 * @author mchyzer $Id: GrouperWsRestGetGroup.java,v 1.3 2009-12-18 02:43:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * all first level resources on a get request
 */
public enum GrouperWsRestGetGroup {

  /** group get requests for members */
  members {

    /**
     * handle the incoming request based on HTTP method GET and group as resource, and members as subresource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "member"}
     * @param groupName in url
     * @param requestObject is the request body converted to object
     * @return the return object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, String groupName, List<String> urlStrings,
        WsRequestBean requestObject) {

      if (GrouperUtil.length(urlStrings) == 0 && (requestObject == null ||
          requestObject instanceof WsRestGetMembersLiteRequest)) {
        //make sure right type
        WsRestGetMembersLiteRequest wsRestGetMembersLiteRequest = GrouperUtil.typeCast(
            requestObject, WsRestGetMembersLiteRequest.class);

        //url should be: /xhtml/v1_3_000/groups/aStem:aGroup/members
        return GrouperServiceRest.getMembersLite(
            clientVersion, groupName,
            wsRestGetMembersLiteRequest);
        
      }
      
      if (GrouperUtil.length(urlStrings) == 0 && requestObject instanceof WsRestHasMemberRequest) {

        //make sure right type
        WsRestHasMemberRequest wsRestHasMemberRequest = GrouperUtil.typeCast(
            requestObject, WsRestHasMemberRequest.class);

        //url should be: /xhtml/v1_3_000/groups/aStem:aGroup/members
        return GrouperServiceRest.hasMember(
            clientVersion, groupName,
            wsRestHasMemberRequest);
        
      }
      
      //if the member id is on the end, then get that
      String subjectId = GrouperServiceUtils.extractSubjectInfoFromUrlStrings(urlStrings, 0, false, false);
      String sourceId = GrouperServiceUtils.extractSubjectInfoFromUrlStrings(urlStrings, 0, true, true);
      
      if (requestObject == null || requestObject instanceof WsRestHasMemberLiteRequest) {
        //make sure right type
        WsRestHasMemberLiteRequest wsRestHasMemberLiteRequest = GrouperUtil.typeCast(
            requestObject, WsRestHasMemberLiteRequest.class);

        //url should be: /xhtml/v1_3_000/groups/aStem:aGroup/members
        return GrouperServiceRest.hasMemberLite(
            clientVersion, groupName, subjectId, sourceId,
            wsRestHasMemberLiteRequest);
      }
      
      throw new RuntimeException("Invalid REST GET Group request: " + clientVersion + " , " + groupName 
          + ", " + subjectId + ", " + sourceId
          + ", " + GrouperUtil.toStringForLog(urlStrings));
    }

  }, 
  /** group get requests for memberships */
  memberships {
  
    /**
     * handle the incoming request based on HTTP method GET and group as resource, and memberships as subresource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b/memberships
     * the urlStrings would be size three: {"group", "a:b", "member"}
     * @param groupName in url
     * @param requestObject is the request body converted to object
     * @return the return object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, String groupName, List<String> urlStrings,
        WsRequestBean requestObject) {
  
      if (GrouperUtil.length(urlStrings) == 0 && (requestObject == null || 
          requestObject instanceof WsRestGetMembershipsLiteRequest)) {
  
        WsRestGetMembershipsLiteRequest wsRestGetMembershipsLiteRequest = 
          (WsRestGetMembershipsLiteRequest)requestObject;
        
        //url should be: /xhtml/v1_3_000/groups/a:b/memberships
        return GrouperServiceRest.getMembershipsLite(clientVersion, groupName, null, 
            null, wsRestGetMembershipsLiteRequest);
        
      }
  
      if (GrouperUtil.length(urlStrings) == 0 && requestObject == null || 
          requestObject instanceof WsRestGetMembershipsRequest) {
  
        WsRestGetMembershipsRequest wsRestGetMembershipsRequest = 
          (WsRestGetMembershipsRequest)requestObject;
        
        //url should be: /xhtml/v1_3_000/groups/a:b/memberships
        return GrouperServiceRest.getMemberships(clientVersion, groupName, null, 
            null, wsRestGetMembershipsRequest);
        
      }
      
      throw new RuntimeException("Invalid REST GET Group request: " + clientVersion + " , " + groupName 
          + ", " + GrouperUtil.toStringForLog(urlStrings));
    }
  
  };

  /**
     * handle the incoming request based on HTTP method GET and group as resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "members"}
     * @param groupName in url
     * @param requestObject is the request body converted to object
     * @return the return object
   */
  public abstract WsResponseBean service(
      GrouperVersion clientVersion, String groupName, List<String> urlStrings,
      WsRequestBean requestObject);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static GrouperWsRestGetGroup valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsRestGetGroup.class, 
        string, exceptionOnNotFound);
  }

}
