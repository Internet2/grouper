package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigFile;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigProperty;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigSection;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiPITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
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
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAOFactory;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.GrouperPagingTag2;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;
import edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic;
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
      
      String networks = GrouperUiConfig.retrieveConfig().propertyValueString("grouperUi.configurationEditor.sourceIpAddresses", "127.0.0.1/32");

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
   * edit config submit
   * @param request
   * @param response
   */
  public void configurationFileItemEditSubmit(final HttpServletRequest request, HttpServletResponse response) {
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    boolean success = configurationFileAddEditHelper(request, response);
        
    if (success) {
      buildConfigFileAndMetadata(null, null);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configure.jsp"));

    }

  }

  /**
   * add config submit
   * @param request
   * @param response
   */
  public void configurationFileAddConfigSubmit(final HttpServletRequest request, HttpServletResponse response) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    boolean success = configurationFileAddEditHelper(request, response);
        
    if (success) {
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
        "/WEB-INF/grouperUi2/configure/configurationFileAddEntry.jsp"));
    }

  }

  /**
   * 
   * @param request
   * @param response
   * @return true if success
   */
  private boolean configurationFileAddEditHelper(final HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return false;
      }
      
      boolean success = (Boolean)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
        
        @Override
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {

          String configFileString = request.getParameter("configFile");
          String propertyNameString = StringUtils.trim(request.getParameter("propertyNameName"));
          String expressionLanguageString = request.getParameter("expressionLanguageName");
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
          
          Boolean[] added = new Boolean[1];
          Boolean[] error = new Boolean[1];
          
          ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, true);
          ConfigFileMetadata configFileMetadata = configFileName.configFileMetadata();

          boolean result = configurationFileAddEditHelper2(configFileName, configFileString, configFileMetadata, propertyNameString,
              expressionLanguageString, valueString, isPassword, message, added, error, true, null);

          GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

          guiResponseJs.addAction(GuiScreenAction.newMessage((added[0] != null && error[0] == null) ? GuiMessageType.success : ((error[0] == null || !error[0]) ? GuiMessageType.info : GuiMessageType.error), message.toString()));
          ConfigPropertiesCascadeBase.clearCache();
          GrouperUiApiTextConfig.clearCache();

          return result;
        }
      });
      return success;
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
   * import config
   * @param request
   * @param response
   */
  public void configurationFileImport(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configurationFileImport.jsp"));
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

  }
  
  /**
   * export config
   * @param request
   * @param response
   */
  public void configurationFileExport(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (!allowedToViewConfiguration()) {
        return;
      }
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
      
      final String configFileString = request.getParameter("configFile");
      ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
      configurationContainer.setConfigFileName(configFileName);
      
      if (StringUtils.isBlank(configFileString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#configFileSelect", 
            TextContainer.retrieveFromRequest().getText().get("configurationFileRequired")));
        return;
      }
      
      
      StringBuilder contents = new StringBuilder();
      
      Set<GrouperConfigHibernate> grouperConfigHibernateSet = Hib3DAOFactory.getFactory().getConfig().findAll(configFileName, null, null);
      
      Map<String, GrouperConfigHibernate> grouperConfigHibernateMap = new HashMap<String, GrouperConfigHibernate>();
      
      for (GrouperConfigHibernate grouperConfigHibernate : GrouperUtil.nonNull(grouperConfigHibernateSet)) {
        grouperConfigHibernateMap.put(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate);
      }
      
      Map<String, String> properties = new TreeMap<String, String>(ConfigDatabaseLogic.retrieveConfigMap(configFileName.getConfigFileName()));
      
      for (String property: properties.keySet()) {
        
        String value = properties.get(property);
        if (grouperConfigHibernateMap.containsKey(property) &&
            grouperConfigHibernateMap.get(property) != null && grouperConfigHibernateMap.get(property).isConfigEncrypted()) {
          value = "*******";
        }
        value = GrouperUtil.replace(value, "\r\n", "\n");
        value = GrouperUtil.replace(value, "\r", "\n");
        value = GrouperUtil.replace(value, "\n", "\\\n");
        
        contents.append(property + " = " + value);
        contents.append("\n");
      }
      
      response.setContentType("application/octet-stream");
      response.setHeader ("Content-Disposition", "inline;filename=\"" + configFileName.getConfigFileName() + "\"");
      
      try {
        PrintWriter out = response.getWriter();
        out.write(contents.toString());
        out.close();
      } catch (IOException e) {
        throw new RuntimeException("Error occured while writing response");
      }
      
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
      
      
      final String configFileString = request.getParameter("configFile");

      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();

      ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);

      configurationContainer.setConfigFileName(configFileName);

      String propertyNameString = StringUtils.trim(request.getParameter("propertyNameName"));
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String result = DbConfigEngine.configurationFileItemDeleteHelper(configFileString, propertyNameString, true, true);
      
      if (!StringUtils.isBlank(result)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info, 
            result));
      }
      buildConfigFileAndMetadata(null, null);
      
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
  public static void configurationFileItemDeleteHelper(GrouperConfigHibernate grouperConfigHibernate, 
      ConfigFileName configFileName, boolean fromUi) {
  
    ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
    
    configurationContainer.setConfigFileName(configFileName);

    String message = DbConfigEngine.configurationFileItemDeleteHelper(grouperConfigHibernate, configFileName, fromUi);
    if (!StringUtils.isBlank(message) && fromUi) {
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          message));
  
    }              
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
      
        buildConfigFileAndMetadata(null, null);

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
   * filter values
   * @param request
   * @param response
   */
  public void configureFilterSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
      
      String configFileString = request.getParameter("configFile");
      ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
      configurationContainer.setConfigFileName(configFileName);
      
      // if not sent, thats a problem
      if (StringUtils.isBlank(configFileString)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, "#configFileSelect", 
            TextContainer.retrieveFromRequest().getText().get("configurationFileRequired")));
        return;
      }
      
      String filter = request.getParameter("filter");
      String configSource = request.getParameter("configSource");
      
      buildConfigFileAndMetadata(filter.trim(), configSource);
      
      configurationContainer.setFilter(filter.trim());
      configurationContainer.setConfigSource(configSource);
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configure.jsp"));
    
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
        
        buildConfigFileAndMetadata(null, null);
        
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
      configurationContainer.setCurrentConfigPropertyName(propertyNameString);

      buildConfigFileAndMetadata(null, null);
      
      GuiConfigFile guiConfigFile = configurationContainer.getGuiConfigFile();
      
      GuiConfigProperty guiConfigProperty = guiConfigFile.findGuiConfigProperty(propertyNameString, true);

      configurationContainer.setCurrentGuiConfigProperty(guiConfigProperty);
      
      Integer index = GrouperUtil.intObjectValue(request.getParameter("index"), true);
      if (StringUtils.isBlank(propertyNameString)) {
        throw new RuntimeException("Index does not exist");
      }
      // hide existing edit forms
      guiResponseJs.addAction(GuiScreenAction.newScript("$('.configFormRow').hide('slow');$('.configFormRow').remove()"));
      
      // add a row
      guiResponseJs.addAction(GuiScreenAction.newScript("$('#row_" + index + "').after(\"<tr class='configFormRow' id='configFormRowId_" + index + "'></tr>\");"));

      // replace the inner html of the row with this jsp
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#configFormRowId_" + index , "/WEB-INF/grouperUi2/configure/configurationFileEditEntry.jsp"));
    
      //guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#row_" + index + "');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }
  
  /**
   * see the history of db configs
   * @param request
   * @param response
   */
  public void history(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      historyHelper(request);
    
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  private void historyHelper(final HttpServletRequest request) {
    
    ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
    
    String filter = request.getParameter("filter");
    if (StringUtils.isNotBlank(filter)) {
      configurationContainer.setFilter(filter.trim());
    } else {
      filter = "";
    }
    
    GuiPaging guiPaging = configurationContainer.getGuiPaging();
    QueryOptions queryOptions = new QueryOptions();

    GrouperPagingTag2.processRequest(request, guiPaging, queryOptions);
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    List<PITGrouperConfigHibernate> pitConfigs = Hib3DAOFactory.getFactory().getPITConfig().findPITConfigs(queryOptions, filter.trim());
    
    List<GuiPITGrouperConfigHibernate> guiPitConfigs = GuiPITGrouperConfigHibernate.convertFromPITGrouperConfigsHibernate(pitConfigs);
    
    configurationContainer.setGuiPitConfigs(guiPitConfigs);
    
    guiPaging.setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
        "/WEB-INF/grouperUi2/configure/history.jsp"));
  }
  
  /**
   * search the history of db configs
   * @param request
   * @param response
   */
//  public void historyFilterSubmit(final HttpServletRequest request, final HttpServletResponse response) {
//    
//    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
//    
//    GrouperSession grouperSession = null;
//  
//    try {
//  
//      grouperSession = GrouperSession.start(loggedInSubject);
//  
//      if (!allowedToViewConfiguration()) {
//        return;
//      }
//      
//      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
//  
//      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
//      
//      String filter = request.getParameter("filter");
//      
//      configurationContainer.setFilter(filter.trim());
//      
//      List<PITGrouperConfigHibernate> pitConfigs = Hib3DAOFactory.getFactory().getPITConfig().findPITConfigs(null, filter.trim());
//      
//      List<GuiPITGrouperConfigHibernate> guiPitConfigs = GuiPITGrouperConfigHibernate.convertFromPITGrouperConfigsHibernate(pitConfigs);
//      
//      configurationContainer.setGuiPitConfigs(guiPitConfigs);
//      
//      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
//          "/WEB-INF/grouperUi2/configure/history.jsp"));
//      
//    } finally {
//      GrouperSession.stopQuietly(grouperSession);
//    }
//  }
  
  /**
   * revert configs to old values
   * @param request
   * @param response
   */
  public void revertConfigValues(final HttpServletRequest request, final HttpServletResponse response) {
   
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      final Set<String> pitIds = new HashSet<String>();
      
      for (int i=0;i<1000;i++) {
        String pitId = request.getParameter("configHistoryRow_" + i + "[]");
        if (!StringUtils.isBlank(pitId)) {
          pitIds.add(pitId);
        }
      }
  
      if (pitIds.size() == 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("configurationHistoryRevertNoPropertiesSelects")));
        return;
      }
      
      StringBuilder message = new StringBuilder();
      List<String> errorsToDisplay = new ArrayList<String>();
      Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
      
      Hib3DAOFactory.getFactory().getPITConfig().revertConfigs(pitIds, message, errorsToDisplay, validationErrorsToDisplay);
      
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
      
      historyHelper(request);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("configurationHistoryRevertSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * import config submit
   * @param request
   * @param response
   */
  public void configurationFileImportSubmit(final HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    StringBuilder message = new StringBuilder();

    ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();

    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      if (!allowedToViewConfiguration()) {
        return;
      }
      
      GrouperRequestWrapper grouperRequestWrapper = (GrouperRequestWrapper)request;
      
      if (!grouperRequestWrapper.isMultipart()) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupImportUploadFile",
            TextContainer.retrieveFromRequest().getText().get("configurationFilesImportFileRequired")));
        return;
      }
      
      FileItem importConfigFile = grouperRequestWrapper.getParameterFileItem("importConfigFile");

      if (importConfigFile == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupImportUploadFile",
            TextContainer.retrieveFromRequest().getText().get("configurationFilesImportFileRequired")));
        return;
      }

      String fileName = StringUtils.defaultString(importConfigFile == null ? "" : importConfigFile.getName());

      // GRP-2836: Configuration file upload fails on browsers that include full file path
      if (fileName.contains("/")) {
        fileName = GrouperUtil.prefixOrSuffix(fileName, "/", false);
      }
      if (fileName.contains("\\")) {
        fileName = GrouperUtil.prefixOrSuffix(fileName, "\\", false);
      }
      ConfigFileName configFileName = null;
      
      try {
        configFileName = ConfigFileName.valueOfIgnoreCase(fileName, false);
      } catch (Exception e) {
        // ignore
      }
      
      if (configFileName == null) {
        
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, 
            "#groupImportUploadFile",
            TextContainer.retrieveFromRequest().getText().get("configurationFilesImportFileNotValid")));
        return;
      }
      
      Reader reader = null;
      Properties propertiesToImport = new Properties();
      
      // load properties from file
      try {
        reader = new InputStreamReader(importConfigFile.getInputStream());
        
        propertiesToImport.load(reader);

      } catch (Exception e) {
        throw new RuntimeException("Cant process config import: '" + fileName + "'", e);
      } finally {
        GrouperUtil.closeQuietly(reader);
      }
      
      configurationfileImportSubmitHelper(message, configFileName, propertiesToImport, true);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    buildConfigFileAndMetadata(null, null);

    guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Configure.configure&configFile=" + configurationContainer.getConfigFileName().name() + "')"));

    boolean success = configurationContainer.getCountSuccess() > 0 && configurationContainer.getCountWarning() == 0 && configurationContainer.getCountError() == 0;
    boolean error = configurationContainer.getCountWarning() > 0 || configurationContainer.getCountError() > 0;

    guiResponseJs.addAction(GuiScreenAction.newMessage(success ? GuiMessageType.success : (error ? GuiMessageType.error : GuiMessageType.info), message.toString()));

  }

  /**
   * @param message
   * @param configFileName
   * @param propertiesToImport
   * @param fromUi true if from UI false if not from UI
   */
  public static void configurationfileImportSubmitHelper(StringBuilder message,
      ConfigFileName configFileName, Properties propertiesToImport, boolean fromUi) {
    
    boolean newlyAssignedUseStaticRequestContainer = false;
    if (!fromUi) {
      newlyAssignedUseStaticRequestContainer = GrouperRequestContainer.assignUseStaticRequestContainer(true);
    }
    try {
  
      ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
      
      configurationContainer.setConfigFileName(configFileName);
      
      int countAdded = 0;
      int countUpdated = 0;
      int countProperties = 0;
      int countSuccess = 0;
      int countUnchanged = 0;
      int countError = 0;
      int countWarning = 0;
  
      Boolean[] added = new Boolean[1];
      Boolean[] error = new Boolean[1];
      
      ConfigFileMetadata configFileMetadata = configFileName.configFileMetadata();
      
      for (Object keyObject : propertiesToImport.keySet()) {
        try {
        
          countProperties++;
          String key = (String)keyObject;
          String value = propertiesToImport.getProperty(key);
   
          configurationFileAddEditHelper2(configFileName, configFileName.name(), configFileMetadata, key, Boolean.toString(key.endsWith(".elConfig")), value, null, message, added, error, fromUi, null);
          
          // added (first index) will be true if added, false if updated, and null if no change
          if (added[0] == null) {
            countUnchanged++;
          } else if (added[0]) {
            countAdded++;
          } else {
            countUpdated++;
          }
          
          //fatalError true if fatal error, false if non fatal error, null if no error
          if (error[0] == null) {
            if (added[0] != null) {
              countSuccess++;
            }
          } else if (error[0]) {
            countError++;
          } else {
            countWarning++;
          }
        } catch (Exception e) {
          final String errorHeader = "Error in import for key '" + keyObject + "': " + configFileName.getConfigFileName();
          LOG.error(errorHeader, e);
          message.append(errorHeader + "\n" + ExceptionUtils.getFullStackTrace(e));
          countError++;
        }
        
      }
  
      configurationContainer.setCountAdded(countAdded);
      configurationContainer.setCountUpdated(countUpdated);
      configurationContainer.setCountProperties(countProperties);
      configurationContainer.setCountSuccess(countSuccess);
      configurationContainer.setCountUnchanged(countUnchanged);
      configurationContainer.setCountError(countError);
      configurationContainer.setCountWarning(countWarning);
  
      // put this at the beginning
      message.insert(0, TextContainer.retrieveFromRequest().getText().get("configurationFilesImportSummary") + "<br />");
  
      // replace newlines
      if (!fromUi) {
        String newMessage = message.toString().replace("<br />", "\n");
        message.setLength(0);
        message.append(newMessage);
      }
      
      ConfigPropertiesCascadeBase.clearCache();
      GrouperUiApiTextConfig.clearCache();

    } finally {
      if (newlyAssignedUseStaticRequestContainer && !fromUi) {
        GrouperRequestContainer.assignUseStaticRequestContainer(false);
      }
    }
      
  }

  /**
   * 
   * @param configFileString
   * @param propertyNameString
   * @param expressionLanguageString
   * @param valueString
   * @param userSelectedPassword
   * @param message
   * @param added (first index) will be true if added, false if updated, and null if no change
   * @param error true if fatal error, false if warning, null if no error
   * @param fromUi true if from UI false if not from UI
   * @param comment notes about settings
   * @return true if ok, false if not
   */
  public static boolean configurationFileAddEditHelper2(ConfigFileName configFileName, String configFileString, ConfigFileMetadata configFileMetadata, String propertyNameString,
      String expressionLanguageString, String valueString, Boolean userSelectedPassword,
      StringBuilder message, Boolean[] added, Boolean[] error, boolean fromUi, String comment) {

    GuiResponseJs guiResponseJs = fromUi ? GuiResponseJs.retrieveGuiResponseJs() : null;
    
    ConfigurationContainer configurationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer();
    
    configurationContainer.setConfigFileName(configFileName);

    List<String> errorsToDisplay = new ArrayList<String>();
    Map<String, String> validationErrorsToDisplay = new LinkedHashMap<String, String>();
    
    boolean result = DbConfigEngine.configurationFileAddEditHelper2(configFileName, configFileString, configFileMetadata, propertyNameString, 
        expressionLanguageString, valueString, userSelectedPassword, message, added, error, 
        fromUi, comment, errorsToDisplay, validationErrorsToDisplay, true);
    
    if (fromUi) {
      for (String errorToDisplay: errorsToDisplay) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, errorToDisplay));
      }
      for (String validationKey: validationErrorsToDisplay.keySet()) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
            validationErrorsToDisplay.get(validationKey)));
      }
    }
    return result;
  } 
  
  
  
  private static boolean includeProperty(String filter, String configSource, GuiConfigProperty guiConfigProperty) {
    
    String sourceOfValue = guiConfigProperty.getValueFromWhere();
    
    if (StringUtils.isBlank(filter) && StringUtils.isBlank(configSource)) {
      return true;
    }
    
    if (StringUtils.isNotBlank(configSource)) {
      if (configSource.equals("nonBase") && (sourceOfValue == null || sourceOfValue.endsWith("base.properties")) ) {
        return false;
      }
      
      if (configSource.equals("db") && (sourceOfValue == null || !sourceOfValue.equals("database")) ) {
        return false;
      }
    }
    
    ConfigItemMetadata configItemMetadata = guiConfigProperty.getConfigItemMetadata();
    
    boolean found = false;
    
    if (configItemMetadata.getKeyOrSampleKey().toLowerCase().contains(filter.toLowerCase())) {
      found = true;
    }
    
    if (!found && guiConfigProperty.getPropertyValue() != null && guiConfigProperty.getPropertyValue().toLowerCase().contains(filter.toLowerCase())) {
      found = true;
    }
    
    if (!found && configItemMetadata.getComment() != null && configItemMetadata.getComment().toLowerCase().contains(filter.toLowerCase())) {
      found = true;
    }
    
    return found;
  }
  
  /**
   * 
   */
  private static void buildConfigFileAndMetadata(String filter, String configSource) {
    
    // get the latest and greatest
    ConfigPropertiesCascadeBase.clearCache();
    GrouperUiApiTextConfig.clearCache();
    
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
        
        if (!includeProperty(filter, configSource, guiConfigProperty) ) {
          continue;
        }
        
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
    
    if (!configFileName.isUseBaseForConfigFileMetadata()) {
      return;
    }
    
    Set<String> propertyNames = configPropertiesCascadeBase.propertyNames();
    
    lbl: for (String propertyName : propertyNames) {
      if (keyNameToGuiProperty.containsKey(propertyName)) {
        continue;
      }
      
      // lets add one
      GuiConfigProperty guiConfigProperty = new GuiConfigProperty();
      {
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setKey(propertyName);

        GrouperConfigHibernate grouperConfigHibernate = grouperConfigHibernateMap.get(configItemMetadata.getKeyOrSampleKey());
        if (grouperConfigHibernate != null && grouperConfigHibernate.isConfigEncrypted()) {
          guiConfigProperty.setEncryptedInDatabase(true);
        }
        
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
              guiConfigProperty.setGuiConfigSection(guiConfigSection);
              
              if (!includeProperty(filter, configSource, guiConfigProperty) ) {
                continue lbl;
              }
              
              guiConfigSection.getGuiConfigProperties().add(guiConfigProperty);
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
          //TODO externalize
          configSectionMetadata.setTitle("Remaining config");
          configSectionMetadata.setComment("Any configuration not in another section");
          addedRemainingConfigSection = true;
          guiConfigFile.getGuiConfigSections().add(guiConfigSection);
        }
        
        GuiConfigSection guiConfigSection = guiConfigFile.getGuiConfigSections().get(guiConfigFile.getGuiConfigSections().size()-1);
        guiConfigProperty.setGuiConfigSection(guiConfigSection);
        if (!includeProperty(filter, configSource, guiConfigProperty) ) {
          continue lbl;
        }
        guiConfigSection.getGuiConfigProperties().add(guiConfigProperty);
        
      }
    }
  }  
}
