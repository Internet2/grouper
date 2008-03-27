/*
 * @author mchyzer $Id: GrouperWsRestPut.java,v 1.2 2008-03-27 20:39:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.rest.group.GrouperWsRestPutGroup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * all first level resources on a put request
 */
public enum GrouperWsRestPut {

  /** group get requests */
  group {

    /**
     * handle the incoming request based on GET HTTP method and group resource
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/group/a:b
     * the urlStrings would be size two: {"group", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public WsResponseBean service(
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {

      //url should be: /v1_3_000/group/aStem:aGroup/members
      String groupName = GrouperServiceUtils.popUrlString(urlStrings);
      String operation = GrouperServiceUtils.popUrlString(urlStrings);

      //validate and get the operation
      GrouperWsRestPutGroup grouperWsRestPutGroup = GrouperWsRestPutGroup
          .valueOfIgnoreCase(operation, true);

      return grouperWsRestPutGroup.service(clientVersion, groupName, urlStrings, requestObject);
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
  public static GrouperWsRestPut valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    return GrouperServiceUtils.enumValueOfIgnoreCase(GrouperWsRestPut.class, 
        string, exceptionOnNotFound);
  }

}
