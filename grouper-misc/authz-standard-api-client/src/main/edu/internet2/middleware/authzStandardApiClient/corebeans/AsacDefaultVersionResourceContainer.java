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
  private AsacDefaultVersionResource defaultVersionResource = new AsacDefaultVersionResource();

  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   * @return standard api
   */
  public AsacDefaultVersionResource getDefaultVersionResource() {
    return this.defaultVersionResource;
  }

  /**
   * e.g. from URL: BASE_URL.json, e.g. url/authzStandardApi.json
   * @param asasDefaultVersionResource1
   */
  public void setDefaultVersionResource(
      AsacDefaultVersionResource asasDefaultVersionResource1) {
    this.defaultVersionResource = asasDefaultVersionResource1;
  }

}
