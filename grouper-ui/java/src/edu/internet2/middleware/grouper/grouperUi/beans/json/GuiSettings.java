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
/*
 * @author mchyzer
 * $Id: GuiSettings.java,v 1.2 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.json;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;


/**
 * settings and common stuff to go to browser
 */
public class GuiSettings implements Serializable {

  /**
   * retrieveFromRequest, cant be null
   * @return the app state in request scope
   */
  public static GuiSettings retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    GuiSettings guiSettings = (GuiSettings)httpServletRequest
      .getAttribute("guiSettings");
    if (guiSettings == null) {
      throw new RuntimeException("GuiSettings is null");
    }
    return guiSettings;
  }

  /**
   * store to request scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("guiSettings", this);
  }

  /** need to send this key back with each request in authnKey param */
  private String authnKey = null;
  
  /** logged in subject */
  private GuiSubject loggedInSubject = null;
  
  /**
   * logged in subject
   * @return logged in subject
   */
  public GuiSubject getLoggedInSubject() {
    return this.loggedInSubject;
  }

  /**
   * logged in subject
   * @param loggedInSubject1
   */
  public void setLoggedInSubject(GuiSubject loggedInSubject1) {
    this.loggedInSubject = loggedInSubject1;
  }

  /**
   * need to send this key back with each request in authnKey param
   * @return the authn key
   */
  public String getAuthnKey() {
    return this.authnKey;
  }

  /**
   * need to send this key back with each request in authnKey param
   * @param authnKey1
   */
  public void setAuthnKey(String authnKey1) {
    this.authnKey = authnKey1;
  }

}
