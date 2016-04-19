/*******************************************************************************
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package edu.internet2.middleware.tierApiAuthzServer.rest;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasResponseBeanBase;
import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


public enum AsasRestDelete {

  /** folder delete requests */
  folders {

    /**
     * handle the incoming request based on DELETE HTTP method and folders resource
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/tierApiAuthz/tierApiAuthz/v1/folders/id:123.json
     * @param requestObject is the request body converted to object
     * @return the result object
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {

      //eventually we will process a request body
      if (!StandardApiServerUtils.isBlank(body)) {
        throw new AsasRestInvalidRequest("Not expecting body in request (size: " + StandardApiServerUtils.length(body) + ")", "400", "ERROR_INVALID_REQUEST_BODY");
      }

      if (StandardApiServerUtils.length(urlStrings) != 1) {
        throw new AsasRestInvalidRequest("Expecting 1 url string after 'folders': " + StandardApiServerUtils.toStringForLog(urlStrings), "400", "ERROR_ID_EXPECTED");
      }
      
      String folderUri = StandardApiServerUtils.popUrlString(urlStrings);
      
      return AsasRestLogic.folderDelete(folderUri, params);
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
  public static AsasRestDelete valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    return StandardApiServerUtils.enumValueOfIgnoreCase(AsasRestDelete.class, 
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
