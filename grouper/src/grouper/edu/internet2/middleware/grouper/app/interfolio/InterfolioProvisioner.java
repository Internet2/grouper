package edu.internet2.middleware.grouper.app.interfolio;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehaviorMembershipType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;

/**
 * Interfolio user provisioner.  Provisions Grouper entities (subjects) as Interfolio users via the
 * IAM create/update API, matching/looking them up with the byc users/search API.
 *
 * This is an entity-only provisioner: it ensures users exist in Interfolio.  Granting product access
 * (RPT/FS subscribe/unsubscribe) is a separate concern - Interfolio has no read-back of "who is
 * subscribed", so it cannot be diffed by the provisioner here.
 */
public class InterfolioProvisioner extends GrouperProvisioner {

  @Override
  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {
    return InterfolioTargetDao.class;
  }

  @Override
  protected Class<? extends GrouperProvisioningConfiguration> grouperProvisioningConfigurationClass() {
    return InterfolioProvisioningConfiguration.class;
  }

  @Override
  public void registerProvisioningBehaviors(GrouperProvisioningBehavior grouperProvisioningBehavior) {
    // membershipObjects is the standard for web service provisioners; this provisioner only operates
    // on entities (operateOnGrouperGroups / operateOnGrouperMemberships are false), so no group or
    // membership objects are created - it just provisions the users who are members of provisionable
    // groups.
    grouperProvisioningBehavior.setGrouperProvisioningBehaviorMembershipType(GrouperProvisioningBehaviorMembershipType.membershipObjects);
  }

  @Override
  protected Class<? extends GrouperProvisioningConfigurationValidation> grouperProvisioningConfigurationValidationClass() {
    return InterfolioProvisioningConfigurationValidation.class;
  }

}
