package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigFile;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigProperty;
import edu.internet2.middleware.grouper.grouperUi.beans.config.GuiConfigSection;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ConfigurationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.subject.Subject;

public class UiV2Configure {

  /**
   * if allowed to view configuration
   * @return true if allowed to view configuration
   */
  public boolean allowedToViewConfiguration() {

    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.configuration.enabled", true)) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("configurationNotEnabled")));
      return false;

    }

    if (!GrouperRequestContainer.retrieveFromRequestOrCreate().getConfigurationContainer().isConfigureShow()) {
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("configurationNotAllowedToView")));
      return false;
    }
    
    String networks = GrouperUiConfig.retrieveConfig().propertyValueString("grouperUi.configuration.allowFromNetworks");

    if (!StringUtils.isBlank(networks)) {
      String sourceIp = GrouperUiFilter.retrieveHttpServletRequest().getRemoteAddr();
      Boolean allowed = null;
      
      if (allowed == null) {
        if (!StringUtils.isBlank(sourceIp) && GrouperUtil.ipOnNetworks(sourceIp, networks)) {
          allowed = true;
        } else {
        
          allowed = false;
        }
      }
      if (allowed != Boolean.TRUE) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("configurationNotAllowedBySourceIp")));
        return false;
      }
    }
    
    return true;

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
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
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
        
        buildConfigFileAndMetadata();
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/configure/configure.jsp"));
    
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
