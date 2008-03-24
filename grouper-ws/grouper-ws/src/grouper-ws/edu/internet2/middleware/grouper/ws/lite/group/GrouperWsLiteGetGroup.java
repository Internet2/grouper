/*
 * @author mchyzer $Id: GrouperWsLiteGetGroup.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite.group;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.lite.GrouperLiteInvalidRequest;
import edu.internet2.middleware.grouper.ws.lite.GrouperServiceLite;
import edu.internet2.middleware.grouper.ws.lite.WsRequestBean;
import edu.internet2.middleware.grouper.ws.lite.WsResponseBean;

/**
 * all first level resources on a get request
 */
public enum GrouperWsLiteGetGroup {

  /** group get requests for members */
  members {

    /**
     * handle the incoming request based on HTTP method GET and group as resource, and members as subresource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesLite/xhtml/xhtml/v3_0_000/group/a:b/members
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
      WsLiteGetMembersSimpleRequest wsLiteGroupGetMembersRequest = GrouperUtil.typeCast(
          requestObject, WsLiteGetMembersSimpleRequest.class);

      //url should be: /xhtml/v1_3_000/group/aStem:aGroup/members
      return GrouperServiceLite.getMembersSimple(
          clientVersion, groupName,
          wsLiteGroupGetMembersRequest);

    }

  };

  /**
     * handle the incoming request based on HTTP method GET and group as resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesLite/xhtml/v3_0_000/group/a:b/members
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
   * @throws GrouperLiteInvalidRequest if there is a problem
   */
  public static GrouperWsLiteGetGroup valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperLiteInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (GrouperWsLiteGetGroup grouperWsLiteGetGroup : GrouperWsLiteGetGroup.values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperWsLiteGetGroup.name())) {
        return grouperWsLiteGetGroup;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find GrouperWsLiteGetGroup from string: '").append(string);
    error.append("', expecting one of: ");
    for (GrouperWsLiteGetGroup grouperWsLiteGetGroup : GrouperWsLiteGetGroup.values()) {
      error.append(grouperWsLiteGetGroup.name()).append(", ");
    }
    throw new GrouperLiteInvalidRequest(error.toString());
  }

}
