package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.customUi.CustomUiConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.CustomUiContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiCustomUiConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2CustomUiConfig {
  
  /**
   * @param request
   * @param response
   */
  public void viewCustomUiConfigs(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
      
      if (!customUiContainer.isCanViewCustomUiMisc()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<CustomUiConfiguration> customUiConfigs = CustomUiConfiguration.retrieveAllCustomUiConfigs();
      
      List<GuiCustomUiConfiguration> guiCustomUiConfigs = GuiCustomUiConfiguration.convertFromCustomUiConfiguration(customUiConfigs);
      
      customUiContainer.setGuiCustomUiConfigurations(guiCustomUiConfigs);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/customUi/customUiConfigs.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    } 
  }
  
  /**
   * @param request
   * @param response
   */
  public void addCustomUiConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
      
      if (!customUiContainer.isCanViewCustomUiMisc()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String customUiConfigId = request.getParameter("customUiConfigId");
      
      String customUiType = request.getParameter("customUiType");
      
      if (StringUtils.isNotBlank(customUiType)) {
        
        Class<CustomUiConfiguration> klass = (Class<CustomUiConfiguration>) GrouperUtil.forName(customUiType);
        CustomUiConfiguration customUiConfiguration = (CustomUiConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(customUiConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#customUiConfigId",
              TextContainer.retrieveFromRequest().getText().get("customUiCreateErrorConfigIdRequired")));
          return;
        }
        
        customUiConfiguration.setConfigId(customUiConfigId);
        customUiConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiCustomUiConfiguration guiCustomUiConfig = GuiCustomUiConfiguration.convertFromCustomUiConfiguration(customUiConfiguration);
        customUiContainer.setGuiCustomUiConfiguration(guiCustomUiConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/customUi/customUiConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * insert a new custom ui config in db
   * @param request
   * @param response
   */
  public void addCustomUiConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
      
      if (!customUiContainer.isCanViewCustomUiMisc()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String customUiConfigId = request.getParameter("customUiConfigId");
      
      if (StringUtils.isBlank(customUiConfigId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#customUiConfigId",
            TextContainer.retrieveFromRequest().getText().get("customUiCreateErrorConfigIdRequired")));
        return;
      }
      
      CustomUiConfiguration customUiConfiguration = new CustomUiConfiguration();
      
      customUiConfiguration.setConfigId(customUiConfigId);
      customUiConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      customUiConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2CustomUiConfig.viewCustomUiConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("customUiConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit custom ui config
   * @param request
   * @param response
   */
  public void editCustomUiConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
      
      if (!customUiContainer.isCanViewCustomUiMisc()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String customUiConfigId = request.getParameter("customUiConfigId");
      
      if (StringUtils.isBlank(customUiConfigId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#customUiConfigId",
            TextContainer.retrieveFromRequest().getText().get("customUiCreateErrorConfigIdRequired")));
        return;
      }
      
      CustomUiConfiguration customUiConfiguration = new CustomUiConfiguration();
      
      customUiConfiguration.setConfigId(customUiConfigId);
      
      String previousCustomUiConfigId = request.getParameter("previousCustomUiConfigId");
      
      if (StringUtils.isBlank(previousCustomUiConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiCustomUiConfiguration guiCustomUiConfiguration = GuiCustomUiConfiguration.convertFromCustomUiConfiguration(customUiConfiguration);
        customUiContainer.setGuiCustomUiConfiguration(guiCustomUiConfiguration);
      } else {
        // change was made on the form
        customUiConfiguration.populateConfigurationValuesFromUi(request);
        GuiCustomUiConfiguration guiCustomUiConfiguration = GuiCustomUiConfiguration.convertFromCustomUiConfiguration(customUiConfiguration);
        customUiContainer.setGuiCustomUiConfiguration(guiCustomUiConfiguration);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/customUi/editCustomUiConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited custom ui config into db
   * @param request
   * @param response
   */
  public void editCustomUiConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
      
      if (!customUiContainer.isCanViewCustomUiMisc()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String customUiConfigId = request.getParameter("customUiConfigId");
      
      if (StringUtils.isBlank(customUiConfigId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#customUiConfigId",
            TextContainer.retrieveFromRequest().getText().get("customUiCreateErrorConfigIdRequired")));
        return;
      }
      
      CustomUiConfiguration customUiConfiguration = new CustomUiConfiguration();
      
      customUiConfiguration.setConfigId(customUiConfigId);
      
      customUiConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      customUiConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2CustomUiConfig.viewCustomUiConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("customUiConfigAddEditSuccess")));
   
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete custom ui config
   * @param request
   * @param response
   */
  public void deleteCustomUiConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
      
      if (!customUiContainer.isCanViewCustomUiMisc()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String customUiConfigId = request.getParameter("customUiConfigId");
      
      if (StringUtils.isBlank(customUiConfigId)) {
        throw new RuntimeException("customUiConfigId cannot be blank");
      }
      
      CustomUiConfiguration customUiConfiguration = new CustomUiConfiguration();
      
      customUiConfiguration.setConfigId(customUiConfigId);
      
      customUiConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2CustomUiConfig.viewCustomUiConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("customUiConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * disable custom ui config
   * @param request
   * @param response
   */
  public void disableCustomUiConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
      
      if (!customUiContainer.isCanViewCustomUiMisc()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String customUiConfigId = request.getParameter("customUiConfigId");
      
      if (StringUtils.isBlank(customUiConfigId)) {
        throw new RuntimeException("customUiConfigId cannot be blank");
      }
      
      CustomUiConfiguration customUiConfiguration = new CustomUiConfiguration();
      
      customUiConfiguration.setConfigId(customUiConfigId);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      customUiConfiguration.changeStatus(false, message, errorsToDisplay, validationErrorsToDisplay);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2CustomUiConfig.viewCustomUiConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("customUiConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * enable custom ui config
   * @param request
   * @param response
   */
  public void enableCustomUiConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      CustomUiContainer customUiContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomUiContainer();
      
      if (!customUiContainer.isCanViewCustomUiMisc()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String customUiConfigId = request.getParameter("customUiConfigId");
      
      if (StringUtils.isBlank(customUiConfigId)) {
        throw new RuntimeException("customUiConfigId cannot be blank");
      }
      
      CustomUiConfiguration customUiConfiguration = new CustomUiConfiguration();
      
      customUiConfiguration.setConfigId(customUiConfigId);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      customUiConfiguration.changeStatus(true, message, errorsToDisplay, validationErrorsToDisplay);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2CustomUiConfig.viewCustomUiConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("customUiConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
