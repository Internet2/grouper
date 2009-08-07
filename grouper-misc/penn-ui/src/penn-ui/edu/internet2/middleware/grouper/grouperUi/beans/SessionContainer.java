/**
 * @author Kate
 * $Id: SessionContainer.java,v 1.1 2009-08-07 07:36:01 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.json.GuiHideShow;


/**
 * hold generic stuff about user in session
 */
public class SessionContainer {

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
