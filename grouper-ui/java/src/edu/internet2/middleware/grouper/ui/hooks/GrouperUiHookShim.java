/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.hooks;
 
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.util.GrouperUtil;
 
 
/**
 * https://spaces.at.internet2.edu/display/Grouper/Grouper+hook+which+adds+link+to+UI
 */
public class GrouperUiHookShim {
 
  /**
   *
   */
  public GrouperUiHookShim() {
  }
 
  /**
   * https://getbootstrap.com/docs/4.0/components/alerts/
   * bootstrap message types
   */
  private static Set<String> messageTypes = GrouperUtil.toSet("primary", "secondary", "success", "danger", "warning", "info", "light", "dark");
  
  /**
   * https://getbootstrap.com/docs/4.0/components/alerts/
   * @param bootstrapAlertType "primary", "secondary", "success", "danger", "warning", "info", "light", "dark"
   * @param escapedMessage note single quotes will be escaped, maybe dont include single quotes
   */
  public static void addMessageToScreen(String bootstrapAlertType, String escapedMessage) {
    
    if (messageTypes.contains(bootstrapAlertType)) {
      throw new RuntimeException("bootstrapAlertType '" + bootstrapAlertType + "' must be one of: " + StringUtils.join(messageTypes, ','));
    }
    
    addScript("$('#messaging').before('<div class=\"messaging row-fluid\""
        + " style=\"display: block;\"><div role=\"alert\" class=\"alert alert-" + bootstrapAlertType + "\"><button type=\"button\""
        + " class=\"close\" data-dismiss=\"alert\">x</button>"
        + GrouperUtil.escapeSingleQuotes(escapedMessage) + "</div></div>');");
     
  }

  /**
   * @param script
   */
  public static void addScript(String script) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newScript(script));
     
  }
}