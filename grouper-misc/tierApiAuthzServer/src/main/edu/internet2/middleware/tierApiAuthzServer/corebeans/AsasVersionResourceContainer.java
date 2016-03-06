package edu.internet2.middleware.tierApiAuthzServer.corebeans;


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
  private AsasVersionResource versionResource = new AsasVersionResource();

  
  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   * displays the resources under the version
   * @return the versionResource
   */
  public AsasVersionResource getVersionResource() {
    return this.versionResource;
  }
  
  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   * displays the resources under the version
   * @param versionResource1 the versionResource to set
   */
  public void setVersionResource(AsasVersionResource versionResource1) {
    this.versionResource = versionResource1;
  }
  
}
