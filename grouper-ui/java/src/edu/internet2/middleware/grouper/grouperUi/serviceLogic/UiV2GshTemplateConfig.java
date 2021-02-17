package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GshTemplateContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiGshTemplateConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2GshTemplateConfig {
  
  /**
   * view all configured gsh templates
   * @param request
   * @param response
   */
  public void viewGshTemplates(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      if (!gshTemplateContainer.isCanViewGshTemplates()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GshTemplateConfiguration> gshTemplateConfigs = GshTemplateConfiguration.retrieveAllGshTemplateConfigs();
      
      List<GuiGshTemplateConfiguration> guiGshTemplateConfigs = GuiGshTemplateConfiguration.convertFromGshTemplateConfiguration(gshTemplateConfigs);
      
      gshTemplateContainer.setGuiGshTemplateConfigurations(guiGshTemplateConfigs);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/gshTemplate/gshTemplateConfigs.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    } 
    
  }
  
  /**
   * show form to add a new gsh template
   * @param request
   * @param response
   */
  public void addGshTemplate(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      if (!gshTemplateContainer.isCanViewGshTemplates()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String gshTemplateConfigId = request.getParameter("gshTemplateConfigId");
      
      String gshTemplateType = request.getParameter("gshTemplateType");
      
      if (StringUtils.isNotBlank(gshTemplateType)) {
        
        Class<GshTemplateConfiguration> klass = (Class<GshTemplateConfiguration>) GrouperUtil.forName(gshTemplateType);
        GshTemplateConfiguration gshTemplateConfiguration = (GshTemplateConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(gshTemplateConfigId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#gshTemplateConfigId",
              TextContainer.retrieveFromRequest().getText().get("gshTemplateCreateErrorConfigIdRequired")));
          return;
        }
        
        gshTemplateConfiguration.setConfigId(gshTemplateConfigId);
        gshTemplateConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiGshTemplateConfiguration guiGshTemplateConfig = GuiGshTemplateConfiguration.convertFromGshTemplateConfiguration(gshTemplateConfiguration);
        gshTemplateContainer.setGuiGshTemplateConfiguration(guiGshTemplateConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/gshTemplate/gshTemplateConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  /**
   * insert a new gsh template in db
   * @param request
   * @param response
   */
  public void addGshTemplateSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      if (!gshTemplateContainer.isCanViewGshTemplates()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String gshTemplateConfigId = request.getParameter("gshTemplateConfigId");
      
      if (StringUtils.isBlank(gshTemplateConfigId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#gshTemplateConfigId",
            TextContainer.retrieveFromRequest().getText().get("gshTemplateCreateErrorConfigIdRequired")));
        return;
      }
      
      GshTemplateConfiguration gshTemplateConfiguration = new GshTemplateConfiguration();
      
      gshTemplateConfiguration.setConfigId(gshTemplateConfigId);
      gshTemplateConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      gshTemplateConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GshTemplateConfig.viewGshTemplates')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("gshTemplateConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit gsh template
   * @param request
   * @param response
   */
  public void editGshTemplate(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      if (!gshTemplateContainer.isCanViewGshTemplates()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String gshTemplateConfigId = request.getParameter("gshTemplateConfigId");
      
      if (StringUtils.isBlank(gshTemplateConfigId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#gshTemplateConfigId",
            TextContainer.retrieveFromRequest().getText().get("gshTemplateCreateErrorConfigIdRequired")));
        return;
      }
      
      GshTemplateConfiguration gshTemplateConfiguration = new GshTemplateConfiguration();
      
      gshTemplateConfiguration.setConfigId(gshTemplateConfigId);
      
      String previousGshTemplateConfigId = request.getParameter("previousGshTemplateConfigId");
      
      if (StringUtils.isBlank(previousGshTemplateConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiGshTemplateConfiguration guiGshTemplateConfig = GuiGshTemplateConfiguration.convertFromGshTemplateConfiguration(gshTemplateConfiguration);
        gshTemplateContainer.setGuiGshTemplateConfiguration(guiGshTemplateConfig);
      } else {
        // change was made on the form
        gshTemplateConfiguration.populateConfigurationValuesFromUi(request);
        GuiGshTemplateConfiguration guiGshTemplateConfig = GuiGshTemplateConfiguration.convertFromGshTemplateConfiguration(gshTemplateConfiguration);
        gshTemplateContainer.setGuiGshTemplateConfiguration(guiGshTemplateConfig);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/gshTemplate/editGshTemplateConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited gsh template into db
   * @param request
   * @param response
   */
  public void editGshTemplateSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      if (!gshTemplateContainer.isCanViewGshTemplates()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String gshTemplateConfigId = request.getParameter("gshTemplateConfigId");
      
      if (StringUtils.isBlank(gshTemplateConfigId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#gshTemplateConfigId",
            TextContainer.retrieveFromRequest().getText().get("gshTemplateCreateErrorConfigIdRequired")));
        return;
      }
      
      GshTemplateConfiguration gshTemplateConfiguration = new GshTemplateConfiguration();
      
      gshTemplateConfiguration.setConfigId(gshTemplateConfigId);
      
      gshTemplateConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      gshTemplateConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GshTemplateConfig.viewGshTemplates')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("gshTemplateConfigAddEditSuccess")));
   
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete gsh template
   * @param request
   * @param response
   */
  public void deleteGshTemplate(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      if (!gshTemplateContainer.isCanViewGshTemplates()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String gshTemplateConfigId = request.getParameter("gshTemplateConfigId");
      
      if (StringUtils.isBlank(gshTemplateConfigId)) {
        throw new RuntimeException("gshTemplateConfigId cannot be blank");
      }
      
      GshTemplateConfiguration gshTemplateConfiguration = new GshTemplateConfiguration();
      
      gshTemplateConfiguration.setConfigId(gshTemplateConfigId);
      
      gshTemplateConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GshTemplateConfig.viewGshTemplates')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("gshTemplateConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * disable gsh template
   * @param request
   * @param response
   */
  public void disableGshTemplate(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      if (!gshTemplateContainer.isCanViewGshTemplates()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String gshTemplateConfigId = request.getParameter("gshTemplateConfigId");
      
      if (StringUtils.isBlank(gshTemplateConfigId)) {
        throw new RuntimeException("gshTemplateConfigId cannot be blank");
      }
      
      GshTemplateConfiguration gshTemplateConfiguration = new GshTemplateConfiguration();
      
      gshTemplateConfiguration.setConfigId(gshTemplateConfigId);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      gshTemplateConfiguration.changeStatus(false, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GshTemplateConfig.viewGshTemplates')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("gshTemplateChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * enable gsh template
   * @param request
   * @param response
   */
  public void enableGshTemplate(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GshTemplateContainer gshTemplateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getGshTemplateContainer();
      
      if (!gshTemplateContainer.isCanViewGshTemplates()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String gshTemplateConfigId = request.getParameter("gshTemplateConfigId");
      
      if (StringUtils.isBlank(gshTemplateConfigId)) {
        throw new RuntimeException("gshTemplateConfigId cannot be blank");
      }
      
      GshTemplateConfiguration gshTemplateConfiguration = new GshTemplateConfiguration();
      
      gshTemplateConfiguration.setConfigId(gshTemplateConfigId);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      gshTemplateConfiguration.changeStatus(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2GshTemplateConfig.viewGshTemplates')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("gshTemplateChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  

}
