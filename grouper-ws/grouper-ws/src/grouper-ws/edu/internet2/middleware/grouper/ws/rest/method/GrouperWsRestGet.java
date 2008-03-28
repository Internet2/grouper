/*
 * @author mchyzer $Id: GrouperWsRestGet.java,v 1.3 2008-03-28 16:45:00 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.rest.group.GrouperWsRestGetGroup;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.GrouperWsRestGetSubject;
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
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /xhtml/v1_3_000/groups/aStem:aGroup/members?subjectIdentifierRequested=pennkey
      String groupName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      //validate and get the operation
      GrouperWsRestGetGroup grouperWsRestGetGroup = GrouperWsRestGetGroup
          .valueOfIgnoreCase(operation, true);

      return grouperWsRestGetGroup.service(
          clientVersion, groupName, urlStrings, requestObject);
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
        GrouperWsVersion clientVersion, List<String> urlStrings,
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
        
        return GrouperServiceRest.getGroups(clientVersion, (WsRestGetGroupsRequest)requestObject);
        
      }
      
      //validate and get the operation
      GrouperWsRestGetSubject grouperWsRestGetSubject = GrouperWsRestGetSubject
          .valueOfIgnoreCase(operation, true);

      return grouperWsRestGetSubject.service(
          clientVersion, subjectId, sourceId, urlStrings, requestObject);
    }

  };

  /**
   * handle the incoming request based on HTTP method
   * @param clientVersion version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/group/a:b
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
  public static GrouperWsRestGet valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsRestGet.class, 
        string, exceptionOnNotFound);
  }
}
