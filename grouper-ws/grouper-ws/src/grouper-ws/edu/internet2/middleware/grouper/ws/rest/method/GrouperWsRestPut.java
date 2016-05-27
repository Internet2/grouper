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
 * @author mchyzer $Id: GrouperWsRestPut.java,v 1.10 2009/12/07 07:31:14 mchyzer Exp $
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
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefActionsRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesBatchRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectRequest;
import edu.internet2.middleware.grouper.ws.rest.messaging.WsRestAcknowledgeMessageRequest;
import edu.internet2.middleware.grouper.ws.rest.messaging.WsRestSendMessageRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * all first level resources on a put request
 */
public enum GrouperWsRestPut {

  /** group put requests */
  groups {

    /**
     * handle the incoming request based on PUT HTTP method and group resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b
     * the urlStrings would be size two: {"group", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /v1_3_000/groups/aStem:aGroup/members
      String groupName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      if (requestObject instanceof WsRestGroupSaveRequest) {
        if (!StringUtils.isBlank(groupName)) {
          throw new WsInvalidQueryException("Dont pass group name when saving batch groups: '" + groupName + "'");
        }
        if (!StringUtils.isBlank(operation)) {
          throw new WsInvalidQueryException("Dont pass sub resource when saving batch groups: '" + operation + "'");
        }
        return GrouperServiceRest.groupSave(clientVersion, (WsRestGroupSaveRequest)requestObject);
      }

      if (requestObject instanceof WsRestGroupSaveLiteRequest && !StringUtils.isBlank(operation)) {
        throw new WsInvalidQueryException("Dont pass sub resource when saving group: '" + operation + "'");
      }
      
      if ((requestObject == null || requestObject instanceof WsRestGroupSaveLiteRequest) 
          && StringUtils.isBlank(operation) ) {
        return GrouperServiceRest.groupSaveLite(clientVersion, groupName, (WsRestGroupSaveLiteRequest)requestObject);
      }
      
      if (requestObject instanceof WsRestAddMemberRequest && StringUtils.isBlank(operation)) {
        return GrouperServiceRest.addMember(clientVersion, groupName, (WsRestAddMemberRequest)requestObject);
      }
      
      if (requestObject instanceof WsRestAddMemberLiteRequest && StringUtils.isBlank(operation)) {
        return GrouperServiceRest.addMemberLite(clientVersion, groupName, null, null, (WsRestAddMemberLiteRequest)requestObject);
      }
      
      //validate and get the operation
      GrouperWsRestPutGroup grouperWsRestPutGroup = GrouperWsRestPutGroup
          .valueOfIgnoreCase(operation, true);

      return grouperWsRestPutGroup.service(clientVersion, groupName, urlStrings, requestObject);
    }

  },
  
  /** stem put requests */
  stems {

    /**
     * handle the incoming request based on PUT HTTP method and group resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b
     * the urlStrings would be size two: {"group", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /v1_3_000/stems/aStem:aStem2
      String stemName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      if (requestObject instanceof WsRestStemSaveRequest) {
        if (!StringUtils.isBlank(stemName)) {
          throw new WsInvalidQueryException("Dont pass stem name when saving batch stems: '" + stemName + "'");
        }
        if (!StringUtils.isBlank(operation)) {
          throw new WsInvalidQueryException("Dont pass sub resource when saving batch stems: '" + operation + "'");
        }
        return GrouperServiceRest.stemSave(clientVersion, (WsRestStemSaveRequest)requestObject);
      }

      if (requestObject instanceof WsRestStemSaveLiteRequest && !StringUtils.isBlank(operation)) {
        throw new WsInvalidQueryException("Dont pass sub resource when saving stem: '" + operation + "'");
      }

      if ((requestObject == null || requestObject instanceof WsRestStemSaveLiteRequest) 
          && StringUtils.isBlank(operation) ) {
        return GrouperServiceRest.stemSaveLite(clientVersion, stemName, (WsRestStemSaveLiteRequest)requestObject);
      }
      
      throw new RuntimeException("Invalid put stem request: " + clientVersion 
          + ", " + stemName + ", " + operation + ", " 
          + GrouperUtil.toStringForLog(urlStrings) + ", " + GrouperUtil.className(requestObject));
    }

  }, 
  
  /** attribute assign requests */
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

      if (requestObject instanceof WsRestAssignAttributesRequest) {

        //assign attributes
        return GrouperServiceRest.assignAttributes(clientVersion,
            (WsRestAssignAttributesRequest)requestObject);
        
      } else if (requestObject instanceof WsRestAssignAttributesLiteRequest) {
        
        //assign attributes
        return GrouperServiceRest.assignAttributesLite(clientVersion,
            (WsRestAssignAttributesLiteRequest)requestObject);

      } else if (requestObject instanceof WsRestAssignAttributesBatchRequest) {
        
        //assign attributes batch
        return GrouperServiceRest.assignAttributesBatch(clientVersion,
            (WsRestAssignAttributesBatchRequest)requestObject);

      } else {
        throw new RuntimeException("Must pass in a request object of type " 
            + WsRestAssignAttributesRequest.class.getSimpleName() + " or "
            + WsRestAssignAttributesLiteRequest.class.getSimpleName());
      }
      
    }

  },
  
  /** grouperPrivileges put requests */
  grouperPrivileges {

    /**
     * handle the incoming request based on PUT HTTP method and group resource
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
        
        if (requestObject instanceof WsRestAssignGrouperPrivilegesLiteRequest) {
          
          //find stems
          return GrouperServiceRest.assignGrouperPrivilegesLite(clientVersion,
              (WsRestAssignGrouperPrivilegesLiteRequest)requestObject);
        } else if (requestObject instanceof WsRestAssignGrouperPrivilegesRequest) {
          
          //find stems
          return GrouperServiceRest.assignGrouperPrivileges(clientVersion,
              (WsRestAssignGrouperPrivilegesRequest)requestObject);
        }

      }
      throw new RuntimeException("Invalid put grouper privileges request: " + clientVersion 
          + ", " + GrouperUtil.toStringForLog(urlStrings) + ", " + GrouperUtil.className(requestObject));
    }

  },
  
  
  /** group put requests */
  members{
  
      /**
       * handle the incoming request based on PUT HTTP method and members resource
       * @param clientVersion version of client, e.g. v1_3_000
       * @param urlStrings not including the app name or servlet.  
       * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/members/a:b
       * the urlStrings would be size two: {"group", "a:b"}
       * @param requestObject is the request body converted to object
       * @return the result object
       */
      @Override
      public WsResponseBean service(
          GrouperVersion clientVersion, List<String> urlStrings,
          WsRequestBean requestObject) {
  
        //url should be: /v1_3_000/members/123412345
        //or url should be: /v1_3_000/members/sourceId/someSource/subjectId/123412345
        String subjectId = GrouperServiceUtils.extractSubjectInfoFromUrlStrings(urlStrings, 0, false, false);
        String sourceId = GrouperServiceUtils.extractSubjectInfoFromUrlStrings(urlStrings, 0, true, true);

        if (requestObject instanceof WsRestMemberChangeSubjectRequest) {
          if (!StringUtils.isBlank(subjectId)) {
            throw new WsInvalidQueryException("Dont pass subjectId when changing subjects of batch members: '" + subjectId + "'");
          }
          if (!StringUtils.isBlank(sourceId)) {
            throw new WsInvalidQueryException("Dont pass sourceId when changing subjects of batch members: '" + sourceId + "'");
          }
          return GrouperServiceRest.memberChangeSubject(clientVersion, (WsRestMemberChangeSubjectRequest)requestObject);
        }
  
        if (requestObject instanceof WsRestMemberChangeSubjectLiteRequest) {
          return GrouperServiceRest.memberChangeSubjectLite(clientVersion, subjectId, sourceId, 
              (WsRestMemberChangeSubjectLiteRequest)requestObject);
        }
        
        throw new RuntimeException("Cants find handler for object: " + GrouperUtil.className(requestObject));
      }
  
    }, 
    /** permission assign requests */
    permissionAssignments {
  
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
  
      if (requestObject instanceof WsRestAssignPermissionsRequest) {
  
        //assign permissions
        return GrouperServiceRest.assignPermissions(clientVersion,
            (WsRestAssignPermissionsRequest)requestObject);
        
      } else if (requestObject instanceof WsRestAssignPermissionsLiteRequest) {
        
        //assign permissions
        return GrouperServiceRest.assignPermissionsLite(clientVersion,
            (WsRestAssignPermissionsLiteRequest)requestObject);
  
      } else {
        throw new RuntimeException("Must pass in a request object of type " 
            + WsRestAssignPermissionsRequest.class.getSimpleName() + " or "
            + WsRestAssignPermissionsLiteRequest.class.getSimpleName());
      }
      
    }
  
  }, 
  
  /** attributeDef put requests */
  attributeDefs {

    /**
     * handle the incoming request based on PUT HTTP method and attributeDef resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/attributeDefs/[nameOfAttributeDef]
     * the urlStrings would be size two: {"attributeDefs", "nameOfAttributeDef"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /v1_3_000/attributeDefs/[nameOfAttributeDef]

      String attributeDefName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      if (!StringUtils.isBlank(operation)) {
        throw new WsInvalidQueryException("Dont pass in an operation! " + operation);
      }

      if (requestObject instanceof WsRestAttributeDefSaveRequest) {
        if (!StringUtils.isBlank(attributeDefName)) {
          throw new WsInvalidQueryException(
              "Dont pass attributeDefName name when saving batch attributeDefs: '"
                  + attributeDefName + "'");
        }
        return GrouperServiceRest.attributeDefSave(clientVersion,
            (WsRestAttributeDefSaveRequest) requestObject);
      }

      if ((requestObject == null || requestObject instanceof WsRestAttributeDefSaveLiteRequest)) {
        return GrouperServiceRest.attributeDefSaveLite(clientVersion, attributeDefName,
            (WsRestAttributeDefSaveLiteRequest) requestObject);
      }

      throw new WsInvalidQueryException("Invalid request object: "
          + (requestObject == null ? null : requestObject.getClass()));
    }

  },
  
  /** attributeDefName put requests */
  attributeDefNames {
    
      /**
       * handle the incoming request based on PUT HTTP method and group resource
       * @param clientVersion version of client, e.g. v1_3_000
       * @param urlStrings not including the app name or servlet.  
       * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b
       * the urlStrings would be size two: {"group", "a:b"}
       * @param requestObject is the request body converted to object
       * @return the result object
       */
      @Override
      public WsResponseBean service(
          GrouperVersion clientVersion, List<String> urlStrings,
          WsRequestBean requestObject) {
    
        //url should be: /v1_3_000/groups/aStem:aGroup/members
        String attributeDefNameName = GrouperServiceUtils.popUrlString(urlStrings);
        String operation = GrouperServiceUtils.popUrlString(urlStrings);
    
        if (!StringUtils.isBlank(operation)) {
          throw new WsInvalidQueryException("Dont pass in an operation! " + operation);
        }
        
        if (requestObject instanceof WsRestAttributeDefNameSaveRequest) {
          if (!StringUtils.isBlank(attributeDefNameName)) {
            throw new WsInvalidQueryException("Dont pass attributeDefName name when saving batch attributeDefNames: '" + attributeDefNameName + "'");
          }
          return GrouperServiceRest.attributeDefNameSave(clientVersion, (WsRestAttributeDefNameSaveRequest)requestObject);
        }
        
        if ((requestObject == null || requestObject instanceof WsRestAttributeDefNameSaveLiteRequest) ) {
          return GrouperServiceRest.attributeDefNameSaveLite(clientVersion, attributeDefNameName, (WsRestAttributeDefNameSaveLiteRequest)requestObject);
        }
        
        if (requestObject instanceof WsRestAssignAttributeDefNameInheritanceRequest) {
          return GrouperServiceRest.assignAttributeDefNameInheritance(clientVersion, (WsRestAssignAttributeDefNameInheritanceRequest)requestObject);
        }
        
        if (requestObject instanceof WsRestAssignAttributeDefNameInheritanceLiteRequest) {
          return GrouperServiceRest.assignAttributeDefNameInheritanceLite(clientVersion, (WsRestAssignAttributeDefNameInheritanceLiteRequest)requestObject);
        }
        
        throw new WsInvalidQueryException("Invalid request object: " + ( requestObject == null ? null : requestObject.getClass()));
      }
    
    },
    
    /** attribute def actions **/
  attributeDefActions {

    /**
      * handle the incoming request based on PUT HTTP method and group resource
      * @param clientVersion version of client, e.g. v1_3_000
      * @param urlStrings not including the app name or servlet.  
      * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/attributeDefActions
      * the urlStrings would be size one: {"attributeDefActions"}
      * @param requestObject is the request body converted to object
      * @return the result object
      */
    @Override
    public WsResponseBean service(
        GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /xhtml/v1_3_000/attributeDefActions
      String somethingElse = GrouperServiceUtils.popUrlString(urlStrings);

      if (!StringUtils.isBlank(somethingElse)) {
        throw new RuntimeException(
            "Cant pass anything after 'attributeDefActions' in URL");
      }

      if (requestObject instanceof WsRestAssignAttributeDefActionsRequest) {
        return GrouperServiceRest.assignAttributeDefActions(clientVersion,
            (WsRestAssignAttributeDefActionsRequest) requestObject);
      }

      throw new WsInvalidQueryException("Invalid request object: "
          + (requestObject == null ? null : requestObject.getClass()));
    }

  },
  /** messaging put requests **/
  messaging {

    @Override
    public WsResponseBean service(GrouperVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /xhtml/v1_3_000/messages
      String somethingElse = GrouperServiceUtils.popUrlString(urlStrings);

      if (!StringUtils.isBlank(somethingElse)) {
        throw new RuntimeException(
            "Cant pass anything after 'messages' in URL");
      }

      if (requestObject instanceof WsRestSendMessageRequest) {
        //send messages
        return GrouperServiceRest.sendMessage(clientVersion,
            (WsRestSendMessageRequest) requestObject);
      }
      if (requestObject instanceof WsRestAcknowledgeMessageRequest) {
        //acknowledge messages
        return GrouperServiceRest.acknowledgeMessages(clientVersion,
            (WsRestAcknowledgeMessageRequest) requestObject);

      }

      throw new WsInvalidQueryException("Invalid request object: "
          + (requestObject == null ? null : requestObject.getClass()));

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
  public static GrouperWsRestPut valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsRestPut.class, 
        string, exceptionOnNotFound);
  }

}
