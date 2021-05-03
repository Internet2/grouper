package edu.internet2.middleware.grouper.app.ldapProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningCompare;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningUpdatable;


public class LdapSyncCompare extends GrouperProvisioningCompare {

  @Override
  public boolean attributeValueEquals(Object first, Object second) {
    return super.attributeValueEquals(first, second);
  }

  @Override
  public boolean compareFieldValueEquals(String fieldName, Object grouperValue,
      Object targetValue, ProvisioningUpdatable grouperTargetUpdatable) {
    return super.compareFieldValueEquals(fieldName, grouperValue, targetValue,
        grouperTargetUpdatable);
  }

}
