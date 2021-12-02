package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.resolver.GlobalAttributeResolverConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GlobalAttributeResolverConfigContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGlobalAttributeResolverConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2GlobalAttributeResolverConfig {
  
  protected static Log LOG = LogFactory.getLog(UiV2GlobalAttributeResolverConfig.class);
  
  /**
   * if allowed to view global attribute resolver configs
   * @return true if allowed to view authentication
   */
  private boolean allowedToViewGlobalAttributeResolverConfigs() {

    Map<String, Object> debugMap = null;
    
    if (LOG.isDebugEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "allowedToViewGlobalAttributeResolverConfigs");
    }
    try {
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final boolean canViewGlobalAttributeResolverConfig = GrouperRequestContainer.retrieveFromRequestOrCreate().getGlobalAttributeResolverConfigContainer().isCanViewGlobalAttributeResolverConfig();

      if (debugMap != null) {
        debugMap.put("canViewGlobalAttributeResolverConfig", canViewGlobalAttributeResolverConfig);
      }

      if (!canViewGlobalAttributeResolverConfig) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverConfigNotAllowedToView")));
        return false;
      }
      
      return true;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

  }
  
  public void viewGlobalAttributeResolverConfigs(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GlobalAttributeResolverConfigContainer globalAttributeResolverConfigContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGlobalAttributeResolverConfigContainer();
      
      if (!allowedToViewGlobalAttributeResolverConfigs()) {
        return;
      }
      
      List<GlobalAttributeResolverConfiguration> globalAttributeResolverConfigurations = GlobalAttributeResolverConfiguration.retrieveAllGlobalAttributeResolverConfigurations();
      
      List<GuiGlobalAttributeResolverConfiguration> guiGlobalAttributeResolverConfigs = GuiGlobalAttributeResolverConfiguration.convertFromWsTrustedJwtConfiguration(globalAttributeResolverConfigurations);
      
      globalAttributeResolverConfigContainer.setGuiGlobalAttributeResolverConfigs(guiGlobalAttributeResolverConfigs);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/globalAttributeResolver/globalAttributeResolverConfigs.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  public void addGlobalAttributeResolverConfig(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GlobalAttributeResolverConfigContainer globalAttributeResolverConfigContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGlobalAttributeResolverConfigContainer();
      
      if (!allowedToViewGlobalAttributeResolverConfigs()) {
        return;
      }
      
      String configId = request.getParameter("globalAttributeResolverConfigId");
      
      String configType = request.getParameter("globalAttributeResolverConfigType");
      
      if (StringUtils.isNotBlank(configType)) {
        
        Class<GlobalAttributeResolverConfiguration> klass = (Class<GlobalAttributeResolverConfiguration>) GrouperUtil.forName(configType);
        GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration = (GlobalAttributeResolverConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#globalAttributeResolverConfigId",
              TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverErrorConfigIdRequired")));
          return;
        }
        
        globalAttributeResolverConfiguration.setConfigId(configId);
        globalAttributeResolverConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiGlobalAttributeResolverConfiguration guiGlobalAttributeResolverConfiguration = GuiGlobalAttributeResolverConfiguration.convertFromGlobalAttributeResolverConfiguration(globalAttributeResolverConfiguration);
        globalAttributeResolverConfigContainer.setGuiGlobalAttributeResolverConfiguration(guiGlobalAttributeResolverConfiguration);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/globalAttributeResolver/globalAttributeResolverConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * insert a new global attribute resolver config in db
   * @param request
   * @param response
   */
  public void addGlobalAttributeResolverConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewGlobalAttributeResolverConfigs()) {
        return;
      }
      
      String configId = request.getParameter("globalAttributeResolverConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#globalAttributeResolverConfigId",
            TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverErrorConfigIdRequired")));
        return;
      }
      
      GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration = new GlobalAttributeResolverConfiguration();
      
      globalAttributeResolverConfiguration.setConfigId(configId);
      globalAttributeResolverConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      globalAttributeResolverConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GlobalAttributeResolverConfig.viewGlobalAttributeResolverConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit global attribute resolver config page
   * @param request
   * @param response
   */
  public void editGlobalAttributeResolverConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GlobalAttributeResolverConfigContainer globalAttributeResolverConfigContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGlobalAttributeResolverConfigContainer();
      
      if (!allowedToViewGlobalAttributeResolverConfigs()) {
        return;
      }
      
      String configId = request.getParameter("globalAttributeResolverConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#globalAttributeResolverConfigId",
            TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverErrorConfigIdRequired")));
        return;
      }
      
      GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration = new GlobalAttributeResolverConfiguration();
      
      globalAttributeResolverConfiguration.setConfigId(configId);
      
      String previousWsTrustedJwtConfigId = request.getParameter("previousGlobalAttributeResolverConfigId");
      
      if (StringUtils.isBlank(previousWsTrustedJwtConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiGlobalAttributeResolverConfiguration guiGlobalAttributeResolverConfiguration = GuiGlobalAttributeResolverConfiguration.convertFromGlobalAttributeResolverConfiguration(globalAttributeResolverConfiguration);
        globalAttributeResolverConfigContainer.setGuiGlobalAttributeResolverConfiguration(guiGlobalAttributeResolverConfiguration);
      } else {
        // change was made on the form
        globalAttributeResolverConfiguration.populateConfigurationValuesFromUi(request);
        GuiGlobalAttributeResolverConfiguration guiGlobalAttributeResolverConfiguration = GuiGlobalAttributeResolverConfiguration.convertFromGlobalAttributeResolverConfiguration(globalAttributeResolverConfiguration);
        globalAttributeResolverConfigContainer.setGuiGlobalAttributeResolverConfiguration(guiGlobalAttributeResolverConfiguration);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/globalAttributeResolver/editGlobalAttributeResolverConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited global attribute resolver config into db
   * @param request
   * @param response
   */
  public void editGlobalAttributeResolverConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewGlobalAttributeResolverConfigs()) {
        return;
      }
      
      String configId = request.getParameter("globalAttributeResolverConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#globalAttributeResolverConfigId",
            TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverErrorConfigIdRequired")));
        return;
      }
      
      GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration = new GlobalAttributeResolverConfiguration();
      
      globalAttributeResolverConfiguration.setConfigId(configId);
      
      globalAttributeResolverConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      globalAttributeResolverConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GlobalAttributeResolverConfig.viewGlobalAttributeResolverConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverConfigAddEditSuccess")));
   
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete global attribute resolver config
   * @param request
   * @param response
   */
  public void deleteGlobalAttributeResolverConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewGlobalAttributeResolverConfigs()) {
        return;
      }
      
      String configId = request.getParameter("globalAttributeResolverConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#globalAttributeResolverConfigId",
            TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverErrorConfigIdRequired")));
        return;
      }
      
      GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration = new GlobalAttributeResolverConfiguration();
      
      globalAttributeResolverConfiguration.setConfigId(configId);
      
      globalAttributeResolverConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GlobalAttributeResolverConfig.viewGlobalAttributeResolverConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * disable global attribute resolver config
   * @param request
   * @param response
   */
  public void disableGlobalAttributeResolverConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewGlobalAttributeResolverConfigs()) {
        return;
      }
      
      String configId = request.getParameter("globalAttributeResolverConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("globalAttributeResolverConfigId cannot be blank");
      }
      
      GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration = new GlobalAttributeResolverConfiguration();
      
      globalAttributeResolverConfiguration.setConfigId(configId);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      globalAttributeResolverConfiguration.changeStatus(false, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GlobalAttributeResolverConfig.viewGlobalAttributeResolverConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * enable global attribute resolver config
   * @param request
   * @param response
   */
  public void enableGlobalAttributeResolverConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewGlobalAttributeResolverConfigs()) {
        return;
      }
      
      String configId = request.getParameter("globalAttributeResolverConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("globalAttributeResolverConfigId cannot be blank");
      }
      
      GlobalAttributeResolverConfiguration globalAttributeResolverConfiguration = new GlobalAttributeResolverConfiguration();
      
      globalAttributeResolverConfiguration.setConfigId(configId);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      globalAttributeResolverConfiguration.changeStatus(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GlobalAttributeResolverConfig.viewGlobalAttributeResolverConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("globalAttributeResolverConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
