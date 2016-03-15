package edu.internet2.middleware.tierApiAuthzServer.rest;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasResponseBeanBase;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


public enum AsasRestGet {

  /** group get requests */
  groups {

    /**
     * handle the incoming request based on GET HTTP method and groups resource
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/tierApiAuthz/tierApiAuthz/v1/groups.json
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      
      if (!StandardApiServerUtils.isBlank(body)) {
        throw new AsasRestInvalidRequest("Not expecting body in request (size: " + StandardApiServerUtils.length(body) + ")", "400", "ERROR_INVALID_REQUEST_BODY");
      }
      if (StandardApiServerUtils.length(urlStrings) == 0) {
        throw new AsasRestInvalidRequest("Not expecting more url strings: " + StandardApiServerUtils.toStringForLog(urlStrings), "404", "ERROR_INVALID_PATH");
      }
      if (StandardApiServerUtils.length(urlStrings) == 1) {
        return AsasRestLogic.getGroups(params);
      }
      String groupUri = StandardApiServerUtils.popUrlString(urlStrings);
      String nextResource = StandardApiServerUtils.popUrlString(urlStrings);
      AsasRestGetGroups asasRestGetGroups = AsasRestGetGroups.valueOfIgnoreCase(
          nextResource, true);
      
      return asasRestGetGroups.service(groupUri, urlStrings, params, body);

    }
  };

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static AsasRestGet valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    return StandardApiServerUtils.enumValueOfIgnoreCase(AsasRestGet.class, 
        string, exceptionOnNotFound);
  }

  /**
   * handle the incoming request based on HTTP method
   * @param clientVersion version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param requestObject is the request body converted to object
   * @return the result object
   */
  public abstract AsasResponseBeanBase service(
      List<String> urlStrings,
      Map<String, String> params, String body);

}
