package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;

/**
 * grouper external subsection. One to many relationship between external system and subsection 
 */
public class ProvisionerConfigurationSubSection {

  /**
   * label to display for the subsection
   */
  private String label;
  
  /**
   * provisioner configuration this subsection is child of 
   */
  private ProvisionerConfiguration provisionerConfiguration;
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
  /**
   * @return provisioner configuration this subsection is child of
   */
  public ProvisionerConfiguration getProvisionerConfiguration() {
    return provisionerConfiguration;
  }

  /**
   * provisioner configuration this subsection is child of
   * @param provisionerConfiguration
   */
  public void setProvisionerConfiguration(ProvisionerConfiguration provisionerConfiguration) {
    this.provisionerConfiguration = provisionerConfiguration;
  }

  /**
   * if label blank, there is no heading
   * @return
   */
  public String getTitle() {
    String title = GrouperTextContainer.textOrNull("provisionerConfiguration." + this.provisionerConfiguration.getClass().getSimpleName() + ".subSection." + this.label +".title");
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
    String title = GrouperTextContainer.textOrNull("provisionerConfiguration." + this.provisionerConfiguration.getClass().getSimpleName() + ".subSection." + this.label + ".description");
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
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.provisionerConfiguration.retrieveAttributes().values()) {
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
  
}
