package edu.internet2.middleware.tierApiAuthzClient.corebeans;

/**
 * default resource: BASE_URL.json e.g. url/authzStandardApi.json
 * 
 * @author mchyzer
 *
 */
public class AsacDefaultVersionResource {
 
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
