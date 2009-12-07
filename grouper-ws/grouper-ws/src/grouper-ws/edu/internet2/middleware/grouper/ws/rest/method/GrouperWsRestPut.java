/*
 * @author mchyzer $Id: GrouperWsRestPut.java,v 1.10 2009-12-07 07:31:14 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectRequest;
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
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /v1_3_000/groups/aStem:aGroup/members
      String groupName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      if (requestObject instanceof WsRestGroupSaveRequest) {
        if (!StringUtils.isBlank(groupName)) {
          throw new WsInvalidQueryException("Dont pass group name when saving batch groups: '" + groupName + "'");
        }
        if (!StringUtils.isBlank(groupName)) {
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
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /v1_3_000/stems/aStem:aStem2
      String stemName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      if (requestObject instanceof WsRestStemSaveRequest) {
        if (!StringUtils.isBlank(stemName)) {
          throw new WsInvalidQueryException("Dont pass stem name when saving batch stems: '" + stemName + "'");
        }
        //TODO why is condition same
        if (!StringUtils.isBlank(stemName)) {
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
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //handle the URL: /groups with nothing after...
      if (urlStrings.size() == 0) {
        
        if (requestObject instanceof WsRestAssignGrouperPrivilegesLiteRequest) {
          
          //find stems
          return GrouperServiceRest.assignGrouperPrivilegesLite(clientVersion,
              (WsRestAssignGrouperPrivilegesLiteRequest)requestObject);
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
          GrouperWsVersion clientVersion, List<String> urlStrings,
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
      GrouperWsVersion clientVersion, List<String> urlStrings, WsRequestBean requestObject);

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
