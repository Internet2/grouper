package edu.internet2.middleware.grouper.app.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;

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
    String configPrefix = getConfiguration().getConfigurationTypePrefix();
    String title = GrouperTextContainer.textOrNull(configPrefix + "." + this.configuration.getClass().getSimpleName() + ".subSection." + this.label +".title");
    
    if (StringUtils.isBlank(title)) {
      String key = "provisionerConfiguration.GenericConfiguration.subSection." + this.label + ".title";
      title = GrouperTextContainer.textOrNull(key);
    }
    
    if (StringUtils.isBlank(title)) {   
      if (this.label.matches(".*[0-9].*")) {
        String key = "provisionerConfiguration.GenericConfiguration.subSection." + (this.label.replaceAll("[0-9]+", "i")) + ".title";
        
        //GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer().setCurrentConfigSuffix(this.label + ".headerTitle");
        
        title = GrouperTextContainer.textOrNull(key);
      } 
    }
    
    if (StringUtils.isBlank(title)) {
      return label;
    }
    return title;
  }
  
  /**
   * if label blank, there is no heading
   * @return
   */
  public String getDescription() {
    String configPrefix = getConfiguration().getConfigurationTypePrefix();
    String title = GrouperTextContainer.textOrNull(configPrefix + "." + this.configuration.getClass().getSimpleName() + ".subSection." + this.label + ".description");
    
    if (StringUtils.isBlank(title)) {
      String key = "provisionerConfiguration.GenericConfiguration.subSection." + this.label + ".description";
      title = GrouperTextContainer.textOrNull(key);
    }
    
    if (StringUtils.isBlank(title)) { 
      if (this.label.matches(".*[0-9].*")) {
        String key = "provisionerConfiguration.GenericConfiguration.subSection." + (this.label.replaceAll("[0-9]+", "i")) + ".description";

        //GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisionerConfigurationContainer().setCurrentConfigSuffix(this.label + ".headerDescription");
        
        title = GrouperTextContainer.textOrNull(key);
      } 
    }
    
    if (StringUtils.isBlank(title)) {
      return label;
    }
    return title;
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
