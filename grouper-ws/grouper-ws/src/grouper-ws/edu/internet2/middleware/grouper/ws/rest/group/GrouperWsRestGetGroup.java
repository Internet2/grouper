/*
 * @author mchyzer $Id: GrouperWsRestGetGroup.java,v 1.1 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.GrouperServiceRest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

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
     * for http://localhost/grouper-ws/servicesRest/xhtml/xhtml/v3_0_000/group/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "members"}
     * @param groupName in url
     * @param requestObject is the request body converted to object
     * @return the return object
     */
    @Override
    public WsResponseBean service(
        GrouperWsVersion clientVersion, String groupName, List<String> urlStrings,
        WsRequestBean requestObject) {

      //make sure right type
      WsRestGetMembersLiteRequest wsRestGetMembersLiteRequest = GrouperUtil.typeCast(
          requestObject, WsRestGetMembersLiteRequest.class);

      //url should be: /xhtml/v1_3_000/group/aStem:aGroup/members
      return GrouperServiceRest.getMembersLite(
          clientVersion, groupName,
          wsRestGetMembersLiteRequest);

    }

  };

  /**
     * handle the incoming request based on HTTP method GET and group as resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/group/a:b/members
     * the urlStrings would be size three: {"group", "a:b", "members"}
     * @param groupName in url
     * @param requestObject is the request body converted to object
     * @return the return object
   */
  public abstract WsResponseBean service(
      GrouperWsVersion clientVersion, String groupName, List<String> urlStrings,
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
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (GrouperWsRestGetGroup grouperWsRestGetGroup : GrouperWsRestGetGroup.values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperWsRestGetGroup.name())) {
        return grouperWsRestGetGroup;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find GrouperWsRestGetGroup from string: '").append(string);
    error.append("', expecting one of: ");
    for (GrouperWsRestGetGroup grouperWsRestGetGroup : GrouperWsRestGetGroup.values()) {
      error.append(grouperWsRestGetGroup.name()).append(", ");
    }
    throw new GrouperRestInvalidRequest(error.toString());
  }

}
