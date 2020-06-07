package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;

/**
 * grouper external subsection. One to many relationship between external system and subsection 
 */
public class GrouperExternalSystemSubSection {

  /**
   * label to display for the subsection
   */
  private String label;
  
  /**
   * grouper external system this subsection is child of 
   */
  private GrouperExternalSystem grouperExternalSystem;
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
  public GrouperExternalSystem getGrouperExternalSystem() {
    return grouperExternalSystem;
  }
  
  public void setGrouperExternalSystem(GrouperExternalSystem grouperExternalSystem) {
    this.grouperExternalSystem = grouperExternalSystem;
  }

  /**
   * if label blank, there is no heading
   * @return
   */
  public String getTitle() {
    String title = GrouperTextContainer.textOrNull("externalSystem." + this.grouperExternalSystem.getClass().getSimpleName() + ".subSection." + this.label +".title");
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
    String title = GrouperTextContainer.textOrNull("externalSystem." + this.grouperExternalSystem.getClass().getSimpleName() + ".subSection." + this.label + ".description");
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
    for (GrouperConfigurationModuleAttribute grouperConfigModuleAttribute : this.grouperExternalSystem.retrieveAttributes().values()) {
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
