package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.sqlSync.SqlSyncConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiSqlSyncConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.SqlSyncConfigurationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2SqlSyncConfiguration {
  
  public void viewSqlSyncConfigurations(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SqlSyncConfigurationContainer sqlSyncConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSqlSyncConfigurationContainer();
      
      if (!sqlSyncConfigurationContainer.isCanViewSqlSyncConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<SqlSyncConfiguration> sqlSyncConfigurations = SqlSyncConfiguration.retrieveAllSqlSyncConfigurations();
      
      List<GuiSqlSyncConfiguration> guiSqlSyncConfigurations = GuiSqlSyncConfiguration.convertFromSqlSyncConfiguration(sqlSyncConfigurations);
      
      sqlSyncConfigurationContainer.setGuiSqlSyncConfigurations(guiSqlSyncConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/sqlSync/sqlSyncConfigurations.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void addSqlSyncConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SqlSyncConfigurationContainer sqlSyncConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSqlSyncConfigurationContainer();
      
      if (!sqlSyncConfigurationContainer.isCanViewSqlSyncConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("sqlSyncConfigId");
      
      String type = request.getParameter("sqlSyncType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<SqlSyncConfiguration> klass = (Class<SqlSyncConfiguration>) GrouperUtil.forName(type);
        SqlSyncConfiguration sqlSyncConfiguration = (SqlSyncConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#sqlSyncConfigId",
              TextContainer.retrieveFromRequest().getText().get("sqlSyncCreateErrorConfigIdRequired")));
          return;
        }
        
        sqlSyncConfiguration.setConfigId(configId);
        sqlSyncConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiSqlSyncConfiguration guiSqlSyncConfig = GuiSqlSyncConfiguration.convertFromSqlSyncConfiguration(sqlSyncConfiguration);
        sqlSyncConfigurationContainer.setGuiSqlSyncConfiguration(guiSqlSyncConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/sqlSync/sqlSyncConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * insert a new config in db
   * @param request
   * @param response
   */
  public void addSqlSyncConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SqlSyncConfigurationContainer sqlSyncConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSqlSyncConfigurationContainer();
      
      if (!sqlSyncConfigurationContainer.isCanViewSqlSyncConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("sqlSyncConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#sqlSyncConfigId",
            TextContainer.retrieveFromRequest().getText().get("sqlSyncCreateErrorConfigIdRequired")));
        return;
      }
      
      SqlSyncConfiguration sqlSyncConfiguration = new SqlSyncConfiguration();
      
      sqlSyncConfiguration.setConfigId(configId);
      sqlSyncConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      sqlSyncConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("sqlSyncConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit sql sync config screen
   * @param request
   * @param response
   */
  public void editSqlSyncConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SqlSyncConfigurationContainer sqlSyncConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSqlSyncConfigurationContainer();
      
      if (!sqlSyncConfigurationContainer.isCanViewSqlSyncConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("sqlSyncConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#sqlSyncConfigId",
            TextContainer.retrieveFromRequest().getText().get("sqlSyncCreateErrorConfigIdRequired")));
        return;
      }
      
      SqlSyncConfiguration sqlSyncConfiguration = new SqlSyncConfiguration();
      
      sqlSyncConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousSqlSyncConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiSqlSyncConfiguration guiSqlSyncConfiguration = GuiSqlSyncConfiguration.convertFromSqlSyncConfiguration(sqlSyncConfiguration);
        sqlSyncConfigurationContainer.setGuiSqlSyncConfiguration(guiSqlSyncConfiguration);
      } else {
        // change was made on the form
        sqlSyncConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiSqlSyncConfiguration guiSqlSyncConfiguration = GuiSqlSyncConfiguration.convertFromSqlSyncConfiguration(sqlSyncConfiguration);
        sqlSyncConfigurationContainer.setGuiSqlSyncConfiguration(guiSqlSyncConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/sqlSync/editSqlSyncConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited sql sync config into db
   * @param request
   * @param response
   */
  public void editSqlSyncConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SqlSyncConfigurationContainer sqlSyncConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSqlSyncConfigurationContainer();
      
      if (!sqlSyncConfigurationContainer.isCanViewSqlSyncConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("sqlSyncConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#sqlSyncConfigId",
            TextContainer.retrieveFromRequest().getText().get("sqlSyncCreateErrorConfigIdRequired")));
        return;
      }
      
      SqlSyncConfiguration sqlSyncConfiguration = new SqlSyncConfiguration();
      
      sqlSyncConfiguration.setConfigId(configId);
      sqlSyncConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      sqlSyncConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("sqlSyncConfigAddEditSuccess")));
   
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete sql sync config
   * @param request
   * @param response
   */
  public void deleteSqlSyncConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SqlSyncConfigurationContainer sqlSyncConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSqlSyncConfigurationContainer();
      
      if (!sqlSyncConfigurationContainer.isCanViewSqlSyncConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("sqlSyncConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      SqlSyncConfiguration sqlSyncConfiguration = new SqlSyncConfiguration();
      
      sqlSyncConfiguration.setConfigId(configId);
      
      sqlSyncConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("sqlSyncConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * disable sql sync config
   * @param request
   * @param response
   */
  public void disableSqlSyncConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SqlSyncConfigurationContainer sqlSyncConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSqlSyncConfigurationContainer();
      
      if (!sqlSyncConfigurationContainer.isCanViewSqlSyncConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("sqlSyncConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      SqlSyncConfiguration sqlSyncConfiguration = new SqlSyncConfiguration();
      
      sqlSyncConfiguration.setConfigId(configId);
      
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      sqlSyncConfiguration.changeStatus(false, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("sqlSyncConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * enable sql sync config
   * @param request
   * @param response
   */
  public void enableSqlSyncConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      SqlSyncConfigurationContainer sqlSyncConfigurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getSqlSyncConfigurationContainer();
      
      if (!sqlSyncConfigurationContainer.isCanViewSqlSyncConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("sqlSyncConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      SqlSyncConfiguration sqlSyncConfiguration = new SqlSyncConfiguration();
      
      sqlSyncConfiguration.setConfigId(configId);
      
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      sqlSyncConfiguration.changeStatus(true, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2SqlSyncConfiguration.viewSqlSyncConfigurations')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("sqlSyncConfigChangeStatusSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}


