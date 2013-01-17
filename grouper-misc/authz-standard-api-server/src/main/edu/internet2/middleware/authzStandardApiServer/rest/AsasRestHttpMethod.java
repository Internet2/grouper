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
      
      String firstResource = StandardApiServerUtils.popUrlString(urlStrings);
  
      //validate and get the first resource
      AsasRestGet asasRestGet = AsasRestGet.valueOfIgnoreCase(
          firstResource, true);
  
      return asasRestGet.service(urlStrings, params, body);

    }

  },

  /** POST */
  POST {

    /**
     * @see AsasRestHttpMethod#service(List, Map)
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {

      if (urlStrings.size() > 0) {

        String firstResource = StandardApiServerUtils.popUrlString(urlStrings);
        
        //validate and get the first resource
        AsasRestPost asasRestPost = AsasRestPost.valueOfIgnoreCase(
            firstResource, true);
    
        return asasRestPost.service(urlStrings, params, body);

      }

      throw new AsasRestInvalidRequest("Not expecting this request");
    }

  },

  /** PUT */
  PUT {

    /**
     * @see AsasRestHttpMethod#service(List, Map)
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      
      if (urlStrings.size() > 0) {
        
        String firstResource = StandardApiServerUtils.popUrlString(urlStrings);
        
        //validate and get the first resource
        AsasRestPut asasRestPut = AsasRestPut.valueOfIgnoreCase(
            firstResource, true);
    
        return asasRestPut.service(urlStrings, params, body);

      }
      
      throw new AsasRestInvalidRequest("Not expecting this request");
    }

  },

  /** DELETE */
  DELETE {

    /**
     * @see AsasRestHttpMethod#service(List, Map)
     */
    @Override
    public AsasResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {

      if (urlStrings.size() > 0) {

        String firstResource = StandardApiServerUtils.popUrlString(urlStrings);

        //validate and get the first resource
        AsasRestDelete asasRestDelete = AsasRestDelete.valueOfIgnoreCase(
            firstResource, true);

        return asasRestDelete.service(urlStrings, params, body);

      }

      throw new AsasRestInvalidRequest("Not expecting this request");
    }

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
