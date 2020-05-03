package edu.internet2.middleware.grouper.app.daemon;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderIncrementalJob;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigSectionMetadata;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public abstract class GrouperDaemonConfiguration {
  
  /**
   * config id of the daemon
   */
  private String configId;
  
  
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
  
  public String getProperySuffixThatIdentifiesThisDaemon() {
    return null;
  }
  
  public String getProperyValueThatIdentifiesThisDaemon() {
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
  
  public Map<String, GrouperDaemonConfigAttribute> retrieveAttributes() {
    
    ConfigFileName configFileName = this.getConfigFileName();
    
    if (configFileName == null) {
      throw new RuntimeException("configFileName cant be null for " + this.getClass().getName());
    }
    
    Map<String, GrouperDaemonConfigAttribute> result = new LinkedHashMap<String, GrouperDaemonConfigAttribute>();

    Pattern pattern = Pattern.compile(this.getConfigIdRegex());

    List<ConfigSectionMetadata> configSectionMetadataList = configFileName.configFileMetadata().getConfigSectionMetadataList();
    
    String configIdThatIdentifiesThisDaemon = null;
    
    if (this.getProperySuffixThatIdentifiesThisDaemon() != null) {
      
      if (StringUtils.isBlank(this.getProperyValueThatIdentifiesThisDaemon())) {
        throw new RuntimeException("getProperyValueThatIdentifiesThisDaemon is required for " + this.getClass().getName());
      }
      
      if (StringUtils.isNotBlank(this.getConfigIdThatIdentifiesThisDaemon())) {
        throw new RuntimeException("can't specify ConfigIdThatIdentifiesThisDaemon and ProperySuffixThatIdentifiesThisDaemon for class "+this.getClass().getName());
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
            throw new RuntimeException("Why is configId blank??? " + this.getClass().getName());
          }
          String suffix = matcher.group(3);
          
          String propertyName = prefix + "." + configId + "." + suffix;
          if (propertyName.equals(prefix + "." + configId + "." + this.getProperySuffixThatIdentifiesThisDaemon())) {
            
            if (StringUtils.equals(configItemMetadata.getSampleValue(), this.getProperyValueThatIdentifiesThisDaemon())) {
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
        String configId = this.getConfigId();
        if (StringUtils.isBlank(configId)) {
          throw new RuntimeException("Why is configId blank??? " + this.getClass().getName());
        }
        
        if (StringUtils.isNotBlank(configIdThatIdentifiesThisDaemon) && !StringUtils.equals(configIdThatIdentifiesThisDaemon, configId)) {
          continue;
        }
        
        String suffix = matcher.group(3);
        
        String propertyName = prefix + "." + configId + "." + suffix;
        
        GrouperDaemonConfigAttribute grouperDaemonConfigAttribute = new GrouperDaemonConfigAttribute();
        grouperDaemonConfigAttribute.setFullPropertyName(propertyName);
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
    
    return null;
  }

}
