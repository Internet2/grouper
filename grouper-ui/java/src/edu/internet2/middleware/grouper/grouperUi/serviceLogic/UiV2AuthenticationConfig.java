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
import edu.internet2.middleware.grouper.authentication.WsTrustedJwtConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AuthenticationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiWsTrustedJwtConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2AuthenticationConfig {
  
  protected static Log LOG = LogFactory.getLog(UiV2AuthenticationConfig.class);
  
  /**
   * if allowed to view authentication
   * @return true if allowed to view authentication
   */
  private boolean allowedToViewAuthentication() {

    Map<String, Object> debugMap = null;
    
    if (LOG.isDebugEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "allowedToViewAuthentication");
    }
    try {
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final boolean canViewAuthentication = GrouperRequestContainer.retrieveFromRequestOrCreate().getAuthenticationContainer().isCanViewAuthentication();

      if (debugMap != null) {
        debugMap.put("canViewAuthentication", canViewAuthentication);
      }

      if (!canViewAuthentication) {
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
  
  
  /**
   * configure
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/authentication/authenticationIndex.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void viewWsTrustedJwts(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      AuthenticationContainer authenticationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAuthenticationContainer();
      
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      List<WsTrustedJwtConfiguration> wsTrustedJwtConfigs = WsTrustedJwtConfiguration.retrieveAllWsTrustedJwtConfigs();
      
      List<GuiWsTrustedJwtConfiguration> guiWsTrustedJwtConfigs = GuiWsTrustedJwtConfiguration.convertFromWsTrustedJwtConfiguration(wsTrustedJwtConfigs);
      
      authenticationContainer.setGuiWsTrustedJwtConfigs(guiWsTrustedJwtConfigs);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/authentication/wsTrustedJwts.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  public void addWsTrustedJwt(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      AuthenticationContainer authenticationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAuthenticationContainer();
      
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      String configId = request.getParameter("wsTrustedJwtConfigId");
      
      String configType = request.getParameter("wsTrustedJwtConfigType");
      
      if (StringUtils.isNotBlank(configType)) {
        
        Class<WsTrustedJwtConfiguration> klass = (Class<WsTrustedJwtConfiguration>) GrouperUtil.forName(configType);
        WsTrustedJwtConfiguration wsTrustedJwtConfiguration = (WsTrustedJwtConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#wsTrustedJwtConfigId",
              TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
          return;
        }
        
        wsTrustedJwtConfiguration.setConfigId(configId);
        wsTrustedJwtConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiWsTrustedJwtConfiguration guiWsTrustedJwtConfig = GuiWsTrustedJwtConfiguration.convertFromWsTrustedJwtConfiguration(wsTrustedJwtConfiguration);
        authenticationContainer.setGuiWsTrustedJwtConfiguration(guiWsTrustedJwtConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/authentication/wsTrustedJwtConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * insert a new gsh template in db
   * @param request
   * @param response
   */
  public void addWsTrustedJwtSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      String configId = request.getParameter("wsTrustedJwtConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#wsTrustedJwtConfigId",
            TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
        return;
      }
      
      WsTrustedJwtConfiguration wsTrustedJwtConfiguration = new WsTrustedJwtConfiguration();
      
      wsTrustedJwtConfiguration.setConfigId(configId);
      wsTrustedJwtConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      wsTrustedJwtConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AuthenticationConfig.viewWsTrustedJwts')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit jwt config page
   * @param request
   * @param response
   */
  public void editWsTrustedJwtConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      AuthenticationContainer authenticationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getAuthenticationContainer();
      
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      String configId = request.getParameter("wsTrustedJwtConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#wsTrustedJwtConfigId",
            TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
        return;
      }
      
      WsTrustedJwtConfiguration wsTrustedJwtConfiguration = new WsTrustedJwtConfiguration();
      
      wsTrustedJwtConfiguration.setConfigId(configId);
      
      String previousWsTrustedJwtConfigId = request.getParameter("previousWsTrustedJwtConfigId");
      
      if (StringUtils.isBlank(previousWsTrustedJwtConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiWsTrustedJwtConfiguration guiWsTrustedJwtConfiguration = GuiWsTrustedJwtConfiguration.convertFromWsTrustedJwtConfiguration(wsTrustedJwtConfiguration);
        authenticationContainer.setGuiWsTrustedJwtConfiguration(guiWsTrustedJwtConfiguration);
      } else {
        // change was made on the form
        wsTrustedJwtConfiguration.populateConfigurationValuesFromUi(request);
        GuiWsTrustedJwtConfiguration guiWsTrustedJwtConfiguration = GuiWsTrustedJwtConfiguration.convertFromWsTrustedJwtConfiguration(wsTrustedJwtConfiguration);
        authenticationContainer.setGuiWsTrustedJwtConfiguration(guiWsTrustedJwtConfiguration);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/authentication/editWsTrustedJwtConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited ws trusted jwt config into db
   * @param request
   * @param response
   */
  public void editWsTrustedJwtConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      String configId = request.getParameter("wsTrustedJwtConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#wsTrustedJwtConfigId",
            TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
        return;
      }
      
      WsTrustedJwtConfiguration wsTrustedJwtConfiguration = new WsTrustedJwtConfiguration();
      
      wsTrustedJwtConfiguration.setConfigId(configId);  
      
      wsTrustedJwtConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      wsTrustedJwtConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AuthenticationConfig.viewWsTrustedJwts')"));
      
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
  public void deleteWsTrustedJwtConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      String configId = request.getParameter("wsTrustedJwtConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#wsTrustedJwtConfigId",
            TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtErrorConfigIdRequired")));
        return;
      }
      
      WsTrustedJwtConfiguration wsTrustedJwtConfiguration = new WsTrustedJwtConfiguration();
      
      wsTrustedJwtConfiguration.setConfigId(configId);  
      
      wsTrustedJwtConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AuthenticationConfig.viewWsTrustedJwts')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * disable ws trusted jwt config
   * @param request
   * @param response
   */
  public void disableWsTrustedJwtConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      String configId = request.getParameter("wsTrustedJwtConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("wsTrustedJwtConfigId cannot be blank");
      }
      
      WsTrustedJwtConfiguration wsTrustedJwtConfiguration = new WsTrustedJwtConfiguration();
      
      wsTrustedJwtConfiguration.setConfigId(configId);  
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      wsTrustedJwtConfiguration.changeStatus(false, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AuthenticationConfig.viewWsTrustedJwts')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * enable ws trusted jwt config
   * @param request
   * @param response
   */
  public void enableWsTrustedJwtConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      if (!allowedToViewAuthentication()) {
        return;
      }
      
      String configId = request.getParameter("wsTrustedJwtConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("wsTrustedJwtConfigId cannot be blank");
      }
      
      WsTrustedJwtConfiguration wsTrustedJwtConfiguration = new WsTrustedJwtConfiguration();
      
      wsTrustedJwtConfiguration.setConfigId(configId);  
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      wsTrustedJwtConfiguration.changeStatus(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2AuthenticationConfig.viewWsTrustedJwts')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("wsTrustedJwtConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
