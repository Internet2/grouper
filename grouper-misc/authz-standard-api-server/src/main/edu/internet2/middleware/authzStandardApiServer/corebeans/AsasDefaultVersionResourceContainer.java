/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.corebeans;


/**
 * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
 * @author mchyzer
 *
 */
public class AsasDefaultVersionResourceContainer extends AsasResponseBeanBase {

  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   */
  private AsasDefaultVersionResource defaultVersionResource = new AsasDefaultVersionResource();

  
  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   * @return the defaultVersionResource
   */
  public AsasDefaultVersionResource getDefaultVersionResource() {
    return this.defaultVersionResource;
  }

  
  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   * @param defaultVersionResource1 the defaultVersionResource to set
   */
  public void setDefaultVersionResource(AsasDefaultVersionResource defaultVersionResource1) {
    this.defaultVersionResource = defaultVersionResource1;
  }


}
