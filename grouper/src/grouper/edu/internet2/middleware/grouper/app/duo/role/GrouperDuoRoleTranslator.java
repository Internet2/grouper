package edu.internet2.middleware.grouper.app.duo.role;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTranslator;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;

/**
 * Duo role translator: the group field that holds a group's Duo role (entityMembershipAttributeValue,
 * e.g. groupAttributeValueCache0) can be a role name or role id.  Resolve it to a Duo role_id here,
 * when it becomes the administrator's role membership value, so the Grouper side compares role ids
 * with the role_id read from Duo.  Otherwise "Administrator" vs service_manager would look like a
 * change on every sync.
 *
 * @author mchyzer
 */
public class GrouperDuoRoleTranslator extends GrouperProvisioningTranslator {

  /**
   * resolve the role membership value to a role id
   */
  @Override
  public Object translateFromGrouperProvisioningGroupField(ProvisioningGroupWrapper provisioningGroupWrapper, String field) {
    Object result = super.translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, field);

    // only the field that is the role membership value, other group translations are left alone
    if (result instanceof String && StringUtils.equals(field,
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeValue())) {
      result = ((GrouperDuoRoleProvisioner)this.getGrouperProvisioner()).retrieveDuoRoleResolver().resolveRoleId((String)result);
    }
    return result;
  }

}
