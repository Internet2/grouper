package edu.internet2.middleware.tierInstrumentationCollector.util;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.tierInstrumentationCollector.config.TierInstrumentationCollectorConfig;
import edu.internet2.middleware.tierInstrumentationCollector.j2ee.TierInstrumentationCollectorFilterJ2ee;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class TierInstrumentationCollectorUtils {

  /**
   * tier instrumentation
   * @param input
   * @return
   */
  public static JSONObject jsonStringToObject(String input) {
    if (input == null) {
      return null;
    }
    return (JSONObject) JSONSerializer.toJSON( input );
  }
  
  /**
   * return something like https://server.whatever.ext/appName/servlet
   * @return the url of the servlet with no slash
   */
  public static String servletUrl() {
    
    HttpServletRequest httpServletRequest = TierInstrumentationCollectorFilterJ2ee.retrieveHttpServletRequest();
    
    if (httpServletRequest == null) {
      String servletUrl = TierInstrumentationCollectorConfig.retrieveConfig().propertyValueStringRequired("tierInstrumentationCollector.servletUrl");
      return servletUrl;
    }
    
    String fullUrl = httpServletRequest.getRequestURL().toString();
    
    String servletPath = httpServletRequest.getServletPath();
    
    return fullUrlToServletUrl(fullUrl, servletPath);
  }
  
  /**
   * 
   * @param fullUrl https://whatever/appName/servlet
   * @oaram servletPath /servlet
   * @return the servlet url
   */
  static String fullUrlToServletUrl(String fullUrl, String servletPath) {
    
    int fromIndex = 0;
    for (int i=0;i<4;i++) {
      fromIndex = fullUrl.indexOf('/', fromIndex+1);
    }
    
    int servletIndex = fullUrl.indexOf(servletPath, fromIndex);
    
    return fullUrl.substring(0, servletIndex + servletPath.length());

  }

  
}
