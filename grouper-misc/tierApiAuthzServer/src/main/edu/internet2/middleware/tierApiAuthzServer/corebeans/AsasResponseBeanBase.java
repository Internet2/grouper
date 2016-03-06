package edu.internet2.middleware.tierApiAuthzServer.corebeans;

import java.util.Date;

import edu.internet2.middleware.tierApiAuthzServer.j2ee.TaasRestServlet;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;

/**
 * base class that beans extends
 * @author mchyzer
 */
public abstract class AsasResponseBeanBase {

  public AsasResponseBeanBase() {
    this.getResponseMeta().setHttpStatusCode(200);
    this.getMeta().setLastModified(StandardApiServerUtils.convertToIso8601(new Date(TaasRestServlet.getStartupTime())));
  }


  /**
   * meta about resource
   */
  private AsasMeta meta = new AsasMeta();
  
  /**
   * meta about resource
   * @return the meta
   */
  public AsasMeta getMeta() {
    return meta;
  }
  
  /**
   * meta about resource
   * @param meta the meta to set
   */
  public void setMeta(AsasMeta _meta) {
    this.meta = _meta;
  }


  /**
   * error code
   */
  private String error;
  
  /**
   * free-form error description
   */
  private String error_description;
  
  /**
   * uri for browser of error message to give more info
   */
  private String error_uri;
  
  
  /**
   * error code
   * @return the error
   */
  public String getError() {
    return this.error;
  }

  
  /**
   * error code
   * @param error1 the error to set
   */
  public void setError(String error1) {
    this.error = error1;
  }

  
  /**
   * free-form error description
   * @return the error_description
   */
  public String getError_description() {
    return this.error_description;
  }

  
  /**
   * free-form error description
   * @param error_description1 the error_description to set
   */
  public void setError_description(String error_description1) {
    this.error_description = error_description1;
  }

  
  /**
   * uri for browser of error message to give more info
   * @return the error_uri
   */
  public String getError_uri() {
    return this.error_uri;
  }

  
  /**
   * uri for browser of error message to give more info
   * @param error_uri1 the error_uri to set
   */
  public void setError_uri(String error_uri1) {
    this.error_uri = error_uri1;
  }

  /** metadata about this particular resource */
  private AsasResponseMeta responseMeta = new AsasResponseMeta();
  
  /**
   * @return the _requestMeta
   */
  public AsasResponseMeta getResponseMeta() {
    return responseMeta;
  }
  
  /**
   * @param _requestMeta the _requestMeta to set
   */
  public void setResponseMeta(AsasResponseMeta _requestMeta) {
    this.responseMeta = _requestMeta;
  }
  
  /**
   * metadata about the service
   */
  private AsasServiceMeta serviceMeta = new AsasServiceMeta();

  /**
   * metadata about the service
   * @return metadata about the service
   */
  public AsasServiceMeta getServiceMeta() {
    return this.serviceMeta;
  }

  /**
   * metadata about the service
   * @param serviceMeta1
   */
  public void setServiceMeta(AsasServiceMeta serviceMeta1) {
    this.serviceMeta = serviceMeta1;
  }
  
}
