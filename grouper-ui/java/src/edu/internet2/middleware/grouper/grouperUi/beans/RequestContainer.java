/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
