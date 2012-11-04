package edu.internet2.middleware.authzStandardApiServer.corebeans;

/**
 * from url: BASE_URL: e.g. url/authzStandardApi
 * asas default resource container
 * @author mchyzer
 *
 */
public class AsasDefaultResourceContainer extends AsasResponseBeanBase {

  /**
   * from url: BASE_URL: e.g. url/authzStandardApi
   * body of the asas default resource
   */
  private AsasDefaultResource asasDefaultResource = new AsasDefaultResource();

  /**
   * from url: BASE_URL: e.g. url/authzStandardApi
   * body of the asas default resource
   * @return the body of the response
   */
  public AsasDefaultResource getAsasDefaultResource() {
    return this.asasDefaultResource;
  }

  /**
   * from url: BASE_URL: e.g. url/authzStandardApi
   * body of the asas default resource
   * @param asasDefaultResource1
   */
  public void setAsasDefaultResource(AsasDefaultResource asasDefaultResource1) {
    this.asasDefaultResource = asasDefaultResource1;
  }
  
}
