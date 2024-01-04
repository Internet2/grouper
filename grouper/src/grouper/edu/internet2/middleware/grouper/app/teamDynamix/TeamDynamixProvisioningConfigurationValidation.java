package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.Collection;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class TeamDynamixProvisioningConfigurationValidation extends GrouperProvisioningConfigurationValidation {
  
  @Override
  public Collection<String> validateGroupAttributeNamesAllowed() {
    return GrouperUtil.toSet("Description");

  }

  @Override
  public Collection<String> validateGroupAttributeNamesRequired() {
    return GrouperUtil.toSet("Name", "id");

  }

  @Override
  public boolean validateGroupAttributesRequireString() {
    return true;
  }
  
  @Override
  public Collection<String> validateEntityAttributeNamesAllowed() {
    return GrouperUtil.toSet();
  }

  @Override
  public Collection<String> validateEntityAttributeNamesRequired() {
    return GrouperUtil.toSet("FirstName", "LastName", "id", "PrimaryEmail", "UserName", "SecurityRoleID", "Company", "ExternalID");
  }

  @Override
  public boolean validateEntityAttributesRequireString() {
    return true;
  }

}
