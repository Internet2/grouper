/*
 * @author mchyzer $Id: GrouperWsLiteGet.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.lite.GrouperLiteInvalidRequest;
import edu.internet2.middleware.grouper.ws.lite.WsRequestBean;
import edu.internet2.middleware.grouper.ws.lite.WsResponseBean;
import edu.internet2.middleware.grouper.ws.lite.group.GrouperWsLiteGetGroup;

/**
 * all first level resources on a get request
 */
public enum GrouperWsLiteGet {

  /** group get requests */
  group {

    /**
     * handle the incoming request based on GET HTTP method and group resource
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

      //url should be: /xhtml/v1_3_000/group/aStem:aGroup/members?subjectIdentifierRequested=pennkey
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
      GrouperWsLiteGetGroup grouperWsLiteGetGroup = GrouperWsLiteGetGroup
          .valueOfIgnoreCase(operation, true);

      return grouperWsLiteGetGroup.service(
          clientVersion, groupName, urlStrings, requestObject);
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
  public static GrouperWsLiteGet valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperLiteInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (GrouperWsLiteGet grouperWsLiteGet : GrouperWsLiteGet.values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperWsLiteGet.name())) {
        return grouperWsLiteGet;
      }
    }
    StringBuilder error = new StringBuilder("Cant find grouperWsLiteGet from string: '")
        .append(string);
    error.append("', expecting one of: ");
    for (GrouperWsLiteGet grouperWsLiteGet : GrouperWsLiteGet.values()) {
      error.append(grouperWsLiteGet.name()).append(", ");
    }
    throw new GrouperLiteInvalidRequest(error.toString());
  }

}
