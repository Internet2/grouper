package edu.internet2.middleware.grouper.app.daemon;

import java.util.ArrayList;
import java.util.Collection;
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
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

public abstract class GrouperDaemonConfiguration extends GrouperConfigurationModuleBase {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonConfiguration.class);

  public abstract boolean isMultiple();
  
  public abstract boolean matchesQuartzJobName(String jobName);
  
  public String getDaemonJobPrefix() {
    return null;
  }
  
  public String getPropertySuffixThatIdentifiesThisDaemon() {
    return null;
  }
  
  public String getPropertyValueThatIdentifiesThisDaemon() {
    return null;
  }
  
  public String getConfigIdThatIdentifiesThisDaemon() {
    return null;
  }
  
  public final static Set<String> grouperDaemonConfigClassNames = new LinkedHashSet<String>();
  
  static {
    grouperDaemonConfigClassNames.add(GrouperDaemonBuiltInMessagingConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogConsumerConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogEsbConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogEsbToMessagingConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogRecentMembershipsConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogRulesConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogSyncGroupsConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogTempToChangeLogConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonChangeLogToMessagingConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonCleanLogsConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonEnabledDisabledConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonMessagingListenerConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonMessagingListenerToChangeLogConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobAttestationConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobCsvReportConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobDeprovisioningConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobFindBadMembershipsConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobInstrumentationConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobLoaderIncrementalConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobObjectTypeConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobProvisioningConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobReportClearConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobSchedulerCheckConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobTableSyncConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobUpgradeTasksConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobUsduConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobWorkflowConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobWorkflowReminderConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonOtherJobWsMessagingBridgeConfiguration.class.getName());
    grouperDaemonConfigClassNames.add(GrouperDaemonRulesConfiguration.class.getName());
    
  }
  
  /**
   * list of daemon types that can be configured
   * @return
   */
  public static List<GrouperDaemonConfiguration> retrieveAllModuleConfigurationTypes() {
    
    List<GrouperDaemonConfiguration> result = new ArrayList<GrouperDaemonConfiguration>();
    
    for (String className: grouperDaemonConfigClassNames) {
      
      try {
        Class<GrouperDaemonConfiguration> grouperDaemonConfigurationClass = (Class<GrouperDaemonConfiguration>) GrouperUtil.forName(className);
        GrouperDaemonConfiguration grouperDaemonConfig = GrouperUtil.newInstance(grouperDaemonConfigurationClass);
        result.add(grouperDaemonConfig);
      } catch (Exception e) {
        //TODO ignore for now.
      }
    }
    return result;
  }
  
  public Collection<GrouperConfigurationModuleAttribute> getConfigAttributes() {
    return this.retrieveAttributes().values();
  }
  
  
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

    List<ConfigSectionMetadata> configSectionMetadataList = configFileName.configFileMetadata().getConfigSectionMetadataList();
    
    // get the attributes based on the configIdThatIdentifiesThisDaemon
    String configIdThatIdentifiesThisDaemon = null;
    
    if (this.isMultiple() && StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Cant have isMultiple and a blank configId! " + this.getClass().getName());
    }
    if (!this.isMultiple() && !StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Cant have not isMultiple and configId! " + this.getClass().getName());
    }

    if (this.getPropertySuffixThatIdentifiesThisDaemon() != null) {
      
      if (StringUtils.isBlank(this.getPropertyValueThatIdentifiesThisDaemon())) {
        throw new RuntimeException("getPropertyValueThatIdentifiesThisDaemon is required for " + this.getClass().getName());
      }
      
      if (StringUtils.isNotBlank(this.getConfigIdThatIdentifiesThisDaemon())) {
        throw new RuntimeException("can't specify ConfigIdThatIdentifiesThisDaemon and PropertySuffixThatIdentifiesThisDaemon for class "+this.getClass().getName());
      }
 
      if (!this.isMultiple()) {
        throw new RuntimeException("Cant have getPropertySuffixThatIdentifiesThisDaemon and not be multiple! " + this.getClass().getName());
      }
      
      outer: for (ConfigSectionMetadata configSectionMetadata: configSectionMetadataList) {
        for (ConfigItemMetadata configItemMetadata: configSectionMetadata.getConfigItemMetadataList()) {
          
          Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
          if (!matcher.matches()) {
            continue;
          }
          
          String configId = matcher.group(2);
          String suffix = matcher.group(3);

          if (StringUtils.equals(suffix, this.getPropertySuffixThatIdentifiesThisDaemon())) {
            
            if (StringUtils.equals(configItemMetadata.getValue(), this.getPropertyValueThatIdentifiesThisDaemon())
                || StringUtils.equals(configItemMetadata.getSampleValue(), this.getPropertyValueThatIdentifiesThisDaemon())) {
              configIdThatIdentifiesThisDaemon = configId;
              break outer;
            }
            
          }
        }
      }
      
      if (StringUtils.isBlank(configIdThatIdentifiesThisDaemon)) {
        throw new RuntimeException("can't find property in config file that identifies this daemon for " + this.getClass().getName());
      }
      
    } else if (this.getConfigIdThatIdentifiesThisDaemon() != null ) {
      configIdThatIdentifiesThisDaemon = this.getConfigIdThatIdentifiesThisDaemon();
    }
    
    try {
      for (ConfigSectionMetadata configSectionMetadata: configSectionMetadataList) {
        for (ConfigItemMetadata configItemMetadata: configSectionMetadata.getConfigItemMetadataList()) {

          String propertyName = configItemMetadata.getKeyOrSampleKey();
          String suffix = propertyName;
          if (!this.retrieveExtraConfigKeys().contains(propertyName)) {
            
            Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
            if (!matcher.matches()) {
              continue;
            }
            
            String prefix = matcher.group(1);
            suffix = null;
                    
            if(this.isMultiple()) { // multiple means config id will not be blank on an edit

              if (StringUtils.isBlank(configIdThatIdentifiesThisDaemon)) {
                throw new RuntimeException("Why is configIdThatIdentifiesThisDaemon blank??? " + this.getClass().getName());
              }

              String currentConfigId = matcher.group(2);

              if (!StringUtils.equals(currentConfigId, configIdThatIdentifiesThisDaemon)) {
                continue;
              }
              
              suffix = matcher.group(3);
              propertyName = prefix + "." + this.getConfigId() + "." + suffix;
              
            } else {
              
              if (!StringUtils.isBlank(this.getConfigId())) {
                throw new RuntimeException("Why is configId not blank??? " + this.getClass().getName());
              }
              suffix = matcher.group(2);
              propertyName = configItemMetadata.getKeyOrSampleKey();
              
            }

          }
          
          GrouperConfigurationModuleAttribute grouperConfigModuleAttribute = new GrouperConfigurationModuleAttribute();
          grouperConfigModuleAttribute.setFullPropertyName(propertyName);
          grouperConfigModuleAttribute.setGrouperConfigModule(this);
          result.put(suffix, grouperConfigModuleAttribute);
          
          {
            boolean hasExpressionLanguage = configPropertiesCascadeBase.hasExpressionLanguage(propertyName);
            grouperConfigModuleAttribute.setExpressionLanguage(hasExpressionLanguage);

            if (hasExpressionLanguage) {
              String rawExpressionLanguage = configPropertiesCascadeBase.rawExpressionLanguage(propertyName);
              grouperConfigModuleAttribute.setExpressionLanguageScript(rawExpressionLanguage);
            }
          }
          
          grouperConfigModuleAttribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
          grouperConfigModuleAttribute.setConfigItemMetadata(configItemMetadata);
          
          grouperConfigModuleAttribute.setConfigSuffix(suffix);
          
          {
            grouperConfigModuleAttribute.setRequired(configItemMetadata.isRequired());
            grouperConfigModuleAttribute.setType(configItemMetadata.getValueType());
            grouperConfigModuleAttribute.setDefaultValue(configItemMetadata.getDefaultValue());
            //grouperDaemonConfigAttribute.setFormElement(ConfigItemFormElement.TEXT);
          }
          
          if (GrouperUtil.length(configItemMetadata.getOptionValues()) > 0) {
            List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
            valuesAndLabels.add(new MultiKey("", ""));
            for (String value : configItemMetadata.getOptionValues()) {
              
              String label = GrouperTextContainer.textOrNull("externalSystem." 
                  + this.getClass().getSimpleName() + ".attribute.option." + grouperConfigModuleAttribute.getConfigSuffix() + "." + value + ".label");
              label = StringUtils.defaultIfBlank(label, value);
              
              MultiKey valueAndLabel = new MultiKey(value, label);
              valuesAndLabels.add(valueAndLabel);
            }
            grouperConfigModuleAttribute.setDropdownValuesAndLabels(valuesAndLabels);
          }
          
          
          ConfigItemFormElement configItemFormElement = configItemMetadata.getFormElement();
          if (configItemFormElement != null) {
            grouperConfigModuleAttribute.setFormElement(configItemFormElement);
          } else {
            // boolean is a drop down
            if (configItemMetadata.getValueType() == ConfigItemMetadataType.BOOLEAN) {
              
              grouperConfigModuleAttribute.setFormElement(ConfigItemFormElement.DROPDOWN);

              if (GrouperUtil.length(grouperConfigModuleAttribute.getDropdownValuesAndLabels()) == 0) {
                
                List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
                valuesAndLabels.add(new MultiKey("", ""));
                
                String trueLabel = GrouperTextContainer.textOrNull("externalSystem." 
                    + this.getClass().getSimpleName() + ".attribute.option." + grouperConfigModuleAttribute.getConfigSuffix() + ".trueLabel");
                
                trueLabel = GrouperUtil.defaultIfBlank(trueLabel, GrouperTextContainer.textOrNull("externalSystem.defaultTrueLabel"));

                String falseLabel = GrouperTextContainer.textOrNull("externalSystem." 
                    + this.getClass().getSimpleName() + ".attribute.option." + grouperConfigModuleAttribute.getConfigSuffix() + ".falseLabel");
                
                falseLabel = GrouperUtil.defaultIfBlank(falseLabel, GrouperTextContainer.textOrNull("externalSystem.defaultFalseLabel"));
                
                valuesAndLabels.add(new MultiKey("true", trueLabel));
                valuesAndLabels.add(new MultiKey("false", falseLabel));
                grouperConfigModuleAttribute.setDropdownValuesAndLabels(valuesAndLabels);
              }
            } else if (GrouperUtil.length(grouperConfigModuleAttribute.getValue()) > 100) {

              grouperConfigModuleAttribute.setFormElement(ConfigItemFormElement.TEXTAREA);

            } else {
              grouperConfigModuleAttribute.setFormElement(ConfigItemFormElement.TEXT);
            }
          }
        
          
        }
      }
      
      this.attributeCache = result;
      Map<String, GrouperConfigurationModuleAttribute> extraAttributes = retrieveExtraAttributes(result);
      result.putAll(extraAttributes);
      
      this.attributeCache = result;
    } catch(Exception e) {
      e.printStackTrace();
    }


    return result;
    
  }
  
  
  public Map<String, GrouperConfigurationModuleAttribute> retrieveExtraAttributes(Map<String, GrouperConfigurationModuleAttribute> attributesFromBaseConfig) {
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperConfigurationModuleAttribute> result = new LinkedHashMap<String, GrouperConfigurationModuleAttribute>();
    
    for (String propertyName: configPropertiesCascadeBase.properties().stringPropertyNames()) {
      
      if (!propertyName.startsWith(this.getConfigItemPrefix())) {
        continue;
      }
      
      String suffix = StringUtils.replace(propertyName, this.getConfigItemPrefix(), "");

      // this is not extra
      if (this.attributeCache.containsKey(suffix)) {
        continue;
      }
      
      if (attributesFromBaseConfig.containsKey(suffix)) {
        GrouperConfigurationModuleAttribute attribute = attributesFromBaseConfig.get(suffix);
        attribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
      } else {
        GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
        
        attribute.setFullPropertyName(propertyName);
        attribute.setGrouperConfigModule(this);
        
        result.put(suffix, attribute);
        
        attribute.setConfigSuffix(suffix);
    
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setFormElement(ConfigItemFormElement.TEXT);
        configItemMetadata.setValueType(ConfigItemMetadataType.STRING);
        attribute.setConfigItemMetadata(configItemMetadata);
        attribute.setType(configItemMetadata.getValueType());
        attribute.setFormElement(ConfigItemFormElement.TEXT);
        attribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
      }
      
    }
    
    return result;
     
  }
  
  /**
   * extra configs that dont match the regex or prefix
   */
  protected Set<String> extraConfigKeys = new LinkedHashSet<String>();

  public Set<String> retrieveExtraConfigKeys() {
    return extraConfigKeys;
  }
  
  /**
   * 
   * @param isInsert
   * @param fromUi
   * @param errorsToDisplay
   * @param validationErrorsToDisplay
   */
  public void validatePreSave(boolean isInsert, boolean fromUi, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    if (isInsert) {
      if (this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
        validationErrorsToDisplay.put("#daemonConfigId", GrouperTextContainer.textOrNull("grouperDaemonConfigurationValidationConfigIdUsed"));
      }
    } else {
      if (this.isMultiple()) {
        if (!this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
          validationErrorsToDisplay.put("#daemonConfigId", GrouperTextContainer.textOrNull("grouperDaemonConfigurationValidationConfigIdDoesntExist"));
        } 
      }
    }
    
    if (this.isMultiple()) {
      Pattern configIdPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
      if (!configIdPattern.matcher(this.getConfigId()).matches()) {
        validationErrorsToDisplay.put("#daemonConfigId", GrouperTextContainer.textOrNull("grouperDaemonConfigurationValidationConfigIdInvalid"));
      }
    }
    
    // first check if checked the el checkbox then make sure theres a script there
    {
      boolean foundElRequiredError = false;
      for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.retrieveAttributes().values()) {
        
        if (grouperConfigModuleAttribute.isExpressionLanguage() && StringUtils.isBlank(grouperConfigModuleAttribute.getExpressionLanguageScript())) {
          
          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperConfigModuleAttribute.getLabel());
          validationErrorsToDisplay.put(grouperConfigModuleAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull("grouperConfigurationValidationElRequired"));
          GrouperTextContainer.resetThreadLocalVariableMap();
          foundElRequiredError = true;
        }
        
      }
      if (foundElRequiredError) {
        return;
      }
    }
    
    // types
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.retrieveAttributes().values()) {
      
      ConfigItemMetadataType configItemMetadataType = grouperConfigModuleAttribute.getConfigItemMetadata().getValueType();
      
      String value = null;
      
      try {
        value = grouperConfigModuleAttribute.getEvaluatedValueForValidation();
      } catch (UnsupportedOperationException uoe) {
        // ignore, it will get validated in the post-save
        continue;
      }
      
      // required
      if (StringUtils.isBlank(value)) {
        if (grouperConfigModuleAttribute.getConfigItemMetadata().isRequired()) {

          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperConfigModuleAttribute.getLabel());
          validationErrorsToDisplay.put(grouperConfigModuleAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull("grouperConfigurationValidationRequired"));
          GrouperTextContainer.resetThreadLocalVariableMap();
          
        }
        
        continue;
      }
      String[] valuesToValidate = null;
      if (grouperConfigModuleAttribute.getConfigItemMetadata().isMultiple()) {
        valuesToValidate = GrouperUtil.splitTrim(value, ",");
      } else {
        valuesToValidate = new String[] {value};
      }

      for (String theValue : valuesToValidate) {
        
        // validate types
        String externalizedTextKey = configItemMetadataType.validate(theValue);
        if (!StringUtils.isBlank(externalizedTextKey)) {
          
          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperConfigModuleAttribute.getLabel());
          validationErrorsToDisplay.put(grouperConfigModuleAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull(externalizedTextKey));
          GrouperTextContainer.resetThreadLocalVariableMap();
          
        }
      }
    }
    
  }
  
  private static ExpirableCache<String, GrouperDaemonConfiguration> jobNameToGrouperDaemonConfigCache = new ExpirableCache<String, GrouperDaemonConfiguration>(10);

  public static GrouperDaemonConfiguration retrieveImplementationFromJobName(String jobName) {
    
    GrouperDaemonConfiguration result = jobNameToGrouperDaemonConfigCache.get(jobName);
    
    if (result != null) {
      return result;
    }
    
    for (String className: grouperDaemonConfigClassNames) {
      
      Class<GrouperDaemonConfiguration> grouperDaemonConfigurationClass = (Class<GrouperDaemonConfiguration>) GrouperUtil.forName(className);
      GrouperDaemonConfiguration grouperDaemonConfig = GrouperUtil.newInstance(grouperDaemonConfigurationClass);
      if (grouperDaemonConfig instanceof GrouperDaemonOtherJobConfiguration) {
        continue;
      }
      if (grouperDaemonConfig instanceof GrouperDaemonChangeLogConsumerConfiguration) {
        continue;
      }
      if (grouperDaemonConfig instanceof GrouperDaemonMessagingListenerConfiguration) {
        continue;
      }
      if (grouperDaemonConfig instanceof GrouperDaemonChangeLogEsbConfiguration) {
        continue;
      }
      if (grouperDaemonConfig.matchesQuartzJobName(jobName)) {          
        if (result != null) {
          throw new RuntimeException(jobName + " matches "+ grouperDaemonConfig + " and also " + result);
        }
        result = grouperDaemonConfig;
      }
    }
      
    if (result != null) {
      jobNameToGrouperDaemonConfigCache.put(jobName, result);
      return result;
    }

    GrouperDaemonMessagingListenerConfiguration grouperDaemonMessagingListenerConfiguration = new GrouperDaemonMessagingListenerConfiguration();
    if (grouperDaemonMessagingListenerConfiguration.matchesQuartzJobName(jobName)) {
      jobNameToGrouperDaemonConfigCache.put(jobName, grouperDaemonMessagingListenerConfiguration);
      return grouperDaemonMessagingListenerConfiguration;
    }

    GrouperDaemonOtherJobConfiguration grouperDaemonOtherJobConfiguration = new GrouperDaemonOtherJobConfiguration();
    if (grouperDaemonOtherJobConfiguration.matchesQuartzJobName(jobName)) {
      jobNameToGrouperDaemonConfigCache.put(jobName, grouperDaemonOtherJobConfiguration);
      return grouperDaemonOtherJobConfiguration;
    }

    // note ESB needs to be above the generic change log below
    GrouperDaemonChangeLogEsbConfiguration grouperDaemonChangeLogEsbConfiguration = new GrouperDaemonChangeLogEsbConfiguration();
    if (grouperDaemonChangeLogEsbConfiguration.matchesQuartzJobName(jobName)) {
      jobNameToGrouperDaemonConfigCache.put(jobName, grouperDaemonChangeLogEsbConfiguration);
      return grouperDaemonChangeLogEsbConfiguration;
    }

    GrouperDaemonChangeLogConsumerConfiguration grouperDaemonChangeLogConsumerConfiguration = new GrouperDaemonChangeLogConsumerConfiguration();
    if (grouperDaemonChangeLogConsumerConfiguration.matchesQuartzJobName(jobName)) {
      jobNameToGrouperDaemonConfigCache.put(jobName, grouperDaemonChangeLogConsumerConfiguration);
      return grouperDaemonChangeLogConsumerConfiguration;
    }

    throw new RuntimeException("Can't find daemon config for jobName "+jobName);
    
  }

}
