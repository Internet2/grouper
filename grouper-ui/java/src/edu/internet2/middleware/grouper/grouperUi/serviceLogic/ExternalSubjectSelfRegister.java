package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.grouperUi.beans.externalSubjectSelfRegister.ExternalRegisterContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;

/**
 * logic for external subject sefl register
 * @author mchyzer
 */
public class ExternalSubjectSelfRegister {

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTopExternal.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/externalSubjectSelfRegister/index.jsp"));

  }

  /**
   * index page of application
   * @param request
   * @param response
   */
  public void externalSubjectSelfRegister(HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //setup the container
    final ExternalRegisterContainer externalRegisterContainer = new ExternalRegisterContainer();
    externalRegisterContainer.storeToRequest();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
        "/WEB-INF/grouperUi/templates/common/commonTopExternal.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/externalSubjectSelfRegister/externalSubjectSelfRegister.jsp"));

  }

  /**
   * 
   * @param request
   * @param response
   */
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    new Misc().logout(request, response);
  }

}
