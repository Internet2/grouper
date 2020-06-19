package edu.internet2.middleware.grouper.app.provisioning;

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

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

public abstract class ProvisionerConfiguration extends GrouperConfigurationModuleBase {
  
  public final static Set<String> provisionerConfigClassNames = new LinkedHashSet<String>();
  
  static {
    provisionerConfigClassNames.add(LdapProvisionerConfiguration.class.getName());
  }
  
  /**
   * list of systems that can be configured
   * @return
   */
  public static List<ProvisionerConfiguration> retrieveAllProvisionerConfigurationTypes() {
    
    List<ProvisionerConfiguration> result = new ArrayList<ProvisionerConfiguration>();
    
    for (String className: provisionerConfigClassNames) {
      
      Class<ProvisionerConfiguration> provisionerConfigClass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(className);
      ProvisionerConfiguration provisionerConfig = GrouperUtil.newInstance(provisionerConfigClass);
      result.add(provisionerConfig);
    }
    
    return result;
  }
  
  /**
   * get all provisioners configured for this type
   * @return
   */
  private List<ProvisionerConfiguration> listAllProvisionerConfigurationsOfThisType() {
    
    List<ProvisionerConfiguration> result = new ArrayList<ProvisionerConfiguration>();
    
    for (String configId : this.retrieveConfigurationConfigIds()) {
      
      @SuppressWarnings("unchecked")
      Class<ProvisionerConfiguration> theClass = (Class<ProvisionerConfiguration>)this.getClass();
      ProvisionerConfiguration provisionerConfig = GrouperUtil.newInstance(theClass);
      provisionerConfig.setConfigId(configId);
      result.add(provisionerConfig);
    }
    
    return result;
  }
  
  /**
   * list of configured external systems
   * @return
   */
  public static List<ProvisionerConfiguration> retrieveAllProvisionerConfigurations() {
    
    List<ProvisionerConfiguration> result = new ArrayList<ProvisionerConfiguration>();
    
    for (String className: provisionerConfigClassNames) {
      Class<ProvisionerConfiguration> provisionerConfigClass = (Class<ProvisionerConfiguration>) GrouperUtil.forName(className);
      ProvisionerConfiguration provisionerConfig = GrouperUtil.newInstance(provisionerConfigClass);
      result.addAll(provisionerConfig.listAllProvisionerConfigurationsOfThisType());
    }
    
    return result;
  }

  @Override
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
  
  private Map<String, GrouperConfigurationModuleAttribute> retrieveExtraAttributes(Map<String, GrouperConfigurationModuleAttribute> attributesFromBaseConfig) {
    
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
        
        ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
        configItemMetadata.setFormElement(ConfigItemFormElement.TEXT);
        configItemMetadata.setValueType(ConfigItemMetadataType.STRING);
        
        GrouperConfigurationModuleAttribute grouperExternalSystemAttribute = 
            buildConfigurationModuleAttribute(propertyName, suffix, false, configItemMetadata, configPropertiesCascadeBase);
        
        result.put(suffix, grouperExternalSystemAttribute);
      }
      
    }
    
    return result;
     
  }
  
  /**
   * get subsections for the UI
   * @return
   */
  public List<ProvisionerConfigurationSubSection> getSubSections() {
    
    List<ProvisionerConfigurationSubSection> results = new ArrayList<ProvisionerConfigurationSubSection>();
    
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
      
      ProvisionerConfigurationSubSection provisionerConfigurationSubSection = new ProvisionerConfigurationSubSection();
      provisionerConfigurationSubSection.setProvisionerConfiguration(this);
      provisionerConfigurationSubSection.setLabel(grouperConfigModuleAttribute.getConfigItemMetadata().getSubSection());
      
      results.add(provisionerConfigurationSubSection);
    }
    
    return results;
  }
  
  
  /**
   * is the config enabled or not
   * @return
   */
  public boolean isEnabled() {
   //TODO: add implementation
    return true;
  }
  
  
  
}
