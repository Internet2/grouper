/*
 * @author mchyzer $Id: GrouperWsRestDelete.java,v 1.6 2009-12-07 07:31:14 mchyzer Exp $
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
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteRequest;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * all first level resources on a Delete request
 */
public enum GrouperWsRestDelete {

  /** group delete requests */
  groups {

    /**
     * handle the incoming request based on DELETE HTTP method and group resource
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
      //operation, e.g. members
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      if (requestObject instanceof WsRestGroupDeleteRequest) {
        return GrouperServiceRest.groupDelete(clientVersion, 
            (WsRestGroupDeleteRequest)requestObject);
      }
      
      if ((requestObject == null || requestObject instanceof WsRestGroupDeleteLiteRequest) 
          && StringUtils.isBlank(operation) ) {
        return GrouperServiceRest.groupDeleteLite(clientVersion, groupName, 
            (WsRestGroupDeleteLiteRequest)requestObject);
      }

      if (StringUtils.isBlank(groupName) && StringUtils.isBlank(operation)) {
        
        if (requestObject instanceof WsRestDeleteMemberRequest) {
          
          return GrouperServiceRest.deleteMember(clientVersion, null, (WsRestDeleteMemberRequest)requestObject);
          
        }
        
        if (requestObject instanceof WsRestDeleteMemberLiteRequest) {
          
          return GrouperServiceRest.deleteMemberLite(clientVersion, null, null, null, (WsRestDeleteMemberLiteRequest)requestObject);
          
        }
        
      }
      
      //validate and get the operation
      GrouperWsRestDeleteGroup grouperWsRestDeleteGroup = GrouperWsRestDeleteGroup
          .valueOfIgnoreCase(operation, true);

      return grouperWsRestDeleteGroup.service(clientVersion, groupName, urlStrings, requestObject);
    }

  }, 
  
  /** attributeDefName delete requests */
  attributeDefNames {

    /**
     * handle the incoming request based on DELETE HTTP method and attributeDefName resource
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

      //url should be: /v1_3_000/attributeDefNames/aStem:aGroup
      String attributeDefNameName = GrouperServiceUtils.popUrlString(urlStrings);

      //operation, e.g. members
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      if (!StringUtils.isBlank(operation)) {
        throw new WsInvalidQueryException("Why are you passing something after the attributeDefName name??? " + operation);
      }
      
      if (StringUtils.isBlank(attributeDefNameName) && requestObject instanceof WsRestAttributeDefNameDeleteRequest) {
        return GrouperServiceRest.attributeDefNameDelete(clientVersion, 
            (WsRestAttributeDefNameDeleteRequest)requestObject);
      }
      
      if (requestObject == null || requestObject instanceof WsRestAttributeDefNameDeleteLiteRequest) {
        return GrouperServiceRest.attributeDefNameDeleteLite(clientVersion, attributeDefNameName, 
            (WsRestAttributeDefNameDeleteLiteRequest)requestObject);
      }

      if (!StringUtils.isBlank(attributeDefNameName)) {
        throw new WsInvalidQueryException("Dont send in attrbuteDefNameName " + requestObject.getClass());
      }
      
      throw new WsInvalidQueryException("Not expecting type: " + requestObject.getClass());
    }

  }, 
  
  /** stem delete requests */
  stems {
  
      /**
       * handle the incoming request based on DELETE HTTP method and stem resource
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
        //operation not valid yet
        String operation = GrouperServiceUtils.popUrlString(urlStrings);
  
        if (requestObject instanceof WsRestStemDeleteRequest) {
          return GrouperServiceRest.stemDelete(clientVersion, 
              (WsRestStemDeleteRequest)requestObject);
        }
        
        if ((requestObject == null || requestObject instanceof WsRestStemDeleteLiteRequest) 
            && StringUtils.isBlank(operation) ) {
          return GrouperServiceRest.stemDeleteLite(clientVersion, stemName, 
              (WsRestStemDeleteLiteRequest)requestObject);
        }
        
        throw new RuntimeException("Invalid delete stem request: " + clientVersion 
            + ", " + stemName + ", " + operation 
            + ", " + GrouperUtil.toStringForLog(urlStrings) 
            + ", " + GrouperUtil.className(requestObject));
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
  public static GrouperWsRestDelete valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsRestDelete.class, 
        string, exceptionOnNotFound);
  }

}
