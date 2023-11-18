package edu.internet2.middleware.grouper.app.duo.role;

import java.util.Collection;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class DuoRoleProvisoningConfigurationValidation extends GrouperProvisioningConfigurationValidation {

  @Override
  public Collection<String> validateGroupAttributeNamesRequired() {
    return GrouperUtil.toSet("role");
  }

  @Override
  public boolean validateGroupAttributesRequireString() {
    return true;
  }

  @Override
  public Collection<String> validateEntityAttributeNamesRequired() {
    return GrouperUtil.toSet("email", "id", "name", "role");
  }

  @Override
  public boolean validateEntityAttributesRequireString() {
    return true;
  }


}
