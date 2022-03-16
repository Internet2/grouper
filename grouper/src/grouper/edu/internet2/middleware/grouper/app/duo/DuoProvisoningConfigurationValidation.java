package edu.internet2.middleware.grouper.app.duo;

import java.util.Collection;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class DuoProvisoningConfigurationValidation extends GrouperProvisioningConfigurationValidation {
  
  @Override
  public Collection<String> validateGroupAttributeNamesAllowed() {
    return GrouperUtil.toSet("description");

  }

  @Override
  public Collection<String> validateGroupAttributeNamesRequired() {
    return GrouperUtil.toSet("name", "id");

  }

  @Override
  public boolean validateGroupAttributesRequireString() {
    return true;
  }

  @Override
  public Collection<String> validateEntityAttributeNamesAllowed() {
    return GrouperUtil.toSet("name", "email", "firstname", "lastname");
  }

  @Override
  public Collection<String> validateEntityAttributeNamesRequired() {
    return GrouperUtil.toSet("id", "loginId");
  }

  @Override
  public boolean validateEntityAttributesRequireString() {
    return true;
  }

  
}
