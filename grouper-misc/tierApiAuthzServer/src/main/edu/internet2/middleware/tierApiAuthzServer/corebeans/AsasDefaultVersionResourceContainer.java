/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerConfig;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 * e.g. from URL: BASE_URL.json, e.g. url/tierApiAuthz.json
 * @author mchyzer
 *
 */
public class AsasDefaultVersionResourceContainer extends AsasResponseBeanBase {

  /**
   * 
   */
  public AsasDefaultVersionResourceContainer() {
    
    this.v1Uri = "/" 
        + StandardApiServerUtils.version();
    this.serverType = StandardApiServerConfig.retrieveConfig()
        .propertyValueStringRequired("tierApiAuthzServer.serverType");
    
    if (StandardApiServerUtils.isBlank(this.serverType)) {
      throw new RuntimeException("Why is tierApiAuthzServer.serverType not defined in the the standardapi.server.properties");
    }
    
  }


  /**
   * describes the implementation of the API, i.e. the underlying groups service
   * e.g. Grouper WS Standard API v2.1.14
   */
  private String serverType;
  
  /**
   * describes the implementation of the API, i.e. the underlying groups service
   * e.g. Grouper WS Standard API v2.1.14
   * @return the server type
   */
  public String getServerType() {
    return this.serverType;
  }
  
  /**
   * describes the implementation of the API, i.e. the underlying groups service
   * e.g. Grouper WS Standard API v2.1.14
   * @param serverType
   */
  public void setServerType(String serverType) {
    this.serverType = serverType;
  }



  /**
   * "v1Uri": "https://groups.institution.edu/groupsApp/tierApiAuthz/v1.json",
   */
  private String v1Uri;


  
  /**
   * @return the v1Uri
   */
  public String getV1Uri() {
    return this.v1Uri;
  }


  
  /**
   * @param v1Uri1 the v1Uri to set
   */
  public void setV1Uri(String v1Uri1) {
    this.v1Uri = v1Uri1;
  }
  
  

}
