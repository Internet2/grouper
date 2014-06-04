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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiHideShow;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter.UiSection;
import edu.internet2.middleware.subject.Subject;


/**
 * hold generic stuff about user in session
 */
public class SessionContainer implements Serializable {

  /**
   * if initted
   */
  private boolean initted = false;
  
  /**
   * logged in subject
   */
  private Subject subjectLoggedIn;
  
  /** allowed ui sections */
  private Set<UiSection> allowedUiSections = new HashSet<UiSection>();
  
  /**
   * @return allowed ui sections
   */
  public Set<UiSection> getAllowedUiSections() {
    return this.allowedUiSections;
  }

  /**
   * logged in subject
   * @return the subjectLoggedIn
   */
  public Subject getSubjectLoggedIn() {
    return this.subjectLoggedIn;
  }

  /**
   * logged in subject
   * @param subjectLoggedIn1 the subjectLoggedIn to set
   */
  public void setSubjectLoggedIn(Subject subjectLoggedIn1) {
    this.subjectLoggedIn = subjectLoggedIn1;
  }


  /**
   * if initted
   * @return the initted
   */
  public boolean isInitted() {
    return this.initted;
  }

  
  /**
   * if initted
   * @param initted1 the initted to set
   */
  public void setInitted(boolean initted1) {
    this.initted = initted1;
  }

  /**
   * retrieveFromSession, will lazy load
   * @return the app state in request scope
   */
  public static SessionContainer retrieveFromSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    SessionContainer sessionContainer = (SessionContainer)httpSession
      .getAttribute("sessionContainer");
    if (sessionContainer == null) {
      sessionContainer = new SessionContainer();
      sessionContainer.storeToSession();
    }
    return sessionContainer;
  }

  
  
  /**
   * store to session scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.getSession().setAttribute("sessionContainer", this);
  }

  /** map of hide shows in session */
  private Map<String, GuiHideShow> hideShows = new LinkedHashMap<String, GuiHideShow>();

  /**
   * map of hide shows in session
   * @return map of hide shows in session
   */
  public Map<String, GuiHideShow> getHideShows() {
    return this.hideShows;
  }
  
}
