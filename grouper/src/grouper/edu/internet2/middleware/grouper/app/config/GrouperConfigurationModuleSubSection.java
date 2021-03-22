package edu.internet2.middleware.grouper.app.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperConfigurationModuleSubSection {
  
  /**
   * label to display for the subsection
   */
  private String label;
  
  /**
   * provisioner configuration this subsection is child of 
   */
  private GrouperConfigurationModuleBase configuration;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
  
  public GrouperConfigurationModuleBase getConfiguration() {
    return configuration;
  }

  
  public void setConfiguration(GrouperConfigurationModuleBase configuration) {
    this.configuration = configuration;
  }

  /**
   * if label blank, there is no heading
   * @return
   */
  public String getTitle() {
    
    String realConfigSuffix = this.label;
    String iOrRealConfigSuffix = realConfigSuffix.replaceAll("\\.[0-9]+", ".i");
    boolean hasIconfigSuffix = !StringUtils.equals(realConfigSuffix, iOrRealConfigSuffix);
    
    String title = GrouperTextContainer.textOrNull("config." + this.configuration.getClass().getSimpleName() + ".subSection." + iOrRealConfigSuffix + ".title");

    if (StringUtils.isBlank(title)) {
      
      title = GrouperTextContainer.textOrNull("config.GenericConfiguration.subSection." + iOrRealConfigSuffix + ".title");
    }
    
    if (StringUtils.isBlank(title)) {
      title = iOrRealConfigSuffix;
    } else {
      title = this.configuration.formatIndexes(realConfigSuffix, hasIconfigSuffix, title);
    }      
    return title;

    
  }
  
  /**
   * if label blank, there is no heading
   * @return
   */
  public String getDescription() {
    
    String realConfigSuffix = this.label;
    String iOrRealConfigSuffix = realConfigSuffix.replaceAll("\\.[0-9]+", ".i");
    boolean hasIconfigSuffix = !StringUtils.equals(realConfigSuffix, iOrRealConfigSuffix);
    
    String description = GrouperTextContainer.textOrNull("config." + this.configuration.getClass().getSimpleName() + ".subSection." + iOrRealConfigSuffix + ".description");

    if (StringUtils.isBlank(description)) {
      
      description = GrouperTextContainer.textOrNull("config.GenericConfiguration.subSection." + iOrRealConfigSuffix + ".description");
    }
    
    if (StringUtils.isBlank(description)) {
      description = iOrRealConfigSuffix;
    } else {
      description = this.configuration.formatIndexes(realConfigSuffix, hasIconfigSuffix, description);
    }      
    return description;

    
  }

  /**
   * return config suffix to attribute
   * @return map
   */
  public Map<String, GrouperConfigurationModuleAttribute> getAttributes() {
    Map<String, GrouperConfigurationModuleAttribute> results = new LinkedHashMap<String, GrouperConfigurationModuleAttribute>();
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.configuration.retrieveAttributes().values()) {
      if (StringUtils.equals(this.getLabel(), grouperConfigModuleAttribute.getConfigItemMetadata().getSubSection())) {
        results.put(grouperConfigModuleAttribute.getConfigSuffix(), grouperConfigModuleAttribute);
      }
    }
    return results;
  }
  
  /**
   * get list of attributes for this subsection
   * @return
   */
  public Collection<GrouperConfigurationModuleAttribute> getAttributesValues() {
    return this.getAttributes().values();
  }
  
  /**
   * only show when at least one of the attributes is show in this subsection
   * @return
   */
  public boolean isShow() {
    
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute:  this.getAttributesValues()) {
      if (grouperConfigModuleAttribute.isShow()) return true;
    }
    return false;
  }

}
