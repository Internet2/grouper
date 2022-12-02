package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;


public class GrouperBoxConfiguration extends GrouperProvisioningConfiguration {

  private String boxExternalSystemConfigId;
  
  private Set<String> entityAttributesToRetrieve = new LinkedHashSet<String>();
  private Set<String> groupAttributesToRetrieve = new LinkedHashSet<String>();
  
  @Override
  public void configureSpecificSettings() {
    
    this.boxExternalSystemConfigId = this.retrieveConfigString("boxExternalSystemConfigId", true);
    Map<String, GrouperProvisioningConfigurationAttribute> entityAttributeNameToConfig = this.getTargetEntityAttributeNameToConfig();
    Map<String, GrouperProvisioningConfigurationAttribute> groupAttributeNameToConfig = this.getTargetGroupAttributeNameToConfig();
    
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityAttributeNameToConfig.values()) {
      
      if (grouperProvisioningConfigurationAttribute.isSelect()) {
        this.entityAttributesToRetrieve.add(grouperProvisioningConfigurationAttribute.getName());
      }
    }
    
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributeNameToConfig.values()) {
      
      if (grouperProvisioningConfigurationAttribute.isSelect()) {
        this.groupAttributesToRetrieve.add(grouperProvisioningConfigurationAttribute.getName());
      }
    }
    
  }
  
  public String getBoxExternalSystemConfigId() {
    return boxExternalSystemConfigId;
  }
  
  public void setBoxExternalSystemConfigId(String boxExternalSystemConfigId) {
    this.boxExternalSystemConfigId = boxExternalSystemConfigId;
  }

  public Set<String> getEntityAttributesToRetrieve() {
    return entityAttributesToRetrieve;
  }

  public Set<String> getGroupAttributesToRetrieve() {
    return groupAttributesToRetrieve;
  }
  
}
