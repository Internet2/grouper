/**
 * @author Kate
 * $Id: GrouperUiFunctions.java,v 1.1 2009-09-09 15:10:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.tags;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiHideShow;
import edu.internet2.middleware.grouper.grouperUi.j2ee.GrouperUiJ2ee;
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
   * <pre>
   * print out the style value for a hide show
   * 
   * Each hide show has a name, and it should be unique in the app, so be explicit, 
   * below you see "hideShowName", that means whatever name you pick
   * 
   * First add this css class to elements which should show when the state is show:
   * shows_hideShowName
   * 
   * Then add this to things which are in the opposite toggle state: hides_hideShowName
   * 
   * Then add this to the button(s):
   * buttons_hideShowName
   *  
   * In the business logic, you must init the hide show before the JSP draws (this has name,
   * text when shown, hidden, if show initially, and if store in session):
   * GuiHideShow.init("simpleMembershipUpdateAdvanced", false, 
   *    GuiUtils.message("simpleMembershipUpdate.hideAdvancedOptionsButton"), 
   *       GuiUtils.message("simpleMembershipUpdate.showAdvancedOptionsButton"), true);
   *
   * Finally, use these EL functions to display the state correctly in JSP:
   * Something that is hidden/shown
   * style="${grouperGui:hideShowStyle('hideShowName', true)}
   * 
   * Button text:
   * ${grouperGui:hideShowButtonText('hideShowName')}
   * 
   * In the button, use this onclick:
   * onclick="return guiHideShow(event, 'hideShowName');"
   * </pre>
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
  
  /**
   * <pre>
   * print out the button text for a hide show
   * 
   * Each hide show has a name, and it should be unique in the app, so be explicit, 
   * below you see "hideShowName", that means whatever name you pick
   * 
   * First add this css class to elements which should show when the state is show:
   * shows_hideShowName
   * 
   * Then add this to things which are in the opposite toggle state: hides_hideShowName
   * 
   * Then add this to the button(s):
   * buttons_hideShowName
   *  
   * In the business logic, you must init the hide show before the JSP draws (this has name,
   * text when shown, hidden, if show initially, and if store in session):
   * GuiHideShow.init("simpleMembershipUpdateAdvanced", false, 
   *    GuiUtils.message("simpleMembershipUpdate.hideAdvancedOptionsButton"), 
   *       GuiUtils.message("simpleMembershipUpdate.showAdvancedOptionsButton"), true);
   *
   * Finally, use these EL functions to display the state correctly in JSP:
   * Something that is hidden/shown
   * style="${grouperGui:hideShowStyle('hideShowName', true)}
   * 
   * Button text:
   * ${grouperGui:hideShowButtonText('hideShowName')}
   * 
   * In the button, use this onclick:
   * onclick="return guiHideShow(event, 'hideShowName');"
   * </pre>
   * @param hideShowName
   * @return the text
   */
  public static String hideShowButtonText(String hideShowName) {
    
    //we need to find the hide show, either it is something we are initializing, or something sent from browser
    GuiHideShow guiHideShow = GuiHideShow.retrieveHideShow(hideShowName, true);
    
    if (guiHideShow.isShowing()) {
      return guiHideShow.getTextWhenShowing();
    }
    
    return guiHideShow.getTextWhenHidden();
    
  }
  
}
