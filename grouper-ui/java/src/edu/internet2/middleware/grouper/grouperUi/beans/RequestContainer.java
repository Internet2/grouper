/**
 * @author mchyzer
 * $Id: SessionContainer.java,v 1.2 2009-10-11 22:04:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;


/**
 * hold generic stuff about user in session
 */
public class RequestContainer implements Serializable {
  
  /** if this is an ajax request */
  private boolean ajaxRequest = false;
  
  /**
   * 
   * @return if this is an ajax request
   */
  public boolean isAjaxRequest() {
    return this.ajaxRequest;
  }

  /**
   * if this is an ajax request
   * @param ajaxRequest1
   */
  public void setAjaxRequest(boolean ajaxRequest1) {
    this.ajaxRequest = ajaxRequest1;
  }

  /**
   * retrieveFromRequest, will lazy load
   * @return the app state in request scope
   */
  public static RequestContainer retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    RequestContainer requestContainer = (RequestContainer)httpServletRequest
      .getAttribute("requestContainer");
    if (requestContainer == null) {
      requestContainer = new RequestContainer();
      requestContainer.storeToSession();
    }
    return requestContainer;
  }

  
  
  /**
   * store to session scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("requestContainer", this);
  }
  
}
