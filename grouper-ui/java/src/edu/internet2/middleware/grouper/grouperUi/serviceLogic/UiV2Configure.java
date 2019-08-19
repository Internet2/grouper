package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.grouperUi.beans.config.ConfigUtils;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigFile;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigProperty;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigSection;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ConfigurationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class UiV2Configure {

  /**
   * 
   */
  protected static Log LOG = LogFactory.getLog(UiV2Configure.class);

  /**
   * if allowed to view configuration
   * @return true if allowed to view configuration
   */
  public boolean allowedToViewConfiguration() {

    Map<String, Object> debugMap = null;
    
    if (LOG.isDebugEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "allowedToViewConfiguration");
    }
    try {
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      final boolean uiConfigurationEnabled = GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.configuration.enabled", true);
      if (debugMap != null) {
        debugMap.put("uiConfigurationEnabled", uiConfigurationEnabled);
      }
      if (!uiConfigurationEnabled) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("configurationNotEnabled")));
        return false;

      }

      final boolean configureShow = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer().isConfigureShow();

      if (debugMap != null) {
        debugMap.put("configureShowEgSysadmin", configureShow);
      }

      if (!configureShow) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("configurationNotAllowedToView")));
        return false;
      }
      
      String networks = GrouperUiConfig.retrieveConfig().propertyValueString("grouperUi.configurationEditor.sourceIpAddresses");

      if (debugMap != null) {
        debugMap.put("allowFromNetworks", networks);
      }

      if (!StringUtils.isBlank(networks)) {

        String sourceIp = GrouperUiFilter.retrieveHttpServletRequest().getRemoteAddr();
        
        if (debugMap != null) {
          debugMap.put("sourceIp", sourceIp);
        }

        Boolean allowed = null;
        
        if (allowed == null) {
          if (!StringUtils.isBlank(sourceIp) && GrouperUtil.ipOnNetworks(sourceIp, networks)) {

            if (debugMap != null) {
              debugMap.put("ipOnNetworks", true);
            }

            allowed = true;
          } else {
            if (!StringUtils.isBlank(sourceIp)) {
              if (debugMap != null) {
                debugMap.put("ipOnNetworks", false);
              }
              
            }
            allowed = false;
          }
        }
        if (debugMap != null) {
          debugMap.put("allowed", allowed);
        }
        if (allowed != Boolean.TRUE) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
              TextContainer.retrieveFromRequest().getText().get("configurationNotAllowedBySourceIp")));
          return false;
        }
      }
      
      if (debugMap != null) {
        debugMap.put("allowed", true);
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
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configureIndex.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * add config submit
   * @param request
   * @param response
   */
  public void configurationFileAddConfigSubmit(final HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      boolean success = (Boolean)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
        
        @Override
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {

          GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

          ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();

          String configFileString = request.getParameter("configFile");
          ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
          configurationContainer.setConfigFileName(configFileName);
          
          // if not sent, thats a problem
          if (StringUtils.isBlank(configFileString)) {
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#configFileSelect", 
                TextContainer.retrieveFromRequest().getText().get("configurationFileRequired")));
            return false;
          }
          
          String propertyNameString = StringUtils.trim(request.getParameter("propertyNameName"));
          if (StringUtils.isBlank(propertyNameString)) {
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#propertyNameId", 
                TextContainer.retrieveFromRequest().getText().get("configurationFilesAddEntryPropertyNameRequired")));
            return false;
          }
          
          if (propertyNameString.endsWith(".elConfig")) {
            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#propertyNameId", 
                TextContainer.retrieveFromRequest().getText().get("configurationFilesAddEntryPropertyNameElConfig")));
            return false;
          }
          
          String expressionLanguageString = request.getParameter("expressionLanguageName");
          if (StringUtils.isBlank(expressionLanguageString)) {
            throw new RuntimeException("Expression language should never be null!");
          }
          boolean isExpressionLanguage = GrouperUtil.booleanValue(expressionLanguageString);

          String propertyNameToUse = isExpressionLanguage ? (propertyNameString + ".elConfig") : propertyNameString;
          
          String valueString = request.getParameter("valueName");
          // this could be blank I guess
//          if (StringUtils.isBlank(valueString)) {
//            guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#valueId", 
//                TextContainer.retrieveFromRequest().getText().get("configurationFilesAddEntryPropertyValueRequired")));
//            return false;
//          }

          String passwordString = request.getParameter("passwordName");
          
          // if not sent, thats a problem
          if (StringUtils.isBlank(passwordString)) {
            throw new RuntimeException("Why is password blank!");
          }
          
          // is this a password or not?
          boolean isPassword = GrouperUtil.booleanValue(passwordString);

          if (isPassword) {
            valueString = request.getParameter("passwordValueName");
          }
          
          StringBuilder message = new StringBuilder();
          
          GrouperConfigHibernate[] grouperConfigHibernateToReturn = new GrouperConfigHibernate[1];

          // standard validation
          if (!ConfigUtils.validateConfigEdit(configurationContainer, configFileName, propertyNameString, 
              valueString, isExpressionLanguage, message, grouperConfigHibernateToReturn)) {
            
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message.toString()));

            return false;
          }
          
          GrouperConfigHibernate grouperConfigHibernate = grouperConfigHibernateToReturn[0];

          // see if we are creating a new one
          if (grouperConfigHibernate == null) {
            grouperConfigHibernate = new GrouperConfigHibernate();
          }

          grouperConfigHibernate.setConfigEncrypted(false);
          grouperConfigHibernate.setConfigFileHierarchyDb("INSTITUTION");
          grouperConfigHibernate.setConfigFileNameDb(configFileName.getConfigFileName());
          // this will switch to or from .elConfig
          grouperConfigHibernate.setConfigKey(propertyNameToUse);
          
          boolean isValueEncrypted = ConfigUtils.isPassword(configFileName, null, propertyNameString, valueString, true, isPassword);
          
          if (isValueEncrypted) {
            grouperConfigHibernate.setConfigEncrypted(true);
            valueString = Morph.encrypt(valueString);
          }
          grouperConfigHibernate.setConfigValue(valueString);
          grouperConfigHibernate.saveOrUpdate();

          message.append(TextContainer.retrieveFromRequest().getText().get("configurationFilesAdded")).append("<br />");

//          # when property is in another file
//          configurationFilesEdited = Success: the property existed and was changed and saved
//
//          # when property is in another file
//          configurationFilesEditedNotChanged = Note: the property existed and the value was not changed

          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, message.toString()));
          
          String valueForAudit = isValueEncrypted ? GuiConfigProperty.ESCAPED_PASSWORD : grouperConfigHibernate.getConfigValueDb();
          
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.CONFIGURATION_ADD, "id", 
              grouperConfigHibernate.getId(), "configFile", grouperConfigHibernate.getConfigFileNameDb(), 
              "key", grouperConfigHibernate.getConfigKey(), "value", 
              valueForAudit, "configHierarchy", grouperConfigHibernate.getConfigFileHierarchyDb());
          auditEntry.setDescription("Add config entry: " + grouperConfigHibernate.getConfigFileNameDb() 
            + ", " + grouperConfigHibernate.getConfigKey() + " = " + valueForAudit);
          auditEntry.saveOrUpdate(true);

          guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
              "/WEB-INF/grouperUi2/configure/configurationFileAddEntry.jsp"));

          return true;
        }
      });
      
      if (success) {
        ConfigPropertiesCascadeBase.clearCache();
      }

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * add config
   * @param request
   * @param response
   */
  public void configurationFileAddConfig(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();

      
      String configFileString = request.getParameter("configFile");
      ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
      configurationContainer.setConfigFileName(configFileName);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configurationFileAddEntry.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * delete config
   * @param request
   * @param response
   */
  public void configurationFileItemDelete(final HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();

      
      final String configFileString = request.getParameter("configFile");
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
      configurationContainer.setConfigFileName(configFileName);

      if (StringUtils.isBlank(configFileString)) {
      
        throw new RuntimeException("configFile is not being sent!");

      }

      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
        
        @Override
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {

          GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

          ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();

          String configFileString = request.getParameter("configFile");
          ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
          configurationContainer.setConfigFileName(configFileName);
          
          // if not sent, thats a problem
          if (StringUtils.isBlank(configFileString)) {
            throw new RuntimeException("Config file name does not exist");
          }
          
          String propertyNameString = StringUtils.trim(request.getParameter("propertyNameName"));
          if (StringUtils.isBlank(propertyNameString)) {
            throw new RuntimeException("Property name does not exist");
          }
          
          propertyNameString = GrouperUtil.stripEnd(propertyNameString, ".elConfig");
          
          Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(configFileName, null, propertyNameString);
          Set<GrouperConfigHibernate> grouperConfigHibernatesEl = GrouperDAOFactory.getFactory().getConfig().findAll(null, null, propertyNameString + ".elConfig");
          
          GrouperConfigHibernate grouperConfigHibernate = null;
          GrouperConfigHibernate grouperConfigHibernateEl = null;
          
          for (GrouperConfigHibernate current : GrouperUtil.nonNull(grouperConfigHibernates)) {
            if (configFileName.getConfigFileName().equals(current.getConfigFileNameDb()) && "INSTITUTION".equals(current.getConfigFileHierarchyDb())) {
              if (grouperConfigHibernate != null) {
                // why are there two???
                LOG.error("Why are there two configs in db with same key and config file???? " + current.getConfigFileNameDb() 
                  + ", " + current.getConfigKey() + ", " + current.getConfigValue());
                current.delete();
                configurationFileItemDeleteHelper(current, configFileName);
              }
              grouperConfigHibernate = current;
            }
          }
          
          for (GrouperConfigHibernate current : GrouperUtil.nonNull(grouperConfigHibernatesEl)) {
            if (configFileName.getConfigFileName().equals(current.getConfigFileNameDb()) && "INSTITUTION".equals(current.getConfigFileHierarchyDb())) {
              if (grouperConfigHibernate != null) {
                // why are there two???
                LOG.error("Why are there two configs in db with same key and config file???? " + current.getConfigFileNameDb() 
                  + ", " + current.getConfigKey() + ", " + current.getConfigValue());
                current.delete();
              }
              grouperConfigHibernateEl = current;
            }
          }

          if (grouperConfigHibernate != null) {
            configurationFileItemDeleteHelper(grouperConfigHibernate, configFileName);
          }
          if (grouperConfigHibernateEl != null) {
            configurationFileItemDeleteHelper(grouperConfigHibernateEl, configFileName);
          }

          return true;
        }
      });
      
      ConfigPropertiesCascadeBase.clearCache();
      
      buildConfigFileAndMetadata();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", "/WEB-INF/grouperUi2/configure/configure.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }

  /**
   * configuration file item delete helper
   * @param grouperConfigHibernate
   * @param configFileName 
   */
  private void configurationFileItemDeleteHelper(GrouperConfigHibernate grouperConfigHibernate, 
      ConfigFileName configFileName) {

    // see if we are creating a new one
    if (grouperConfigHibernate == null) {
      return;
    }

    grouperConfigHibernate.delete();
    
    boolean isValueEncrypted = ConfigUtils.isPassword(configFileName, null, grouperConfigHibernate.getConfigKey(), 
        grouperConfigHibernate.getConfigValueDb(), true, grouperConfigHibernate.isConfigEncrypted());

    String valueForAudit = isValueEncrypted ? GuiConfigProperty.ESCAPED_PASSWORD : grouperConfigHibernate.getConfigValueDb();

    AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.CONFIGURATION_DELETE, "id", 
        grouperConfigHibernate.getId(), "configFile", grouperConfigHibernate.getConfigFileNameDb(), 
        "key", grouperConfigHibernate.getConfigKey(), "previousValue", 
        valueForAudit, 
            "configHierarchy", grouperConfigHibernate.getConfigFileHierarchyDb());
    auditEntry.setDescription("Delete config entry: " + grouperConfigHibernate.getConfigFileNameDb() 
      + ", " + grouperConfigHibernate.getConfigKey() + " = " + valueForAudit);
    auditEntry.saveOrUpdate(true);

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        
    guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
        TextContainer.retrieveFromRequest().getText().get("configurationFilesDeleted")));
  }
  
  /**
   * configure
   * @param request
   * @param response
   */
  public void configure(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
      
      String configFileString = request.getParameter("configFile");
      ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
      configurationContainer.setConfigFileName(configFileName);

      if (!StringUtils.isBlank(configFileString)) {
      
        buildConfigFileAndMetadata();

      }

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configure.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
      
  public void configurationFileSelectPassword(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String passwordString = request.getParameter("passwordName");
      
      // if not sent, thats a problem
      if (StringUtils.isBlank(passwordString)) {
        throw new RuntimeException("Why is password blank!");
      }
      
      // is this a password or not?
      boolean isPassword = GrouperUtil.booleanValue(passwordString);

      // clear out values
      guiResponseJs.addAction(GuiScreenAction.newScript("$('#passwordValueId').val('');"));
      guiResponseJs.addAction(GuiScreenAction.newFormFieldValue("valueName", null));

      // we need to hide or show the password and value
      if (isPassword) {
        guiResponseJs.addAction(GuiScreenAction.newScript("$('#passwordValueDivId').show('slow')"));
        guiResponseJs.addAction(GuiScreenAction.newScript("$('#valueDivId').hide('slow')"));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newScript("$('#passwordValueDivId').hide('slow')"));
        guiResponseJs.addAction(GuiScreenAction.newScript("$('#valueDivId').show('slow')"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  
  /**
   * configure
   * @param request
   * @param response
   */
  public void configureSelectFile(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
      
      {
        String configFileString = request.getParameter("configFile");
        ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
        configurationContainer.setConfigFileName(configFileName);
        
        // if not sent, thats a problem
        if (StringUtils.isBlank(configFileString)) {
          guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#configFileSelect", 
              TextContainer.retrieveFromRequest().getText().get("configurationFileRequired")));
          return;
        }
        
        buildConfigFileAndMetadata();
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configure.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * edit config
   * @param request
   * @param response
   */
  public void configurationFileItemEdit(final HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
  
      
      final String configFileString = request.getParameter("configFile");
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
      configurationContainer.setConfigFileName(configFileName);
  
      if (StringUtils.isBlank(configFileString)) {
      
        throw new RuntimeException("configFile is not being sent!");
  
      }
  
      String propertyNameString = StringUtils.trim(request.getParameter("propertyNameName"));
      if (StringUtils.isBlank(propertyNameString)) {
        throw new RuntimeException("Property name does not exist");
      }
          
      Integer index = GrouperUtil.intObjectValue(request.getParameter("index"), true);
      if (StringUtils.isBlank(propertyNameString)) {
        throw new RuntimeException("Index does not exist");
      }
      // hide existing edit forms
      guiResponseJs.addAction(GuiScreenAction.newScript("$('.configFormRow').hide('slow');"));
      
      // add a row
      guiResponseJs.addAction(GuiScreenAction.newScript("$('#row_" + index + "').after(\"<tr class='configFormRow' id='configFormRowId_" + index + "'></tr>\");"));

      // replace the inner html of the row with this jsp
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#configFormRowId_" + index , "/WEB-INF/grouperUi2/configure/configurationFileEditEntry.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * 
   */
  private static void buildConfigFileAndMetadata() {
    
    ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
    
    ConfigFileName configFileName = configurationContainer.getConfigFileName();
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configurationContainer.getConfig();
    
    ConfigFileMetadata configFileMetadata = configFileName.configFileMetadata();
 
    List<ConfigSectionMetadata> configSectionMetadatas = configFileMetadata.getConfigSectionMetadataList();
    
    GuiConfigFile guiConfigFile = new GuiConfigFile();
    
    guiConfigFile.setConfigPropertiesCascadeBase(configPropertiesCascadeBase);

    // keep track of all properties so we can collate them with ad hoc properties
    Map<String, GuiConfigProperty> keyNameToGuiProperty = new HashMap<String, GuiConfigProperty>();

    // keep track of metadata vs gui objects so we can walk backward for ad hoc properties
    Map<ConfigSectionMetadata, GuiConfigSection> sectionMetadataToSection = new HashMap<ConfigSectionMetadata, GuiConfigSection>();
    
    Set<GrouperConfigHibernate> grouperConfigHibernateSet = Hib3DAOFactory.getFactory().getConfig().findAll(configFileName, null, null);
    
    Map<String, GrouperConfigHibernate> grouperConfigHibernateMap = new HashMap<String, GrouperConfigHibernate>();
    
    for (GrouperConfigHibernate grouperConfigHibernate : GrouperUtil.nonNull(grouperConfigHibernateSet)) {
      grouperConfigHibernateMap.put(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate);
    }
    
    for (ConfigSectionMetadata configSectionMetadata : configSectionMetadatas) {
      
      GuiConfigSection guiConfigSection = new GuiConfigSection();
      guiConfigSection.setGuiConfigFile(guiConfigFile);
      guiConfigSection.setConfigSectionMetadata(configSectionMetadata);
      sectionMetadataToSection.put(configSectionMetadata, guiConfigSection);
      
      for (ConfigItemMetadata configItemMetadata : configSectionMetadata.getConfigItemMetadataList()) {
        
        GuiConfigProperty guiConfigProperty = new GuiConfigProperty();
        keyNameToGuiProperty.put(configItemMetadata.getKeyOrSampleKey(), guiConfigProperty);
        guiConfigProperty.setGuiConfigSection(guiConfigSection);
        guiConfigProperty.setConfigItemMetadata(configItemMetadata);
        guiConfigSection.getGuiConfigProperties().add(guiConfigProperty);
        
        // see if the current database item has an encrypted value
        GrouperConfigHibernate grouperConfigHibernate = grouperConfigHibernateMap.get(configItemMetadata.getKeyOrSampleKey());
        if (grouperConfigHibernate != null && grouperConfigHibernate.isConfigEncrypted()) {
          guiConfigProperty.setEncryptedInDatabase(true);
        }
      }
      
      guiConfigFile.getGuiConfigSections().add(guiConfigSection);
    }
    
    configurationContainer.setGuiConfigFile(guiConfigFile);
    
    boolean addedRemainingConfigSection = false;
    
    Set<String> propertyNames = configPropertiesCascadeBase.propertyNames();
    
    for (String propertyName : propertyNames) {
      if (keyNameToGuiProperty.containsKey(propertyName)) {
        continue;
      }
      
      // lets add one
      GuiConfigProperty guiConfigProperty = new GuiConfigProperty();
      {
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setKey(propertyName);
        
        guiConfigProperty.setConfigItemMetadata(configItemMetadata);
      }
      
      // lets find a section
      boolean foundSection = false;
      OUTER: for (ConfigSectionMetadata configSectionMetadata : configSectionMetadatas) {
        for (ConfigItemMetadata configItemMetadata : configSectionMetadata.getConfigItemMetadataList()) {
          if (!StringUtils.isBlank(configItemMetadata.getRegex())) {
            Pattern pattern = Pattern.compile(configItemMetadata.getRegex());
            Matcher matcher = pattern.matcher(propertyName);
            if (matcher.matches()) {
              GuiConfigSection guiConfigSection = sectionMetadataToSection.get(configSectionMetadata);
              guiConfigSection.getGuiConfigProperties().add(guiConfigProperty);
              guiConfigProperty.setGuiConfigSection(guiConfigSection);
              foundSection = true;
              break OUTER;
            }
          }
        }
      }
      
      if (!foundSection) {
        if (!addedRemainingConfigSection) {
          
          GuiConfigSection guiConfigSection = new GuiConfigSection();
          guiConfigSection.setGuiConfigFile(guiConfigFile);
          List<GuiConfigProperty> guiConfigProperties = new ArrayList<GuiConfigProperty>();
          guiConfigSection.setGuiConfigProperties(guiConfigProperties);
          ConfigSectionMetadata configSectionMetadata = new ConfigSectionMetadata();
          guiConfigSection.setConfigSectionMetadata(configSectionMetadata);
          configSectionMetadata.setTitle("Remaining config");
          configSectionMetadata.setComment("Any configuration not in another section");
          addedRemainingConfigSection = true;
          guiConfigFile.getGuiConfigSections().add(guiConfigSection);
        }
        
        GuiConfigSection guiConfigSection = guiConfigFile.getGuiConfigSections().get(guiConfigFile.getGuiConfigSections().size()-1);
        guiConfigSection.getGuiConfigProperties().add(guiConfigProperty);
        guiConfigProperty.setGuiConfigSection(guiConfigSection);
        
      }
    }
  }  
}
