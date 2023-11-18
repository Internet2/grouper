package edu.internet2.middleware.grouper.app.provisioningExamples.exampleGroupAttributeSql;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlGrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;

public class ExampleGroupAttributeProvisioningConfiguration extends SqlProvisioningConfiguration {

  @Override
  public void configureGenericSettings() {
    
    super.configureGenericSettings();
    
    SqlGrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        (SqlGrouperProvisioningConfigurationAttribute)this.getTargetGroupAttributeNameToConfig().get("uugid");
    grouperProvisioningConfigurationAttribute.setStorageType("groupTableColumn");
    grouperProvisioningConfigurationAttribute = 
        (SqlGrouperProvisioningConfigurationAttribute)this.getTargetGroupAttributeNameToConfig().get("displayname");
    grouperProvisioningConfigurationAttribute.setStorageType("groupTableColumn");
    grouperProvisioningConfigurationAttribute = 
        (SqlGrouperProvisioningConfigurationAttribute)this.getTargetGroupAttributeNameToConfig().get("members");
    grouperProvisioningConfigurationAttribute.setStorageType("separateAttributesTable");
    grouperProvisioningConfigurationAttribute = 
        (SqlGrouperProvisioningConfigurationAttribute)this.getTargetGroupAttributeNameToConfig().get("contacts");
    grouperProvisioningConfigurationAttribute.setStorageType("separateAttributesTable");
    grouperProvisioningConfigurationAttribute = 
        (SqlGrouperProvisioningConfigurationAttribute)this.getTargetGroupAttributeNameToConfig().get("administrators");
    grouperProvisioningConfigurationAttribute.setStorageType("separateAttributesTable");
  }

}
