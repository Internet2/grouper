/**
 * @author Kate
 * $Id: GrouperUiFunctions.java,v 1.1 2009-08-07 07:36:01 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.tags;

import edu.internet2.middleware.grouper.grouperUi.json.GuiHideShow;


/**
 * EL functions
 */
public class GrouperUiFunctions {

  /**
   * print out the style value for a hide show
   * @param hideShowName
   * @param showWhenShowing true if the section should show when the hide show is showing
   * @return the style
   */
  public static String hideShowStyle(String hideShowName, boolean showWhenShowing) {
    
    //we need to find the hide show, either it is something we are initializing, or something sent from browser
    GuiHideShow guiHideShow = GuiHideShow.retrieveHideShow(hideShowName, true);
    
    if (guiHideShow.isShowing() != showWhenShowing) {
      return "display: none;";
    }
    
    //note, dont make assumptions, do the default
    return "";
    
  }
  
}
