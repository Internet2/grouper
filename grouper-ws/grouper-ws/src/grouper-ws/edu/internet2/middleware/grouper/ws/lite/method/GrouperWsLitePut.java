/*
 * @author mchyzer $Id: GrouperWsLitePut.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.lite.GrouperLiteInvalidRequest;
import edu.internet2.middleware.grouper.ws.lite.WsRequestBean;
import edu.internet2.middleware.grouper.ws.lite.WsResponseBean;
import edu.internet2.middleware.grouper.ws.lite.group.GrouperWsLitePutGroup;

/**
 * all first level resources on a put request
 */
public enum GrouperWsLitePut {

  /** group get requests */
  group {

    /**
     * handle the incoming request based on GET HTTP method and group resource
     * @param wsLiteRequestContentType content type of request / response (e.g. xhtml)
     * @param wsLiteResponseContentType content type of response (e.g. xml)
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesLite/xhtml/v3_0_000/group/a:b
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
      GrouperWsLitePutGroup grouperWsLitePutGroup = GrouperWsLitePutGroup
          .valueOfIgnoreCase(operation, true);

      return grouperWsLitePutGroup.service(clientVersion, groupName, urlStrings, requestObject);
    }

  };

  /**
   * handle the incoming request based on HTTP method
   * @param clientVersion version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesLite/group/a:b
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
   * @throws GrouperLiteInvalidRequest if there is a problem
   */
  public static GrouperWsLitePut valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperLiteInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (GrouperWsLitePut grouperWsLitePut : GrouperWsLitePut.values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperWsLitePut.name())) {
        return grouperWsLitePut;
      }
    }
    StringBuilder error = new StringBuilder("Cant find grouperWsLitePut from string: '")
        .append(string);
    error.append("', expecting one of: ");
    for (GrouperWsLitePut grouperWsLitePut : GrouperWsLitePut.values()) {
      error.append(grouperWsLitePut.name()).append(", ");
    }
    throw new GrouperLiteInvalidRequest(error.toString());
  }

}
