package edu.internet2.middleware.grouper.app.interfolio;

import java.util.Collection;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Validation of the allowed/required target attribute names for the Interfolio provisioner.  This is
 * an entity-only provisioner, so there are no group attributes.
 */
public class InterfolioProvisioningConfigurationValidation extends GrouperProvisioningConfigurationValidation {

  @Override
  public Collection<String> validateGroupAttributeNamesAllowed() {
    return GrouperUtil.toSet();
  }

  @Override
  public Collection<String> validateGroupAttributeNamesRequired() {
    return GrouperUtil.toSet();
  }

  @Override
  public boolean validateGroupAttributesRequireString() {
    return true;
  }

  @Override
  public Collection<String> validateEntityAttributeNamesAllowed() {
    return GrouperUtil.toSet("saml_id", "user_type");
  }

  @Override
  public Collection<String> validateEntityAttributeNamesRequired() {
    return GrouperUtil.toSet("institution_user_id", "first_name", "last_name", "email");
  }

  @Override
  public boolean validateEntityAttributesRequireString() {
    return true;
  }

}
