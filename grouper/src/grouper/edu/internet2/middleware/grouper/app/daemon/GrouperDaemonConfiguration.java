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

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystemAttribute;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public abstract class GrouperDaemonConfiguration {
  
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
    
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
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
        
        grouperDaemonConfigAttribute.setConfigItemMetadata(configItemMetadata);
        
        grouperDaemonConfigAttribute.setConfigSuffix(suffix);
        
        {
          grouperDaemonConfigAttribute.setRequired(configItemMetadata.isRequired());
          grouperDaemonConfigAttribute.setType(configItemMetadata.getValueType());
          grouperDaemonConfigAttribute.setDefaultValue(configItemMetadata.getDefaultValue());
          grouperDaemonConfigAttribute.setFormElement(ConfigItemFormElement.TEXT);
        }
        
      }
    }
    this.attributeCache = result;
    
    Map<String, GrouperDaemonConfigAttribute> extraAttributes = retrieveExtraAttributes();
    
    result.putAll(extraAttributes);

    return result;
    
  }
  
  
  /**
   * retrieve attributes
   * @return
   */
  public Map<String, GrouperDaemonConfigAttribute> retrieveAttributesForAdd() {
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    Map<String, GrouperDaemonConfigAttribute> result = new LinkedHashMap<String, GrouperDaemonConfigAttribute>();

    Pattern pattern = Pattern.compile(this.getConfigIdRegex());

    List<ConfigSectionMetadata> configSectionMetadataList = configFileName.configFileMetadata().getConfigSectionMetadataList();
    
    String configIdThatIdentifiesThisDaemon = null;
    
    if (this.getPropertySuffixThatIdentifiesThisDaemon() != null) {
      
      if (StringUtils.isBlank(this.getPropertyValueThatIdentifiesThisDaemon())) {
        throw new RuntimeException("getPropertyValueThatIdentifiesThisDaemon is required for " + this.getClass().getName());
      }
      
      if (StringUtils.isNotBlank(this.getConfigIdThatIdentifiesThisDaemon())) {
        throw new RuntimeException("can't specify ConfigIdThatIdentifiesThisDaemon and PropertySuffixThatIdentifiesThisDaemon for class "+this.getClass().getName());
      }
      
      if (StringUtils.isBlank(this.getConfigId())) {
        
      }
      
      outer: for (ConfigSectionMetadata configSectionMetadata: configSectionMetadataList) {
        for (ConfigItemMetadata configItemMetadata: configSectionMetadata.getConfigItemMetadataList()) {
          
          Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
          if (!matcher.matches()) {
            continue;
          }
          
          String prefix = matcher.group(1);
          String configId = this.getConfigId();
          if (StringUtils.isBlank(configId)) {
            configId = matcher.group(2);
            
            if (StringUtils.isBlank(configId)) {
              
              configId = configItemMetadata.getKeyOrSampleKey();
              
              
            }
          }
          String suffix = matcher.group(3);
          
          String propertyName = prefix + "." + configId + "." + suffix;
          if (propertyName.equals(prefix + "." + configId + "." + this.getPropertySuffixThatIdentifiesThisDaemon())) {
            
            if (StringUtils.equals(configItemMetadata.getValue(), this.getPropertyValueThatIdentifiesThisDaemon())) {
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
    
    
    // get the attributes based on the configIdThatIdentifiesThisDaemon
    for (ConfigSectionMetadata configSectionMetadata: configSectionMetadataList) {
      for (ConfigItemMetadata configItemMetadata: configSectionMetadata.getConfigItemMetadataList()) {
        
        Matcher matcher = pattern.matcher(configItemMetadata.getKeyOrSampleKey());
        if (!matcher.matches()) {
          continue;
        }
        
        String prefix = matcher.group(1);
        String configId = this.getConfigId();
        if (StringUtils.isBlank(configId)) {
          configId = matcher.group(2);
          if (StringUtils.isBlank(configId)) {
            throw new RuntimeException("Why is configId blank??? " + this.getClass().getName());
          }
        }
        
        if (StringUtils.isNotBlank(configIdThatIdentifiesThisDaemon) && !StringUtils.equals(configIdThatIdentifiesThisDaemon, configId)) {
          continue;
        }
        
        String suffix = matcher.group(3);
        
        String propertyName = prefix + "." + configId + "." + suffix;
        
        GrouperDaemonConfigAttribute grouperDaemonConfigAttribute = new GrouperDaemonConfigAttribute();
        grouperDaemonConfigAttribute.setFullPropertyName(propertyName);
        grouperDaemonConfigAttribute.setGrouperDaemonConfiguration(this);
        result.put(suffix, grouperDaemonConfigAttribute);
        
        grouperDaemonConfigAttribute.setConfigItemMetadata(configItemMetadata);
        
        grouperDaemonConfigAttribute.setConfigSuffix(suffix);
        
        {
          grouperDaemonConfigAttribute.setRequired(configItemMetadata.isRequired());
          grouperDaemonConfigAttribute.setType(configItemMetadata.getValueType());
          grouperDaemonConfigAttribute.setDefaultValue(configItemMetadata.getDefaultValue());
          grouperDaemonConfigAttribute.setFormElement(ConfigItemFormElement.TEXT);
        }
        
      }
    }
    
    return result;
  }

  public Map<String, GrouperDaemonConfigAttribute> retrieveExtraAttributes() {
    
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
      
      GrouperDaemonConfigAttribute grouperExternalSystemAttribute = new GrouperDaemonConfigAttribute();
  
      grouperExternalSystemAttribute.setFullPropertyName(propertyName);
      grouperExternalSystemAttribute.setGrouperDaemonConfiguration(this);
      
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
    
    return result;
     
  }

}
