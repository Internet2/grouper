/*
 * @author mchyzer $Id: GrouperWsRestDelete.java,v 1.1 2008-03-26 07:39:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;
import edu.internet2.middleware.grouper.ws.rest.group.GrouperWsRestDeleteGroup;

/**
 * all first level resources on a Delete request
 */
public enum GrouperWsRestDelete {

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
      String groupName = null;
      int urlStringsLength = GrouperUtil.length(urlStrings);

      if (urlStringsLength > 1) {
        groupName = urlStrings.get(1);
      }
      String operation = null;

      if (urlStringsLength > 2) {
        operation = urlStrings.get(2);
      }

      //validate and get the operation
      GrouperWsRestDeleteGroup grouperWsRestDeleteGroup = GrouperWsRestDeleteGroup
          .valueOfIgnoreCase(operation, true);

      return grouperWsRestDeleteGroup.service(clientVersion, groupName, urlStrings, requestObject);
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
  public static GrouperWsRestDelete valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (GrouperWsRestDelete grouperWsRestDelete : GrouperWsRestDelete.values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperWsRestDelete.name())) {
        return grouperWsRestDelete;
      }
    }
    StringBuilder error = new StringBuilder("Cant find grouperWsLiteDelete from string: '")
        .append(string);
    error.append("', expecting one of: ");
    for (GrouperWsRestDelete grouperWsLiteDelete : GrouperWsRestDelete.values()) {
      error.append(grouperWsLiteDelete.name()).append(", ");
    }
    throw new GrouperRestInvalidRequest(error.toString());
  }

}
