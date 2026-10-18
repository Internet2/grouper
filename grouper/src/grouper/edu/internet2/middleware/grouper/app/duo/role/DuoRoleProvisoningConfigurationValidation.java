package edu.internet2.middleware.grouper.app.duo.role;

import java.util.Collection;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningValidationIssue;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
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


  /**
   * also validate the role priority JSON so a typo shows on save instead of failing the sync
   */
  @Override
  public void validateFromSuffixValueMap() {
    super.validateFromSuffixValueMap();
    validateRolePriority();
  }

  /**
   * rolePriority must be a JSON object of role id or name to integer priority
   */
  public void validateRolePriority() {
    try {
      GrouperDuoRoleResolver.parseRolePriorityJson(this.getSuffixToConfigValue().get("rolePriority"));
    } catch (RuntimeException re) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(
          GrouperTextContainer.textOrNull("provisioning.configuration.validation.duoRolePriorityInvalid") + " " + re.getMessage())
          .assignJqueryHandle("rolePriority"));
    }
  }

}
