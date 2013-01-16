package edu.internet2.middleware.authzStandardApiClient.corebeans;


/**
 * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
 * default resources under the version
 * 
 * @author mchyzer
 *
 */
public class AsacVersionResourceContainer extends AsacResponseBeanBase {
 
  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   * displays the resources under the version
   */
  private AsacVersionResource versionResource = new AsacVersionResource();

  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   * displays the resources under the version
   * @return the version resource
   */
  public AsacVersionResource getVersionResource() {
    return this.versionResource;
  }

  /**
   * from URL: BASE_URL/v1.json, e.g. url/authzStandardApi/v1.json
   * displays the resources under the version
   * @param asasVersionResource
   */
  public void setVersionResource(AsacVersionResource asasVersionResource) {
    this.versionResource = asasVersionResource;
  }

  
  
}
