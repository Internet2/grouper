/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.7 2009-11-13 14:56:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateSessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.simpleMembershipUpdate.SimpleMembershipUpdateContainer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;


/**
 * main ajax methods for simple attribute update module
 */
public class SimpleAttributeUpdate {

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newScript("document.title = '" 
        + GrouperUiUtils.message("simpleAttributeUpdate.title", false) + "'"));
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeUpdateIndex.jsp"));

  }

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void assignInit(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //setup the container
    final AttributeUpdateSessionContainer attributeUpdateSessionContainer = new AttributeUpdateSessionContainer();
    //simpleMembershipUpdateContainer.storeToSession();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignInit.jsp"));
  
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(SimpleAttributeUpdate.class);
  
}
