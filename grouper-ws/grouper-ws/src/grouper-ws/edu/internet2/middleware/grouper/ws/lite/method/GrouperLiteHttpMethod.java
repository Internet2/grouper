/*
 * @author mchyzer $Id: GrouperLiteHttpMethod.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite.method;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.lite.GrouperLiteInvalidRequest;
import edu.internet2.middleware.grouper.ws.lite.WsRequestBean;
import edu.internet2.middleware.grouper.ws.lite.WsResponseBean;

/**
 * types of http methods accepted by grouper lite
 */
public enum GrouperLiteHttpMethod {

  /** GET */
  GET {

    /**
     * handle the incoming request based on HTTP method
     * @param wsLiteRequestContentType content type of request / response (e.g. xhtml)
     * @param wsLiteResponseContentType content type of response (e.g. xml)
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/grouper-ws/servicesLite/xhtml/v3_0_000/group/a:b
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
      GrouperWsLiteGet grouperWsLiteGet = GrouperWsLiteGet.valueOfIgnoreCase(
          firstResource, true);

      return grouperWsLiteGet.service(clientVersion, urlStrings, requestObject);
    }

  },

  /** POST */
  POST {

    /**
     * handle the incoming request based on HTTP method
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesLite/group/a:b
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
     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesLite/group/a:b
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
      GrouperWsLitePut grouperWsLitePut = GrouperWsLitePut.valueOfIgnoreCase(
          firstResource, true);

      return grouperWsLitePut.service(
          clientVersion, urlStrings, requestObject);
    }

  },

  /** DELETE */
  DELETE {

    /**
     * handle the incoming request based on HTTP method
     * @param clientVersion version of client, e.g. v1_3_000
     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesLite/group/a:b
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
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesLite/group/a:b
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
   * @throws GrouperLiteInvalidRequest if there is a problem
   */
  public static GrouperLiteHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws GrouperLiteInvalidRequest {
    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      return null;
    }
    for (GrouperLiteHttpMethod grouperLiteHttpMethod : GrouperLiteHttpMethod.values()) {
      if (StringUtils.equalsIgnoreCase(string, grouperLiteHttpMethod.name())) {
        return grouperLiteHttpMethod;
      }
    }
    StringBuilder error = new StringBuilder(
        "Cant find grouperLiteHttpMethod from string: '").append(string);
    error.append("', expecting one of: ");
    for (GrouperLiteHttpMethod grouperLiteHttpMethod : GrouperLiteHttpMethod.values()) {
      error.append(grouperLiteHttpMethod.name()).append(", ");
    }
    throw new GrouperLiteInvalidRequest(error.toString());
  }
}
