package edu.internet2.middleware.authzStandardApiServer.corebeans;


/**
 * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
 * default resources under the version
 * 
 * @author mchyzer
 *
 */
public class AsasVersionResourceContainer extends AsasResponseBeanBase {
 
  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   * displays the resources under the version
   */
  private AsasVersionResource asasVersionResource = new AsasVersionResource();

  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   * displays the resources under the version
   * @return the version resource
   */
  public AsasVersionResource getAsasVersionResource() {
    return this.asasVersionResource;
  }

  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   * displays the resources under the version
   * @param asasVersionResource
   */
  public void setAsasVersionResource(AsasVersionResource asasVersionResource) {
    this.asasVersionResource = asasVersionResource;
  }

  
  
}
