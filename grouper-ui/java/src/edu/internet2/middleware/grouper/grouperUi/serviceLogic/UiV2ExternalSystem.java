package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystemAttribute;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ExternalSystemContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGrouperExternalSystem;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2ExternalSystem {
  
  /**
   * view all external systems
   * @param request
   * @param response
   */
  public void viewExternalSystems(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ExternalSystemContainer externalSystemContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getExternalSystemContainer();
      
      if (!externalSystemContainer.isCanViewExternalSystems()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GrouperExternalSystem> grouperExternalSystems = GrouperExternalSystem.retrieveAllGrouperExternalSystems();
      
      List<GuiGrouperExternalSystem> guiGrouperExternalSystems = GuiGrouperExternalSystem.convertFromGrouperExternalSystem(grouperExternalSystems);
      
      externalSystemContainer.setGuiGrouperExternalSystems(guiGrouperExternalSystems);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/externalSystems/externalSystems.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view details of one external system for edit purposes
   * @param request
   * @param response
   */
  public void editExternalSystemConfigDetails(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ExternalSystemContainer externalSystemContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getExternalSystemContainer();
      
      if (!externalSystemContainer.isCanViewExternalSystems()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      final String externalSystemConfigId = request.getParameter("externalSystemConfigId");
      final String externalSystemType = request.getParameter("externalSystemType");
      
      if (StringUtils.isBlank(externalSystemConfigId)) {
        throw new RuntimeException("externalSystemConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(externalSystemType)) {
        throw new RuntimeException("externalSystemType cannot be blank");
      }
      
      if (!GrouperExternalSystem.externalTypeClassNames.contains(externalSystemType)) {
        throw new RuntimeException("Invalid externalSystemType "+externalSystemType);
      }
      
      Class<GrouperExternalSystem> klass = (Class<GrouperExternalSystem>) GrouperUtil.forName(externalSystemType);
      GrouperExternalSystem grouperExternalSystem = (GrouperExternalSystem) GrouperUtil.newInstance(klass);
      
      grouperExternalSystem.setConfigId(externalSystemConfigId);
      
      String previousExternalSystemConfigId = request.getParameter("previousExternalSystemConfigId");
      
      if (StringUtils.isBlank(previousExternalSystemConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiGrouperExternalSystem guiGrouperExternalSystem = GuiGrouperExternalSystem.convertFromGrouperExternalSystem(grouperExternalSystem);
        
        externalSystemContainer.setGuiGrouperExternalSystem(guiGrouperExternalSystem);
      }
      
      // change was made on the form
      if (StringUtils.isNotBlank(previousExternalSystemConfigId)) {
        populateGrouperExternalSystemFromUi(request, grouperExternalSystem);
        GuiGrouperExternalSystem guiGrouperExternalSystem = GuiGrouperExternalSystem.convertFromGrouperExternalSystem(grouperExternalSystem);
        externalSystemContainer.setGuiGrouperExternalSystem(guiGrouperExternalSystem);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/externalSystems/editExternalSystemConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save the new values
   * @param request
   * @param response
   */
  public void editExternalSystemConfigDetailsSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ExternalSystemContainer externalSystemContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getExternalSystemContainer();
      
      if (!externalSystemContainer.isCanViewExternalSystems()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      final String externalSystemConfigId = request.getParameter("externalSystemConfigId");
      final String externalSystemType = request.getParameter("externalSystemType");
      
      if (StringUtils.isBlank(externalSystemConfigId)) {
        throw new RuntimeException("externalSystemConfigId cannot be blank");
      }
      
      if (StringUtils.isBlank(externalSystemType)) {
        throw new RuntimeException("externalSystemType cannot be blank");
      }
      
      if (!GrouperExternalSystem.externalTypeClassNames.contains(externalSystemType)) {
        throw new RuntimeException("Invalid externalSystemType "+externalSystemType);
      }
      
      Class<GrouperExternalSystem> klass = (Class<GrouperExternalSystem>) GrouperUtil.forName(externalSystemType);
      GrouperExternalSystem grouperExternalSystem = (GrouperExternalSystem) GrouperUtil.newInstance(klass);
      
      grouperExternalSystem.setConfigId(externalSystemConfigId);
      
      populateGrouperExternalSystemFromUi(request, grouperExternalSystem);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      grouperExternalSystem.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ExternalSystem.viewExternalSystems')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConfigAddEditSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  private void populateGrouperExternalSystemFromUi(final HttpServletRequest request, GrouperExternalSystem externalSystem) {
    
    Map<String, GrouperExternalSystemAttribute> attributes = externalSystem.retrieveAttributes();
    
    for (GrouperExternalSystemAttribute attribute: attributes.values()) {
      String name = "config_"+attribute.getConfigSuffix();
      String elCheckboxName = "config_el_"+attribute.getConfigSuffix();
      
      String elValue = request.getParameter(elCheckboxName);
      
      String value = request.getParameter(name);
      
      if (StringUtils.isNotBlank(elValue) && elValue.equalsIgnoreCase("on")) {
        attribute.setExpressionLanguage(true);
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setExpressionLanguageScript(value);
      } else {
        attribute.setExpressionLanguage(false);
        attribute.setValue(value);
      }
        
    }
    
    
  }

}
