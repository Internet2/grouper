/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;

/**
 * container object for the response back to screen
 * @author mchyzer
 *
 */
public class GuiResponseJs {

  /**
   * retrieve or create the gui repsonse js object
   * @return the response
   */
  public static GuiResponseJs retrieveGuiResponseJs() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
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
    return actions;
  }

  /**
   * list of actions for screen
   * @param actions1 the actions to set
   */
  public void setActions(List<GuiScreenAction> actions1) {
    this.actions = actions1;
  }
  
  
  
}
