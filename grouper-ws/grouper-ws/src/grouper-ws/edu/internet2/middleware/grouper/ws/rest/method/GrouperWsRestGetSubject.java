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
 * @author mchyzer $Id: GrouperWsRestGetSubject.java,v 1.2 2009-12-18 02:43:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * all first level resources on a get request
 */
public enum GrouperWsRestGetSubject {

  /** group get requests for groups */
  groups {

    /**
     * handle the incoming request based on HTTP method GET and group as resource, and members as subresource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "member"}
     * @param sourceId in url (if applicable)
     * @param subjectId in url (if applicable)
     * @param requestObject is the request body converted to object
     * @return the return object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, String subjectId,  String sourceId, 
        List<String> urlStrings,
        WsRequestBean requestObject) {

      if (GrouperUtil.length(urlStrings) == 0 && (requestObject == null || 
          requestObject instanceof WsRestGetGroupsLiteRequest)) {

        WsRestGetGroupsLiteRequest wsRestGetGroupsLiteRequest = 
          (WsRestGetGroupsLiteRequest)requestObject;
        
        //url should be: /xhtml/v1_3_000/subjects/12345/groups
        return GrouperServiceRest.getGroupsLite(clientVersion, subjectId, 
            sourceId, wsRestGetGroupsLiteRequest);
        
      }
      
      throw new RuntimeException("Invalid REST GET Subject / Group request: " + clientVersion 
          + " , subjectId: " + subjectId + ", sourceId: " + sourceId  
          + ", " + GrouperUtil.toStringForLog(urlStrings) + ", requestObject: " 
          + GrouperUtil.className(requestObject));
    }

  }, 
  
  /** group get requests for subject */
  memberships {
  
    /**
     * handle the incoming request based on HTTP method GET and subject as resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b
     * the urlStrings would be size two: {"groups", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the result object
     * @param sourceId in url (if applicable)
     * @param subjectId in url (if applicable)
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, String subjectId,  String sourceId, 
        List<String> urlStrings,
        WsRequestBean requestObject) {
  
      if (GrouperUtil.length(urlStrings) == 0 && (requestObject == null || 
          requestObject instanceof WsRestGetMembershipsLiteRequest)) {
  
        WsRestGetMembershipsLiteRequest wsRestGetMembershipsLiteRequest = 
          (WsRestGetMembershipsLiteRequest)requestObject;
        
        //url should be: /xhtml/v1_3_000/subjects/12345/memberships
        return GrouperServiceRest.getMembershipsLite(clientVersion, null, subjectId, 
            sourceId, wsRestGetMembershipsLiteRequest);
        
      }
  
      if (GrouperUtil.length(urlStrings) == 0 &&
          requestObject instanceof WsRestGetMembershipsRequest) {
  
        WsRestGetMembershipsRequest wsRestGetMembershipsRequest = 
          (WsRestGetMembershipsRequest)requestObject;
        
        //url should be: /xhtml/v1_3_000/subjects/12345/memberships
        return GrouperServiceRest.getMemberships(clientVersion, null, subjectId, 
            sourceId, wsRestGetMembershipsRequest);
        
      }
      
      throw new RuntimeException("Invalid REST GET Subject / Group request: " + clientVersion 
          + " , subjectId: " + subjectId + ", sourceId: " + sourceId  
          + ", " + GrouperUtil.toStringForLog(urlStrings) + ", requestObject: " 
          + GrouperUtil.className(requestObject));
    }
  
  };

  /**
     * handle the incoming request based on HTTP method GET and group as resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "members"}
     * @param sourceId in url (if applicable)
     * @param subjectId in url (if applicable)
     * @param requestObject is the request body converted to object
     * @return the return object
   */
  public abstract WsResponseBean service(
      GrouperVersion clientVersion, String subjectId, String sourceId, 
      List<String> urlStrings,
      WsRequestBean requestObject);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static GrouperWsRestGetSubject valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsRestGetSubject.class, 
        string, exceptionOnNotFound);
  }

}
