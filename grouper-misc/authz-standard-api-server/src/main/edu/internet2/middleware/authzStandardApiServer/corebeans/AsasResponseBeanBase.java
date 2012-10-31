package edu.internet2.middleware.authzStandardApiServer.corebeans;

import java.util.Date;

import edu.internet2.middleware.authzStandardApiServer.j2ee.AsasRestServlet;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;

/**
 * base class that beans extends
 * @author mchyzer
 */
public abstract class AsasResponseBeanBase {

  public AsasResponseBeanBase() {
    this.get_requestMeta().setHttpStatusCode(200);
    this.get_meta().setLastModified(StandardApiServerUtils.convertToIso8601(new Date(AsasRestServlet.getStartupTime())));
    this.setSuccess(true);
  }


  /**
   * meta about resource
   */
  private AsasMeta _meta = new AsasMeta();
  
  /**
   * meta about resource
   * @return the _meta
   */
  public AsasMeta get_meta() {
    return _meta;
  }
  
  /**
   * meta about resource
   * @param _meta the _meta to set
   */
  public void set_meta(AsasMeta _meta) {
    this._meta = _meta;
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
   * response status text code
   */
  private String statusCode;

  /**
   * true or false if valid request
   */
  private boolean success;
  
  
  
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

  
  /**
   * response status text code
   * @return the statusCode
   */
  public String getStatusCode() {
    return this.statusCode;
  }

  
  /**
   * response status text code
   * @param statusCode1 the statusCode to set
   */
  public void setStatusCode(String statusCode1) {
    this.statusCode = statusCode1;
  }


  /** 
   * if there are warnings, they will be there
   */
  private StringBuilder resultWarning = new StringBuilder();

  /**
   * append error message to list of error messages
   * 
   * @param warning
   */
  public void appendWarning(String warning) {
    this.resultWarning.append(warning);
  }

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getWarning() {
    return StandardApiServerUtils.trimToNull(this.resultWarning.toString());
  }

  /**
   * the builder for warnings
   * @return the builder for warnings
   */
  public StringBuilder warnings() {
    return this.resultWarning;
  }


  /**
   * @param resultWarnings1 the resultWarnings to set
   */
  public void setWarning(String resultWarnings1) {
    this.resultWarning = StandardApiServerUtils.isBlank(resultWarnings1) ? new StringBuilder() : new StringBuilder(resultWarnings1);
  }

  
  /**
   * true or false if valid request
   * @return the success
   */
  public boolean isSuccess() {
    return success;
  }

  
  /**
   * true or false if valid request
   * @param success the success to set
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }

  /** metadata about this particular resource */
  private AsasRequestMeta _requestMeta = new AsasRequestMeta();
  
  /**
   * @return the _requestMeta
   */
  public AsasRequestMeta get_requestMeta() {
    return _requestMeta;
  }
  
  /**
   * @param _requestMeta the _requestMeta to set
   */
  public void set_requestMeta(AsasRequestMeta _requestMeta) {
    this._requestMeta = _requestMeta;
  }
  
}
