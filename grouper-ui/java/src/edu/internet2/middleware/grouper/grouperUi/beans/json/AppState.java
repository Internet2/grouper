package edu.internet2.middleware.grouper.grouperUi.beans.json;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.ezmorph.bean.BeanMorpher;
import net.sf.json.util.JSONUtils;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;

/**
 * AppState object comes from javascript on ajax requests
 */
@SuppressWarnings("serial")
public class AppState implements Serializable {

  /** if simple membership update is initted */
  private boolean inittedSimpleMembershipUpdate;
  
  /**
   * get an app state object or param
   * @param name
   * @return the value
   */
  public String getUrlArgObjectOrParam(String name) {
    HttpServletRequest request = GrouperUiFilter.retrieveHttpServletRequest();

    String result = null;
    
    if (this.getUrlArgObjects() != null) {
      result = this.getUrlArgObjects().get(name);
    }
    if (StringUtils.isBlank(result)) {
      result = request.getParameter(name);
    }
    return result;
    
  }
  
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
  
  /** placeholder since safari sends back this function
   * return null 
   * @return url arg object map
   */
  public Object getUrlArgObjectMap() {
    return null;
  }

  /** placeholder since safari sends back this function
   * @param urlArgObjectMap
   */
  public void setUrlArgObjectMap(Object urlArgObjectMap) {
    //no need to do anything here
  }

  /** if the javascript is initted */
  private boolean initted;

  /**
   * store to request scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("appState", this);
  }
  
  /**
   * retrieveFromRequest, must not be null
   * @return the app state in request scope
   */
  public static AppState retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
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
   * init a request with app state (from browser)
   */
  public void initRequest() {
    SessionContainer sessionContainer = SessionContainer.retrieveFromSession();
    
    {
      //save screen state of all the hide shows which are in session
      Map<String, GuiHideShow> appStateHideShows = this.getHideShows();
      
      BeanMorpher beanMorpher = new BeanMorpher(GuiHideShow.class, JSONUtils.getMorpherRegistry());
      
      if (appStateHideShows != null) {
        Map<String, GuiHideShow> newAppStateHideShows = new LinkedHashMap<String, GuiHideShow>();
        
        for (String hideShowName : appStateHideShows.keySet()) {
          
          //morph this
          GuiHideShow appStateHideShow = (GuiHideShow)beanMorpher.morph(appStateHideShows.get(hideShowName));
          newAppStateHideShows.put(hideShowName, appStateHideShow);
        }
  
        this.setHideShows(newAppStateHideShows);
        appStateHideShows = newAppStateHideShows;
        
        for (String hideShowName : appStateHideShows.keySet()) {
          GuiHideShow sessionHideShow = sessionContainer.getHideShows().get(hideShowName);
          if (sessionHideShow != null) {
            
            //copy over the current state
            GuiHideShow appStateHideShow = appStateHideShows.get(hideShowName);
            sessionHideShow.setShowing( appStateHideShow.isShowing());
            
          }
        }
      }
    }
    
    {
      //convert pagers to real object model
      Map<String, GuiPaging> appStatePagers = this.getPagers();
      
      BeanMorpher beanMorpher = new BeanMorpher(GuiPaging.class, JSONUtils.getMorpherRegistry());
      
      if (appStatePagers != null) {
        Map<String, GuiPaging> newAppStatePagers = new LinkedHashMap<String, GuiPaging>();
        
        for (String pagerName : appStatePagers.keySet()) {
          
          //morph this
          GuiPaging appStateHideShow = (GuiPaging)beanMorpher.morph(appStatePagers.get(pagerName));
          newAppStatePagers.put(pagerName, appStateHideShow);
        }
  
        this.setPagers(newAppStatePagers);
      }
    }    
    
    this.storeToRequest();

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
   * <pre>
   * hide shows, the name, and if showing, text, etc.  Anything with class:
   * shows_hideShowName, e.g. shows_simpleMembershipAdvanced
   * Anything with class: hides_hideShowName, e.g. hides_simpleMembershipAdvanced
   * will show if false.
   * </pre>
   */
  private Map<String, GuiHideShow> hideShows = new LinkedHashMap<String,GuiHideShow>();

  
  /**
   * <pre>
   * hide shows, the name, and if showing, text, etc.  Anything with class:
   * shows_hideShowName, e.g. shows_simpleMembershipAdvanced
   * Anything with class: hides_hideShowName, e.g. hides_simpleMembershipAdvanced
   * will show if false.
   * </pre>
   * @return the hideShows
   */
  public Map<String, GuiHideShow> getHideShows() {
    return this.hideShows;
  }

  
  /**
   * <pre>
   * hide shows, the name, and if showing, text, etc.  Anything with class:
   * shows_hideShowName, e.g. shows_simpleMembershipAdvanced
   * Anything with class: hides_hideShowName, e.g. hides_simpleMembershipAdvanced
   * will show if false.
   * </pre>
   * @param hideShows1 the hideShows to set
   */
  public void setHideShows(Map<String, GuiHideShow> hideShows1) {
    this.hideShows = hideShows1;
  }

  
  /**
   * <pre>
   * pagers keep track of which page and how many on a page
   * </pre>
   * @return the pagers
   */
  public Map<String, GuiPaging> getPagers() {
    return this.pagers;
  }

  
  /**
   * <pre>
   * pagers keep track of which page and how many on a page
   * </pre>
   * @param pagers1 the pagers to set
   */
  public void setPagers(Map<String, GuiPaging> pagers1) {
    this.pagers = pagers1;
  }

  /**
   * <pre>
   * pagers keep track of which page and how many on a page
   * </pre>
   */
  private Map<String, GuiPaging> pagers = new LinkedHashMap<String,GuiPaging>();

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
