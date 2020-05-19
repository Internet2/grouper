package edu.internet2.middleware.grouper.app.daemon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public abstract class GrouperDaemonConfiguration {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDaemonConfiguration.class);
  
  /**
   * config id of the daemon
   */
  private String configId;
  /**
   * call retrieveAttributes() to get this
   */
  private Map<String, GrouperDaemonConfigAttribute> attributeCache = null;
  
  
  public String getConfigId() {
    return configId;
  }
  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  public abstract ConfigFileName getConfigFileName();

  public abstract String getConfigIdRegex();
  
  public abstract String getConfigItemPrefix();
  
  public abstract boolean isMultiple();
  
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
  
  /**
   * get title of the grouper daemon configuration
   * @return
   */
  public String getTitle() {
    String title = GrouperTextContainer.textOrNull("grouperDaemon." + this.getClass().getSimpleName() + ".title");
    if (StringUtils.isBlank(title)) {
      return this.getClass().getSimpleName();
    }
    return title;
  }
  
  public final static Set<String> grouperDaemonConfigClassNames = new LinkedHashSet<String>();
  static {
    grouperDaemonConfigClassNames.add("edu.internet2.middleware.grouper.app.daemon.GrouperDaemonChangeLogTempToChangeLogConfiguration");
    grouperDaemonConfigClassNames.add("edu.internet2.middleware.grouper.app.daemon.GrouperDaemonOtherJobConfiguration");
    grouperDaemonConfigClassNames.add("edu.internet2.middleware.grouper.app.daemon.GrouperDaemonOtherJobLoaderIncrementalConfiguration");
    grouperDaemonConfigClassNames.add("edu.internet2.middleware.grouper.app.daemon.GrouperDaemonFindBadMembershipsConfiguration");
  }
  
  
  /**
   * list of daemon types that can be configured
   * @return
   */
  public static List<GrouperDaemonConfiguration> retrieveAllDaemonTypesConfiguration() {
    
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
  
  public Collection<GrouperDaemonConfigAttribute> getConfigAttributes() {
    return this.retrieveAttributes().values();
  }
  
  
  public Map<String, GrouperDaemonConfigAttribute> retrieveAttributes() { 
    
    if (this.attributeCache != null) {
      return this.attributeCache;
    }
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperDaemonConfigAttribute> result = new LinkedHashMap<String, GrouperDaemonConfigAttribute>();

    Pattern pattern = Pattern.compile(this.getConfigIdRegex());

    List<ConfigSectionMetadata> configSectionMetadataList = configFileName.configFileMetadata().getConfigSectionMetadataList();
    
    // get the attributes based on the configIdThatIdentifiesThisDaemon
    String configIdThatIdentifiesThisDaemon = null;
    
    if (this.isMultiple() && StringUtils.isBlank(this.configId)) {
      throw new RuntimeException("Cant have isMultiple and a blank configId! " + this.getClass().getName());
    }
    if (!this.isMultiple() && !StringUtils.isBlank(this.configId)) {
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
          
          Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
          if (!matcher.matches()) {
            continue;
          }
          
          String prefix = matcher.group(1);
          String propertyName = null;
          String suffix = null;
                  
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
            
            if (!StringUtils.isBlank(configId)) {
              throw new RuntimeException("Why is configId not blank??? " + this.getClass().getName());
            }
            suffix = matcher.group(2);
            propertyName = configItemMetadata.getKeyOrSampleKey();
            
          }
          
          GrouperDaemonConfigAttribute grouperDaemonConfigAttribute = new GrouperDaemonConfigAttribute();
          grouperDaemonConfigAttribute.setFullPropertyName(propertyName);
          grouperDaemonConfigAttribute.setGrouperDaemonConfiguration(this);
          result.put(suffix, grouperDaemonConfigAttribute);
          
          {
            boolean hasExpressionLanguage = configPropertiesCascadeBase.hasExpressionLanguage(propertyName);
            grouperDaemonConfigAttribute.setExpressionLanguage(hasExpressionLanguage);

            if (hasExpressionLanguage) {
              String rawExpressionLanguage = configPropertiesCascadeBase.rawExpressionLanguage(propertyName);
              grouperDaemonConfigAttribute.setExpressionLanguageScript(rawExpressionLanguage);
            }
          }
          
          grouperDaemonConfigAttribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
          grouperDaemonConfigAttribute.setConfigItemMetadata(configItemMetadata);
          
          grouperDaemonConfigAttribute.setConfigSuffix(suffix);
          
          {
            grouperDaemonConfigAttribute.setRequired(configItemMetadata.isRequired());
            grouperDaemonConfigAttribute.setType(configItemMetadata.getValueType());
            grouperDaemonConfigAttribute.setDefaultValue(configItemMetadata.getDefaultValue());
            //grouperDaemonConfigAttribute.setFormElement(ConfigItemFormElement.TEXT);
          }
          
          if (GrouperUtil.length(configItemMetadata.getOptionValues()) > 0) {
            List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
            valuesAndLabels.add(new MultiKey("", ""));
            for (String value : configItemMetadata.getOptionValues()) {
              
              String label = GrouperTextContainer.textOrNull("externalSystem." 
                  + this.getClass().getSimpleName() + ".attribute.option." + grouperDaemonConfigAttribute.getConfigSuffix() + "." + value + ".label");
              label = StringUtils.defaultIfBlank(label, value);
              
              MultiKey valueAndLabel = new MultiKey(value, label);
              valuesAndLabels.add(valueAndLabel);
            }
            grouperDaemonConfigAttribute.setDropdownValuesAndLabels(valuesAndLabels);
          }
          
          
          ConfigItemFormElement configItemFormElement = configItemMetadata.getFormElement();
          if (configItemFormElement != null) {
            grouperDaemonConfigAttribute.setFormElement(configItemFormElement);
          } else {
            // boolean is a drop down
            if (configItemMetadata.getValueType() == ConfigItemMetadataType.BOOLEAN) {
              
              grouperDaemonConfigAttribute.setFormElement(ConfigItemFormElement.DROPDOWN);

              if (GrouperUtil.length(grouperDaemonConfigAttribute.getDropdownValuesAndLabels()) == 0) {
                
                List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
                valuesAndLabels.add(new MultiKey("", ""));
                
                String trueLabel = GrouperTextContainer.textOrNull("externalSystem." 
                    + this.getClass().getSimpleName() + ".attribute.option." + grouperDaemonConfigAttribute.getConfigSuffix() + ".trueLabel");
                
                trueLabel = GrouperUtil.defaultIfBlank(trueLabel, GrouperTextContainer.textOrNull("externalSystem.defaultTrueLabel"));

                String falseLabel = GrouperTextContainer.textOrNull("externalSystem." 
                    + this.getClass().getSimpleName() + ".attribute.option." + grouperDaemonConfigAttribute.getConfigSuffix() + ".falseLabel");
                
                falseLabel = GrouperUtil.defaultIfBlank(falseLabel, GrouperTextContainer.textOrNull("externalSystem.defaultFalseLabel"));
                
                valuesAndLabels.add(new MultiKey("true", trueLabel));
                valuesAndLabels.add(new MultiKey("false", falseLabel));
                grouperDaemonConfigAttribute.setDropdownValuesAndLabels(valuesAndLabels);
              }
            } else if (GrouperUtil.length(grouperDaemonConfigAttribute.getValue()) > 100) {

              grouperDaemonConfigAttribute.setFormElement(ConfigItemFormElement.TEXTAREA);

            } else {
              grouperDaemonConfigAttribute.setFormElement(ConfigItemFormElement.TEXT);
            }
          }
        
          
        }
      }
      
      this.attributeCache = result;
      Map<String, GrouperDaemonConfigAttribute> extraAttributes = retrieveExtraAttributes(result);
      result.putAll(extraAttributes);
      
      this.attributeCache = result;
    } catch(Exception e) {
      e.printStackTrace();
    }


    return result;
    
  }
  
  
  public Map<String, GrouperDaemonConfigAttribute> retrieveExtraAttributes(Map<String, GrouperDaemonConfigAttribute> attributesFromBaseConfig) {
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperDaemonConfigAttribute> result = new LinkedHashMap<String, GrouperDaemonConfigAttribute>();
    
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
        GrouperDaemonConfigAttribute attribute = attributesFromBaseConfig.get(suffix);
        attribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
      } else {
        GrouperDaemonConfigAttribute attribute = new GrouperDaemonConfigAttribute();
        
        attribute.setFullPropertyName(propertyName);
        attribute.setGrouperDaemonConfiguration(this);
        
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
   * get a set of config ids
   * @return
   */
  public Set<String> retrieveConfigurationConfigIds() {
    
    String regex = this.getConfigIdRegex();
    
    if (StringUtils.isBlank(regex)) {
      throw new RuntimeException("Regex is reqired for " + this.getClass().getName());
    }
    
    Set<String> result = new TreeSet<String>();
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Properties properties = configPropertiesCascadeBase.properties();

    Pattern pattern = Pattern.compile(regex);
    
    for (Object propertyNameObject : properties.keySet()) {
      String propertyName = (String)propertyNameObject;
      
      Matcher matcher = pattern.matcher(propertyName);
      
      if (!matcher.matches()) {
        continue;
      }

      String configId = matcher.group(2);
      result.add(configId);
    }
    return result;
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
      for (GrouperDaemonConfigAttribute grouperDaemonConfigAttribute : this.retrieveAttributes().values()) {
        
        if (grouperDaemonConfigAttribute.isExpressionLanguage() && StringUtils.isBlank(grouperDaemonConfigAttribute.getExpressionLanguageScript())) {
          
          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperDaemonConfigAttribute.getLabel());
          validationErrorsToDisplay.put(grouperDaemonConfigAttribute.getHtmlForElementIdHandle(), 
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
    for (GrouperDaemonConfigAttribute grouperDaemonConfigAttribute : this.retrieveAttributes().values()) {
      
      ConfigItemMetadataType configItemMetadataType = grouperDaemonConfigAttribute.getConfigItemMetadata().getValueType();
      
      String value = null;
      
      try {
        value = grouperDaemonConfigAttribute.getEvaluatedValueForValidation();
      } catch (UnsupportedOperationException uoe) {
        // ignore, it will get validated in the post-save
        continue;
      }
      
      // required
      if (StringUtils.isBlank(value)) {
        if (grouperDaemonConfigAttribute.getConfigItemMetadata().isRequired()) {

          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperDaemonConfigAttribute.getLabel());
          validationErrorsToDisplay.put(grouperDaemonConfigAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull("grouperConfigurationValidationRequired"));
          GrouperTextContainer.resetThreadLocalVariableMap();
          
        }
        
        continue;
      }
      String[] valuesToValidate = null;
      if (grouperDaemonConfigAttribute.getConfigItemMetadata().isMultiple()) {
        valuesToValidate = GrouperUtil.splitTrim(value, ",");
      } else {
        valuesToValidate = new String[] {value};
      }

      for (String theValue : valuesToValidate) {
        
        // validate types
        String externalizedTextKey = configItemMetadataType.validate(theValue);
        if (!StringUtils.isBlank(externalizedTextKey)) {
          
          GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", grouperDaemonConfigAttribute.getLabel());
          validationErrorsToDisplay.put(grouperDaemonConfigAttribute.getHtmlForElementIdHandle(), 
              GrouperTextContainer.textOrNull(externalizedTextKey));
          GrouperTextContainer.resetThreadLocalVariableMap();
          
        }
      }
    }
    
  }
  
  /**
   * save the attribute in an insert.  Note, if theres a failure, you should see if any made it
   * @param attributesToSave are the attributes from "retrieveAttributes" with values in there
   * if a value is blank, then dont save that one
   * @param errorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message.toString()));
   * @param validationErrorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
   *      validationErrorsToDisplay.get(validationKey)));
   */
  public void insertConfig(boolean fromUi, 
      StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    //TODO add validation
    validatePreSave(true, fromUi, errorsToDisplay, validationErrorsToDisplay);

    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
    Pattern endOfStringNewlinePattern = Pattern.compile(".*<br[ ]*\\/?>$");
    
    // add all the possible ones
    Map<String, GrouperDaemonConfigAttribute> attributes = this.retrieveAttributes();
    for (String suffix : attributes.keySet()) {
    
      GrouperDaemonConfigAttribute grouperDaemonConfigAttribute = attributes.get(suffix);
      
      if (grouperDaemonConfigAttribute.isHasValue()) {
        
        StringBuilder localMessage = new StringBuilder();
        
        DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
            grouperDaemonConfigAttribute.getFullPropertyName(), 
            grouperDaemonConfigAttribute.isExpressionLanguage() ? "true" : "false", 
            grouperDaemonConfigAttribute.isExpressionLanguage() ? grouperDaemonConfigAttribute.getExpressionLanguageScript() : grouperDaemonConfigAttribute.getValue(),
            grouperDaemonConfigAttribute.isPassword(), localMessage, new Boolean[] {false},
            new Boolean[] {false}, fromUi, "Added from daemon config editor", errorsToDisplay, validationErrorsToDisplay, false);
        
        if (localMessage.length() > 0) {
          if(message.length() > 0) {
            
            if (fromUi && !endOfStringNewlinePattern.matcher(message).matches()) {
              message.append("<br />\n");
            } else if (!fromUi && message.charAt(message.length()-1) != '\n') {
              message.append("\n");
            }
            message.append(localMessage);
          }
        }
        
      }
    }

    ConfigPropertiesCascadeBase.clearCache();
    this.attributeCache = null;
    
  }
  
  /**
   * delete config
   * @param fromUi
   */
  public void deleteConfig(boolean fromUi) {
    
    Map<String, GrouperDaemonConfigAttribute> attributes = this.retrieveAttributes();
    
    for (GrouperDaemonConfigAttribute attribute: attributes.values()) {
      
      Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(this.getConfigFileName(), null, attribute.getFullPropertyName());
      
      if (grouperConfigHibernates != null && grouperConfigHibernates.size() > 0) {
        for (GrouperConfigHibernate grouperConfigHibernate: grouperConfigHibernates) {
          grouperConfigHibernate.setConfigValue("");
          grouperConfigHibernate.saveOrUpdate();
        }
      } else {
        DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
            attribute.getFullPropertyName(), 
            attribute.isExpressionLanguage() ? "true" : "false", 
            "",
            attribute.isPassword(), new StringBuilder(), new Boolean[] {false},
            new Boolean[] {false}, fromUi, "Added from daemon config editor", new ArrayList<String>(), 
            new HashMap<String, String>(), false);
      }
    }
    
    Set<String> propertyNamesToDelete = new HashSet<String>();
    if (!StringUtils.isBlank(this.getConfigId())) {
      
      Set<String> configKeys = this.retrieveConfigurationKeysByPrefix(this.getConfigItemPrefix());
      
      if (GrouperUtil.length(configKeys) > 0) {
        propertyNamesToDelete.addAll(configKeys);
      }
    }
    
    for (String key : propertyNamesToDelete) {
      DbConfigEngine.configurationFileItemDeleteHelper(this.getConfigFileName().getConfigFileName(), key , fromUi, false);
    }
    ConfigPropertiesCascadeBase.clearCache();
    this.attributeCache = null;
    
  }
  
  public abstract boolean matchesQuartzJobName(String jobName);
  
  public static GrouperDaemonConfiguration retrieveImplementationFromJobName(String jobName) {
    
    GrouperDaemonConfiguration result = null;
    
    for (String className: grouperDaemonConfigClassNames) {
      Class<GrouperDaemonConfiguration> grouperDaemonConfigurationClass = (Class<GrouperDaemonConfiguration>) GrouperUtil.forName(className);
      GrouperDaemonConfiguration grouperDaemonConfig = GrouperUtil.newInstance(grouperDaemonConfigurationClass);
      if (grouperDaemonConfig instanceof GrouperDaemonOtherJobConfiguration) {
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
      return result;
    }
    
    GrouperDaemonOtherJobConfiguration grouperDaemonOtherJobConfiguration = new GrouperDaemonOtherJobConfiguration();
    if (grouperDaemonOtherJobConfiguration.matchesQuartzJobName(jobName)) {
      return grouperDaemonOtherJobConfiguration;
    }
    
    throw new RuntimeException("Can't find daemon config for jobName "+jobName);
    
  }
  
  /**
   * save the attribute in an edit.  Note, if theres a failure, you should see if any made it
   * @param attributesFromUser are the attributes from "retrieveAttributes" with values in there
   * if a value is blank, then dont save that one (delete)
   * @param errorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message.toString()));
   * @param validationErrorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
   *      validationErrorsToDisplay.get(validationKey)));
   */
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    validatePreSave(false, fromUi, errorsToDisplay, validationErrorsToDisplay);

    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }
    
    Map<String, GrouperDaemonConfigAttribute> attributes = this.retrieveAttributes();
    
    Set<String> propertyNamesToDelete = new HashSet<String>();

    // add all the possible ones
    for (String suffix : attributes.keySet()) {
    
      GrouperDaemonConfigAttribute grouperDaemonConfigAttribute = attributes.get(suffix);

      propertyNamesToDelete.add(grouperDaemonConfigAttribute.getFullPropertyName());
      
    }

    // and all the ones we detect
    if (!StringUtils.isBlank(this.getConfigId())) {
      
      Set<String> configKeys = this.retrieveConfigurationKeysByPrefix(this.getConfigItemPrefix());
      
      if (GrouperUtil.length(configKeys) > 0) {
        propertyNamesToDelete.addAll(configKeys);
      }
    }
    
    Map<String, GrouperDaemonConfigAttribute> attributesToSave = new HashMap<String, GrouperDaemonConfigAttribute>();
    
    // remove the edited ones
    for (String suffix : attributes.keySet()) {
    
      GrouperDaemonConfigAttribute grouperDaemonConfigAttribute = attributes.get(suffix);
      
      if (grouperDaemonConfigAttribute.isHasValue()) {
        propertyNamesToDelete.remove(grouperDaemonConfigAttribute.getFullPropertyName());
        attributesToSave.put(suffix, grouperDaemonConfigAttribute);
      }
    }
    // delete some
    for (String key : propertyNamesToDelete) {
      DbConfigEngine.configurationFileItemDeleteHelper(this.getConfigFileName().getConfigFileName(), key , fromUi, false);
    }

    Pattern endOfStringNewlinePattern = Pattern.compile(".*<br[ ]*\\/?>$");
    
    // add/edit all the possible ones
    for (String suffix : attributesToSave.keySet()) {
    
      GrouperDaemonConfigAttribute grouperDaemonConfigAttribute = attributesToSave.get(suffix);
      
      StringBuilder localMessage = new StringBuilder();
      
      DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
          grouperDaemonConfigAttribute.getFullPropertyName(), 
          grouperDaemonConfigAttribute.isExpressionLanguage() ? "true" : "false", 
          grouperDaemonConfigAttribute.isExpressionLanguage() ? grouperDaemonConfigAttribute.getExpressionLanguageScript() : grouperDaemonConfigAttribute.getValue(),
          grouperDaemonConfigAttribute.isPassword(), localMessage, new Boolean[] {false},
          new Boolean[] {false}, fromUi, "Added from external system editor", errorsToDisplay, validationErrorsToDisplay, false);
      
      if (localMessage.length() > 0) {
        if(message.length() > 0) {
          
          if (fromUi && !endOfStringNewlinePattern.matcher(message).matches()) {
            message.append("<br />\n");
          } else if (!fromUi && message.charAt(message.length()-1) != '\n') {
            message.append("\n");
          }
          message.append(localMessage);
        }
      }
    }

    ConfigPropertiesCascadeBase.clearCache();
    this.attributeCache = null;
  }
  
  /**
   * get configuration names configured by prefix 
   * @param prefix of config e.g. ldap.personLdap.
   * @return the list of configured keys
   */
  protected Set<String> retrieveConfigurationKeysByPrefix(String prefix) {
    Set<String> result = new HashSet<String>();
    ConfigFileName configFileName = this.getConfigFileName();
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Properties properties = configPropertiesCascadeBase.properties();

    for (Object propertyNameObject : properties.keySet()) {
      String propertyName = (String)propertyNameObject;
      if (propertyName.startsWith(prefix)) {

        if (result.contains(propertyName)) {
          LOG.error("Config key '" + propertyName + "' is defined in '" + configFileName.getConfigFileName() + "' more than once!");
        } else {
          result.add(propertyName);
        }
      }
    }
    return result;
  }

}
