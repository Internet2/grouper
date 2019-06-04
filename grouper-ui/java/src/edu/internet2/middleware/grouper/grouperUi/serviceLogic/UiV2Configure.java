package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ConfigurationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2Configure {

  /**
   * if allowed to view configuration
   * @return true if allowed to view configuration
   */
  public boolean allowedToViewConfiguration() {

    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.configuration.enabled", true)) {
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("configurationNotEnabled")));
      return false;

    }
    
    if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getAdminContainer().isConfigureShow()) {
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("configurationNotAllowedToView")));
      return false;
    }
    
    return true;

  }
  
  /**
   * configure
   * @param request
   * @param response
   */
  public void configure(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
      
      {
        String configFileString = request.getParameter("configFile");
        ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
        configurationContainer.setConfigFileName(configFileName);
      }      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configure.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
