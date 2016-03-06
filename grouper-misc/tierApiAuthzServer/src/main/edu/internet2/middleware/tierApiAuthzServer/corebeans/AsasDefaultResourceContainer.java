package edu.internet2.middleware.tierApiAuthzServer.corebeans;

/**
 * from url: BASE_URL: e.g. url/tierApiAuthz
 * asas default resource container
 * @author mchyzer
 *
 */
public class AsasDefaultResourceContainer extends AsasResponseBeanBase {

  /**
   * from url: BASE_URL: e.g. url/tierApiAuthz
   * body of the asas default resource
   */
  private AsasDefaultResource defaultResource = new AsasDefaultResource();

  /**
   * from url: BASE_URL: e.g. url/tierApiAuthz
   * body of the asas default resource
   * @return the body of the response
   */
  public AsasDefaultResource getDefaultResource() {
    return this.defaultResource;
  }

  /**
   * from url: BASE_URL: e.g. url/tierApiAuthz
   * body of the asas default resource
   * @param defaultResource1
   */
  public void setDefaultResource(AsasDefaultResource defaultResource1) {
    this.defaultResource = defaultResource1;
  }
  
}
