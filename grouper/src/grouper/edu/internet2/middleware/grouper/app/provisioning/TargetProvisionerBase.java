package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.provisioning.TargetGroup;
import edu.internet2.middleware.grouper.app.provisioning.TargetProvisionerRetrieveGroupParam;
import edu.internet2.middleware.grouper.app.provisioning.TargetProvisionerRetrieveGroupsParam;

import edu.internet2.middleware.grouper.app.provisioning.TargetGroup;
import edu.internet2.middleware.grouper.app.provisioning.TargetProvisionerRetrieveGroupParam;
import edu.internet2.middleware.grouper.app.provisioning.TargetProvisionerRetrieveGroupsParam;
import edu.internet2.middleware.grouper.app.provisioning.TargetTranslationToGrouperGroupType;

/**
 * 
 * @author mchyzer
 *
 */
public abstract class TargetProvisionerBase {

  public TargetGroup retrieveGroup(TargetProvisionerRetrieveGroupParam targetProvisionerRetrieveGroupParam) {
    throw new UnsupportedOperationException();
  }

  public TargetGroup retrieveGroup(TargetProvisionerRetrieveGroupsParam targetProvisionerRetrieveGroupsParam) {
    throw new UnsupportedOperationException();
  }

  public Object translateTargetGroupToGrouper(TargetGroup targetGroup) {
    throw new UnsupportedOperationException();
  }

  public TargetTranslationToGrouperGroupType translateTargetGroupType() {
    throw new UnsupportedOperationException();
  }

}
