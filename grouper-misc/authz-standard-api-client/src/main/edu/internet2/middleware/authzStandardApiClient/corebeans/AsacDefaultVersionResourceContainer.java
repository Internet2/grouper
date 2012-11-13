/**
 * 
 */
package edu.internet2.middleware.authzStandardApiClient.corebeans;


/**
 * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
 * @author mchyzer
 *
 */
public class AsacDefaultVersionResourceContainer extends AsacResponseBeanBase {

  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   */
  private AsacDefaultVersionResource asasDefaultVersionResource = new AsacDefaultVersionResource();

  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   * @return standard api
   */
  public AsacDefaultVersionResource getAsasDefaultVersionResource() {
    return this.asasDefaultVersionResource;
  }

  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   * @param asasDefaultVersionResource1
   */
  public void setAsasDefaultVersionResource(
      AsacDefaultVersionResource asasDefaultVersionResource1) {
    this.asasDefaultVersionResource = asasDefaultVersionResource1;
  }

}
