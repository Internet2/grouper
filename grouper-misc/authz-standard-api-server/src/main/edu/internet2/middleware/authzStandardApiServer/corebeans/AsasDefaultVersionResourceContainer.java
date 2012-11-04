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
  private AsasDefaultVersionResource asasDefaultVersionResource = new AsasDefaultVersionResource();

  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   * @return standard api
   */
  public AsasDefaultVersionResource getAsasDefaultVersionResource() {
    return this.asasDefaultVersionResource;
  }

  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   * @param asasDefaultVersionResource1
   */
  public void setAsasDefaultVersionResource(
      AsasDefaultVersionResource asasDefaultVersionResource1) {
    this.asasDefaultVersionResource = asasDefaultVersionResource1;
  }

}
