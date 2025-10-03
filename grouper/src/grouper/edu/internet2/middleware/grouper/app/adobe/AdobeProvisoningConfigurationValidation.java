package edu.internet2.middleware.grouper.app.adobe;

import java.util.Collection;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class AdobeProvisoningConfigurationValidation extends GrouperProvisioningConfigurationValidation {
  
  @Override
  public Collection<String> validateGroupAttributeNamesAllowed() {
    return GrouperUtil.toSet("name", "id");

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
    return GrouperUtil.toSet("id", "email", "firstname", "lastname", "type", "country", "userName");
  }

  @Override
  public Collection<String> validateEntityAttributeNamesRequired() {
    return GrouperUtil.toSet("id", "email");
  }

  @Override
  public boolean validateEntityAttributesRequireString() {
    return true;
  }

  
}
