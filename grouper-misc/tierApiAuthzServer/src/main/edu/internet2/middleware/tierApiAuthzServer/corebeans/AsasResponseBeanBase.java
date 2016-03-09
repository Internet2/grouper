package edu.internet2.middleware.tierApiAuthzServer.corebeans;


/**
 * base class that beans extends
 * @author mchyzer
 */
public abstract class AsasResponseBeanBase {

  public AsasResponseBeanBase() {
    this.getMeta().setHttpStatusCode(200);
    //this.getMeta().setLastModified(StandardApiServerUtils.convertToIso8601(new Date(TaasRestServlet.getStartupTime())));
  }

  /**
   * 
   */
  private String[] schemas = null;

  
  /**
   * @return the schemas
   */
  public String[] getSchemas() {
    return this.schemas;
  }

  
  /**
   * @param schemas the schemas to set
   */
  public void setSchemas(String[] schemas) {
    this.schemas = schemas;
  }

  /**
   * meta about resource
   */
  private TaasMeta meta = new TaasMeta();
  
  /**
   * meta about resource
   * @return the meta
   */
  public TaasMeta getMeta() {
    return meta;
  }
  
  /**
   * meta about resource
   * @param meta the meta to set
   */
  public void setMeta(TaasMeta _meta) {
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

  
}
