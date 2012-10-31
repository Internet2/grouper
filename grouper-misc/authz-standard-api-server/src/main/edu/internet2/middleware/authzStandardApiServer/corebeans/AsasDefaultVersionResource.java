package edu.internet2.middleware.authzStandardApiServer.corebeans;

import edu.internet2.middleware.authzStandardApiServer.j2ee.AsasRestServlet;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;

/**
 * default resource
 * 
 * @author mchyzer
 *
 */
public class AsasDefaultVersionResource extends AsasResponseBeanBase {
 
  /**
   * 
   */
  public AsasDefaultVersionResource() {
    
    this.v1Uri = StandardApiServerUtils.servletUrl() + "/v1." + AsasRestServlet.retrieveContentType();
    
  }


  /**
   * "v1Uri": "https://groups.institution.edu/groupsApp/authzStandardApi/v1.json",
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
