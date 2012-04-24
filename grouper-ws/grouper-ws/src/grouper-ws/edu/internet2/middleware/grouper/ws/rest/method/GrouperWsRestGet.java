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
 * @author mchyzer $Id: GrouperWsRestGet.java,v 1.9 2009-12-29 07:39:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * all first level resources on a get request
 */
public enum GrouperWsRestGet {

  /** group get requests */
  groups {

    /**
     * handle the incoming request based on GET HTTP method and group resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b
     * the urlStrings would be size two: {"groups", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /xhtml/v1_3_000/groups/aStem:aGroup/members?subjectIdentifierRequested=pennkey
      String groupName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);
      
      //handle the URL: /groups with nothing after...
      if (StringUtils.isBlank(groupName) && StringUtils.isBlank(operation)) {
        if (requestObject instanceof WsRestGetMembersRequest) {
          
          //get members of multiple groups
          return GrouperServiceRest.getMembers(clientVersion,
              (WsRestGetMembersRequest)requestObject);
        }
        
        if (requestObject instanceof WsRestFindGroupsRequest) {
          
          //find groups
          return GrouperServiceRest.findGroups(clientVersion,
              (WsRestFindGroupsRequest)requestObject);
        }
        if (requestObject instanceof WsRestFindGroupsLiteRequest) {
          
          //find groups lite
          return GrouperServiceRest.findGroupsLite(clientVersion,
              (WsRestFindGroupsLiteRequest)requestObject);
        }
        if (requestObject instanceof WsRestHasMemberRequest) {
          
          //has member
          return GrouperServiceRest.hasMember(clientVersion, null,
              (WsRestHasMemberRequest)requestObject);
        }
        if (requestObject instanceof WsRestHasMemberLiteRequest) {
          
          //has member lite
          return GrouperServiceRest.hasMemberLite(clientVersion,null, null, null,
              (WsRestHasMemberLiteRequest)requestObject);
        }
      }
      
      //validate and get the operation
      GrouperWsRestGetGroup grouperWsRestGetGroup = GrouperWsRestGetGroup
          .valueOfIgnoreCase(operation, true);

      return grouperWsRestGetGroup.service(
          clientVersion, groupName, urlStrings, requestObject);
    }

  },
  
  /** attributeDefNames get requests */
  attributeDefNames {

    /**
     * handle the incoming request based on GET HTTP method and attributeDefName resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/attributeDefNames/a:b
     * the urlStrings would be size two: {"attributeDefNames", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /xhtml/v1_3_000/attributeDefNames/aStem:aGroup
      String attributeDefNameName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);
      
      if (!StringUtils.isBlank(operation)) {
        throw new WsInvalidQueryException("Why is operation sent in??? " + operation);
      }
      
      //handle the URL: /groups with nothing after...
      if (StringUtils.isBlank(attributeDefNameName)) {
        if (requestObject instanceof WsRestFindAttributeDefNamesRequest) {
          
          //find attribute def names
          return GrouperServiceRest.findAttributeDefNames(clientVersion,
              (WsRestFindAttributeDefNamesRequest)requestObject);
        }
      }
      if (requestObject == null || requestObject instanceof WsRestFindAttributeDefNamesLiteRequest) {
        
        //find attribute def names lite
        return GrouperServiceRest.findAttributeDefNamesLite(clientVersion, attributeDefNameName,
            (WsRestFindAttributeDefNamesLiteRequest)requestObject);
        
      }
      if (!StringUtils.isBlank(attributeDefNameName)) {
        throw new WsInvalidQueryException("If you pass in an attributeDefNameName then you must not pass in body or a WsRestFindAttributeDefNamesLiteRequest");
      }
        
      throw new WsInvalidQueryException("Invalid input: " + (requestObject == null ? null : requestObject.getClass()));
    }

  },
  
  /** attribute get requests */
  attributeAssignments {

    /**
     * handle the incoming request based on GET HTTP method and group resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/attributeAssignments
     * the urlStrings would be size one: {"attributeAssignments"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /xhtml/v1_3_000/attributeAssignments
      String somethingElse = GrouperServiceUtils.popUrlString(urlStrings);
      
      if (!StringUtils.isBlank(somethingElse)) {
        throw new RuntimeException("Cant pass anything after 'attributeAssignments' in URL");
      }

      if (requestObject instanceof WsRestGetAttributeAssignmentsRequest) {

        //get attributeAssignments
        return GrouperServiceRest.getAttributeAssignments(clientVersion,
            (WsRestGetAttributeAssignmentsRequest)requestObject);
        
      } else if (requestObject instanceof WsRestGetAttributeAssignmentsLiteRequest) {
        
        //get attributeAssignments
        return GrouperServiceRest.getAttributeAssignmentsLite(clientVersion,
            (WsRestGetAttributeAssignmentsLiteRequest)requestObject);

      } else {
        throw new RuntimeException("Must pass in a request object of type " 
            + WsRestGetAttributeAssignmentsRequest.class.getSimpleName() + " or "
            + WsRestGetAttributeAssignmentsLiteRequest.class.getSimpleName());
      }
      
    }

  },
  
  /** stem get requests */
  stems {

    /**
     * handle the incoming request based on GET HTTP method and group resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/stems
     * the urlStrings would be size one: {"stems"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /xhtml/v1_3_000/stems/aStem
      String stemName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);
      
      //handle the URL: /groups with nothing after...
      if (StringUtils.isBlank(stemName) && StringUtils.isBlank(operation)) {
        
        if (requestObject instanceof WsRestFindStemsRequest) {
          
          //find stems
          return GrouperServiceRest.findStems(clientVersion,
              (WsRestFindStemsRequest)requestObject);
        }
        if (requestObject instanceof WsRestFindStemsLiteRequest) {
          
          //find stems lite
          return GrouperServiceRest.findStemsLite(clientVersion,
              (WsRestFindStemsLiteRequest)requestObject);
        }
      }
      throw new RuntimeException("Invalid get stem request: " + clientVersion 
          + ", " + stemName + ", " + operation + ", " + GrouperUtil.toStringForLog(urlStrings) + ", " + GrouperUtil.className(requestObject));
    }

  },
  
  /** grouperPrivileges get requests */
  grouperPrivileges {

    /**
     * handle the incoming request based on GET HTTP method and group resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/grouperPrivileges
     * the urlStrings would be size one: {"grouperPrivileges"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //handle the URL: /groups with nothing after...
      if (urlStrings.size() == 0) {
        
        if (requestObject instanceof WsRestGetGrouperPrivilegesLiteRequest) {
          
          //find stems
          return GrouperServiceRest.getGrouperPrivilegesLite(clientVersion,
              (WsRestGetGrouperPrivilegesLiteRequest)requestObject);
        }
      }
      throw new RuntimeException("Invalid get grouper privileges request: " + clientVersion 
          + ", " + GrouperUtil.toStringForLog(urlStrings) + ", " + GrouperUtil.className(requestObject));
    }

  },
  
  /** subject get requests */
  subjects {

    /**
     * handle the incoming request based on GET HTTP method and subject resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/subjects/1234
     * the urlStrings would be size two: {"subjects", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /xhtml/v1_3_000/subjects/1234/groups
      //url should be: /v1_3_000/subjects/sourceId/abc/subjectId/1234/groups
      
      String subjectId = GrouperServiceUtils.extractSubjectInfoFromUrlStrings(
          urlStrings, 0, false, false);
      String sourceId = GrouperServiceUtils.extractSubjectInfoFromUrlStrings(
          urlStrings, 0, true, true);
      
      //e.g. groups
      String operation = GrouperServiceUtils.popUrlString(urlStrings);
      
      //if (operation is null and the request object says get groups for list of subjects, then
      //do that
      if (StringUtils.isBlank(operation) && (requestObject instanceof WsRestGetGroupsRequest)) {
        
        return GrouperServiceRest.getGroups(clientVersion, subjectId, sourceId, (WsRestGetGroupsRequest)requestObject);
        
      }
      
      if (StringUtils.isBlank(operation) && (requestObject == null || requestObject instanceof WsRestGetSubjectsRequest)) {
        
        return GrouperServiceRest.getSubjects(clientVersion, subjectId, sourceId, (WsRestGetSubjectsRequest)requestObject);
        
      }
      
      if (StringUtils.isBlank(operation) && requestObject instanceof WsRestGetSubjectsLiteRequest) {
        
        return GrouperServiceRest.getSubjectsLite(clientVersion, subjectId, sourceId, (WsRestGetSubjectsLiteRequest)requestObject);
        
      }
      
      
      //validate and get the operation
      GrouperWsRestGetSubject grouperWsRestGetSubject = GrouperWsRestGetSubject
          .valueOfIgnoreCase(operation, true);

      return grouperWsRestGetSubject.service(
          clientVersion, subjectId, sourceId, urlStrings, requestObject);
    }

  }, 
  /** group get requests */
  memberships {
  
    /**
     * handle the incoming request based on GET HTTP method and memberships resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b
     * the urlStrings would be size two: {"groups", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {
  
      //url should be: /xhtml/v1_3_000/memberships?something=somethingelse
      String next = GrouperServiceUtils.popUrlString(urlStrings);

      if (!StringUtils.isBlank(next)) {
        throw new RuntimeException("Why is there a param here, shouldnt be: " + next);
      }

      if (requestObject instanceof WsRestGetMembershipsLiteRequest) {
        
        //get memberships
        return GrouperServiceRest.getMembershipsLite(clientVersion,null, null, null,
            (WsRestGetMembershipsLiteRequest)requestObject);
      } else if (requestObject instanceof WsRestGetMembershipsRequest) {
        
        //get memberships
        return GrouperServiceRest.getMemberships(clientVersion,null, null, null,
            (WsRestGetMembershipsRequest)requestObject);
      } else {
        throw new RuntimeException("Not expecting object type: " + GrouperUtil.className(requestObject) 
            + ", must be a WsRestGetMembershipsLiteRequest or WsRestGetMembershipsRequest");
      }

      
    }
  
  }, 
  
  /** permission get requests */
  permissionAssignments{
  
    /**
     * handle the incoming request based on GET HTTP method and group resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/permissionAssignments
     * the urlStrings would be size one: {"permissionAssignments"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {
  
      //url should be: /xhtml/v1_3_000/permissionAssignments
      String somethingElse = GrouperServiceUtils.popUrlString(urlStrings);
      
      if (!StringUtils.isBlank(somethingElse)) {
        throw new RuntimeException("Cant pass anything after 'permissionAssignments' in URL");
      }
  
      if (requestObject instanceof WsRestGetPermissionAssignmentsRequest) {
  
        //get permissions
        return GrouperServiceRest.getPermissionAssignments(clientVersion,
            (WsRestGetPermissionAssignmentsRequest)requestObject);
        
      } else if (requestObject instanceof WsRestGetPermissionAssignmentsLiteRequest) {
        
        //get permissions
        return GrouperServiceRest.getPermissionAssignmentsLite(clientVersion,
            (WsRestGetPermissionAssignmentsLiteRequest)requestObject);
  
      } else {
        throw new RuntimeException("Must pass in a request object of type " 
            + WsRestGetPermissionAssignmentsRequest.class.getSimpleName() + " or "
            + WsRestGetPermissionAssignmentsLiteRequest.class.getSimpleName());
      }
      
    }
  
  };

  /**
   * handle the incoming request based on HTTP method
   * @param clientVersion version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param requestObject is the request body converted to object
   * @return the result object
   */
  public abstract WsResponseBean service(
      GrouperVersion clientVersion, List<String> urlStrings, WsRequestBean requestObject);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static GrouperWsRestGet valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsRestGet.class, 
        string, exceptionOnNotFound);
  }
}
