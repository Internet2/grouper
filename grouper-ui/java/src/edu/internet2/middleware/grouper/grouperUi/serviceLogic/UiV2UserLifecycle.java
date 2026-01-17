package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiUserLifecycleActionConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiUserLifecycleEventConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiUserLifecyclePolicyConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiUserLifecyclePolicyPartConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.UserLifecycleContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleActionConfiguration;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleEventConfiguration;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecyclePolicyConfiguration;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecyclePolicyPartConfiguration;
import edu.internet2.middleware.grouper.userLifecycle.UserLifecycleService;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2UserLifecycle {
  
  /**
   * view users lifecycle summary
   * @param request
   * @param response
   */
  public void viewUserLifecycleSummary(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      //TODO verify with Chris
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      int lifecycleEventNumberOfConfigs = UserLifecycleService.retrieveUserLifecycleEventNumberOfConfigs();
      userLifecycleContainer.setUserLifecycleEventCount(lifecycleEventNumberOfConfigs);
      
      int lifecycleActionNumberOfConfigs = UserLifecycleService.retrieveUserLifecycleActionNumberOfConfigs();
      userLifecycleContainer.setUserLifecycleActionCount(lifecycleActionNumberOfConfigs);
      
      int lifecyclePolicyNumberOfConfigs = UserLifecycleService.retrieveUserLifecyclePolicyNumberOfConfigs();
      userLifecycleContainer.setUserLifecyclePolicyCount(lifecyclePolicyNumberOfConfigs);
      
      int lifecyclePolicyPartNumberOfConfigs = UserLifecycleService.retrieveUserLifecyclePolicyPartNumberOfConfigs();
      userLifecycleContainer.setUserLifecyclePolicyPartCount(lifecyclePolicyPartNumberOfConfigs);

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/userLifecycleSummary.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void viewUserLifecycleEvents(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<UserLifecycleEventConfiguration> userLifecycleEventConfigs = UserLifecycleEventConfiguration.retrieveAllUserLifecycleEventsConfigurations();
      
      List<GuiUserLifecycleEventConfiguration> guiConfigurations = GuiUserLifecycleEventConfiguration.convertFromUserLifecycleEventConfiguration(userLifecycleEventConfigs);
      
      userLifecycleContainer.setGuiUserLifecycleEventConfigurations(guiConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/userLifecycleEvents.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void addUserLifecycleEventConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("userLifecycleEventConfigId");
      
      String type = request.getParameter("userLifecycleEventType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<UserLifecycleEventConfiguration> klass = (Class<UserLifecycleEventConfiguration>) GrouperUtil.forName(type);
        UserLifecycleEventConfiguration eventConfiguration = (UserLifecycleEventConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#userLifecycleEventConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
          return;
        }
        
        eventConfiguration.setConfigId(configId);
        eventConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiUserLifecycleEventConfiguration guiUserLifecycleEventConfig = GuiUserLifecycleEventConfiguration.convertFromUserLifecycleEventConfiguration(eventConfiguration);
        userLifecycleContainer.setGuiUserLifecycleEventConfiguration(guiUserLifecycleEventConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/userLifecycle/userLifecycleEventConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void addUserLifecycleEventConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("userLifecycleEventConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecycleEventConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecycleEventConfiguration userLifecycleEventConfiguration = new UserLifecycleEventConfiguration();
      
      userLifecycleEventConfiguration.setConfigId(configId);
      userLifecycleEventConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      userLifecycleEventConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecycleEvents')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecycleEventConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit user lifecycle event config screen
   * @param request
   * @param response
   */
  public void editUserLifecycleEventConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecycleEventConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecycleEventConfiguration userLifecycleEventConfiguration = new UserLifecycleEventConfiguration();
      
      userLifecycleEventConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiUserLifecycleEventConfiguration guiUserLifecycleEventConfig = GuiUserLifecycleEventConfiguration.convertFromUserLifecycleEventConfiguration(userLifecycleEventConfiguration);
        userLifecycleContainer.setGuiUserLifecycleEventConfiguration(guiUserLifecycleEventConfig);
      } else {
        // change was made on the form
        userLifecycleEventConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiUserLifecycleEventConfiguration guiUserLifecycleEventConfig = GuiUserLifecycleEventConfiguration.convertFromUserLifecycleEventConfiguration(userLifecycleEventConfiguration);
        userLifecycleContainer.setGuiUserLifecycleEventConfiguration(guiUserLifecycleEventConfig);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/editUserLifecycleEventConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited user lifecycle config into db
   * @param request
   * @param response
   */
  public void editUserLifecycleEventConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecycleEventConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecycleEventConfiguration userLifecycleEventConfiguration = new UserLifecycleEventConfiguration();
      
      userLifecycleEventConfiguration.setConfigId(configId);
      userLifecycleEventConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      userLifecycleEventConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecycleEvents')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecycleEventConfigAddEditSuccess")));
   
      if (actionsPerformed.size() > 0) {
        for (String actionPerformed: actionsPerformed) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, actionPerformed));
        }
      }      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete data field config
   * @param request
   * @param response
   */
  public void deleteUserLifecycleEventConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecycleEventConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecycleEventConfiguration userLifecycleEventConfiguration = new UserLifecycleEventConfiguration();
      
      userLifecycleEventConfiguration.setConfigId(configId);
      
      userLifecycleEventConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecycleEvents')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecycleEventConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void viewUserLifecycleActions(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<UserLifecycleActionConfiguration> userLifecycleConfigs = UserLifecycleActionConfiguration.retrieveAllUserLifecycleActionConfigurations();
      
      List<GuiUserLifecycleActionConfiguration> guiConfigurations = GuiUserLifecycleActionConfiguration.convertFromUserLifecycleActionConfiguration(userLifecycleConfigs);
      
      userLifecycleContainer.setGuiUserLifecycleActionConfigurations(guiConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/userLifecycleActions.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void addUserLifecycleActionConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("userLifecycleActionConfigId");
      
      String type = request.getParameter("userLifecycleActionType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<UserLifecycleActionConfiguration> klass = (Class<UserLifecycleActionConfiguration>) GrouperUtil.forName(type);
        UserLifecycleActionConfiguration actionConfiguration = (UserLifecycleActionConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#userLifecycleActionConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
          return;
        }
        
        actionConfiguration.setConfigId(configId);
        actionConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiUserLifecycleActionConfiguration guiUserLifecycleActionConfig = GuiUserLifecycleActionConfiguration.convertFromUserLifecycleActionConfiguration(actionConfiguration);
        userLifecycleContainer.setGuiUserLifecycleActionConfiguration(guiUserLifecycleActionConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/userLifecycle/userLifecycleActionConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void addUserLifecycleActionConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("userLifecycleActionConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecycleActionConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecycleActionConfiguration userLifecycleActionConfiguration = new UserLifecycleActionConfiguration();
      
      userLifecycleActionConfiguration.setConfigId(configId);
      userLifecycleActionConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      userLifecycleActionConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecycleActions')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecycleActionConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit user lifecycle action config screen
   * @param request
   * @param response
   */
  public void editUserLifecycleActionConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecycleActionConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecycleActionConfiguration userLifecycleActionConfiguration = new UserLifecycleActionConfiguration();
      
      userLifecycleActionConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiUserLifecycleActionConfiguration guiUserLifecycleActionConfig = GuiUserLifecycleActionConfiguration.convertFromUserLifecycleActionConfiguration(userLifecycleActionConfiguration);
        userLifecycleContainer.setGuiUserLifecycleActionConfiguration(guiUserLifecycleActionConfig);
      } else {
        // change was made on the form
        userLifecycleActionConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiUserLifecycleActionConfiguration guiUserLifecycleActionConfig = GuiUserLifecycleActionConfiguration.convertFromUserLifecycleActionConfiguration(userLifecycleActionConfiguration);
        userLifecycleContainer.setGuiUserLifecycleActionConfiguration(guiUserLifecycleActionConfig);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/editUserLifecycleActionConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited user lifecycle config into db
   * @param request
   * @param response
   */
  public void editUserLifecycleActionConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecycleActionConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecycleActionConfiguration userLifecycleActionConfiguration = new UserLifecycleActionConfiguration();
      
      userLifecycleActionConfiguration.setConfigId(configId);
      userLifecycleActionConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      userLifecycleActionConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecycleActions')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecycleActionConfigAddEditSuccess")));
   
      if (actionsPerformed.size() > 0) {
        for (String actionPerformed: actionsPerformed) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, actionPerformed));
        }
      }      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete data field config
   * @param request
   * @param response
   */
  public void deleteUserLifecycleActionConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecycleActionConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecycleActionConfiguration userLifecycleActionConfiguration = new UserLifecycleActionConfiguration();
      
      userLifecycleActionConfiguration.setConfigId(configId);
      
      userLifecycleActionConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecycleActions')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecycleActionConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void viewUserLifecyclePolicies(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<UserLifecyclePolicyConfiguration> userLifecycleConfigs = UserLifecyclePolicyConfiguration.retrieveAllUserLifecyclePolicyConfigurations();
      
      List<GuiUserLifecyclePolicyConfiguration> guiConfigurations = GuiUserLifecyclePolicyConfiguration.convertFromUserLifecyclePolicyConfiguration(userLifecycleConfigs);
      
      userLifecycleContainer.setGuiUserLifecyclePolicyConfigurations(guiConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/userLifecyclePolicies.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void addUserLifecyclePolicyConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("userLifecyclePolicyConfigId");
      
      String type = request.getParameter("userLifecyclePolicyType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<UserLifecyclePolicyConfiguration> klass = (Class<UserLifecyclePolicyConfiguration>) GrouperUtil.forName(type);
        UserLifecyclePolicyConfiguration policyConfiguration = (UserLifecyclePolicyConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#userLifecyclePolicyConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
          return;
        }
        
        policyConfiguration.setConfigId(configId);
        policyConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiUserLifecyclePolicyConfiguration guiUserLifecyclePolicyConfig = GuiUserLifecyclePolicyConfiguration.convertFromUserLifecyclePolicyConfiguration(policyConfiguration);
        userLifecycleContainer.setGuiUserLifecyclePolicyConfiguration(guiUserLifecyclePolicyConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/userLifecycle/userLifecyclePolicyConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void addUserLifecyclePolicyConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("userLifecyclePolicyConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecyclePolicyConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration = new UserLifecyclePolicyConfiguration();
      
      userLifecyclePolicyConfiguration.setConfigId(configId);
      userLifecyclePolicyConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      userLifecyclePolicyConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecyclePolicies')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecyclePolicyConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit user lifecycle policy config screen
   * @param request
   * @param response
   */
  public void editUserLifecyclePolicyConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecyclePolicyConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration = new UserLifecyclePolicyConfiguration();
      
      userLifecyclePolicyConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiUserLifecyclePolicyConfiguration guiUserLifecyclePolicyConfig = GuiUserLifecyclePolicyConfiguration.convertFromUserLifecyclePolicyConfiguration(userLifecyclePolicyConfiguration);
        userLifecycleContainer.setGuiUserLifecyclePolicyConfiguration(guiUserLifecyclePolicyConfig);
      } else {
        // change was made on the form
        userLifecyclePolicyConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiUserLifecyclePolicyConfiguration guiUserLifecyclePolicyConfig = GuiUserLifecyclePolicyConfiguration.convertFromUserLifecyclePolicyConfiguration(userLifecyclePolicyConfiguration);
        userLifecycleContainer.setGuiUserLifecyclePolicyConfiguration(guiUserLifecyclePolicyConfig);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/editUserLifecyclePolicyConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited user lifecycle config into db
   * @param request
   * @param response
   */
  public void editUserLifecyclePolicyConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecyclePolicyConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration = new UserLifecyclePolicyConfiguration();
      
      userLifecyclePolicyConfiguration.setConfigId(configId);
      userLifecyclePolicyConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      userLifecyclePolicyConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecyclePolicies')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecyclePolicyConfigAddEditSuccess")));
   
      if (actionsPerformed.size() > 0) {
        for (String actionPerformed: actionsPerformed) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, actionPerformed));
        }
      }      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete data field config
   * @param request
   * @param response
   */
  public void deleteUserLifecyclePolicyConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecyclePolicyConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecyclePolicyConfiguration userLifecyclePolicyConfiguration = new UserLifecyclePolicyConfiguration();
      
      userLifecyclePolicyConfiguration.setConfigId(configId);
      
      userLifecyclePolicyConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecyclePolicies')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecyclePolicyConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  public void viewUserLifecyclePolicyParts(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<UserLifecyclePolicyPartConfiguration> userLifecycleConfigs = UserLifecyclePolicyPartConfiguration.retrieveAllUserLifecyclePolicyPartConfigurations();
      
      List<GuiUserLifecyclePolicyPartConfiguration> guiConfigurations = GuiUserLifecyclePolicyPartConfiguration.convertFromUserLifecyclePolicyPartConfiguration(userLifecycleConfigs);
      
      userLifecycleContainer.setGuiUserLifecyclePolicyPartConfigurations(guiConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/userLifecyclePolicyParts.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void addUserLifecyclePolicyPartConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("userLifecyclePolicyPartConfigId");
      
      String type = request.getParameter("userLifecyclePolicyPartType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<UserLifecyclePolicyPartConfiguration> klass = (Class<UserLifecyclePolicyPartConfiguration>) GrouperUtil.forName(type);
        UserLifecyclePolicyPartConfiguration policyConfiguration = (UserLifecyclePolicyPartConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#userLifecyclePolicyPartConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
          return;
        }
        
        policyConfiguration.setConfigId(configId);
        policyConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiUserLifecyclePolicyPartConfiguration guiUserLifecyclePolicyPartConfig = GuiUserLifecyclePolicyPartConfiguration.convertFromUserLifecyclePolicyPartConfiguration(policyConfiguration);
        userLifecycleContainer.setGuiUserLifecyclePolicyPartConfiguration(guiUserLifecyclePolicyPartConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/userLifecycle/userLifecyclePolicyPartConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  public void addUserLifecyclePolicyPartConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("userLifecyclePolicyPartConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecyclePolicyPartConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecyclePolicyPartConfiguration userLifecyclePolicyPartConfiguration = new UserLifecyclePolicyPartConfiguration();
      
      userLifecyclePolicyPartConfiguration.setConfigId(configId);
      userLifecyclePolicyPartConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      userLifecyclePolicyPartConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecyclePolicyParts')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecyclePolicyConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit user lifecycle policy part config screen
   * @param request
   * @param response
   */
  public void editUserLifecyclePolicyPartConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecyclePolicyPartConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecyclePolicyPartConfiguration userLifecyclePolicyPartConfiguration = new UserLifecyclePolicyPartConfiguration();
      
      userLifecyclePolicyPartConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiUserLifecyclePolicyPartConfiguration guiUserLifecyclePolicyConfig = GuiUserLifecyclePolicyPartConfiguration.convertFromUserLifecyclePolicyPartConfiguration(userLifecyclePolicyPartConfiguration);
        userLifecycleContainer.setGuiUserLifecyclePolicyPartConfiguration(guiUserLifecyclePolicyConfig);
      } else {
        // change was made on the form
        userLifecyclePolicyPartConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiUserLifecyclePolicyPartConfiguration guiUserLifecyclePolicyConfig = GuiUserLifecyclePolicyPartConfiguration.convertFromUserLifecyclePolicyPartConfiguration(userLifecyclePolicyPartConfiguration);
        userLifecycleContainer.setGuiUserLifecyclePolicyPartConfiguration(guiUserLifecyclePolicyConfig);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/userLifecycle/editUserLifecyclePolicyPartConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited user lifecycle policy part config into db
   * @param request
   * @param response
   */
  public void editUserLifecyclePolicyPartConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecyclePolicyPartConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecyclePolicyPartConfiguration userLifecyclePolicyPartConfiguration = new UserLifecyclePolicyPartConfiguration();
      
      userLifecyclePolicyPartConfiguration.setConfigId(configId);
      userLifecyclePolicyPartConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      userLifecyclePolicyPartConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecyclePolicyParts')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecyclePolicyConfigAddEditSuccess")));
   
      if (actionsPerformed.size() > 0) {
        for (String actionPerformed: actionsPerformed) {
          guiResponseJs.addAction(GuiScreenAction.newMessageAppend(GuiMessageType.success, actionPerformed));
        }
      }      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete policy part config
   * @param request
   * @param response
   */
  public void deleteUserLifecyclePolicyPartConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final UserLifecycleContainer userLifecycleContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getUserLifecycleContainer();
      
      if (!userLifecycleContainer.isCanOperateOnUserLifecycleConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("configId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#userLifecyclePolicyPartConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      UserLifecyclePolicyPartConfiguration userLifecyclePolicyPartConfiguration = new UserLifecyclePolicyPartConfiguration();
      
      userLifecyclePolicyPartConfiguration.setConfigId(configId);
      
      userLifecyclePolicyPartConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2UserLifecycle.viewUserLifecyclePolicyParts')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("userLifecyclePolicyConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
