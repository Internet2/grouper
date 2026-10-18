package edu.internet2.middleware.grouper.app.duo.role;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeManipulation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;


public class DuoRoleProvisioningAttributeManipulation extends GrouperProvisioningAttributeManipulation {

  public DuoRoleProvisioningAttributeManipulation() {
  }

  @Override
  public boolean isConvertNullValuesToEmpty() {
    return true;
  }

  /**
   * the role attribute default (e.g. Read-only) is compared with the role_id read from Duo,
   * so resolve it to a role id (e.g. read_only) after it is assigned
   */
  @Override
  public void assignDefault(Collection<Object> changedObjects, ProvisioningUpdatable provisioningUpdatable,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, int[] count) {

    super.assignDefault(changedObjects, provisioningUpdatable, grouperProvisioningConfigurationAttribute, count);

    if (grouperProvisioningConfigurationAttribute == null || !StringUtils.equals("role", grouperProvisioningConfigurationAttribute.getName())
        || StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
      return;
    }
    String defaultValue = grouperProvisioningConfigurationAttribute.getDefaultValue();
    String defaultRoleId = ((GrouperDuoRoleProvisioner)this.getGrouperProvisioner()).retrieveDuoRoleResolver().resolveRoleId(defaultValue);
    if (StringUtils.equals(defaultValue, defaultRoleId)) {
      return;
    }

    // replace the default name with its role id, role membership values are already role ids
    Object currentValue = provisioningUpdatable.retrieveAttributeValue("role");
    if (currentValue instanceof String) {
      if (StringUtils.equals((String)currentValue, defaultValue)) {
        provisioningUpdatable.assignAttributeValue("role", defaultRoleId);
      }
    } else if (currentValue instanceof Collection) {
      Collection<Object> currentValueCollection = (Collection<Object>)currentValue;
      if (currentValueCollection.remove(defaultValue)) {
        currentValueCollection.add(defaultRoleId);
      }
    }
  }

}
