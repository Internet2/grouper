/**
 * @author Kate
 * $Id: SessionContainer.java,v 1.1 2009-09-09 15:10:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.j2ee.GrouperUiJ2ee;
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
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
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
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
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
