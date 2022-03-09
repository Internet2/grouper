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
import edu.internet2.middleware.grouper.app.oidc.OidcConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiOidcConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.OidcConfigContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2OidcConfig {
  
  protected static Log LOG = LogFactory.getLog(UiV2OidcConfig.class);
  
  /**
   * if allowed to view oidc config
   * @return true if allowed to view oidc config
   */
  private boolean allowedToViewOidcConfig() {

    Map<String, Object> debugMap = null;
    
    if (LOG.isDebugEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "allowedToViewOidcConfig");
    }
    try {
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final boolean canViewOidcConfig = GrouperRequestContainer.retrieveFromRequestOrCreate().getOidcConfigContainer().isCanViewOidcConfig();

      if (debugMap != null) {
        debugMap.put("canViewOidcConfig", canViewOidcConfig);
      }

      if (!canViewOidcConfig) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("authenticationNotAllowedToView")));
        return false;
      }
      
      return true;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }

  }
  
  public void viewOidcConfigs(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      OidcConfigContainer oidcConfigContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getOidcConfigContainer();
      
      if (!allowedToViewOidcConfig()) {
        return;
      }
      
      List<OidcConfiguration> oidcConfigs = OidcConfiguration.retrieveAllOidcConfigs();
      
      List<GuiOidcConfiguration> guiOidcConfigs = GuiOidcConfiguration.convertFromOidcConfiguration(oidcConfigs);
      
      oidcConfigContainer.setGuiOidcConfigs(guiOidcConfigs);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/authentication/oidcConfigs.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  public void addOidcConfig(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      OidcConfigContainer oidcConfigContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getOidcConfigContainer();
      
      if (!allowedToViewOidcConfig()) {
        return;
      }
      
      String configId = request.getParameter("oidcConfigId");
      
      String configType = request.getParameter("oidcConfigType");
      
      if (StringUtils.isNotBlank(configType)) {
        
        Class<OidcConfiguration> klass = (Class<OidcConfiguration>) GrouperUtil.forName(configType);
        OidcConfiguration oidcConfiguration = (OidcConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#wsTrustedJwtConfigId",
              TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
          return;
        }
        
        oidcConfiguration.setConfigId(configId);
        oidcConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiOidcConfiguration guiOidcConfiguration = GuiOidcConfiguration.convertFromOidcConfiguration(oidcConfiguration);
        oidcConfigContainer.setGuiOidcConfiguration(guiOidcConfiguration);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/authentication/wsTrustedOidcConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * insert a new gsh template in db
   * @param request
   * @param response
   */
  public void addOidcConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewOidcConfig()) {
        return;
      }
      
      String configId = request.getParameter("oidcConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#wsTrustedJwtConfigId",
            TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
        return;
      }
      
      OidcConfiguration oidcConfiguration = new OidcConfiguration();
      
      oidcConfiguration.setConfigId(configId);
      oidcConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      oidcConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2OidcConfig.viewOidcConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit oidc config page
   * @param request
   * @param response
   */
  public void editOidcConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      OidcConfigContainer oidcConfigContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getOidcConfigContainer();
      
      if (!allowedToViewOidcConfig()) {
        return;
      }
      
      String configId = request.getParameter("oidcConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#wsTrustedJwtConfigId",
            TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
        return;
      }
      
      OidcConfiguration oidcConfiguration = new OidcConfiguration();
      
      oidcConfiguration.setConfigId(configId);
      
      String previousOidcConfigId = request.getParameter("previousOidcConfigId");
      
      if (StringUtils.isBlank(previousOidcConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiOidcConfiguration guiOidcConfiguration = GuiOidcConfiguration.convertFromOidcConfiguration(oidcConfiguration);
        oidcConfigContainer.setGuiOidcConfiguration(guiOidcConfiguration);
      } else {
        // change was made on the form
        oidcConfiguration.populateConfigurationValuesFromUi(request);
        GuiOidcConfiguration guiOidcConfiguration = GuiOidcConfiguration.convertFromOidcConfiguration(oidcConfiguration);
        oidcConfigContainer.setGuiOidcConfiguration(guiOidcConfiguration);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/authentication/editWsTrustedOidcConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited oidc config into db
   * @param request
   * @param response
   */
  public void editOidcConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewOidcConfig()) {
        return;
      }
      
      String configId = request.getParameter("oidcConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#wsTrustedJwtConfigId",
            TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
        return;
      }
      
      OidcConfiguration oidcConfiguration = new OidcConfiguration();
      
      oidcConfiguration.setConfigId(configId);  
      
      oidcConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      oidcConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2OidcConfig.viewOidcConfigs')"));
      
      if (actionsPerformed.size() > 0) {

        for (String actionPerformed: actionsPerformed) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, actionPerformed));
        }
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigAddEditSuccess")));
   
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete gsh template
   * @param request
   * @param response
   */
  public void deleteOidcConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewOidcConfig()) {
        return;
      }
      
      String configId = request.getParameter("oidcConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#wsTrustedJwtConfigId",
            TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
        return;
      }
      
      OidcConfiguration oidcConfiguration = new OidcConfiguration();
      
      oidcConfiguration.setConfigId(configId);  
      
      oidcConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2OidcConfig.viewOidcConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * disable oidc config
   * @param request
   * @param response
   */
  public void disableOidcConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewOidcConfig()) {
        return;
      }
      
      String configId = request.getParameter("oidcConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("wsTrustedJwtConfigId cannot be blank");
      }
      
      OidcConfiguration oidcConfiguration = new OidcConfiguration();
      
      oidcConfiguration.setConfigId(configId);  
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      oidcConfiguration.changeStatus(false, message, errorsToDisplay, validationErrorsToDisplay);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2OidcConfig.viewOidcConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * enable oidc config
   * @param request
   * @param response
   */
  public void enableOidcConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewOidcConfig()) {
        return;
      }
      
      String configId = request.getParameter("oidcConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("oidcConfigId cannot be blank");
      }
      
      OidcConfiguration oidcConfiguration = new OidcConfiguration();
      
      oidcConfiguration.setConfigId(configId);  
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      oidcConfiguration.changeStatus(true, message, errorsToDisplay, validationErrorsToDisplay);
      
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {

        for (String errorToDisplay: errorsToDisplay) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errorToDisplay));
        }
        for (String validationKey: validationErrorsToDisplay.keySet()) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
              validationErrorsToDisplay.get(validationKey)));
        }

        return;

      }
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2OidcConfig.viewOidcConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
