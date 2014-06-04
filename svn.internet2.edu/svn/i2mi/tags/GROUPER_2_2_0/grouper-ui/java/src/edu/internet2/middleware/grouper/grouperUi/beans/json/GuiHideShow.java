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
 * $Id: GuiHideShow.java,v 1.1 2009-09-09 15:10:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.json;

import java.io.Serializable;

import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * <pre>
 * hide shows, the name, and if showing, text, etc.  Anything with class:
 * shows_hideShowName, e.g. shows_simpleMembershipAdvanced
 * Anything with class: hides_hideShowName, e.g. hides_simpleMembershipAdvanced
 * will show if false.
 * The buttons should have the class: buttons_simpleMembershipUpdateGroupDetails
 * </pre>
 * state on screen of hide show
 */
public class GuiHideShow implements Serializable {

  /**
   * init a hide show if not in session (and using session)
   * @param hideShowName
   * @param showing
   * @param textWhenShowing
   * @param textWhenHidden
   * @param storeInSession if this should persist in session
   */
  public static void init(String hideShowName, boolean showing, String textWhenShowing, String textWhenHidden, boolean storeInSession) {
    
    GuiHideShow guiHideShow = storeInSession ? SessionContainer.retrieveFromSession().getHideShows().get(hideShowName) : null;
    
    if (guiHideShow == null) {
      guiHideShow = new GuiHideShow(showing, textWhenShowing, textWhenHidden);
    }
    
    //setup a hideShow to send to screen
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addHideShow(hideShowName, guiHideShow);
    if (storeInSession) {
      SessionContainer.retrieveFromSession().getHideShows().put(hideShowName, guiHideShow);
    }
  }
  
  /**
   * find a hide show in response or app state
   * @param hideShowName
   * @param exceptionIfNotFound
   * @return the hide show
   */
  public static GuiHideShow retrieveHideShow(String hideShowName, boolean exceptionIfNotFound) {
    //we need to find the hide show, either it is something we are initializing, or something sent from browser
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    GuiHideShow guiHideShow = (GrouperUtil.nonNull(guiResponseJs.getHideShows())).get(hideShowName);
    if (guiHideShow == null) {
      
      AppState appState = AppState.retrieveFromRequest();
      guiHideShow = (GrouperUtil.nonNull(appState.getHideShows())).get(hideShowName);
      
    }
    
    //last ditch effort, get from session
    if (guiHideShow == null) {
      guiHideShow = SessionContainer.retrieveFromSession().getHideShows().get(hideShowName);
    }
    
    if (guiHideShow == null && exceptionIfNotFound) {
      throw new RuntimeException("Cant find hideShow: '" + hideShowName + "' it needs to exist on the screen or be initted");
    }
    return guiHideShow;
  }
  
  /**
   * default constructor
   */
  public GuiHideShow() {
    //not much to do
  }
  
  /**
   * <pre>
   * hide shows, the name, and if showing, text, etc.  Anything with class:
   * shows_hideShowName, e.g. shows_simpleMembershipAdvanced
   * Anything with class: hides_hideShowName, e.g. hides_simpleMembershipAdvanced
   * will show if false.
   * The buttons should have the class: buttons_simpleMembershipUpdateGroupDetails
   * </pre>
   * construct with fields
   * @param showing1
   * @param textWhenShowing1
   * @param testWhenHidden1
   */
  public GuiHideShow(boolean showing1, String textWhenShowing1, String testWhenHidden1) {
    super();
    this.showing = showing1;
    this.textWhenShowing = textWhenShowing1;
    this.textWhenHidden = testWhenHidden1;
  }

  /** if showing */
  private boolean showing;

  /** text when showing */
  private String textWhenShowing;
  
  /** text when hidden */
  private String textWhenHidden;

  
  /**
   * if showing
   * @return the showing
   */
  public boolean isShowing() {
    return this.showing;
  }

  
  /**
   * if showing
   * @param showing1 the showing to set
   */
  public void setShowing(boolean showing1) {
    this.showing = showing1;
  }

  
  /**
   * text when showing
   * @return the textWhenShowing
   */
  public String getTextWhenShowing() {
    return this.textWhenShowing;
  }

  
  /**
   * text when showing
   * @param textWhenShowing1 the textWhenShowing to set
   */
  public void setTextWhenShowing(String textWhenShowing1) {
    this.textWhenShowing = textWhenShowing1;
  }

  
  /**
   * text when hidden
   * @return the testWhenHidden
   */
  public String getTextWhenHidden() {
    return this.textWhenHidden;
  }

  
  /**
   * text when hidden
   * @param testWhenHidden1 the testWhenHidden to set
   */
  public void setTextWhenHidden(String testWhenHidden1) {
    this.textWhenHidden = testWhenHidden1;
  }
  
  
  
}
