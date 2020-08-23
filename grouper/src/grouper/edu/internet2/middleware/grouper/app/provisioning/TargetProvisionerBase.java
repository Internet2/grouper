package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

/**
 * 
 * @author mchyzer
 *
 */
public abstract class TargetProvisionerBase {

  public ProvisioningGroup retrieveGroup(TargetProvisionerRetrieveGroupParam targetProvisionerRetrieveGroupParam) {
    throw new UnsupportedOperationException();
  }

  public ProvisioningGroup retrieveGroup(TargetProvisionerRetrieveGroupsParam targetProvisionerRetrieveGroupsParam) {
    throw new UnsupportedOperationException();
  }

  public Object translateTargetGroupToGrouper(ProvisioningGroup targetGroup) {
    throw new UnsupportedOperationException();
  }

  public TargetTranslationToGrouperGroupType translateTargetGroupType() {
    throw new UnsupportedOperationException();
  }

}
