/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.util;


/**
 * response from http call
 */
public class HttpCallResponse {

  /**
   * uri from http call
   */
  private String url;
  
  /**
   * method from http call
   */
  private HttpCallMethod httpCallMethod;
  
  /**
   * content type from http call
   */
  private String contentType;
  
  /**
   * uri from http call
   * @return the uri
   */
  public String getUrl() {
    return this.url;
  }
  
  /**
   * uri from http call
   * @param uri1 the uri to set
   */
  public void setUrl(String uri1) {
    this.url = uri1;
  }
  
  /**
   * method from http call
   * @return the method
   */
  public HttpCallMethod getHttpCallMethod() {
    return this.httpCallMethod;
  }
  
  /**
   * method from http call
   * @param method1 the method to set
   */
  public void setHttpCallMethod(HttpCallMethod method1) {
    this.httpCallMethod = method1;
  }
  
  /**
   * content type from http call
   * @return the contentType
   */
  public String getContentType() {
    return this.contentType;
  }

  
  /**
   * content type from http call
   * @param contentType1 the contentType to set
   */
  public void setContentType(String contentType1) {
    this.contentType = contentType1;
  }

  /**
   * 
   */
  public HttpCallResponse() {
  }

  /**
   * @param theHttpResponseCode http response code
   */
  public HttpCallResponse(int theHttpResponseCode) {
    this.httpResponseCode = theHttpResponseCode;
  }

  /**
   * make sure response code is ok
   * @param validResponseCodes
   */
  public void assertResponseCodes(int... validResponseCodes) {
    
    if (validResponseCodes == null || validResponseCodes.length == 0) {
      throw new RuntimeException("Pass in some calid response codes!");
    }
    for (int code : validResponseCodes) {
      if (this.getHttpResponseCode() == code) {
        return;
      }
    }
    
    StringBuilder validResponseCodesStringBuilder = new StringBuilder();

    boolean first = true;
    for (int code : validResponseCodes) {
      if (!first) {
        validResponseCodesStringBuilder.append(", ");
      }
      first = false;
      
      validResponseCodesStringBuilder.append(code); 
    }
    
    throw new RuntimeException("Expected " + validResponseCodesStringBuilder.toString() 
        + " but got " + this.getHttpResponseCode() + ", uri: '" + this.url + "', method: " + this.httpCallMethod
        + ", content type: " + this.contentType + ", body: '" + this.getResponseBody() + "'");

  }
  
  /**
   * 
   * @param theHttpResponseCode
   * @param theResponseBody
   */
  public HttpCallResponse(int theHttpResponseCode, String theResponseBody) {
    this.httpResponseCode = theHttpResponseCode;
    this.responseBody = theResponseBody;
  }
  
  /**
   * response body if there is one
   */
  private String responseBody;
  
  /**
   * http response code
   */
  private int httpResponseCode;

  
  /**
   * response body if there is one
   * @return the responseBody
   */
  public String getResponseBody() {
    return this.responseBody;
  }

  
  /**
   * response body if there is one
   * @param responseBody1 the responseBody to set
   */
  public void setResponseBody(String responseBody1) {
    this.responseBody = responseBody1;
  }

  
  /**
   * http response code
   * @return the httpRepsonseCode
   */
  public int getHttpResponseCode() {
    return this.httpResponseCode;
  }

  
  /**
   * http response code
   * @param httpRepsonseCode1 the httpRepsonseCode to set
   */
  public void setHttpResponseCode(int httpRepsonseCode1) {
    this.httpResponseCode = httpRepsonseCode1;
  }

  
  
}
