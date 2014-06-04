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
 * @author mchyzer $Id: GrouperWsRestDeleteGroup.java,v 1.2 2008-03-30 09:01:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * all first level resources on a delete request
 */
public enum GrouperWsRestDeleteGroup {

  /** group delete requests for members */
  members {

    /**
     * handle the incoming request based on HTTP method Delete and group as resource, and members as subresource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/xhtml/v3_0_000/groups/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "members"}
     * @param groupName in url
     * @param requestObject is the request body converted to object
     * @return the return object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, String groupName, List<String> urlStrings,
        WsRequestBean requestObject) {

      //maybe deleting all members (url strings size 3)
      //url should be: /v1_3_000/groups/aStem:aGroup/members
      if (urlStrings.size() == 0 && (!(requestObject instanceof WsRestDeleteMemberLiteRequest))) {
        
        WsRestDeleteMemberRequest wsRestDeleteMembersRequest = GrouperUtil.typeCast(
            requestObject, WsRestDeleteMemberRequest.class);
        
        return GrouperServiceRest.deleteMember(clientVersion, groupName, wsRestDeleteMembersRequest);
        
      }
      
      //make sure right type
      WsRestDeleteMemberLiteRequest wsRestDeleteMemberLiteRequest = GrouperUtil.typeCast(
          requestObject, WsRestDeleteMemberLiteRequest.class);
      
      //url should be: /v1_3_000/groups/aStem:aGroup/members/123412345
      String subjectId = GrouperServiceUtils.extractSubjectInfoFromUrlStrings(urlStrings, 0, false, false);
      String sourceId = GrouperServiceUtils.extractSubjectInfoFromUrlStrings(urlStrings, 0, true, true);

      return GrouperServiceRest.deleteMemberLite(clientVersion, groupName, subjectId, sourceId,
          wsRestDeleteMemberLiteRequest);

    }

  };

  /**
   * handle the incoming request based on HTTP method DELETE and group as resource
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
  public static GrouperWsRestDeleteGroup valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsRestDeleteGroup.class, 
        string, exceptionOnNotFound);
  }

}
