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
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * container object for the response back to screen
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiResponseJs implements Serializable {

  /**
   * print this object to screen
   */
  public void printToScreen() {
    StringBuilder result = new StringBuilder();

    // if this is an ajax file submit, we need to add textarea around response
    // since it is submitted to a hidden frame
    if (this.isAddTextAreaTag()) {
      result.append("<textarea>");
    }
    //take the object to print (bean) and print it
    String json = GrouperUtil.jsonConvertToNoWrap(this);
    result.append(json);
    if (this.isAddTextAreaTag()) {
      result.append("</textarea>");
    }
    
    GrouperUiUtils.printToScreen(result.toString(), 
        this.isAddTextAreaTag() ? HttpContentType.TEXT_HTML : HttpContentType.APPLICATION_JSON, false, false);

  }
  
  /** if this is an ajax file submit, we need to add textarea around response
   * since it is submitted to a hidden frame
   */
  private boolean addTextAreaTag;
  
  
  /**
   * if this is an ajax file submit, we need to add textarea around response
   * since it is submitted to a hidden frame
   * @return the addTextAreaTag
   */
  public boolean isAddTextAreaTag() {
    return this.addTextAreaTag;
  }

  
  /**
   * if this is an ajax file submit, we need to add textarea around response
   * since it is submitted to a hidden frame
   * @param addTextAreaTag1 the addTextAreaTag to set
   */
  public void setAddTextAreaTag(boolean addTextAreaTag1) {
    this.addTextAreaTag = addTextAreaTag1;
  }

  /**
   * add a hide show
   * @param name of hideShow
   * @param guiHideShow1
   */
  public void addHideShow(String name, GuiHideShow guiHideShow1) {
    if (this.hideShows == null) {
      this.hideShows = new LinkedHashMap<String, GuiHideShow>();
    }
    this.hideShows.put(name, guiHideShow1);
  }

  /**
   * add a pager
   * @param name of pager
   * @param guiPaging1
   */
  public void addPager(String name, GuiPaging guiPaging1) {
    if (this.pagers == null) {
      this.pagers = new LinkedHashMap<String, GuiPaging>();
    }
    this.pagers.put(name, guiPaging1);
  }

  /**
   * retrieve or create the gui repsonse js object
   * @return the response
   */
  public static GuiResponseJs retrieveGuiResponseJs() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    GuiResponseJs guiResponseJs = (GuiResponseJs)httpServletRequest.getAttribute("guiResponseJs");
    if (guiResponseJs == null) {
      guiResponseJs = new GuiResponseJs();
      httpServletRequest.setAttribute("guiResponseJs", guiResponseJs);
    }
    return guiResponseJs;
  }
  
  /** list of actions for screen */
  private List<GuiScreenAction> actions = null;
  
  /**
   * <pre>
   * hide shows, the name, and if showing, text, etc.  Anything with class:
   * shows_hideShowName, e.g. shows_simpleMembershipAdvanced
   * Anything with class: hides_hideShowName, e.g. hides_simpleMembershipAdvanced
   * will show if false.
   * The buttons should have the class: buttons_simpleMembershipUpdateGroupDetails
   * </pre>
   */
  private Map<String, GuiHideShow> hideShows = new LinkedHashMap<String,GuiHideShow>();
  
  /**
   * <pre>
   * pagers keep track of which page and how many on a page
   * </pre>
   */
  private Map<String, GuiPaging> pagers = new LinkedHashMap<String,GuiPaging>();

  /**
   * add an action to the action list
   * @param guiScreenAction
   */
  public void addAction(GuiScreenAction guiScreenAction) {
    if (this.actions == null) {
      this.actions = new ArrayList<GuiScreenAction>();
    }
    this.actions.add(guiScreenAction);
  }
  
  /**
   * list of actions for screen
   * @return the actions
   */
  public List<GuiScreenAction> getActions() {
    return this.actions;
  }

  /**
   * list of actions for screen
   * @param actions1 the actions to set
   */
  public void setActions(List<GuiScreenAction> actions1) {
    this.actions = actions1;
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
   * @param pagers1 the pagers to set
   */
  public void setPagers(Map<String, GuiPaging> pagers1) {
    this.pagers = pagers1;
  }

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
   * pagers keep track of which page and how many on a page
   * </pre>
   * @return the pagers
   */
  public Map<String, GuiPaging> getPagers() {
    return this.pagers;
  }
  
  
  
}
