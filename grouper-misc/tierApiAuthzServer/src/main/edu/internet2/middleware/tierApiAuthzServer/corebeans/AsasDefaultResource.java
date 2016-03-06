package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;

/**
 * from url: BASE_URL: e.g. url/tierApiAuthz
 * default resource
 * 
 * @author mchyzer
 *
 */
public class AsasDefaultResource {

  /**
   * 
   */
  public AsasDefaultResource() {
    super();
    
    this.jsonDefaultUri = StandardApiServerUtils.servletUrl() + ".json";
    
  }


  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/tierApiAuthz.json",
   */
  private String jsonDefaultUri;

  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/tierApiAuthz.json",
   * @return the jsonDefaultUri
   */
  public String getJsonDefaultUri() {
    return this.jsonDefaultUri;
  }

  
  /**
   * "jsonDefaultUri": "https://groups.institution.edu/groupsApp/tierApiAuthz.json",
   * @param jsonDefaultUri1 the jsonDefaultUri to set
   */
  public void setJsonDefaultUri(String jsonDefaultUri1) {
    this.jsonDefaultUri = jsonDefaultUri1;
  }
  
  
  
}
