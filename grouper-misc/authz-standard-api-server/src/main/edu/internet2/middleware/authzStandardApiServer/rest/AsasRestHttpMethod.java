/*
 * @author mchyzer $Id: AsasRestHttpMethodva,v 1.5 2008-03-29 10:50:43 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiServer.rest;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasResponseBeanBase;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasVersionResourceContainer;
import edu.internet2.middleware.authzStandardApiServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;

/**
 * types of http methods accepted by grouper rest
 */
public enum AsasRestHttpMethod {

  /** GET */
  GET {

    /**
     * @see AsasRestHttpMethod#service(List, Map)
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      
      if (urlStrings.size() == 0) {
        
        return new AsasVersionResourceContainer();
        
      }
      
      throw new AsasRestInvalidRequest("No expecting this request");

    }

//    /**
//     * handle the incoming request based on HTTP method
//     * @param clientVersion version of client, e.g. v1
//     * @param urlStrings not including the app name or servlet.  
//     * for http://localhost/grouper-ws/servicesRest/xhtml/v3_0_000/groups/a:b
//     * the urlStrings would be size two: {"group", "a:b"}
//     * @param requestObject is the request body converted to object
//     * @return the resultObject
//     */
//    @Override
//    public WsResponseBean service(
//        GrouperVersion clientVersion, List<String> urlStrings,
//        WsRequestBean requestObject) {
//
//      String firstResource = GrouperServiceUtils.popUrlString(urlStrings);
//
//      //validate and get the first resource
//      GrouperWsRestGet grouperWsRestGet = GrouperWsRestGet.valueOfIgnoreCase(
//          firstResource, true);
//
//      return grouperWsRestGet.service(clientVersion, urlStrings, requestObject);
//    }

  },

  /** POST */
  POST {

    /**
     * @see AsasRestHttpMethod#service(List, Map)
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      throw new AsasRestInvalidRequest("No expecting this request");
    }

//    /**
//     * handle the incoming request based on HTTP method
//     * @param clientVersion version of client, e.g. v1_3_000
//     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
//     * the urlStrings would be size two: {"group", "a:b"}
//     * @param requestObject is the request body converted to object
//     * @return the resultObject
//     */
//    @Override
//    public WsResponseBean service(
//        GrouperVersion clientVersion, List<String> urlStrings,
//        WsRequestBean requestObject) {
//      throw new RuntimeException("Invalid POST request");
//    }

  },

  /** PUT */
  PUT {

    /**
     * @see AsasRestHttpMethod#service(List, Map)
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      throw new AsasRestInvalidRequest("No expecting this request");
    }

//    /**
//     * handle the incoming request based on HTTP method
//     * @param clientVersion version of client, e.g. v1_3_000
//     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
//     * the urlStrings would be size two: {"group", "a:b"}
//     * @param requestObject is the request body converted to object
//     * @return the resultObject
//     */
//    @Override
//    public WsResponseBean service(
//        GrouperVersion clientVersion, List<String> urlStrings,
//        WsRequestBean requestObject) {
//      
//      String firstResource = GrouperServiceUtils.popUrlString(urlStrings);
//      
//      //validate and get the first resource
//      GrouperWsRestPut grouperWsRestPut = GrouperWsRestPut.valueOfIgnoreCase(
//          firstResource, true);
//
//      return grouperWsRestPut.service(
//          clientVersion, urlStrings, requestObject);
//    }

  },

  /** DELETE */
  DELETE {

    /**
     * @see AsasRestHttpMethod#service(List, Map)
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      throw new AsasRestInvalidRequest("No expecting this request");
    }

//    /**
//     * handle the incoming request based on HTTP method
//     * @param clientVersion version of client, e.g. v1_3_000
//     * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
//     * the urlStrings would be size two: {"group", "a:b"}
//     * @param requestObject is the request body converted to object
//     * @return the resultObject
//     */
//    @Override
//    public WsResponseBean service(
//        GrouperVersion clientVersion, List<String> urlStrings,
//        WsRequestBean requestObject) {
//
//      String firstResource = GrouperServiceUtils.popUrlString(urlStrings);
//
//      //validate and get the first resource
//      GrouperWsRestDelete grouperWsRestDelete = GrouperWsRestDelete.valueOfIgnoreCase(
//          firstResource, true);
//
//      return grouperWsRestDelete.service(
//          clientVersion, urlStrings, requestObject);
//    }

  };

  /**
   * handle the incoming request based on HTTP method
   * @param clientVersion version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param requestObject is the request body converted to object
   * @return the resultObject
   */
  public abstract AsasResponseBeanBase service(
      List<String> urlStrings, Map<String, String> params, String body);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static AsasRestHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    return StandardApiServerUtils.enumValueOfIgnoreCase(AsasRestHttpMethod.class, string, exceptionOnNotFound);
  }
}
