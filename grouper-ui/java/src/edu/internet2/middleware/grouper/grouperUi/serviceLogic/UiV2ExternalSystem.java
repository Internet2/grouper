package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ExternalSystemContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGrouperExternalSystem;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.exception.ExceptionUtils;
import edu.internet2.middleware.subject.Subject;

public class UiV2ExternalSystem {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2ExternalSystem.class);
  
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
      } else {
        // change was made on the form
        grouperExternalSystem.populateConfigurationValuesFromUi(request);
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
      
      grouperExternalSystem.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      grouperExternalSystem.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ExternalSystem.viewExternalSystems')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConfigAddEditSuccess")));
      
      try {
        grouperExternalSystem.refreshConnectionsIfNeeded();
      } catch (UnsupportedOperationException e) {
        // ok
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * show form to add a new grouper external system
   * @param request
   * @param response
   */
  public void addExternalSystem(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      ExternalSystemContainer externalSystemContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getExternalSystemContainer();
      
      if (!externalSystemContainer.isCanViewExternalSystems()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String externalSystemConfigId = request.getParameter("externalSystemConfigId");
      
      String externalSystemType = request.getParameter("externalSystemType");
      
      if (StringUtils.isNotBlank(externalSystemType)) {
        
        if (!GrouperExternalSystem.externalTypeClassNames.contains(externalSystemType)) {            
          throw new RuntimeException("Invalid externalSystemType "+externalSystemType);
        }

        Class<GrouperExternalSystem> klass = (Class<GrouperExternalSystem>) GrouperUtil.forName(externalSystemType);
        GrouperExternalSystem grouperExternalSystem = (GrouperExternalSystem) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(externalSystemConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#externalSystemConfigId",
              TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemCreateErrorConfigIdRequired")));
          return;
        }
        
        if (grouperExternalSystem instanceof DatabaseGrouperExternalSystem && StringUtils.equals(externalSystemConfigId, "grouper")) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#externalSystemConfigId",
              TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemCreateErrorConfigIdGrouperCanNotBeUsed")));
          return;
        }
        
        grouperExternalSystem.setConfigId(externalSystemConfigId);
        grouperExternalSystem.populateConfigurationValuesFromUi(request);
        
        GuiGrouperExternalSystem guiGrouperExternalSystem = GuiGrouperExternalSystem.convertFromGrouperExternalSystem(grouperExternalSystem);
        externalSystemContainer.setGuiGrouperExternalSystem(guiGrouperExternalSystem);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/externalSystems/externalSystemAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * insert a new external system config
   * @param request
   * @param response
   */
  public void addExternalSystemSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      grouperExternalSystem.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      grouperExternalSystem.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);

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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ExternalSystem.viewExternalSystems')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConfigAddEditSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * delete external system config
   * @param request
   * @param response
   */
  public void deleteExternalSystemConfigDetails(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      grouperExternalSystem.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ExternalSystem.viewExternalSystems')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * view external system details
   * @param request
   * @param response
   */
  public void viewExternalSystemConfigDetails(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      GuiGrouperExternalSystem guiGrouperExternalSystem = GuiGrouperExternalSystem.convertFromGrouperExternalSystem(grouperExternalSystem);
      externalSystemContainer.setGuiGrouperExternalSystem(guiGrouperExternalSystem);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/externalSystems/viewExternalSystemConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * test connection between external system and grouper
   * @param request
   * @param response
   */
  public void testExternalSystemConfigDetails(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      List<String> errors = new ArrayList<String>();
      
      try {        
        errors = grouperExternalSystem.test();
      } catch (UnsupportedOperationException e) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
            TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConnectionNotSupported")));
        return;
      } catch (Exception e) {
        LOG.error("error testing external system: '" + externalSystemConfigId + "'", e);
        String stackTrace = ExceptionUtils.getStackTrace(e);
        String error = TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConnectionTestException");
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, error + "<pre>" + stackTrace + "</pre>"));
        return;
      }
      
      if (errors != null && errors.size() > 0) {
        for (String error: errors) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, error));
        }
        return;
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
            TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConnectionTestSuccess")));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * disable external system
   * @param request
   * @param response
   */
  public void disableExternalSystem(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      grouperExternalSystem.changeStatus(false, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ExternalSystem.viewExternalSystems')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * enable external system
   * @param request
   * @param response
   */
  public void enableExternalSystem(final HttpServletRequest request, final HttpServletResponse response) {
    
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
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      grouperExternalSystem.changeStatus(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2ExternalSystem.viewExternalSystems')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("grouperExternalSystemConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
}
