package edu.internet2.middleware.grouper.j2ee;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * communicate with client using this response object so everything gets logged
 * @author mchyzer
 *
 */
public class MockServiceResponse {

  public MockServiceResponse() {
  }

  private HttpServletResponse httpServletResponse;
  
  private String responseBody;
  
  private int responseCode = -1;
  
  private Map<String, String> responseHeaders = new LinkedHashMap<String, String>();

  public HttpServletResponse getHttpServletResponse() {
    return httpServletResponse;
  }

  
  public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
    this.httpServletResponse = httpServletResponse;
  }

  
  public String getResponseBody() {
    return responseBody;
  }

  
  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  
  public int getResponseCode() {
    return responseCode;
  }

  
  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  
  public Map<String, String> getResponseHeaders() {
    return responseHeaders;
  }

  private String contentType;

  
  public String getContentType() {
    return contentType;
  }


  
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  
  
  
}
