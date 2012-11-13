package edu.internet2.middleware.authzStandardApiClient.corebeans;

/**
 * from url: BASE_URL: e.g. url/authzStandardApi
 * asas default resource container
 * @author mchyzer
 *
 */
public class AsacDefaultResourceContainer extends AsacResponseBeanBase {

  /**
   * from url: BASE_URL: e.g. url/authzStandardApi
   * body of the asas default resource
   */
  private AsacDefaultResource asasDefaultResource = new AsacDefaultResource();

  /**
   * from url: BASE_URL: e.g. url/authzStandardApi
   * body of the asas default resource
   * @return the body of the response
   */
  public AsacDefaultResource getAsasDefaultResource() {
    return this.asasDefaultResource;
  }

  /**
   * from url: BASE_URL: e.g. url/authzStandardApi
   * body of the asas default resource
   * @param asasDefaultResource1
   */
  public void setAsasDefaultResource(AsacDefaultResource asasDefaultResource1) {
    this.asasDefaultResource = asasDefaultResource1;
  }
  
}
