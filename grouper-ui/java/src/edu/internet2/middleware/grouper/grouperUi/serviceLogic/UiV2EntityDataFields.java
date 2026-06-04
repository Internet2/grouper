package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
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
import edu.internet2.middleware.grouper.exception.GrouperReferentialIntegrityException;
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
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class UiV2EntityDataFields {

  private static final Log LOG = GrouperUtil.getLog(UiV2EntityDataFields.class);

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

      // check if this privacy realm is referenced by any data fields or data rows
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      grouperDataEngine.loadFieldsAndRows(null);

      List<String> referencingConfigs = new ArrayList<>();

      for (Map.Entry<String, GrouperDataFieldConfig> fieldEntry : grouperDataEngine.getFieldConfigByConfigId().entrySet()) {
        if (StringUtils.equals(configId, fieldEntry.getValue().getGrouperPrivacyRealmConfigId())) {
          referencingConfigs.add("data field '" + fieldEntry.getKey() + "'");
        }
      }

      for (Map.Entry<String, GrouperDataRowConfig> rowEntry : grouperDataEngine.getRowConfigByConfigId().entrySet()) {
        if (StringUtils.equals(configId, rowEntry.getValue().getPrivacyRealmName())) {
          referencingConfigs.add("data row '" + rowEntry.getKey() + "'");
        }
      }

      if (referencingConfigs.size() > 0) {
        String referencingConfigsString = StringUtils.join(referencingConfigs, ", ");
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("privacyRealmConfigDeleteErrorInUse")
            + " " + referencingConfigsString));
        return;
      }

      GrouperPrivacyRealmConfiguration privacyRealmConfiguration = new GrouperPrivacyRealmConfiguration();

      privacyRealmConfiguration.setConfigId(configId);

      try {
        privacyRealmConfiguration.deleteConfig(true);
      } catch (GrouperReferentialIntegrityException e) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, e.getMessage()));
        return;
      }

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

      // check if this data row is referenced by any data provider queries
      List<String> referencingConfigs = new ArrayList<>();

      List<GrouperDataProviderQueryConfiguration> allQueryConfigs = GrouperDataProviderQueryConfiguration.retrieveAllDataProviderQueryConfigurations();
      for (GrouperDataProviderQueryConfiguration queryConfig : GrouperUtil.nonNull(allQueryConfigs)) {
        String rowConfigId = queryConfig.retrieveAttributeValueFromConfig("providerQueryRowConfigId", false);
        if (StringUtils.equals(configId, rowConfigId)) {
          referencingConfigs.add("data provider query '" + queryConfig.getConfigId() + "'");
        }
      }

      if (referencingConfigs.size() > 0) {
        String referencingConfigsString = StringUtils.join(referencingConfigs, ", ");
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("dataRowConfigDeleteErrorInUse")
            + " " + referencingConfigsString));
        return;
      }

      GrouperDataRowConfiguration dataRowConfiguration = new GrouperDataRowConfiguration();

      dataRowConfiguration.setConfigId(configId);

      try {
        dataRowConfiguration.deleteConfig(true);
      } catch (GrouperReferentialIntegrityException e) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, e.getMessage()));
        return;
      }

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

      // check if this data field is referenced by any data rows or data provider queries
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      grouperDataEngine.loadFieldsAndRows(null);

      List<String> referencingConfigs = new ArrayList<>();

      for (Map.Entry<String, GrouperDataRowConfig> rowEntry : grouperDataEngine.getRowConfigByConfigId().entrySet()) {
        if (rowEntry.getValue().getDataFieldConfigIds().contains(configId)) {
          referencingConfigs.add("data row '" + rowEntry.getKey() + "'");
        }
      }

      // check if this data field is referenced by any data provider queries
      List<GrouperDataProviderQueryConfiguration> allQueryConfigs = GrouperDataProviderQueryConfiguration.retrieveAllDataProviderQueryConfigurations();
      for (GrouperDataProviderQueryConfiguration queryConfig : GrouperUtil.nonNull(allQueryConfigs)) {
        String numberOfFieldsString = queryConfig.retrieveAttributeValueFromConfig("providerQueryNumberOfDataFields", false);
        int numberOfFields = GrouperUtil.intValue(numberOfFieldsString, 0);
        for (int i = 0; i < numberOfFields; i++) {
          String fieldConfigId = queryConfig.retrieveAttributeValueFromConfig("providerQueryDataField." + i + ".providerDataFieldConfigId", false);
          if (StringUtils.equals(configId, fieldConfigId)) {
            referencingConfigs.add("data provider query '" + queryConfig.getConfigId() + "'");
            break;
          }
        }
      }

      // check if this data field is used by any scripted groups (ABAC)
      List<String> dependentGroupNames = new GcDbAccess().sql("select distinct depen_group_name from grouper_sql_dependency_attr_v where owner_data_field_config_id = ?").addBindVar(configId).selectList(String.class);
      for (String groupName : GrouperUtil.nonNull(dependentGroupNames)) {
        referencingConfigs.add("scripted group '" + groupName + "'");
      }

      if (referencingConfigs.size() > 0) {
        String referencingConfigsString = StringUtils.join(referencingConfigs, ", ");
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("dataFieldConfigDeleteErrorInUse")
            + " " + referencingConfigsString));
        return;
      }

      GrouperDataFieldConfiguration dataFieldConfiguration = new GrouperDataFieldConfiguration();

      dataFieldConfiguration.setConfigId(configId);

      try {
        dataFieldConfiguration.deleteConfig(true);
      } catch (GrouperReferentialIntegrityException e) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, e.getMessage()));
        return;
      }

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

      try {
        dataProviderConfiguration.deleteConfig(true);
      } catch (GrouperReferentialIntegrityException e) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, e.getMessage()));
        return;
      }

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
   * View data field and row dictionary — search-driven page.
   * Reads filter params from request, populates dropdown options, and
   * renders results only when a filter is applied (or show-all / no filters).
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
      
      // read filter parameters from request
      String filterDataRow = StringUtils.trimToNull(request.getParameter("dataRow"));
      String filterDataField = StringUtils.trimToNull(request.getParameter("dataField"));
      String filterPrivacyRealm = StringUtils.trimToNull(request.getParameter("privacyRealm"));
      String filterSearchText = StringUtils.trimToNull(request.getParameter("search"));
      boolean filterShowAll = "true".equals(request.getParameter("showAll"));
      boolean filterAutoExpandAll = "true".equals(request.getParameter("autoExpandAll"));
      
      // determine if we should show results: if any filter is set, or showAll, or submit was clicked
      boolean hasAnyFilter = filterShowAll || filterDataRow != null || filterDataField != null 
          || filterPrivacyRealm != null || filterSearchText != null;
      // if submit was pressed with nothing selected, treat as show-all
      boolean submitted = "true".equals(request.getParameter("submitted"));
      if (submitted && !hasAnyFilter) {
        filterShowAll = true;
        hasAnyFilter = true;
      }
      
      // store filter state
      entityDataFieldsContainer.setDictionaryFilterDataRow(filterDataRow);
      entityDataFieldsContainer.setDictionaryFilterDataField(filterDataField);
      entityDataFieldsContainer.setDictionaryFilterPrivacyRealm(filterPrivacyRealm);
      entityDataFieldsContainer.setDictionaryFilterSearchText(filterSearchText);
      entityDataFieldsContainer.setDictionaryFilterShowAll(filterShowAll);
      entityDataFieldsContainer.setDictionaryFilterAutoExpandAll(filterAutoExpandAll);
      entityDataFieldsContainer.setDictionaryHasResults(hasAnyFilter);
      
      // load data engine
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      grouperDataEngine.loadFieldsAndRows(grouperConfig);
      
      // populate dropdown options
      populateDictionaryDropdowns(entityDataFieldsContainer, grouperDataEngine, loggedInSubject, filterDataRow);
      
      // build results if filter is active
      if (hasAnyFilter) {
        List<GuiDataFieldRowDictionaryTable> result = buildDictionaryResults(
            grouperDataEngine, loggedInSubject, filterDataRow, filterDataField, 
            filterPrivacyRealm, filterSearchText, filterShowAll);
        entityDataFieldsContainer.setGuiDataFieldRowDictionaryTables(result);
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/entityDataFields/dataFieldRowDictionary.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * AJAX endpoint to get data fields for a selected data row (repopulates the data field dropdown).
   * Uses standard GuiResponseJs innerHtml pattern to replace the select contents via a JSP fragment.
   * @param request
   * @param response
   */
  public void dataFieldDictionaryFieldsForRow(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = null;
    
    final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      final EntityDataFieldsContainer entityDataFieldsContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getEntityDataFieldsContainer();
      
      String dataRowConfigId = StringUtils.trimToNull(request.getParameter("dataRow"));
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      grouperDataEngine.loadFieldsAndRows(grouperConfig);
      
      // populate field options: only the selected row's fields if a row is selected,
      // otherwise all data fields the user can access (not every field belongs to a row)
      List<String[]> fieldOptions;
      if (dataRowConfigId != null) {
        fieldOptions = buildDataFieldOptionsForRow(grouperDataEngine, dataRowConfigId);
      } else {
        fieldOptions = buildAllDataFieldOptions(grouperDataEngine, loggedInSubject);
      }
      entityDataFieldsContainer.setDictionaryDataFieldOptions(fieldOptions);
      
      // replace the dropdown contents; always keep it enabled since fields can be selected without a row
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#dictDataField",
          "/WEB-INF/grouperUi2/entityDataFields/dataFieldDictionaryFieldOptions.jsp"));
      guiResponseJs.addAction(GuiScreenAction.newScript("$('#dictDataField').prop('disabled', false);"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Export dictionary results as CSV or JSON.
   * @param request
   * @param response
   */
  public void dataFieldDictionaryExport(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      String filterDataRow = StringUtils.trimToNull(request.getParameter("dataRow"));
      String filterDataField = StringUtils.trimToNull(request.getParameter("dataField"));
      String filterPrivacyRealm = StringUtils.trimToNull(request.getParameter("privacyRealm"));
      String filterSearchText = StringUtils.trimToNull(request.getParameter("search"));
      boolean filterShowAll = "true".equals(request.getParameter("showAll"));
      String format = StringUtils.defaultIfBlank(request.getParameter("format"), "csv");
      
      boolean hasAnyFilter = filterShowAll || filterDataRow != null || filterDataField != null 
          || filterPrivacyRealm != null || filterSearchText != null;
      if (!hasAnyFilter) {
        filterShowAll = true;
      }
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      grouperDataEngine.loadFieldsAndRows(grouperConfig);
      
      List<GuiDataFieldRowDictionaryTable> tables = buildDictionaryResults(
          grouperDataEngine, loggedInSubject, filterDataRow, filterDataField, 
          filterPrivacyRealm, filterSearchText, filterShowAll);
      
      // flatten all field items
      List<GuiDataFieldRowDictionary> allFields = new ArrayList<>();
      for (GuiDataFieldRowDictionaryTable table : tables) {
        if (table.getGuiDataFieldRowDictionary() != null) {
          for (GuiDataFieldRowDictionary field : table.getGuiDataFieldRowDictionary()) {
            // resolve inherited values for export
            if (StringUtils.isBlank(field.getDataOwner()) && StringUtils.isNotBlank(field.getDataRowDataOwner())) {
              field.setDataOwner(field.getDataRowDataOwner());
            }
            if (StringUtils.isBlank(field.getHowToGetAccess()) && StringUtils.isNotBlank(field.getDataRowHowToGetAccess())) {
              field.setHowToGetAccess(field.getDataRowHowToGetAccess());
            }
            allFields.add(field);
          }
        }
      }
      
      if ("json".equalsIgnoreCase(format)) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"data_field_dictionary.json\"");
        PrintWriter writer = response.getWriter();
        writer.write("[");
        for (int i = 0; i < allFields.size(); i++) {
          if (i > 0) writer.write(",");
          GuiDataFieldRowDictionary f = allFields.get(i);
          writer.write("{");
          writer.write("\"data_row\":" + jsonVal(f.getDataRowName()));
          writer.write(",\"data_field_aliases\":" + jsonVal(f.getDataFieldAliases()));
          writer.write(",\"description\":" + jsonVal(f.getDescription()));
          writer.write(",\"privilege\":" + jsonVal(f.getPrivilege()));
          writer.write(",\"privilege_humanized\":" + jsonVal(f.getPrivilegeHumanized()));
          writer.write(",\"data_type\":" + jsonVal(f.getDataType()));
          writer.write(",\"data_owner\":" + jsonVal(f.getDataOwner()));
          writer.write(",\"how_to_get_access\":" + jsonVal(f.getHowToGetAccess()));
          writer.write(",\"privacy_realm\":" + jsonVal(f.getPrivacyRealmConfigId()));
          writer.write(",\"examples\":" + jsonVal(f.getExamples()));
          writer.write(",\"value_type\":" + jsonVal(f.getValueType()));
          writer.write(",\"multi_valued\":" + f.isMultiValued());
          writer.write(",\"jexl_snippet\":" + jsonVal(f.getJexlSnippet()));
          writer.write("}");
        }
        writer.write("]");
      } else {
        // CSV
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"data_field_dictionary.csv\"");
        PrintWriter writer = response.getWriter();
        writer.println("data_row,data_field_aliases,description,privilege,privilege_humanized,data_type,data_owner,how_to_get_access,privacy_realm,examples,value_type,multi_valued,jexl_snippet");
        for (GuiDataFieldRowDictionary f : allFields) {
          writer.println(
            csvVal(f.getDataRowName()) + "," +
            csvVal(f.getDataFieldAliases()) + "," +
            csvVal(f.getDescription()) + "," +
            csvVal(f.getPrivilege()) + "," +
            csvVal(f.getPrivilegeHumanized()) + "," +
            csvVal(f.getDataType()) + "," +
            csvVal(f.getDataOwner()) + "," +
            csvVal(f.getHowToGetAccess()) + "," +
            csvVal(f.getPrivacyRealmConfigId()) + "," +
            csvVal(f.getExamples()) + "," +
            csvVal(f.getValueType()) + "," +
            f.isMultiValued() + "," +
            csvVal(f.getJexlSnippet())
          );
        }
      }
      
      throw new ControllerDone();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  private static String jsonVal(String val) {
    if (val == null) return "null";
    return "\"" + GrouperUtil.escapeDoubleQuotesSlashesAndNewlinesForString(val) + "\"";
  }
  
  private static String csvVal(String val) {
    if (val == null) return "";
    // strip HTML tags for CSV export
    String stripped = val.replaceAll("<[^>]*>", "").replace("&nbsp;", " ")
        .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
    // escape for CSV
    if (stripped.contains(",") || stripped.contains("\"") || stripped.contains("\n")) {
      return "\"" + stripped.replace("\"", "\"\"") + "\"";
    }
    return stripped;
  }
  
  /**
   * Populate dropdown options for the filter panel.
   */
  private void populateDictionaryDropdowns(EntityDataFieldsContainer container, 
      GrouperDataEngine grouperDataEngine, Subject loggedInSubject, String selectedDataRow) {
    
    // data row options - sorted by alias
    List<GrouperDataRowConfig> dataRows = grouperDataEngine.retrieveGrouperDataRowsForDataFieldAndDictionary(loggedInSubject);
    Map<String, GrouperDataRowConfig> sortedRows = new TreeMap<>();
    for (GrouperDataRowConfig rowConfig : dataRows) {
      List<String> aliases = new ArrayList<>(rowConfig.getRowAliases());
      Collections.sort(aliases, String.CASE_INSENSITIVE_ORDER);
      sortedRows.put(aliases.get(0).toLowerCase(), rowConfig);
    }
    List<String[]> rowOptions = new ArrayList<>();
    for (Map.Entry<String, GrouperDataRowConfig> entry : sortedRows.entrySet()) {
      GrouperDataRowConfig rowConfig = entry.getValue();
      rowOptions.add(new String[]{rowConfig.getConfigId(), String.join(", ", rowConfig.getRowAliases())});
    }
    container.setDictionaryDataRowOptions(rowOptions);
    
    // data field options: only the selected row's fields if a row is selected,
    // otherwise all data fields the user can access (not every field belongs to a row)
    List<String[]> fieldOptions;
    if (selectedDataRow != null) {
      fieldOptions = buildDataFieldOptionsForRow(grouperDataEngine, selectedDataRow);
    } else {
      fieldOptions = buildAllDataFieldOptions(grouperDataEngine, loggedInSubject);
    }
    container.setDictionaryDataFieldOptions(fieldOptions);
    
    // privacy realm options - sorted alphabetically
    Set<String> realmIds = new TreeSet<>(grouperDataEngine.getPrivacyRealmConfigByConfigId().keySet());
    List<String[]> realmOptions = new ArrayList<>();
    for (String realmId : realmIds) {
      GrouperPrivacyRealmConfig realmConfig = grouperDataEngine.getPrivacyRealmConfigByConfigId().get(realmId);
      String label = StringUtils.isNotBlank(realmConfig.getPrivacyRealmName()) ? realmConfig.getPrivacyRealmName() : realmId;
      realmOptions.add(new String[]{realmId, label});
    }
    container.setDictionaryPrivacyRealmOptions(realmOptions);
  }
  
  /**
   * Build data field dropdown options for the fields belonging to a specific data row.
   */
  private List<String[]> buildDataFieldOptionsForRow(GrouperDataEngine grouperDataEngine, String dataRowConfigId) {
    List<String[]> fieldOptions = new ArrayList<>();
    GrouperDataRowConfig rowConfig = grouperDataEngine.getRowConfigByConfigId().get(dataRowConfigId);
    if (rowConfig == null) {
      rowConfig = grouperDataEngine.getRowConfigByAlias().get(dataRowConfigId.toLowerCase());
    }
    if (rowConfig != null) {
      for (String fieldConfigId : rowConfig.getDataFieldConfigIds()) {
        GrouperDataFieldConfig fieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(fieldConfigId);
        if (fieldConfig != null) {
          fieldOptions.add(new String[]{fieldConfigId, String.join(", ", fieldConfig.getFieldAliases())});
        }
      }
    }
    return fieldOptions;
  }
  
  /**
   * Build data field dropdown options for all data fields the user can access — the
   * standalone sections (individuals, global, groups) plus fields belonging to data rows.
   * This mirrors what the dictionary results render, so every dropdown entry maps to a
   * displayable result. Deduplicated by config id and sorted by alias.
   */
  private List<String[]> buildAllDataFieldOptions(GrouperDataEngine grouperDataEngine, Subject loggedInSubject) {
    
    Map<String, GrouperDataFieldConfig> configIdToField = new LinkedHashMap<>();
    
    // standalone fields across the three categories the dictionary renders
    for (String assignableTo : new String[]{"individuals", "global", "groups"}) {
      MultiKey fieldsAndHasAccess = grouperDataEngine.retrieveGrouperDataFieldsForDataFieldAndDictionary(loggedInSubject, assignableTo);
      @SuppressWarnings("unchecked")
      List<GrouperDataFieldConfig> fieldConfigs = (List<GrouperDataFieldConfig>) fieldsAndHasAccess.getKey(0);
      for (GrouperDataFieldConfig fieldConfig : fieldConfigs) {
        configIdToField.put(fieldConfig.getConfigId(), fieldConfig);
      }
    }
    
    // row fields from accessible data rows
    List<GrouperDataRowConfig> dataRows = grouperDataEngine.retrieveGrouperDataRowsForDataFieldAndDictionary(loggedInSubject);
    for (GrouperDataRowConfig rowConfig : dataRows) {
      for (String fieldConfigId : rowConfig.getDataFieldConfigIds()) {
        GrouperDataFieldConfig fieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(fieldConfigId);
        if (fieldConfig != null) {
          configIdToField.put(fieldConfig.getConfigId(), fieldConfig);
        }
      }
    }
    
    // sort by first alias, case-insensitive
    List<GrouperDataFieldConfig> allFields = new ArrayList<>(configIdToField.values());
    allFields.sort((a, b) -> {
      String aliasA = a.getFieldAliases().isEmpty() ? a.getConfigId() : a.getFieldAliases().iterator().next();
      String aliasB = b.getFieldAliases().isEmpty() ? b.getConfigId() : b.getFieldAliases().iterator().next();
      return aliasA.compareToIgnoreCase(aliasB);
    });
    
    List<String[]> fieldOptions = new ArrayList<>();
    for (GrouperDataFieldConfig fieldConfig : allFields) {
      fieldOptions.add(new String[]{fieldConfig.getConfigId(), String.join(", ", fieldConfig.getFieldAliases())});
    }
    return fieldOptions;
  }
  
  /**
   * Build filtered dictionary results.
   */
  @SuppressWarnings("unchecked")
  private List<GuiDataFieldRowDictionaryTable> buildDictionaryResults(
      GrouperDataEngine grouperDataEngine, Subject loggedInSubject,
      String filterDataRow, String filterDataField, String filterPrivacyRealm, 
      String filterSearchText, boolean filterShowAll) {
    
    List<GuiDataFieldRowDictionaryTable> result = new ArrayList<>();
    
    // parse search terms (split on whitespace or comma, AND logic)
    List<String> searchTerms = new ArrayList<>();
    if (StringUtils.isNotBlank(filterSearchText)) {
      String[] parts = filterSearchText.split("[,\\s]+");
      for (String part : parts) {
        String trimmed = part.trim().toLowerCase();
        if (trimmed.length() > 0) {
          searchTerms.add(trimmed);
        }
      }
    }
    
    // process "individuals" data fields
    buildFieldSection(result, grouperDataEngine, loggedInSubject, "individuals",
        "entityDataFieldRowDictionaryDataFieldIndividualsTitle",
        "entityDataFieldRowDictionaryDataFieldIndividualsDescription",
        "entityDataFieldRowDictionaryDataFieldIndividualsDocumentation",
        filterDataRow, filterDataField, filterPrivacyRealm, searchTerms, filterShowAll);
    
    // process data rows with their fields
    List<GrouperDataRowConfig> dataRows = grouperDataEngine.retrieveGrouperDataRowsForDataFieldAndDictionary(loggedInSubject);
    
    Map<String, GrouperDataRowConfig> aliasToRowConfig = new TreeMap<>();
    for (GrouperDataRowConfig dataRowConfig : dataRows) {
      List<String> aliases = new ArrayList<>(dataRowConfig.getRowAliases());
      Collections.sort(aliases, String.CASE_INSENSITIVE_ORDER);
      aliasToRowConfig.put(aliases.get(0).toLowerCase(), dataRowConfig);
    }
    
    for (GrouperDataRowConfig dataRowConfig : aliasToRowConfig.values()) {
      
      // apply data row filter
      if (filterDataRow != null && !filterShowAll) {
        if (!dataRowConfig.getConfigId().equals(filterDataRow)) {
          // also try matching by alias
          boolean matchByAlias = false;
          for (String alias : dataRowConfig.getRowAliases()) {
            if (alias.equalsIgnoreCase(filterDataRow)) {
              matchByAlias = true;
              break;
            }
          }
          if (!matchByAlias) continue;
        }
      }
      
      // apply privacy realm filter to row
      if (filterPrivacyRealm != null && !filterShowAll) {
        if (!filterPrivacyRealm.equals(dataRowConfig.getPrivacyRealmName())) {
          continue;
        }
      }
      
      String dataRowName = String.join(", ", dataRowConfig.getRowAliases());
      
      GuiDataFieldRowDictionaryTable guiTable = new GuiDataFieldRowDictionaryTable();
      guiTable.setCanAccess(true);
      guiTable.setDataRowName(dataRowName);
      guiTable.setConfigId(dataRowConfig.getConfigId());
      guiTable.setTitle(GrouperTextContainer.textOrNull("entityDataFieldRowDictionaryDataRowLabel") + " " + dataRowName);
      guiTable.setDescription(GrouperUtil.defaultIfBlank(dataRowConfig.getDescriptionHtml(), ""));
      guiTable.setDataOwner(GrouperUtil.defaultIfBlank(dataRowConfig.getDataOwnerHtml(), ""));
      guiTable.setHowToGetAccess(GrouperUtil.defaultIfBlank(dataRowConfig.getHowToGetAccessHtml(), ""));
      guiTable.setDocumentation(GrouperUtil.defaultIfBlank(dataRowConfig.getZeroToManyExamplesHtml(), ""));
      
      List<GuiDataFieldRowDictionary> fieldItems = new ArrayList<>();
      
      for (String dataFieldConfigId : dataRowConfig.getDataFieldConfigIds()) {
        
        GrouperDataFieldConfig dataFieldConfig = grouperDataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
        if (dataFieldConfig == null) {
          LOG.error("Data row config '" + dataRowConfig.getConfigId()
              + "' references data field config id '" + dataFieldConfigId
              + "' which does not exist.");
          continue;
        }
        
        // apply data field filter
        if (filterDataField != null && !filterShowAll) {
          if (!dataFieldConfigId.equals(filterDataField)) continue;
        }
        
        // apply privacy realm filter to field
        if (filterPrivacyRealm != null && !filterShowAll) {
          String fieldRealm = dataFieldConfig.getGrouperPrivacyRealmConfigId();
          if (fieldRealm != null && !filterPrivacyRealm.equals(fieldRealm)) {
            continue;
          }
        }
        
        GuiDataFieldRowDictionary guiField = populateGuiField(grouperDataEngine, dataFieldConfig, loggedInSubject);
        guiField.setDataRowName(dataRowName);
        guiField.setDataRowConfigId(dataRowConfig.getConfigId());
        guiField.setDataRowDataOwner(GrouperUtil.defaultIfBlank(dataRowConfig.getDataOwnerHtml(), ""));
        guiField.setDataRowHowToGetAccess(GrouperUtil.defaultIfBlank(dataRowConfig.getHowToGetAccessHtml(), ""));
        
        // build JEXL snippet for row fields
        String firstAlias = dataFieldConfig.getFieldAliases().iterator().next();
        String firstRowAlias = dataRowConfig.getRowAliases().iterator().next();
        guiField.setJexlSnippet("entity.hasRow('" + firstRowAlias + "', \"" + firstAlias + "=='value'\")");
        
        // apply search text filter
        if (!searchTerms.isEmpty() && !filterShowAll) {
          if (!matchesSearch(guiField, dataRowName, searchTerms)) continue;
        }
        
        fieldItems.add(guiField);
      }
      
      if (!fieldItems.isEmpty()) {
        guiTable.setGuiDataFieldRowDictionary(fieldItems);
        result.add(guiTable);
      }
    }
    
    // process "global" data fields
    buildFieldSection(result, grouperDataEngine, loggedInSubject, "global",
        "entityDataFieldRowDictionaryDataFieldGlobalTitle",
        "entityDataFieldRowDictionaryDataFieldGlobalDescription",
        "entityDataFieldRowDictionaryDataFieldGlobalDocumentation",
        filterDataRow, filterDataField, filterPrivacyRealm, searchTerms, filterShowAll);
    
    // process "groups" data fields
    buildFieldSection(result, grouperDataEngine, loggedInSubject, "groups",
        "entityDataFieldRowDictionaryDataFieldGroupsTitle",
        "entityDataFieldRowDictionaryDataFieldGroupsDescription",
        "entityDataFieldRowDictionaryDataFieldGroupsDocumentation",
        filterDataRow, filterDataField, filterPrivacyRealm, searchTerms, filterShowAll);
    
    return result;
  }
  
  /**
   * Build a section of standalone data fields (individuals, global, or groups).
   */
  @SuppressWarnings("unchecked")
  private void buildFieldSection(List<GuiDataFieldRowDictionaryTable> result,
      GrouperDataEngine grouperDataEngine, Subject loggedInSubject, String fieldDataAssignableTo,
      String titleKey, String descKey, String docKey,
      String filterDataRow, String filterDataField, String filterPrivacyRealm, 
      List<String> searchTerms, boolean filterShowAll) {
    
    // if filtering by data row and not show-all, skip standalone field sections
    if (filterDataRow != null && !filterShowAll) {
      return;
    }
    
    MultiKey fieldsAndHasAccess = grouperDataEngine.retrieveGrouperDataFieldsForDataFieldAndDictionary(loggedInSubject, fieldDataAssignableTo);
    
    GuiDataFieldRowDictionaryTable guiTable = new GuiDataFieldRowDictionaryTable();
    guiTable.setCanAccess((Boolean) fieldsAndHasAccess.getKey(1));
    guiTable.setDataField(true);
    
    List<GuiDataFieldRowDictionary> fieldItems = new ArrayList<>();
    
    for (GrouperDataFieldConfig dataFieldConfig : (List<GrouperDataFieldConfig>) fieldsAndHasAccess.getKey(0)) {
      
      // apply privacy realm filter
      if (filterPrivacyRealm != null && !filterShowAll) {
        String fieldRealm = dataFieldConfig.getGrouperPrivacyRealmConfigId();
        if (fieldRealm != null && !filterPrivacyRealm.equals(fieldRealm)) {
          continue;
        }
      }
      
      // apply data field filter
      if (filterDataField != null && !filterShowAll) {
        if (!dataFieldConfig.getConfigId().equals(filterDataField)) continue;
      }
      
      GuiDataFieldRowDictionary guiField = populateGuiField(grouperDataEngine, dataFieldConfig, loggedInSubject);
      
      // build JEXL snippet for standalone fields
      String firstAlias = dataFieldConfig.getFieldAliases().iterator().next();
      if ("individuals".equals(fieldDataAssignableTo)) {
        guiField.setJexlSnippet("entity.hasAttribute('" + firstAlias + "')");
      } else if ("global".equals(fieldDataAssignableTo)) {
        guiField.setJexlSnippet("grouperDataEngine.getFieldValue('" + firstAlias + "')");
      } else if ("groups".equals(fieldDataAssignableTo)) {
        guiField.setJexlSnippet("group.hasAttribute('" + firstAlias + "')");
      }
      
      // apply search text filter
      if (!searchTerms.isEmpty() && !filterShowAll) {
        if (!matchesSearch(guiField, null, searchTerms)) continue;
      }
      
      fieldItems.add(guiField);
    }
    
    // remove fields that belong to data rows (they appear under their row section)
    if ("individuals".equals(fieldDataAssignableTo)) {
      List<GrouperDataRowConfig> dataRows = grouperDataEngine.retrieveGrouperDataRowsForDataFieldAndDictionary(loggedInSubject);
      Set<String> rowFieldConfigIds = new java.util.HashSet<>();
      for (GrouperDataRowConfig rowConfig : dataRows) {
        rowFieldConfigIds.addAll(rowConfig.getDataFieldConfigIds());
      }
      fieldItems.removeIf(f -> rowFieldConfigIds.contains(f.getDataFieldConfigId()));
    }
    
    if (!fieldItems.isEmpty() || guiTable.isCanAccess()) {
      guiTable.setGuiDataFieldRowDictionary(fieldItems);
      guiTable.setTitle(GrouperTextContainer.textOrNull(titleKey));
      guiTable.setDescription(GrouperTextContainer.textOrNull(descKey));
      guiTable.setDocumentation(GrouperTextContainer.textOrNull(docKey));
      result.add(guiTable);
    }
  }
  
  /**
   * Populate a GuiDataFieldRowDictionary from a GrouperDataFieldConfig.
   */
  private GuiDataFieldRowDictionary populateGuiField(GrouperDataEngine grouperDataEngine, 
      GrouperDataFieldConfig dataFieldConfig, Subject loggedInSubject) {
    
    GuiDataFieldRowDictionary guiField = new GuiDataFieldRowDictionary();
    
    String grouperPrivacyRealmConfigId = dataFieldConfig.getGrouperPrivacyRealmConfigId();
    GrouperPrivacyRealmConfig grouperPrivacyRealmConfig = grouperDataEngine.getPrivacyRealmConfigByConfigId().get(grouperPrivacyRealmConfigId);
    String highestLevelAccess = grouperDataEngine.calculateHighestLevelAccess(grouperPrivacyRealmConfig, loggedInSubject);
    
    guiField.setDataFieldConfigId(dataFieldConfig.getConfigId());
    guiField.setDataFieldAliases(String.join(", ", dataFieldConfig.getFieldAliases()));
    guiField.setDataOwner(dataFieldConfig.getDataOwnerHtml());
    guiField.setDataType(dataFieldConfig.getFieldDataType().name());
    guiField.setDescription(dataFieldConfig.getDescriptionHtml());
    guiField.setExamples(dataFieldConfig.getZeroToManyExamplesHtml());
    guiField.setHowToGetAccess(dataFieldConfig.getHowToGetAccessHtml());
    guiField.setPrivilege(highestLevelAccess);
    guiField.setPrivilegeHumanized(GuiDataFieldRowDictionary.humanizePrivilege(highestLevelAccess));
    guiField.setPrivacyRealmConfigId(grouperPrivacyRealmConfigId);
    guiField.setValueType(dataFieldConfig.getFieldDataType().name());
    guiField.setMultiValued(dataFieldConfig.isFieldMultiValued());
    
    return guiField;
  }
  
  /**
   * Check if a field matches ALL search terms (AND logic).
   * Matches against data field aliases, data row name, and description.
   */
  private boolean matchesSearch(GuiDataFieldRowDictionary field, String dataRowName, List<String> searchTerms) {
    // build searchable text from all relevant fields
    StringBuilder searchable = new StringBuilder();
    if (field.getDataFieldAliases() != null) searchable.append(field.getDataFieldAliases().toLowerCase()).append(" ");
    if (field.getDescription() != null) searchable.append(field.getDescription().toLowerCase()).append(" ");
    if (dataRowName != null) searchable.append(dataRowName.toLowerCase()).append(" ");
    if (field.getDataRowName() != null) searchable.append(field.getDataRowName().toLowerCase()).append(" ");
    if (field.getExamples() != null) searchable.append(field.getExamples().toLowerCase()).append(" ");
    if (field.getDataOwner() != null) searchable.append(field.getDataOwner().toLowerCase()).append(" ");
    if (field.getDataRowDataOwner() != null) searchable.append(field.getDataRowDataOwner().toLowerCase()).append(" ");
    
    String text = searchable.toString();
    
    for (String term : searchTerms) {
      if (!text.contains(term)) {
        return false;
      }
    }
    return true;
  }

}
