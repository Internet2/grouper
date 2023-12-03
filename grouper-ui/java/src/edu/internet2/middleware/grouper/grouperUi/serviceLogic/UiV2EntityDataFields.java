package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.EntityDataFieldsService;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderChangeLogQueryConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfiguration;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfig;
import edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealmConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.EntityDataFieldsContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiDataFieldConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiDataFieldRowDictionary;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiDataFieldRowDictionaryTable;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiDataProviderChangeLogQueryConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiDataProviderConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiDataProviderQueryConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiDataRowConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiPrivacyRealmConfiguration;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class UiV2EntityDataFields {
  
  /**
   * view entity data fields summary
   * @param request
   * @param response
   */
  public void viewEntityDataFieldsSummary(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      int dataFieldsNumberOfConfigs = EntityDataFieldsService.retrieveDataFieldsNumberOfConfigs();
      entityDataFieldsContainer.setDataFieldsNumberOfConfigs(dataFieldsNumberOfConfigs);
      
      int dataRowsNumberOfConfigs = EntityDataFieldsService.retrieveDataRowsNumberOfConfigs();
      entityDataFieldsContainer.setDataRowsNumberOfConfigs(dataRowsNumberOfConfigs);
      
      int dataProvidersNumberOfConfigs = EntityDataFieldsService.retrieveDataProvidersNumberOfConfigs();
      entityDataFieldsContainer.setDataProvidersNumberOfConfigs(dataProvidersNumberOfConfigs);

      int dataProviderQueriesNumberOfConfigs = EntityDataFieldsService.retrieveDataProviderQueriesNumberOfConfigs();
      entityDataFieldsContainer.setDataProviderQueriesNumberOfConfigs(dataProviderQueriesNumberOfConfigs);

      int dataProviderChangeLogQueriesNumberOfConfigs = EntityDataFieldsService.retrieveDataProviderChangeLogQueriesNumberOfConfigs();
      entityDataFieldsContainer.setDataProviderChangeLogQueriesNumberOfConfigs(dataProviderChangeLogQueriesNumberOfConfigs);
      
      int privacyRealmNumberOfConfigs = EntityDataFieldsService.retrievePrivacyRealmNumberOfConfigs();
      entityDataFieldsContainer.setPrivacyRealmNumberOfConfigs(privacyRealmNumberOfConfigs);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/entityDataFieldsSummary.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view data fields
   * @param request
   * @param response
   */
  public void viewEntityDataFields(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GrouperDataFieldConfiguration> dataFieldConfigurations = GrouperDataFieldConfiguration.retrieveAllDataFieldConfigurations();
      
      List<GuiDataFieldConfiguration> guiDataFieldConfigurations = GuiDataFieldConfiguration.convertFromDataFieldConfiguration(dataFieldConfigurations);
      
      entityDataFieldsContainer.setGuiDataFieldConfigurations(guiDataFieldConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/entityDataFields.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
//  public void viewPrivacyRealmConfigs(final HttpServletRequest request, final HttpServletResponse response) {
//
//    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
//    
//    GrouperSession grouperSession = null;
//  
//    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//    
//    try {
//  
//      grouperSession = GrouperSession.start(loggedInSubject);
//      
//      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
//      
//      List<GrouperDataField> grouperDataFields = EntityDataFieldsService.retrieveGrouperDataFields();
//      
//      entityDataFieldsContainer.setGrouperDataFields(grouperDataFields);
//      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
//          "/WEB-INF/grouperUi2/entityDataFields/privacyRealms.jsp"));
//      
//    } finally {
//      GrouperSession.stopQuietly(grouperSession);
//    }
//    
//  }
  
  public void viewPrivacyRealmConfigs(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GrouperPrivacyRealmConfiguration> privacyRealmConfigurations = GrouperPrivacyRealmConfiguration.retrieveAllPrivacyRealmConfigurations();
      
      List<GuiPrivacyRealmConfiguration> guiPrivacyRealmConfigurations = GuiPrivacyRealmConfiguration.convertFromPrivacyRealmConfiguration(privacyRealmConfigurations);
      
      entityDataFieldsContainer.setGuiPrivacyRealmConfigurations(guiPrivacyRealmConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/privacyRealms.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * show edit privacy realm config screen
   * @param request
   * @param response
   */
  public void editPrivacyRealmConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("privacyRealmConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#privacyRealmConfigId",
            TextContainer.retrieveFromRequest().getText().get("privacyRealmCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperPrivacyRealmConfiguration grouperPrivacyRealmConfiguration = new GrouperPrivacyRealmConfiguration();
      
      grouperPrivacyRealmConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousPrivacyRealmConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiPrivacyRealmConfiguration guiPrivacyRealmConfiguration = GuiPrivacyRealmConfiguration.convertFromPrivacyRealmConfiguration(grouperPrivacyRealmConfiguration);
        entityDataFieldsContainer.setGuiPrivacyRealmConfiguration(guiPrivacyRealmConfiguration);
      } else {
        // change was made on the form
        grouperPrivacyRealmConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiPrivacyRealmConfiguration guiPrivacyRealmConfiguration = GuiPrivacyRealmConfiguration.convertFromPrivacyRealmConfiguration(grouperPrivacyRealmConfiguration);
        entityDataFieldsContainer.setGuiPrivacyRealmConfiguration(guiPrivacyRealmConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/editPrivacyRealmConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * show edit data provider config screen
   * @param request
   * @param response
   */
  public void editDataProviderConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderConfiguration grouperDataProviderConfiguration = new GrouperDataProviderConfiguration();
      
      grouperDataProviderConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousDataProviderConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiDataProviderConfiguration guiDataProviderConfiguration = GuiDataProviderConfiguration.convertFromDataProviderConfiguration(grouperDataProviderConfiguration);
        entityDataFieldsContainer.setGuiDataProviderConfiguration(guiDataProviderConfiguration);
      } else {
        // change was made on the form
        grouperDataProviderConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataProviderConfiguration guiDataProviderConfiguration = GuiDataProviderConfiguration.convertFromDataProviderConfiguration(grouperDataProviderConfiguration);
        entityDataFieldsContainer.setGuiDataProviderConfiguration(guiDataProviderConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/editDataProviderConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit data provider query config screen
   * @param request
   * @param response
   */
  public void editDataProviderQueryConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderQueryConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderQueryConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderQueryCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderQueryConfiguration grouperDataProviderQueryConfiguration = new GrouperDataProviderQueryConfiguration();
      
      grouperDataProviderQueryConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousDataProviderQueryConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiDataProviderQueryConfiguration guiDataProviderQueryConfiguration = GuiDataProviderQueryConfiguration.convertFromDataProviderQueryConfiguration(grouperDataProviderQueryConfiguration);
        entityDataFieldsContainer.setGuiDataProviderQueryConfiguration(guiDataProviderQueryConfiguration);
      } else {
        // change was made on the form
        grouperDataProviderQueryConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataProviderQueryConfiguration guiDataProviderQueryConfiguration = GuiDataProviderQueryConfiguration.convertFromDataProviderQueryConfiguration(grouperDataProviderQueryConfiguration);
        entityDataFieldsContainer.setGuiDataProviderQueryConfiguration(guiDataProviderQueryConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/editDataProviderQueryConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit data provider change log query config screen
   * @param request
   * @param response
   */
  public void editDataProviderChangeLogQueryConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderChangeLogQueryConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderChangeLogQueryConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderChangeLogQueryCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderChangeLogQueryConfiguration grouperDataProviderChangeLogQueryConfiguration = new GrouperDataProviderChangeLogQueryConfiguration();
      
      grouperDataProviderChangeLogQueryConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousDataProviderChangeLogQueryConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiDataProviderChangeLogQueryConfiguration guiDataProviderChangeLogQueryConfiguration = GuiDataProviderChangeLogQueryConfiguration.convertFromDataProviderChangeLogQueryConfiguration(grouperDataProviderChangeLogQueryConfiguration);
        entityDataFieldsContainer.setGuiDataProviderChangeLogQueryConfiguration(guiDataProviderChangeLogQueryConfiguration);
      } else {
        // change was made on the form
        grouperDataProviderChangeLogQueryConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataProviderChangeLogQueryConfiguration guiDataProviderChangeLogQueryConfiguration = GuiDataProviderChangeLogQueryConfiguration.convertFromDataProviderChangeLogQueryConfiguration(grouperDataProviderChangeLogQueryConfiguration);
        entityDataFieldsContainer.setGuiDataProviderChangeLogQueryConfiguration(guiDataProviderChangeLogQueryConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/editDataProviderChangeLogQueryConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit data row config screen
   * @param request
   * @param response
   */
  public void editDataRowConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataRowConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataRowConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataRowCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataRowConfiguration grouperDataRowConfiguration = new GrouperDataRowConfiguration();
      
      grouperDataRowConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousDataRowConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiDataRowConfiguration guiDataRowConfiguration = GuiDataRowConfiguration.convertFromDataRowConfiguration(grouperDataRowConfiguration);
        entityDataFieldsContainer.setGuiDataRowConfiguration(guiDataRowConfiguration);
      } else {
        // change was made on the form
        grouperDataRowConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataRowConfiguration guiDataRowConfiguration = GuiDataRowConfiguration.convertFromDataRowConfiguration(grouperDataRowConfiguration);
        entityDataFieldsContainer.setGuiDataRowConfiguration(guiDataRowConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/editDataRowConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show edit data field config screen
   * @param request
   * @param response
   */
  public void editDataFieldConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataFieldConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataFieldConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataFieldConfiguration grouperDataFieldConfiguration = new GrouperDataFieldConfiguration();
      
      grouperDataFieldConfiguration.setConfigId(configId);
      
      String previousConfigId = request.getParameter("previousDataFieldConfigId");
      
      if (StringUtils.isBlank(previousConfigId)) {
        // first time loading the screen. let's get values from config files/database
        GuiDataFieldConfiguration guiDataFieldConfiguration = GuiDataFieldConfiguration.convertFromDataFieldConfiguration(grouperDataFieldConfiguration);
        entityDataFieldsContainer.setGuiDataFieldConfiguration(guiDataFieldConfiguration);
      } else {
        // change was made on the form
        grouperDataFieldConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataFieldConfiguration guiDataFieldConfiguration = GuiDataFieldConfiguration.convertFromDataFieldConfiguration(grouperDataFieldConfiguration);
        entityDataFieldsContainer.setGuiDataFieldConfiguration(guiDataFieldConfiguration);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/editDataFieldConfigDetails.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * save edited privacy realm config into db
   * @param request
   * @param response
   */
  public void editPrivacyRealmConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("privacyRealmConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#privacyRealmConfigId",
            TextContainer.retrieveFromRequest().getText().get("privacyRealmCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperPrivacyRealmConfiguration privacyRealmConfiguration = new GrouperPrivacyRealmConfiguration();
      
      privacyRealmConfiguration.setConfigId(configId);
      privacyRealmConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      privacyRealmConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewPrivacyRealmConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("privacyRealmConfigAddEditSuccess")));
   
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
   * delete privacy realm config
   * @param request
   * @param response
   */
  public void deletePrivacyRealmConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("privacyRealmConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      GrouperPrivacyRealmConfiguration privacyRealmConfiguration = new GrouperPrivacyRealmConfiguration();
      
      privacyRealmConfiguration.setConfigId(configId);
      
      privacyRealmConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewPrivacyRealmConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("privacyRealmConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete data row config
   * @param request
   * @param response
   */
  public void deleteDataRowConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataRowConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      GrouperDataRowConfiguration dataRowConfiguration = new GrouperDataRowConfiguration();
      
      dataRowConfiguration.setConfigId(configId);
      
      dataRowConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataRows')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataRowConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * save edited data row config into db
   * @param request
   * @param response
   */
  public void editDataRowConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataRowConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataRowConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataRowCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataRowConfiguration dataRowConfiguration = new GrouperDataRowConfiguration();
      
      dataRowConfiguration.setConfigId(configId);
      dataRowConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      dataRowConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataRows')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("dataRowConfigAddEditSuccess")));
   
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
   * insert a new config in db
   * @param request
   * @param response
   */
  public void addDataRowConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataRowConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataRowConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataRowCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataRowConfiguration dataRowConfiguration = new GrouperDataRowConfiguration();
      
      dataRowConfiguration.setConfigId(configId);
      dataRowConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      dataRowConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataRows')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataRowConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * save edited data field config into db
   * @param request
   * @param response
   */
  public void editDataFieldConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataFieldConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataFieldConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataFieldConfiguration dataFieldConfiguration = new GrouperDataFieldConfiguration();
      
      dataFieldConfiguration.setConfigId(configId);
      dataFieldConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      dataFieldConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataFields')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("dataFieldConfigAddEditSuccess")));
   
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
   * save edited data provider config into db
   * @param request
   * @param response
   */
  public void editDataProviderConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderConfiguration dataProviderConfiguration = new GrouperDataProviderConfiguration();
      
      dataProviderConfiguration.setConfigId(configId);
      dataProviderConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      dataProviderConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewDataProviders')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("dataProviderConfigAddEditSuccess")));
   
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
   * save edited data provider query config into db
   * @param request
   * @param response
   */
  public void editDataProviderQueryConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderQueryConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderQueryConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderQueryCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderQueryConfiguration dataProviderQueryConfiguration = new GrouperDataProviderQueryConfiguration();
      
      dataProviderQueryConfiguration.setConfigId(configId);
      dataProviderQueryConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      dataProviderQueryConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataProviderQueries')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("dataProviderQueryConfigAddEditSuccess")));
   
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
   * save edited data provider change log query config into db
   * @param request
   * @param response
   */
  public void editDataProviderChangeLogQueryConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderChangeLogQueryConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderChangeLogQueryConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderChangeLogQueryCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderChangeLogQueryConfiguration dataProviderChangeLogQueryConfiguration = new GrouperDataProviderChangeLogQueryConfiguration();
      
      dataProviderChangeLogQueryConfiguration.setConfigId(configId);
      dataProviderChangeLogQueryConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      List<String> actionsPerformed = new ArrayList<String>();

      dataProviderChangeLogQueryConfiguration.editConfig(true, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataProviderChangeLogQueries')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("dataProviderChangeLogQueryConfigAddEditSuccess")));
   
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
  public void deleteDataFieldConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataFieldConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      GrouperDataFieldConfiguration dataFieldConfiguration = new GrouperDataFieldConfiguration();
      
      dataFieldConfiguration.setConfigId(configId);
      
      dataFieldConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataFields')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataFieldConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete data provider config
   * @param request
   * @param response
   */
  public void deleteDataProviderConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      GrouperDataProviderConfiguration dataProviderConfiguration = new GrouperDataProviderConfiguration();
      
      dataProviderConfiguration.setConfigId(configId);
      
      dataProviderConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewDataProviders')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataProviderConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete data provider query config
   * @param request
   * @param response
   */
  public void deleteDataProviderQueryConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderQueryConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      GrouperDataProviderQueryConfiguration dataProviderQueryConfiguration = new GrouperDataProviderQueryConfiguration();
      
      dataProviderQueryConfiguration.setConfigId(configId);
      
      dataProviderQueryConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataProviderQueries')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataProviderQueryConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * delete data provider change log query config
   * @param request
   * @param response
   */
  public void deleteDataProviderChangeLogQueryConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderChangeLogQueryConfigId");
      
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("ConfigId cannot be blank");
      }
      
      GrouperDataProviderChangeLogQueryConfiguration dataProviderChangeLogQueryConfiguration = new GrouperDataProviderChangeLogQueryConfiguration();
      
      dataProviderChangeLogQueryConfiguration.setConfigId(configId);
      
      dataProviderChangeLogQueryConfiguration.deleteConfig(true);
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataProviderChangeLogQueries')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataProviderChangeLogQueryConfigDeleteSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * @param request
   * @param response
   */
  public void addDataFieldConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataFieldConfigId");
      
      String type = request.getParameter("dataFieldType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<GrouperDataFieldConfiguration> klass = (Class<GrouperDataFieldConfiguration>) GrouperUtil.forName(type);
        GrouperDataFieldConfiguration dataFieldConfiguration = (GrouperDataFieldConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#dataFieldConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
          return;
        }
        
        dataFieldConfiguration.setConfigId(configId);
        dataFieldConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataFieldConfiguration guiDataFieldConfig = GuiDataFieldConfiguration.convertFromDataFieldConfiguration(dataFieldConfiguration);
        entityDataFieldsContainer.setGuiDataFieldConfiguration(guiDataFieldConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/entityDataFields/dataFieldConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * insert a new config in db
   * @param request
   * @param response
   */
  public void addDataFieldConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataFieldConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataFieldConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataFieldCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataFieldConfiguration dataFieldConfiguration = new GrouperDataFieldConfiguration();
      
      dataFieldConfiguration.setConfigId(configId);
      dataFieldConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      dataFieldConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataFields')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataFieldConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * @param request
   * @param response
   */
  public void addPrivacyRealmConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("privacyRealmConfigId");
      
      String type = request.getParameter("privacyRealmType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<GrouperPrivacyRealmConfiguration> klass = (Class<GrouperPrivacyRealmConfiguration>) GrouperUtil.forName(type);
        GrouperPrivacyRealmConfiguration privacyRealmConfiguration = (GrouperPrivacyRealmConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#privacyRealmConfigId",
              TextContainer.retrieveFromRequest().getText().get("privacyRealmCreateErrorConfigIdRequired")));
          return;
        }
        
        privacyRealmConfiguration.setConfigId(configId);
        privacyRealmConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiPrivacyRealmConfiguration guiPrivacyRealmConfig = GuiPrivacyRealmConfiguration.convertFromPrivacyRealmConfiguration(privacyRealmConfiguration);
        entityDataFieldsContainer.setGuiPrivacyRealmConfiguration(guiPrivacyRealmConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/entityDataFields/privacyRealmConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * insert a new config in db
   * @param request
   * @param response
   */
  public void addPrivacyRealmConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("privacyRealmConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#privacyRealmConfigId",
            TextContainer.retrieveFromRequest().getText().get("privacyRealmCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperPrivacyRealmConfiguration privacyRealmConfiguration = new GrouperPrivacyRealmConfiguration();
      
      privacyRealmConfiguration.setConfigId(configId);
      privacyRealmConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      privacyRealmConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewPrivacyRealmConfigs')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("privacyRealmConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  
  /**
   * @param request
   * @param response
   */
  public void addDataRowConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataRowConfigId");
      
      String type = request.getParameter("dataRowType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<GrouperDataRowConfiguration> klass = (Class<GrouperDataRowConfiguration>) GrouperUtil.forName(type);
        GrouperDataRowConfiguration dataRowConfiguration = (GrouperDataRowConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#dataRowConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataRowCreateErrorConfigIdRequired")));
          return;
        }
        
        dataRowConfiguration.setConfigId(configId);
        dataRowConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataRowConfiguration guiDataRowConfig = GuiDataRowConfiguration.convertFromDataRowConfiguration(dataRowConfiguration);
        entityDataFieldsContainer.setGuiDataRowConfiguration(guiDataRowConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/entityDataFields/dataRowConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * @param request
   * @param response
   */
  public void addDataProviderConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderConfigId");
      
      String type = request.getParameter("dataProviderType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<GrouperDataProviderConfiguration> klass = (Class<GrouperDataProviderConfiguration>) GrouperUtil.forName(type);
        GrouperDataProviderConfiguration dataProviderConfiguration = (GrouperDataProviderConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#dataProviderConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataProviderCreateErrorConfigIdRequired")));
          return;
        }
        
        dataProviderConfiguration.setConfigId(configId);
        dataProviderConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataProviderConfiguration guiDataProviderConfig = GuiDataProviderConfiguration.convertFromDataProviderConfiguration(dataProviderConfiguration);
        entityDataFieldsContainer.setGuiDataProviderConfiguration(guiDataProviderConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/entityDataFields/dataProviderConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  /**
   * @param request
   * @param response
   */
  public void addDataProviderQueryConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderQueryConfigId");
      
      String type = request.getParameter("dataProviderQueryType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<GrouperDataProviderQueryConfiguration> klass = (Class<GrouperDataProviderQueryConfiguration>) GrouperUtil.forName(type);
        GrouperDataProviderQueryConfiguration dataProviderQueryConfiguration = (GrouperDataProviderQueryConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#dataProviderQueryConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataProviderQueryCreateErrorConfigIdRequired")));
          return;
        }
        
        dataProviderQueryConfiguration.setConfigId(configId);
        dataProviderQueryConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataProviderQueryConfiguration guiDataProviderQueryConfig = GuiDataProviderQueryConfiguration.convertFromDataProviderQueryConfiguration(dataProviderQueryConfiguration);
        entityDataFieldsContainer.setGuiDataProviderQueryConfiguration(guiDataProviderQueryConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/entityDataFields/dataProviderQueryConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  

  /**
   * @param request
   * @param response
   */
  public void addDataProviderChangeLogQueryConfig(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderChangeLogQueryConfigId");
      
      String type = request.getParameter("dataProviderChangeLogQueryType");
      
      if (StringUtils.isNotBlank(type)) {
        
        Class<GrouperDataProviderChangeLogQueryConfiguration> klass = (Class<GrouperDataProviderChangeLogQueryConfiguration>) GrouperUtil.forName(type);
        GrouperDataProviderChangeLogQueryConfiguration dataProviderChangeLogQueryConfiguration = (GrouperDataProviderChangeLogQueryConfiguration) GrouperUtil.newInstance(klass);
        
        if (StringUtils.isBlank(configId)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
              "#dataProviderChangeLogQueryConfigId",
              TextContainer.retrieveFromRequest().getText().get("dataProviderChangeLogQueryCreateErrorConfigIdRequired")));
          return;
        }
        
        dataProviderChangeLogQueryConfiguration.setConfigId(configId);
        dataProviderChangeLogQueryConfiguration.populateConfigurationValuesFromUi(request);
        
        GuiDataProviderChangeLogQueryConfiguration guiDataProviderChangeLogQueryConfig = GuiDataProviderChangeLogQueryConfiguration.convertFromDataProviderChangeLogQueryConfiguration(dataProviderChangeLogQueryConfiguration);
        entityDataFieldsContainer.setGuiDataProviderChangeLogQueryConfiguration(guiDataProviderChangeLogQueryConfig);
        
      }

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/entityDataFields/dataProviderChangeLogQueryConfigAdd.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  
  /**
   * insert a new config in db
   * @param request
   * @param response
   */
  public void addDataProviderConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderConfiguration dataProviderConfiguration = new GrouperDataProviderConfiguration();
      
      dataProviderConfiguration.setConfigId(configId);
      dataProviderConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      dataProviderConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewDataProviders')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataProviderConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * insert a new config in db
   * @param request
   * @param response
   */
  public void addDataProviderQueryConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderQueryConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderQueryConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderQueryCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderQueryConfiguration dataProviderQueryConfiguration = new GrouperDataProviderQueryConfiguration();
      
      dataProviderQueryConfiguration.setConfigId(configId);
      dataProviderQueryConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      dataProviderQueryConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataProviderQueries')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataProviderQueryConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * insert a new config in db
   * @param request
   * @param response
   */
  public void addDataProviderChangeLogQueryConfigSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      String configId = request.getParameter("dataProviderChangeLogQueryConfigId");
      
      if (StringUtils.isBlank(configId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#dataProviderChangeLogQueryConfigId",
            TextContainer.retrieveFromRequest().getText().get("dataProviderChangeLogQueryCreateErrorConfigIdRequired")));
        return;
      }
      
      GrouperDataProviderChangeLogQueryConfiguration dataProviderChangeLogQueryConfiguration = new GrouperDataProviderChangeLogQueryConfiguration();
      
      dataProviderChangeLogQueryConfiguration.setConfigId(configId);
      dataProviderChangeLogQueryConfiguration.populateConfigurationValuesFromUi(request);
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      dataProviderChangeLogQueryConfiguration.insertConfig(true, message, errorsToDisplay, validationErrorsToDisplay, new ArrayList<String>());
      
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
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2EntityDataFields.viewEntityDataProviderChangeLogQueries')"));
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("dataProviderChangeLogQueryConfigAddEditSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * view data rows
   * @param request
   * @param response
   */
  public void viewEntityDataRows(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GrouperDataRowConfiguration> dataRowConfigurations = GrouperDataRowConfiguration.retrieveAllDataRowConfigurations();
      
      List<GuiDataRowConfiguration> guiDataRowConfigurations = GuiDataRowConfiguration.convertFromDataRowConfiguration(dataRowConfigurations);
      
      entityDataFieldsContainer.setGuiDataRowConfigurations(guiDataRowConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/entityDataRows.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view data provider queries
   * @param request
   * @param response
   */
  public void viewEntityDataProviderQueries(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
       
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GrouperDataProviderQueryConfiguration> dataProviderQueryConfigurations = GrouperDataProviderQueryConfiguration.retrieveAllDataProviderQueryConfigurations();
      
      List<GuiDataProviderQueryConfiguration> guiDataProviderQueryConfigurations = GuiDataProviderQueryConfiguration.convertFromDataProviderQueryConfiguration(dataProviderQueryConfigurations);
      
      entityDataFieldsContainer.setGuiDataProviderQueryConfigurations(guiDataProviderQueryConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/entityDataProviderQueries.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view data provider change log queries
   * @param request
   * @param response
   */
  public void viewEntityDataProviderChangeLogQueries(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
       
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GrouperDataProviderChangeLogQueryConfiguration> dataProviderChangeLogQueryConfigurations = GrouperDataProviderChangeLogQueryConfiguration.retrieveAllDataProviderChangeLogQueryConfigurations();
      
      List<GuiDataProviderChangeLogQueryConfiguration> guiDataProviderChangeLogQueryConfigurations = GuiDataProviderChangeLogQueryConfiguration.convertFromDataProviderChangeLogQueryConfiguration(dataProviderChangeLogQueryConfigurations);
      
      entityDataFieldsContainer.setGuiDataProviderChangeLogQueryConfigurations(guiDataProviderChangeLogQueryConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/entityDataProviderChangeLogQueries.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * view data providers
   * @param request
   * @param response
   */
  public void viewDataProviders(final HttpServletRequest request, final HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      if (!entityDataFieldsContainer.isCanOperateOnEntityDataFieldConfigs()) {
        throw new RuntimeException("Not allowed!!!!!");
      }
      
      List<GrouperDataProviderConfiguration> dataProviderConfigurations = GrouperDataProviderConfiguration.retrieveAllDataProviderConfigurations();
      
      List<GuiDataProviderConfiguration> guiDataProviderConfigurations = GuiDataProviderConfiguration.convertFromDataProviderConfiguration(dataProviderConfigurations);
      
      entityDataFieldsContainer.setGuiDataProviderConfigurations(guiDataProviderConfigurations);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/entityDataProviders.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * 
   * @param request
   * @param response
   */
  public void viewDataFieldAndRowDictionary(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      List<GuiDataFieldRowDictionaryTable> result = new ArrayList<>(); 
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      grouperDataEngine.loadFieldsAndRows(grouperConfig);
      
      List<GrouperDataFieldConfig> dataFields = grouperDataEngine.retrieveGrouperDataFieldsForDataFieldAndDictionary(loggedInSubject);
      List<GrouperDataRowConfig> dataRows = grouperDataEngine.retrieveGrouperDataRowsForDataFieldAndDictionary(loggedInSubject);
      
      GuiDataFieldRowDictionaryTable guiDataFieldRowDictionaryTable = new GuiDataFieldRowDictionaryTable();

      List<GuiDataFieldRowDictionary> fieldConfigItems = new ArrayList<>();
      
      for (GrouperDataFieldConfig dataFieldConfig: dataFields) {
        
        GuiDataFieldRowDictionary guiDataFieldRowDictionary = new GuiDataFieldRowDictionary();

        String grouperPrivacyRealmConfigId = dataFieldConfig.getGrouperPrivacyRealmConfigId();
        
        GrouperPrivacyRealmConfig grouperPrivacyRealmConfig = grouperDataEngine.getPrivacyRealmConfigByConfigId().get(grouperPrivacyRealmConfigId);
        
        String highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(grouperPrivacyRealmConfig, loggedInSubject);
        
        guiDataFieldRowDictionary.setDataFieldAliases(String.join(", ", dataFieldConfig.getFieldAliases()));
        guiDataFieldRowDictionary.setDataOwner(dataFieldConfig.getDataOwnerHtml());
        guiDataFieldRowDictionary.setDataType(dataFieldConfig.getFieldDataType().name());
        guiDataFieldRowDictionary.setDescription(dataFieldConfig.getDescriptionHtml());
        guiDataFieldRowDictionary.setExamples(dataFieldConfig.getZeroToManyExamplesHtml());
        guiDataFieldRowDictionary.setHowToGetAccess(dataFieldConfig.getHowToGetAccessHtml());
        guiDataFieldRowDictionary.setPrivilege(highestLevelAccess);
        
        fieldConfigItems.add(guiDataFieldRowDictionary);
      }
      guiDataFieldRowDictionaryTable.setGuiDataFieldRowDictionary(fieldConfigItems);
      result.add(guiDataFieldRowDictionaryTable);
      
      for (GrouperDataRowConfig dataRowConfig: dataRows) {
        
        guiDataFieldRowDictionaryTable = new GuiDataFieldRowDictionaryTable();
        guiDataFieldRowDictionaryTable.setDataRowAlias(String.join(", ", dataRowConfig.getRowAliases()));
        guiDataFieldRowDictionaryTable.setDescription(dataRowConfig.getDescriptionHtml());
        guiDataFieldRowDictionaryTable.setDataOwner(dataRowConfig.getDataOwnerHtml());
        guiDataFieldRowDictionaryTable.setHowToGetAccess(dataRowConfig.getHowToGetAccessHtml());
        
        fieldConfigItems = new ArrayList<>();
        
        for (String dataFieldConfigId : dataRowConfig.getDataFieldConfigIds()) {
          
          GrouperDataFieldConfig dataFieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
          
          GuiDataFieldRowDictionary guiDataFieldRowDictionary = new GuiDataFieldRowDictionary();

          String grouperPrivacyRealmConfigId = dataFieldConfig.getGrouperPrivacyRealmConfigId();
          
          GrouperPrivacyRealmConfig grouperPrivacyRealmConfig = grouperDataEngine.getPrivacyRealmConfigByConfigId().get(grouperPrivacyRealmConfigId);
          
          String highestLevelAccess = GrouperDataEngine.calculateHighestLevelAccess(grouperPrivacyRealmConfig, loggedInSubject);
          
          guiDataFieldRowDictionary.setDataFieldAliases(String.join(", ", dataFieldConfig.getFieldAliases()));
          guiDataFieldRowDictionary.setDataOwner(dataFieldConfig.getDataOwnerHtml());
          guiDataFieldRowDictionary.setDataType(dataFieldConfig.getFieldDataType().name());
          guiDataFieldRowDictionary.setDescription(dataFieldConfig.getDescriptionHtml());
          guiDataFieldRowDictionary.setExamples(dataFieldConfig.getZeroToManyExamplesHtml());
          guiDataFieldRowDictionary.setHowToGetAccess(dataFieldConfig.getHowToGetAccessHtml());
          guiDataFieldRowDictionary.setPrivilege(highestLevelAccess);
          fieldConfigItems.add(guiDataFieldRowDictionary);
        }
        
        guiDataFieldRowDictionaryTable.setGuiDataFieldRowDictionary(fieldConfigItems);
        result.add(guiDataFieldRowDictionaryTable);
      }
      
      entityDataFieldsContainer.setGuiDataFieldRowDictionaryTables(result);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/dataFieldRowDictionary.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

}
