package edu.internet2.middleware.tierApiAuthzClient.corebeans;

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
  private AsacDefaultResource defaultResource = new AsacDefaultResource();

  /**
   * from url: BASE_URL: e.g. url/authzStandardApi
   * body of the asas default resource
   * @return the body of the response
   */
  public AsacDefaultResource getDefaultResource() {
    return this.defaultResource;
  }

  /**
   * from url: BASE_URL: e.g. url/authzStandardApi
   * body of the asas default resource
   * @param asasDefaultResource1
   */
  public void setDefaultResource(AsacDefaultResource asasDefaultResource1) {
    this.defaultResource = asasDefaultResource1;
  }
  
}
