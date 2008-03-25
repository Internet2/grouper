/*
 * @author mchyzer $Id: GrouperRestHttpMethod.java,v 1.1 2008-03-25 05:15:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.rest.GrouperRestInvalidRequest;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.WsResponseBean;

/**
 * types of http methods accepted by grouper rest
 */
public enum GrouperRestHttpMethod {

  /** GET */
  GET {

    /**
     * handle the incoming request based on HTTP method
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/group/a:b
     * the urlStrings would be size two: {"group", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the resultObject
     */
    @Override
    public WsResponseBean service(
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {
      int urlStringsLength = GrouperUtil.length(urlStrings);

      //skip the request/response type, and the version
      String firstResource = null;
      if (urlStringsLength > 0) {
        firstResource = urlStrings.get(0);
      }

      //validate and get the first resource
      GrouperWsRestGet grouperWsRestGet = GrouperWsRestGet.valueOfIgnoreCase(
          firstResource, true);

      return grouperWsRestGet.service(clientVersion, urlStrings, requestObject);
    }

  },

  /** POST */
  POST {

    /**
     * handle the incoming request based on HTTP method
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/group/a:b
     * the urlStrings would be size two: {"group", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the resultObject
     */
    @Override
    public WsResponseBean service(
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {
      throw new RuntimeException("Invalid POST request");
    }

  },

  /** PUT */
  PUT {

    /**
     * handle the incoming request based on HTTP method
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/group/a:b
     * the urlStrings would be size two: {"group", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the resultObject
     */
    @Override
    public WsResponseBean service(
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {
      
      int urlStringsLength = GrouperUtil.length(urlStrings);

      //skip the request/response type, and the version
      String firstResource = null;
      if (urlStringsLength > 0) {
        firstResource = urlStrings.get(0);
      }

      //validate and get the first resource
      GrouperWsRestPut grouperWsRestPut = GrouperWsRestPut.valueOfIgnoreCase(
          firstResource, true);

      return grouperWsRestPut.service(
          clientVersion, urlStrings, requestObject);
    }

  },

  /** DELETE */
  DELETE {

    /**
     * handle the incoming request based on HTTP method
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/group/a:b
     * the urlStrings would be size two: {"group", "a:b"}
     * @param requestObject is the request body converted to object
     * @return the resultObject
     */
    @Override
    public WsResponseBean service(
        GrouperWsVersion clientVersion, List<String> urlStrings,
        WsRequestBean requestObject) {
      throw new RuntimeException("Invalid DELETE request");
    }

  };

  /**
   * handle the incoming request based on HTTP method
   * @param clientVersion version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/group/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param requestObject is the request body converted to object
   * @return the resultObject
   */
  public abstract WsResponseBean service(
      GrouperWsVersion clientVersion, List<String> urlStrings, WsRequestBean requestObject);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static GrouperRestHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperRestInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (GrouperRestHttpMethod grouperRestHttpMethod : GrouperRestHttpMethod.values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperRestHttpMethod.name())) {
        return grouperRestHttpMethod;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find grouperLiteHttpMethod from string: '").append(string);
    error.append("', expecting one of: ");
    for (GrouperRestHttpMethod grouperLiteHttpMethod : GrouperRestHttpMethod.values()) {
      error.append(grouperLiteHttpMethod.name()).append(", ");
    }
    throw new GrouperRestInvalidRequest(error.toString());
  }
}
