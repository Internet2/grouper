package edu.internet2.middleware.authzStandardApiServer.rest;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.authzStandardApiServer.contentType.AsasRestContentType;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasFolderSaveRequest;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasResponseBeanBase;
import edu.internet2.middleware.authzStandardApiServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.authzStandardApiServer.j2ee.AsasRestServlet;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;

/**
 * posts into the server
 * @author mchyzer
 *
 */
public enum AsasRestPost {

  /** folder post requests */
  folders {

    /**
     * handle the incoming request based on POST HTTP method and folders resource
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/authzStandardApi/authzStandardApi/v1/folders/id:123.json
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {

      AsasFolderSaveRequest asasFolderSaveRequest = null;
      if (!StandardApiServerUtils.isBlank(body)) {
        AsasRestContentType asasRestContentType = AsasRestContentType.retrieveContentType();
        asasFolderSaveRequest = asasRestContentType.parseString(AsasFolderSaveRequest.class, body, 
            AsasRestServlet.threadLocalWarnings());
      }

      if (StandardApiServerUtils.length(urlStrings) != 1) {
        throw new AsasRestInvalidRequest("Expecting 1 url string after 'folders': " + StandardApiServerUtils.toStringForLog(urlStrings));
      }
      
      String folderUri = StandardApiServerUtils.popUrlString(urlStrings);
      
      return AsasRestLogic.folderSave(asasFolderSaveRequest, folderUri, params, false);
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
  public static AsasRestPost valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    return StandardApiServerUtils.enumValueOfIgnoreCase(AsasRestPost.class, 
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
