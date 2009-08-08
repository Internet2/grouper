/**
 * @author Kate
 * $Id: GrouperUiFunctions.java,v 1.2 2009-08-08 06:19:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.tags;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.grouper.grouperUi.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.grouperUi.util.MapBundleWrapper;


/**
 * EL functions
 */
public class GrouperUiFunctions {

  /**
   * prints out a message, assumes it is there
   * @param key
   * @param escapeHtml (true to escape html)
   * @param escapeSingleQuotes if escaping html, should we also escape single quotes?
   * @return the message string
   */
  public static String message(String key, boolean escapeHtml, boolean escapeSingleQuotes) {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    MapBundleWrapper mapBundleWrapper = (MapBundleWrapper)httpServletRequest.getSession().getAttribute("navNullMap");
    
    String value = (String)mapBundleWrapper.get(key);

    if (escapeHtml) {
      value = GuiUtils.escapeHtml(value, true, escapeSingleQuotes);
    }
    
    return value;
  }
  
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
