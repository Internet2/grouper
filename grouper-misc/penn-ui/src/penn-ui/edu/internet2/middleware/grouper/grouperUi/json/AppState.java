package edu.internet2.middleware.grouper.grouperUi.json;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;

/**
 * AppState object comes from javascript on ajax requests
 */
public class AppState {

  /** if simple membership update is initted */
  private boolean inittedSimpleMembershipUpdate;
  
  /**
   * if simple membership update is initted
   * @return the inittedSimpleMembershipUpdate
   */
  public boolean isInittedSimpleMembershipUpdate() {
    return this.inittedSimpleMembershipUpdate;
  }
  
  /**
   * if simple membership update is initted
   * @param inittedSimpleMembershipUpdate1 the inittedSimpleMembershipUpdate to set
   */
  public void setInittedSimpleMembershipUpdate(boolean inittedSimpleMembershipUpdate1) {
    this.inittedSimpleMembershipUpdate = inittedSimpleMembershipUpdate1;
  }

  /** if the javascript is initted */
  private boolean initted;

  /**
   * store to request scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("appState", this);
  }
  
  /**
   * retrieveFromRequest, must not be null
   * @return the app state in request scope
   */
  public static AppState retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    AppState appState = (AppState)httpServletRequest.getAttribute("appState");
    if (appState == null) {
      throw new RuntimeException("App state is null");
    }
    return appState;
  }
  
  /**
   * if the javascript is initted
   * @return the initted
   */
  public boolean isInitted() {
    return this.initted;
  }

  
  /**
   * if the javascript is initted
   * @param initted1 the initted to set
   */
  public void setInitted(boolean initted1) {
    this.initted = initted1;
  }

  /**
   * 
   */
  public AppState() {
    
  }
  
  /**
   * url in cache
   */
  private String urlInCache;

  /**
   * urlInCache
   * @return the urlInCache
   */
  public String getUrlInCache() {
    return this.urlInCache;
  }

  /**
   * urlInCache
   * @param urlInCache1 the urlInCache to set
   */
  public void setUrlInCache(String urlInCache1) {
    this.urlInCache = urlInCache1;
  }
  
  /**
   * url split out into args
   */
  private Map<String, String> urlArgObjects = new LinkedHashMap<String,String>();

  /**
   * @return the urlArgObjects
   */
  public Map<String, String> getUrlArgObjects() {
    return this.urlArgObjects;
  }

  /**
   * @param urlArgObjects1 the urlArgObjects to set
   */
  public void setUrlArgObjects(Map<String, String> urlArgObjects1) {
    this.urlArgObjects = urlArgObjects1;
  }
  
  
  
}
