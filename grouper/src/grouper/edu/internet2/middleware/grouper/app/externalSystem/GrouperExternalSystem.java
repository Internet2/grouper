package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public abstract class GrouperExternalSystem extends GrouperConfigurationModuleBase {
  
  /**
   * return list of error messages
   * @return
   * @throws UnsupportedOperationException
   */
  public List<String> test() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  /**
   * 
   * @param suffix
   * @return
   */
  public Boolean showAttributeOverride(String suffix) {
    return null;
  }
  
  /**
   * 
   * @param isInsert
   * @param fromUi
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void validatePreSave(boolean isInsert, boolean fromUi, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, fromUi, errorsToDisplay, validationErrorsToDisplay);

    if (!isInsert && !this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
      validationErrorsToDisplay.put("#externalSystemConfigId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdDoesntExist"));
    }
    
    Pattern configIdPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
    if (!configIdPattern.matcher(this.getConfigId()).matches()) {
      validationErrorsToDisplay.put("#externalSystemConfigId", GrouperTextContainer.textOrNull("grouperConfigurationValidationConfigIdInvalid"));
    }

  }
  
  /**
   * get all external systems configured for this type
   * @return
   */
  public List<GrouperExternalSystem> listAllExternalSystemsOfThisType() {
    
    List<GrouperExternalSystem> result = new ArrayList<GrouperExternalSystem>();
    
    for (String configId : this.retrieveConfigurationConfigIds()) {
      
      @SuppressWarnings("unchecked")
      Class<GrouperExternalSystem> theClass = (Class<GrouperExternalSystem>)this.getClass();
      GrouperExternalSystem grouperExternalSystem = GrouperUtil.newInstance(theClass);
      grouperExternalSystem.setConfigId(configId);
      result.add(grouperExternalSystem);
    }
    
    return result;
  }
  
  /**
   * 
   * @return
   */
  public List<GrouperExternalSystemConsumer> retrieveAllUsedBy() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperExternalSystem.class);

  /**
   * get subsections for the UI
   * @return
   */
  public List<GrouperExternalSystemSubSection> getSubSections() {
    
    List<GrouperExternalSystemSubSection> results = new ArrayList<GrouperExternalSystemSubSection>();
    
    Set<String> sectionLabelsUsed = new HashSet<String>();
    
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.retrieveAttributes().values()) {
      
      String sectionLabel = grouperConfigModuleAttribute.getConfigItemMetadata().getSubSection();
      if (StringUtils.isBlank(sectionLabel)) {
        sectionLabel = "NULL";
      }
      if (sectionLabelsUsed.contains(sectionLabel)) {
        continue;
      }
      sectionLabelsUsed.add(sectionLabel);
      
      GrouperExternalSystemSubSection grouperExternalSystemSection = new GrouperExternalSystemSubSection();
      grouperExternalSystemSection.setGrouperExternalSystem(this);
      grouperExternalSystemSection.setLabel(grouperConfigModuleAttribute.getConfigItemMetadata().getSubSection());
      
      results.add(grouperExternalSystemSection);
    }
    
    return results;
  }
  
  
  public Map<String, GrouperConfigurationModuleAttribute> retrieveExtraAttributes(Map<String, GrouperConfigurationModuleAttribute> attributesFromBaseConfig) {
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperConfigurationModuleAttribute> result = new LinkedHashMap<String, GrouperConfigurationModuleAttribute>();
    
    Pattern pattern = Pattern.compile(this.getConfigIdRegex());
    
    for (String propertyName: configPropertiesCascadeBase.properties().stringPropertyNames()) {
      
      Matcher matcher = pattern.matcher(propertyName);
     
      if (!matcher.matches()) {
        continue;
      }
      
      String configId = this.getConfigId();
      if (StringUtils.isBlank(configId)) {
        throw new RuntimeException("Why is configId blank??? " + this.getClass().getName());
      }
      
      String configIdFromProperty = matcher.group(2);
      
      if (!StringUtils.equals(configId, configIdFromProperty)) {
        continue;
      }
      
      String suffix = matcher.group(3);
      
      if (attributesFromBaseConfig.containsKey(suffix)) {
        GrouperConfigurationModuleAttribute attribute = attributesFromBaseConfig.get(suffix);
        if (DbConfigEngine.isPasswordHelper(attribute.getConfigItemMetadata(), configPropertiesCascadeBase.propertyValueString(propertyName))) {
          attribute.setValue(DbConfigEngine.ESCAPED_PASSWORD);
        } else {
          attribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
        }
        
      } else {
        GrouperConfigurationModuleAttribute grouperExternalSystemAttribute = new GrouperConfigurationModuleAttribute();

        grouperExternalSystemAttribute.setFullPropertyName(propertyName);
        grouperExternalSystemAttribute.setGrouperConfigModule(this);
        
        result.put(suffix, grouperExternalSystemAttribute);
        
        grouperExternalSystemAttribute.setConfigSuffix(suffix);
        
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setFormElement(ConfigItemFormElement.TEXT);
        configItemMetadata.setValueType(ConfigItemMetadataType.STRING);
        grouperExternalSystemAttribute.setConfigItemMetadata(configItemMetadata);
        grouperExternalSystemAttribute.setType(configItemMetadata.getValueType());
        grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.TEXT);
        grouperExternalSystemAttribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
      }
      
    }
    
    return result;
     
  }
  
  /**
   * 
   * @return get the attributes from config by suffix
   */
  public Map<String, GrouperConfigurationModuleAttribute> retrieveAttributes() {
    
    if (this.attributeCache != null) {
      return this.attributeCache;
    }
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperConfigurationModuleAttribute> result = new LinkedHashMap<String, GrouperConfigurationModuleAttribute>();

    Pattern pattern = Pattern.compile(this.getConfigIdRegex());
    
    for (ConfigSectionMetadata configSectionMetadata : configFileName.configFileMetadata().getConfigSectionMetadataList()) {
      for (ConfigItemMetadata configItemMetadata : configSectionMetadata.getConfigItemMetadataList()) {
        
        Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
        if (!matcher.matches()) {
          continue;
        }
        
        String prefix = matcher.group(1);
        String configId = this.getConfigId();
        if (StringUtils.isBlank(configId)) {
          throw new RuntimeException("Why is configId blank??? " + this.getClass().getName());
        }
        String suffix = matcher.group(3);
        
        String propertyName = prefix + "." + configId + "." + suffix;

        GrouperConfigurationModuleAttribute grouperConfigModuleAttribute = buildConfigurationModuleAttribute(propertyName, suffix, false, configItemMetadata, configPropertiesCascadeBase);

        result.put(suffix, grouperConfigModuleAttribute);
      
      }
    }
    
    Map<String, GrouperConfigurationModuleAttribute> extraAttributes = retrieveExtraAttributes(result);
    
    result.putAll(extraAttributes);
    
    this.attributeCache = result;
    return result;
  }
  
  /**
   * is the config enabled or not
   * @return
   */
  public boolean isEnabled() {
   try {
     GrouperConfigurationModuleAttribute enabledAttribute = this.retrieveAttributes().get("enabled");
     String enabledString = enabledAttribute.getValue();
     if (StringUtils.isBlank(enabledString)) {
       enabledString = enabledAttribute.getDefaultValue();
     }
     return GrouperUtil.booleanValue(enabledString, true);
   } catch (Exception e) {
     return false;
   }
    
  }
  
  /**
   * change status of config to disable/enable
   * @param enable
   * @param message
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void changeStatus(boolean enable, StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    GrouperConfigurationModuleAttribute enabledAttribute = this.retrieveAttributes().get("enabled");
    enabledAttribute.setValue(enable? "true": "false");
    
    DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
        enabledAttribute.getFullPropertyName(), 
        enabledAttribute.isExpressionLanguage() ? "true" : "false", 
        enabledAttribute.isExpressionLanguage() ? enabledAttribute.getExpressionLanguageScript() : enabledAttribute.getValue(),
        enabledAttribute.isPassword(), message, new Boolean[] {false},
        new Boolean[] {false}, true, "Added from external system editor", errorsToDisplay, validationErrorsToDisplay, false);    
    ConfigPropertiesCascadeBase.clearCache();
  }
  
  /**
   * get value for one property
   * @param attributeName
   * @return
   */
  public String propertiesApiProperyValue(String attributeName) {
    return this.getConfigFileName().getConfig().propertyValueString(this.getConfigItemPrefix()+attributeName);
  }
  
  
  public final static Set<String> externalTypeClassNames = new LinkedHashSet<String>();
  static {
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.azure.AzureGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.externalSystem.LdapGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.changelogconsumer.googleapps.GoogleGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouper.o365.Office365GrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouperBox.BoxGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouperDuo.DuoGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouperMessagingActiveMQ.ActiveMqGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouperMessagingRabbitmq.RabbitMqGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouperMessagingAWS.SqsGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.file.SftpGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.smtp.SmtpGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.remedy.RemedyGrouperExternalSystem");
    externalTypeClassNames.add("edu.internet2.middleware.grouper.app.remedy.RemedyDigitalMarketplaceGrouperExternalSystem");
  }
  
  /**
   * list of systems that can be configured
   * @return
   */
  public static List<GrouperExternalSystem> retrieveAllModuleConfigurationTypes() {
    
    List<GrouperExternalSystem> result = new ArrayList<GrouperExternalSystem>();
    
    for (String className: externalTypeClassNames) {
      
      try {
        Class<GrouperExternalSystem> externalSystemClass = (Class<GrouperExternalSystem>) GrouperUtil.forName(className);
        GrouperExternalSystem externalSystem = GrouperUtil.newInstance(externalSystemClass);
        result.add(externalSystem);
      } catch (Exception e) {
        //TODO ignore for now. 
      }
      
    }
    
    return result;
  }

  /**
   * list of configured external systems
   * @return
   */
  public static List<GrouperExternalSystem> retrieveAllGrouperExternalSystems() {
    
    List<GrouperExternalSystem> result = new ArrayList<GrouperExternalSystem>();
    
    for (String className: externalTypeClassNames) {    
      try {
        Class<GrouperExternalSystem> externalSystemClass = (Class<GrouperExternalSystem>) GrouperUtil.forName(className);
        GrouperExternalSystem externalSystem = GrouperUtil.newInstance(externalSystemClass);
        result.addAll(externalSystem.listAllExternalSystemsOfThisType());
      } catch (Exception e) {
        // TODO: ignore for now
      }
    }
    
    return result;
  }
}
