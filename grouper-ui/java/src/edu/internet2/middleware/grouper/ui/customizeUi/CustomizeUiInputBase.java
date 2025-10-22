package edu.internet2.middleware.grouper.ui.customizeUi;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class CustomizeUiInputBase {
  
  public CustomizeUiInputBase(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    this.request = httpServletRequest;
    this.response = httpServletResponse;
  }

  private HttpServletRequest request;
  
  private HttpServletResponse response;
  
  public HttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }
  
  public HttpServletResponse getResponse() {
    return response;
  }
  
  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }
  
  /**
   * get logged in subject
   */
  public Subject getLoggedInSubject() {
    return GrouperUiFilter.retrieveSubjectLoggedIn();
  }
  
}
