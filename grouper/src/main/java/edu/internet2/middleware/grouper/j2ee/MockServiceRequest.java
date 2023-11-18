package edu.internet2.middleware.grouper.j2ee;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MockServiceRequest {

  public MockServiceRequest() {
  }

  private HttpServletRequest httpServletRequest;
  
  /**
   * e.g. azure
   */
  private String mockName;
  
  /**
   * not including slash.
   * if url is: http://localhost/grouper/mockServices/azure/groups/abc123
   * this will return groups/abc123
   */
  private String postMockNamePath;

  /**
   * not including slash.
   * if url is: http://localhost/grouper/mockServices/azure/groups/abc123
   * this will return array of ["groups", "abc123"]
   */
  private String[] postMockNamePaths;

  /**
   * put stuff here to get logged if logging is on
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
  /**
   * request body
   */
  private String requestBody;

  
  public HttpServletRequest getHttpServletRequest() {
    return httpServletRequest;
  }

  
  public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
    this.httpServletRequest = httpServletRequest;
  }

  
  public String getMockName() {
    return mockName;
  }

  
  public void setMockName(String mockName) {
    this.mockName = mockName;
  }

  
  public String getPostMockNamePath() {
    return postMockNamePath;
  }

  
  public void setPostMockNamePath(String postMockNamePath) {
    this.postMockNamePath = postMockNamePath;
  }

  
  public String[] getPostMockNamePaths() {
    return postMockNamePaths;
  }

  
  public void setPostMockNamePaths(String[] postMockNamePaths) {
    this.postMockNamePaths = postMockNamePaths;
  }

  
  public Map<String, Object> getDebugMap() {
    return debugMap;
  }

  
  
  public String getRequestBody() {
    return requestBody;
  }

  
  public void setRequestBody(String requestBody) {
    this.requestBody = requestBody;
  }
  
  
}
