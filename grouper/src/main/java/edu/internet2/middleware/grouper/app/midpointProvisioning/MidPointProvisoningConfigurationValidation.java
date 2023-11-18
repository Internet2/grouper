package edu.internet2.middleware.grouper.app.midpointProvisioning;

import java.util.Collection;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class MidPointProvisoningConfigurationValidation
    extends GrouperProvisioningConfigurationValidation {
  
  @Override
  public Collection<String> validateGroupAttributeNamesForbidden() {
    return GrouperUtil.toSet("id_index", "last_modified", "deleted", "target");
  }

  @Override
  public Collection<String> validateEntityAttributeNamesForbidden() {
    return GrouperUtil.toSet("subject_id_index", "last_modified", "deleted");
  }

}
