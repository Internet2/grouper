package edu.internet2.middleware.tierApiAuthzClient.corebeans;


/**
 * base class that beans extends
 * @author mchyzer
 */
public abstract class AsacResponseBeanBase {

  /**
   * meta about resource
   */
  private AsacMeta meta = new AsacMeta();
  
  /**
   * meta about resource
   * @return the meta
   */
  public AsacMeta getMeta() {
    return meta;
  }
  
  /**
   * meta about resource
   * @param meta the meta to set
   */
  public void setMeta(AsacMeta _meta) {
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
  private AsacResponseMeta responseMeta = new AsacResponseMeta();
  
  /**
   * @return the _requestMeta
   */
  public AsacResponseMeta getResponseMeta() {
    return responseMeta;
  }
  
  /**
   * @param _requestMeta the _requestMeta to set
   */
  public void setResponseMeta(AsacResponseMeta _requestMeta) {
    this.responseMeta = _requestMeta;
  }
  
  /**
   * metadata about the service
   */
  private AsacServiceMeta serviceMeta = new AsacServiceMeta();

  /**
   * metadata about the service
   * @return metadata about the service
   */
  public AsacServiceMeta getServiceMeta() {
    return this.serviceMeta;
  }

  /**
   * metadata about the service
   * @param serviceMeta1
   */
  public void setServiceMeta(AsacServiceMeta serviceMeta1) {
    this.serviceMeta = serviceMeta1;
  }
  
}
