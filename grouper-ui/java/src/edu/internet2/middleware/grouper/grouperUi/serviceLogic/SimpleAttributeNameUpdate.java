package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.grouperUi.beans.attributeNameUpdate.AttributeNameUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;

/**
 * ajax method logic for the attribute definition name screen
 * @author mchyzer
 *
 */
public class SimpleAttributeNameUpdate {

  
  /**
   * index page of application
   * @param request
   * @param response
   */
  public void createEditAttributeNames(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //setup the container
    AttributeNameUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
        + GrouperUiUtils.message("simpleAttributeNameUpdate.addEditTitle", false) + "'"));
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeNameUpdate/simpleAttributeNameCreateEditInit.jsp"));
  
  }

}
