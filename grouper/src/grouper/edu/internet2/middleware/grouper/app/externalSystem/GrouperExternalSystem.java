package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public abstract class GrouperExternalSystem {
  
  public abstract String getType();

  private String configId;
  
  private boolean enabled;
  
  public List<String> validate(boolean isAdd) {
    List<String> errors = new ArrayList<String>();
    if(StringUtils.isBlank(configId)) {
      errors.add(""); //TODO fill me
    }
    return errors;
  }
  
  public List<String> test() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  /**
   * save the attribute in an insert.  Note, if theres a failure, you should see if any made it
   * @param attributesToSave are the attributes from "retrieveAttributes" with values in there
   * if a value is blank, then dont save that one
   * @param errorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message.toString()));
   * @param validationErrorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
   *      validationErrorsToDisplay.get(validationKey)));
   */
  public void insertConfig(boolean fromUi, Map<String, GrouperExternalSystemAttribute> attributesToSave,
      StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    if (this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
      throw new RuntimeException("Why does this config id already exist??? '" + this.getConfigId() + "'");
    }
    
    Pattern configIdPattern = Pattern.compile("^[a-zA-Z0-9_]+$");
    if (!configIdPattern.matcher(this.getConfigId()).matches()) {
      throw new RuntimeException("Config it must be alphanumeric or underscore!");
    }

    Pattern endOfStringNewlinePattern = Pattern.compile(".*<br[ ]*\\/?>$");
    
    // add all the possible ones
    for (String suffix : attributesToSave.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributesToSave.get(suffix);
      
      if (!StringUtils.isBlank(grouperExternalSystemAttribute.getValue()) 
          || !StringUtils.isBlank(grouperExternalSystemAttribute.getExpressionLanguageScript())) {
        
        StringBuilder localMessage = new StringBuilder();
        
        DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
            grouperExternalSystemAttribute.getFullPropertyName(), 
            grouperExternalSystemAttribute.isExpressionLanguage() ? "true" : "false", 
            grouperExternalSystemAttribute.isExpressionLanguage() ? grouperExternalSystemAttribute.getExpressionLanguageScript() : grouperExternalSystemAttribute.getValue(),
            grouperExternalSystemAttribute.isPassword(), localMessage, new Boolean[] {false},
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
    }

    ConfigPropertiesCascadeBase.clearCache();
    this.attributeCache = null;
    
  }
  
  /**
   * save the attribute in an insert.  Note, if theres a failure, you should see if any made it
   * @param attributesFromUser are the attributes from "retrieveAttributes" with values in there
   * if a value is blank, then dont save that one (delete)
   * @param errorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, message.toString()));
   * @param validationErrorsToDisplay call from ui: guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error, validationKey, 
   *      validationErrorsToDisplay.get(validationKey)));
   */
  public void editConfig(boolean fromUi, Map<String, GrouperExternalSystemAttribute> attributesFromUser,
      StringBuilder message, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {

    if (!this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
      throw new RuntimeException("Why doesn't this config id already exist??? '" + this.getConfigId() + "'");
    }

    Map<String, GrouperExternalSystemAttribute> attributes = this.retrieveAttributes();
    
    Set<String> propertyNamesToDelete = new HashSet<String>();

    // add all the possible ones
    for (String suffix : attributes.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributes.get(suffix);

      propertyNamesToDelete.add(grouperExternalSystemAttribute.getFullPropertyName());
      
    }

    // and all the ones we detect
    if (!StringUtils.isBlank(this.getConfigId())) {
      
      Set<String> configKeys = this.retrieveConfigurationKeysByPrefix(this.getConfigItemPrefix());
      
      if (GrouperUtil.length(configKeys) > 0) {
        propertyNamesToDelete.addAll(configKeys);
      }
    }
    
    Map<String, GrouperExternalSystemAttribute> attributesToSave = new HashMap<String, GrouperExternalSystemAttribute>();
    
    // add all the possible ones
    for (String suffix : attributesToSave.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributesToSave.get(suffix);
      
      if (!StringUtils.isBlank(grouperExternalSystemAttribute.getValue()) 
          || !StringUtils.isBlank(grouperExternalSystemAttribute.getExpressionLanguageScript())) {
        propertyNamesToDelete.remove(grouperExternalSystemAttribute.getFullPropertyName());
        attributesToSave.put(suffix, grouperExternalSystemAttribute);
      }
    }
    // delete some
    for (String key : propertyNamesToDelete) {
      DbConfigEngine.configurationFileItemDeleteHelper(this.getConfigFileName().getConfigFileName(), key , fromUi, false);
    }

    Pattern endOfStringNewlinePattern = Pattern.compile(".*<br[ ]*\\/?>$");
    
    // add/edit all the possible ones
    for (String suffix : attributesToSave.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributesToSave.get(suffix);
      
      StringBuilder localMessage = new StringBuilder();
      
      DbConfigEngine.configurationFileAddEditHelper2(this.getConfigFileName().getConfigFileName(), 
          grouperExternalSystemAttribute.getFullPropertyName(), 
          grouperExternalSystemAttribute.isExpressionLanguage() ? "true" : "false", 
          grouperExternalSystemAttribute.isExpressionLanguage() ? grouperExternalSystemAttribute.getExpressionLanguageScript() : grouperExternalSystemAttribute.getValue(),
          grouperExternalSystemAttribute.isPassword(), localMessage, new Boolean[] {false},
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
  
  public String getTitle() {
    String title = GrouperTextContainer.textOrNull("externalSystem." + this.getClass().getSimpleName() + ".title");
    if (StringUtils.isBlank(title)) {
      return this.getClass().getSimpleName();
    }
    return title;
  }
  
  public String getDescription() {
    String title = GrouperTextContainer.textOrNull("externalSystem." + this.getClass().getSimpleName() + ".description");
    if (StringUtils.isBlank(title)) {
      return this.getClass().getSimpleName();
    }
    return title;
  }
  
  /**
   * 
   */
  public void deleteConfig(boolean fromUi) {
    
    if (!this.retrieveConfigurationConfigIds().contains(this.getConfigId())) {
      throw new RuntimeException("Why doesnt this config id already exist??? '" + this.getConfigId() + "'");
    }
    
    Map<String, GrouperExternalSystemAttribute> attributes = this.retrieveAttributes();
    
    Set<String> propertyNamesToDelete = new HashSet<String>();

    // add all the possible ones
    for (String suffix : attributes.keySet()) {
    
      GrouperExternalSystemAttribute grouperExternalSystemAttribute = attributes.get(suffix);

      propertyNamesToDelete.add(grouperExternalSystemAttribute.getFullPropertyName());
      
    }

    // and all the ones we detect
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
  
  /**
   * 
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
  
  /**
   * call retrieveAttributes() to get this
   */
  private Map<String, GrouperExternalSystemAttribute> attributeCache = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperExternalSystem.class);

  /**
   * 
   * @return get the attributes from config by suffix
   */
  public Map<String, GrouperExternalSystemAttribute> retrieveAttributes() {
    
    if (this.attributeCache != null) {
      return this.attributeCache;
    }
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = configFileName.getConfig();
    
    Map<String, GrouperExternalSystemAttribute> result = new HashMap<String, GrouperExternalSystemAttribute>();

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

        GrouperExternalSystemAttribute grouperExternalSystemAttribute = new GrouperExternalSystemAttribute();

        grouperExternalSystemAttribute.setFullPropertyName(propertyName);
        
        result.put(suffix, grouperExternalSystemAttribute);
        
        grouperExternalSystemAttribute.setConfigSuffix(suffix);

        grouperExternalSystemAttribute.setConfigItemMetadata(configItemMetadata);
        
        {
          boolean hasExpressionLanguage = configPropertiesCascadeBase.hasExpressionLanguage(propertyName);
          grouperExternalSystemAttribute.setExpressionLanguage(hasExpressionLanguage);

          if (hasExpressionLanguage) {
            String rawExpressionLanguage = configPropertiesCascadeBase.rawExpressionLanguage(propertyName);
            grouperExternalSystemAttribute.setExpressionLanguageScript(rawExpressionLanguage);
          }
        }
        grouperExternalSystemAttribute.setValue(configPropertiesCascadeBase.propertyValueString(propertyName));
        {
          // use the metadata
          grouperExternalSystemAttribute.setDefaultValue(configItemMetadata.getDefaultValue());
          grouperExternalSystemAttribute.setPassword(configItemMetadata.isSensitive());
          grouperExternalSystemAttribute.setRequired(configItemMetadata.isRequired());
          grouperExternalSystemAttribute.setType(configItemMetadata.getValueType());

          if (GrouperUtil.length(configItemMetadata.getOptionValues()) > 0) {
            List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
            for (String value : configItemMetadata.getOptionValues()) {
              MultiKey valueAndLabel = new MultiKey(value, value);
              valuesAndLabels.add(valueAndLabel);
            }
            grouperExternalSystemAttribute.setDropdownValuesAndLabels(valuesAndLabels);
          }

          if (grouperExternalSystemAttribute.isPassword()) {
            grouperExternalSystemAttribute.setPassword(true);
            grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.PASSWORD);
          } else {
            
            ConfigItemFormElement configItemFormElement = configItemMetadata.getFormElement();
            if (configItemFormElement != null) {
              grouperExternalSystemAttribute.setFormElement(configItemFormElement);
            } else {
              
              // boolean is a drop down
              if (configItemMetadata.getValueType() == ConfigItemMetadataType.BOOLEAN) {
                
                grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.DROPDOWN);

                if (GrouperUtil.length(grouperExternalSystemAttribute.getDropdownValuesAndLabels()) == 0) {
                  
                  List<MultiKey> valuesAndLabels = new ArrayList<MultiKey>();
                  valuesAndLabels.add(new MultiKey("", ""));
                  // TODO externalize text
                  valuesAndLabels.add(new MultiKey("true", "true"));
                  valuesAndLabels.add(new MultiKey("false", "false"));
                  grouperExternalSystemAttribute.setDropdownValuesAndLabels(valuesAndLabels);
                }
              } else if (GrouperUtil.length(grouperExternalSystemAttribute.getValue()) > 100) {

                grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.TEXTAREA);

              } else {
                
                grouperExternalSystemAttribute.setFormElement(ConfigItemFormElement.TEXT);

              }
            }
          }
        }
      }
    }
    this.attributeCache = result;
    return result;
  }
  
  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  
  public boolean isEnabled() {
    return enabled;
  }

  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * which config file this is in
   * @return the config file
   */
  public abstract ConfigFileName getConfigFileName();
  
  /**
   * if any config in the file has this prefix, then its related.  includes the config id and dot.
   * e.g. for ldap this is a property ldap.personLdap.url and this is the prefix: ldap.personLdap.
   * the prefix concatenated with the suffix the the config item key
   * @return the prefix
   */
  public abstract String getConfigItemPrefix();
  
  /**
   * get the config id regex. This is a regex that will return the configId, and will do that for all properties
   * for ldap.personLdap.url, the regex is ^(ldap)\.([^.]+)\.(.*)$
   * The first group in regex should be prefix excluding dot.  the second group is the config id.  The third group should be the suffix after that
   * @return the regex
   */
  public abstract String getConfigIdRegex();
  
  /**
   * regex like: 
   * @param regex
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
   * get configuration names configured by prefix 
   * @param prefix of config e.g. ldap.personLdap.
   * @return the list of configured keys
   */
  public Set<String> retrieveConfigurationKeysByPrefix(String prefix) {
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
